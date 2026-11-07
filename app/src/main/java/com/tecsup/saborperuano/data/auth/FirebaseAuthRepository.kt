package com.tecsup.saborperuano.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.tecsup.saborperuano.domain.model.Rol
import com.tecsup.saborperuano.domain.model.Sesion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {
    override suspend fun iniciarSesion(correo: String, clave: String): Result<Sesion> {
        return try {
            val usuario = checkNotNull(auth.signInWithEmailAndPassword(correo, clave).await().user)
            try {
                Result.success(leerSesion(usuario))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                auth.signOut()
                Result.failure(error)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override suspend fun sesionActual(): Sesion? = auth.currentUser?.let { leerSesion(it) }

    override fun cerrarSesion() = auth.signOut()

    private suspend fun leerSesion(usuario: FirebaseUser): Sesion {
        val documento = firestore.collection("usuarios").document(usuario.uid).get().await()
        val rol = when (documento.get("rol")) {
            "admin" -> Rol.ADMIN
            "usuario" -> Rol.USUARIO
            else -> throw PerfilSinRolException()
        }
        return Sesion(usuario.uid, usuario.email.orEmpty(), rol)
    }
}

class PerfilSinRolException : IllegalStateException()
