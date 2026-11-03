const express = require('express');
const admin = require('firebase-admin');
const cors = require('cors');
const fetch = require('node-fetch');
require('dotenv').config();

// Inicializar Firebase Admin SDK
admin.initializeApp({
  credential: admin.credential.cert({
    projectId: process.env.FIREBASE_PROJECT_ID,
    clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
    privateKey: process.env.FIREBASE_PRIVATE_KEY ? process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n') : undefined
  })
});

const db = admin.firestore();
const app = express();
app.use(express.json());
app.use(cors());

// Middleware de Autenticación: Verifica Firebase ID Token
async function verificarAuth(req, res, next) {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Token de autorización ausente o inválido' });
  }
  const token = authHeader.split('Bearer ')[1];
  try {
    const decodedToken = await admin.auth().verifyIdToken(token);
    req.usuarioId = decodedToken.uid;
    next();
  } catch (error) {
    return res.status(403).json({ error: 'Token de Firebase inválido o expirado', detalle: error.message });
  }
}

// 1. Endpoint: crearPedido con validación estricta de precios y stock (Transacción Atómica)
app.post('/api/crear-pedido', verificarAuth, async (req, res) => {
  const { direccionEnvio } = req.body;
  const usuarioId = req.usuarioId;

  if (!direccionEnvio) {
    return res.status(400).json({ error: 'La dirección de envío es obligatoria' });
  }

  try {
    const resultadoPedido = await db.runTransaction(async (transaction) => {
      const carritoRef = db.collection('carritos').doc(usuarioId);
      const carritoDoc = await transaction.get(carritoRef);

      if (!carritoDoc.exists || !carritoDoc.data().items || carritoDoc.data().items.length === 0) {
        throw new Error('El carrito está vacío');
      }

      const itemsCliente = carritoDoc.data().items;
      let subtotalGeneral = 0;
      const detalles = [];

      const configRef = db.collection('configuracion').doc('envio');
      const configDoc = await transaction.get(configRef);
      const tarifaFija = configDoc.exists ? configDoc.data().tarifaFija : 10000;
      const umbralGratis = configDoc.exists ? configDoc.data().umbralEnvioGratis : 100000;

      for (const item of itemsCliente) {
        const prodRef = db.collection('productos').doc(item.productoId);
        const invRef = db.collection('inventario').doc(item.productoId);

        const prodDoc = await transaction.get(prodRef);
        const invDoc = await transaction.get(invRef);

        if (!prodDoc.exists) throw new Error(`El producto ${item.nombre} ya no existe`);
        if (!invDoc.exists) throw new Error(`Inventario no disponible para ${item.nombre}`);

        const productoServidor = prodDoc.data();
        const inventarioServidor = invDoc.data();

        if (inventarioServidor.cantidadDisponible < item.cantidad) {
          throw new Error(`Stock insuficiente para ${productoServidor.nombre}. Disponible: ${inventarioServidor.cantidadDisponible}`);
        }

        // SEGURIDAD: El servidor fija el precio real, ignorando cualquier precio enviado por el cliente
        const precioReal = productoServidor.precio;
        const subtotalItem = precioReal * item.cantidad;
        subtotalGeneral += subtotalItem;

        detalles.push({
          productoId: productoServidor.id,
          nombre: productoServidor.nombre,
          presentacion: productoServidor.presentacion,
          precioUnitario: precioReal,
          cantidad: item.cantidad,
          subtotal: subtotalItem
        });

        const nuevoStock = inventarioServidor.cantidadDisponible - item.cantidad;
        transaction.update(invRef, { cantidadDisponible: nuevoStock });
      }

      const costoEnvio = subtotalGeneral >= umbralGratis ? 0 : tarifaFija;
      const totalGeneral = subtotalGeneral + costoEnvio;

      const pedidoRef = db.collection('pedidos').doc();
      const pedido = {
        id: pedidoRef.id,
        usuarioId: usuarioId,
        items: detalles,
        subtotal: subtotalGeneral,
        costoEnvio: costoEnvio,
        total: totalGeneral,
        direccionEnvio: direccionEnvio,
        estado: 'PENDIENTE_PAGO',
        fechaCreacion: Date.now(),
        fechaExpiracionReserva: Date.now() + (30 * 60 * 1000)
      };

      transaction.set(pedidoRef, pedido);
      transaction.set(carritoRef, { usuarioId: usuarioId, items: [], ultimaActualizacion: Date.now() });

      return pedido;
    });

    return res.status(201).json({ mensaje: 'Pedido creado exitosamente', pedido: resultadoPedido });
  } catch (error) {
    return res.status(400).json({ error: error.message });
  }
});

// 2. Endpoint: crear transacción de pago en Wompi
app.post('/api/crear-pago', verificarAuth, async (req, res) => {
  const { pedidoId, metodo } = req.body;
  const usuarioId = req.usuarioId;

  try {
    const pedidoDoc = await db.collection('pedidos').doc(pedidoId).get();
    if (!pedidoDoc.exists || pedidoDoc.data().usuarioId !== usuarioId) {
      return res.status(404).json({ error: 'Pedido no encontrado' });
    }

    const pedido = pedidoDoc.data();
    const referencia = `AGEDOGS-${pedido.id}-${Date.now()}`;
    const montoCentavos = pedido.total * 100;

    const pagoRef = db.collection('pagos').doc();
    const pago = {
      id: pagoRef.id,
      pedidoId: pedido.id,
      proveedor: 'Wompi',
      referencia: referencia,
      idTransaccion: `TRX-${Date.now()}`,
      estado: 'PENDIENTE',
      montoCentavos: montoCentavos,
      moneda: 'COP',
      metodo: metodo || 'CARD',
      fecha: Date.now(),
      respuestaResumen: 'Transacción iniciada en Wompi Sandbox'
    };

    await pagoRef.set(pago);
    return res.status(200).json({
      mensaje: 'Transacción de pago creada',
      pago,
      wompiSandboxCheckoutUrl: `https://checkout.wompi.co/p/?public-key=pub_test_xxxxx&currency=COP&amount-in-cents=${montoCentavos}&reference=${referencia}`
    });
  } catch (error) {
    return res.status(500).json({ error: error.message });
  }
});

// 3. Endpoint: Webhook de pagos Wompi (Idempotente)
app.post('/api/webhook-wompi', async (req, res) => {
  const evento = req.body;
  try {
    const dataTransaccion = evento.data?.transaction;
    if (!dataTransaccion) {
      return res.status(400).json({ error: 'Estructura de evento Wompi inválida' });
    }

    const referencia = dataTransaccion.reference;
    const estadoWompi = dataTransaccion.status;

    const pagosSnap = await db.collection('pagos').where('referencia', '==', referencia).limit(1).get();
    if (pagosSnap.empty) {
      return res.status(404).json({ error: 'Pago no encontrado para esta referencia' });
    }

    const pagoDoc = pagosSnap.docs[0];
    const pagoData = pagoDoc.data();

    // Idempotencia: Si ya está procesado con el mismo estado, retornar 200 OK
    if (pagoData.estado === estadoWompi) {
      return res.status(200).json({ mensaje: 'Evento ya procesado previamente (Idempotente)' });
    }

    await db.runTransaction(async (transaction) => {
      transaction.update(pagoDoc.ref, { estado: estadoWompi, respuestaResumen: `Webhook Wompi recibido: ${estadoWompi}` });

      const pedidoRef = db.collection('pedidos').doc(pagoData.pedidoId);
      const estadoPedido = estadoWompi === 'APPROVED' ? 'PAGADO' : 'RECHAZADO';

      transaction.update(pedidoRef, { estado: estadoPedido });

      if (estadoPedido === 'RECHAZADO') {
        const pedidoDoc = await transaction.get(pedidoRef);
        if (pedidoDoc.exists) {
          const pedido = pedidoDoc.data();
          for (const item of pedido.items) {
            const invRef = db.collection('inventario').doc(item.productoId);
            const invDoc = await transaction.get(invRef);
            if (invDoc.exists) {
              const stockActual = invDoc.data().cantidadDisponible;
              transaction.update(invRef, { cantidadDisponible: stockActual + item.cantidad });
            }
          }
        }
      }
    });

    return res.status(200).json({ recibido: true });
  } catch (error) {
    return res.status(500).json({ error: error.message });
  }
});

// 4. Endpoint: Proxy de Chat IA
app.post('/api/proxy-ia', verificarAuth, async (req, res) => {
  const { mensaje, perroContexto, sistemaPrompt } = req.body;
  const webhookUrl = process.env.AI_WEBHOOK_URL || 'https://hook.eu1.make.com/your-webhook-endpoint';

  try {
    const resp = await fetch(webhookUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ mensaje, perroContexto, sistemaPrompt })
    });

    if (!resp.ok) {
      throw new Error(`Error en el servicio de IA: HTTP ${resp.status}`);
    }

    const data = await resp.json();
    return res.status(200).json(data);
  } catch (error) {
    return res.status(500).json({ error: error.message });
  }
});

// 5. Endpoint: Liberar reservas vencidas (Cron Job)
app.post('/api/liberar-reservas', async (req, res) => {
  try {
    const ahora = Date.now();
    const pedidosPendientes = await db.collection('pedidos')
      .where('estado', '==', 'PENDIENTE_PAGO')
      .where('fechaExpiracionReserva', '<', ahora)
      .get();

    let liberadosCount = 0;
    for (const doc of pedidosPendientes.docs) {
      const pedido = doc.data();
      await db.runTransaction(async (transaction) => {
        transaction.update(doc.ref, { estado: 'RECHAZADO', respuestaResumen: 'Reserva expirada por tiempo de pago' });
        for (const item of pedido.items) {
          const invRef = db.collection('inventario').doc(item.productoId);
          const invDoc = await transaction.get(invRef);
          if (invDoc.exists) {
            const stockActual = invDoc.data().cantidadDisponible;
            transaction.update(invRef, { cantidadDisponible: stockActual + item.cantidad });
          }
        }
      });
      liberadosCount++;
    }

    return res.status(200).json({ mensaje: `Reservas expiradas liberadas con éxito`, cantidad: liberadosCount });
  } catch (error) {
    return res.status(500).json({ error: error.message });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Backend Age Dogs ejecutándose en puerto ${PORT}`);
});
