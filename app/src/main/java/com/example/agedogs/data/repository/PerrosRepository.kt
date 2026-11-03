package com.example.agedogs.data.repository

import android.net.Uri
import com.example.agedogs.data.model.Perro
import com.example.agedogs.data.remote.FirebaseProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PerrosRepository(
    private val firestore: FirebaseFirestore = FirebaseProvider.firestore,
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    suspend fun obtenerPerros(usuarioId: String): Result<List<Perro>> {
        return try {
            val querySnapshot = firestore.collection("perros")
                .whereEqualTo("usuarioId", usuarioId)
                .get()
                .await()

            val perros = querySnapshot.documents.mapNotNull { it.toObject(Perro::class.java) }
            Result.success(perros)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun guardarPerro(perro: Perro): Result<Unit> {
        return try {
            if (perro.fotos.size > 5) {
                return Result.failure(Exception("Máximo 5 fotos permitidas por perro"))
            }
            val docRef = if (perro.id.isBlank()) {
                firestore.collection("perros").document()
            } else {
                firestore.collection("perros").document(perro.id)
            }

            val perroToSave = perro.copy(id = docRef.id)
            docRef.set(perroToSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarPerro(perroId: String): Result<Unit> {
        return try {
            firestore.collection("perros").document(perroId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun subirImagen(uri: Uri, usuarioId: String): Result<String> {
        return try {
            val filename = "perros/${usuarioId}/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(filename)
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
