package com.example.agedogs.data.repository

import com.example.agedogs.data.model.Conversacion
import com.example.agedogs.data.model.Mensaje
import com.example.agedogs.data.model.Usuario
import com.example.agedogs.data.remote.FirebaseProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val firestore: FirebaseFirestore = FirebaseProvider.firestore
) {

    suspend fun obtenerDirectorioUsuarios(uidActual: String): Result<List<Usuario>> {
        return try {
            // Obtener lista de bloqueados
            val bloqueadosSnap = firestore.collection("usuarios").document(uidActual)
                .collection("bloqueados").get().await()
            val bloqueadosUids = bloqueadosSnap.documents.mapNotNull { it.getString("usuarioBloqueadoId") }

            val snapshot = firestore.collection("usuarios").get().await()
            val usuarios = snapshot.documents
                .mapNotNull { it.toObject(Usuario::class.java) }
                .filter { it.id != uidActual && !bloqueadosUids.contains(it.id) }
                .map { usr ->
                    // PRIVACIDAD: Nunca exponer correo ni teléfono en el directorio
                    usr.copy(email = "", telefono = "")
                }

            Result.success(usuarios)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerOcrearConversacion(uid1: String, uid2: String): Result<Conversacion> {
        return try {
            // Buscar si ya existe una conversación con estos dos participantes
            val query = firestore.collection("conversaciones")
                .whereArrayContains("participantes", uid1)
                .get()
                .await()

            val existente = query.documents.mapNotNull { it.toObject(Conversacion::class.java) }
                .firstOrNull { it.participantes.contains(uid2) && it.participantes.size == 2 }

            if (existente != null) {
                return Result.success(existente)
            }

            // Si no existe, crear nueva
            val docRef = firestore.collection("conversaciones").document()
            val nuevaConv = Conversacion(
                id = docRef.id,
                participantes = listOf(uid1, uid2),
                ultimoMensaje = "Conversación iniciada",
                ultimoMensajeFecha = System.currentTimeMillis(),
                fechaCreacion = System.currentTimeMillis()
            )

            docRef.set(nuevaConv).await()
            Result.success(nuevaConv)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun enviarMensaje(conversacionId: String, remitenteId: String, texto: String): Result<Unit> {
        return try {
            val msgRef = firestore.collection("conversaciones").document(conversacionId)
                .collection("mensajes").document()

            val mensaje = Mensaje(
                id = msgRef.id,
                remitenteId = remitenteId,
                texto = texto,
                fechaHora = System.currentTimeMillis(),
                leido = false
            )

            firestore.runTransaction { transaction ->
                transaction.set(msgRef, mensaje)
                val convRef = firestore.collection("conversaciones").document(conversacionId)
                transaction.update(convRef, mapOf(
                    "ultimoMensaje" to texto,
                    "ultimoMensajeFecha" to mensaje.fechaHora
                ))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun bloquearUsuario(uidActual: String, uidABloquear: String): Result<Unit> {
        return try {
            val ref = firestore.collection("usuarios").document(uidActual)
                .collection("bloqueados").document(uidABloquear)
            ref.set(mapOf("usuarioBloqueadoId" to uidABloquear, "fecha" to System.currentTimeMillis())).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
