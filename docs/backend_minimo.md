# Backend Mínimo — Age Dogs (Node.js + Express)

## 1. Comparación de Opciones (Cloud Functions vs. Node.js + Express)

1. **A) Firebase Cloud Functions (TypeScript)**: Integración nativa con Firebase y mantenimiento cero de servidores, pero exige tarjeta de crédito en plan Blaze (pago por consumo con capa gratuita generosa y alertas de presupuesto para limitar costos).
2. **B) Node.js + Express en hosting gratuito (Render / Railway / Glitch)**: Mayor flexibilidad, despliegue sencillo sin tarjeta de crédito obligatoria, aunque el servicio gratuito puede "dormirse" tras unos minutos de inactividad y requiere configurar una URL pública HTTPS para los webhooks.
* **Recomendación**: **Node.js + Express** para mayor portabilidad y desarrollo universitario gratuito sin requerir tarjeta de crédito en Firebase.

---

## 2. Archivos del Backend

### [`backend/package.json`](file:///C:/Users/crist/AndroidStudioProjects/AgeDogs/backend/package.json)
```json
{
  "name": "agedogs-backend",
  "version": "1.0.0",
  "description": "Backend mínimo para Age Dogs (Validación, Wompi, IA Proxy, Reservas)",
  "main": "server.js",
  "scripts": {
    "start": "node server.js"
  },
  "dependencies": {
    "cors": "^2.8.5",
    "dotenv": "^16.4.5",
    "express": "^4.19.2",
    "firebase-admin": "^12.1.0",
    "node-fetch": "^2.7.0"
  }
}
```

### [`backend/.env.example`](file:///C:/Users/crist/AndroidStudioProjects/AgeDogs/backend/.env.example)
```env
PORT=3000
FIREBASE_PROJECT_ID=age-docs
FIREBASE_CLIENT_EMAIL=firebase-adminsdk-xxxxx@age-docs.iam.gserviceaccount.com
FIREBASE_PRIVATE_KEY="-----BEGIN PRIVATE KEY-----\nMIIEvgIBADA...==\n-----END PRIVATE KEY-----\n"
WOMPI_PRIVATE_KEY=prv_test_xxxxx
WOMPI_INTEGRITY_SECRET=test_integrity_xxxxx
AI_WEBHOOK_URL=https://hook.eu1.make.com/your-webhook-endpoint
```

### [`backend/server.js`](file:///C:/Users/crist/AndroidStudioProjects/AgeDogs/backend/server.js)
*(Incluye verificación de token ID de Firebase, validación atómica de precios y stock en `crearPedido`, creación de pago Wompi, webhook idempotente, proxy de IA y liberación de reservas vencidas).*

---

## 3. Instrucciones de Despliegue y Pruebas (cURL / Postman)

1. **Instalación local**:
   ```bash
   cd backend
   npm install
   ```
2. **Configuración de Variables**: Copia `.env.example` a `.env` y llena tus credenciales de Firebase Admin SDK.
3. **Ejecución local**:
   ```bash
   npm start
   ```
4. **Pruebas con cURL**:
   - **Crear Pedido**:
     ```bash
     curl -X POST http://localhost:3000/api/crear-pedido \
       -H "Authorization: Bearer <FIREBASE_ID_TOKEN>" \
       -H "Content-Type: application/json" \
       -d '{"direccionEnvio": "Calle 100 # 15-20"}'
     ```
   - **Webhook Wompi (Idempotente)**:
     ```bash
     curl -X POST http://localhost:3000/api/webhook-wompi \
       -H "Content-Type: application/json" \
       -d '{"data": {"transaction": {"reference": "AGEDOGS-PED-001-171000", "status": "APPROVED"}}}'
     ```
