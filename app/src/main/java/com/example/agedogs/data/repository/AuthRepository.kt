package com.example.agedogs.data.repository

import com.example.agedogs.data.model.Usuario
import com.example.agedogs.data.remote.FirebaseProvider
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseProvider.auth,
    private val firestore: FirebaseFirestore = FirebaseProvider.firestore
) {

    val currentUid: String? get() = auth.currentUser?.uid

    suspend fun register(
        nombre: String,
        email: String,
        pass: String,
        telefono: String = "",
        direccion: String = ""
    ): Result<Usuario> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("Error al obtener UID del usuario")

            val usuario = Usuario(
                id = uid,
                email = email,
                nombre = nombre,
                telefono = telefono,
                direccion = direccion,
                rol = Usuario.ROL_CLIENTE,
                fechaRegistro = System.currentTimeMillis()
            )

            firestore.collection("usuarios").document(uid).set(usuario).await()
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, pass: String): Result<Usuario> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("Error de autenticación")

            val docSnapshot = firestore.collection("usuarios").document(uid).get().await()
            val usuario = docSnapshot.toObject(Usuario::class.java)
                ?: Usuario(id = uid, email = email, nombre = email.substringBefore("@"))

            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUserData(): Usuario? {
        val uid = currentUid ?: return null
        return try {
            val docSnapshot = firestore.collection("usuarios").document(uid).get().await()
            docSnapshot.toObject(Usuario::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfile(usuario: Usuario): Result<Unit> {
        return try {
            val uid = currentUid ?: throw Exception("No hay sesión activa")
            val updatedData = mapOf(
                "nombre" to usuario.nombre,
                "telefono" to usuario.telefono,
                "direccion" to usuario.direccion,
                "fotoUrl" to usuario.fotoUrl
            )
            firestore.collection("usuarios").document(uid).update(updatedData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(oldPass: String, newPass: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: throw Exception("No hay sesión activa")
            val email = user.email ?: throw Exception("Correo no disponible")

            val credential = EmailAuthProvider.getCredential(email, oldPass)
            user.reauthenticate(credential).await()
            user.updatePassword(newPass).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }
}
