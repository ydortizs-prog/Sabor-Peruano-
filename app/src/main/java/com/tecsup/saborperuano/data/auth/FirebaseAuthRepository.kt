package com.tecsup.saborperuano.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
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

    override suspend fun registrar(nombre: String, correo: String, clave: String): Sesion {
        val usuario = try {
            checkNotNull(auth.createUserWithEmailAndPassword(correo, clave).await().user)
        } catch (error: FirebaseAuthUserCollisionException) {
            // Permite completar un registro interrumpido tras crear la cuenta en Auth.
            checkNotNull(auth.signInWithEmailAndPassword(correo, clave).await().user)
        }

        var registrado = false
        try {
            val perfil = firestore.collection("usuarios").document(usuario.uid)
            val creado = firestore.runTransaction { transaccion ->
                if (transaccion.get(perfil).exists()) {
                    false
                } else {
                    transaccion.set(perfil, mapOf("nombre" to nombre, "rol" to "usuario"))
                    true
                }
            }.await()
            if (!creado) throw CuentaYaRegistradaException()

            registrado = true
            return Sesion(usuario.uid, usuario.email.orEmpty(), Rol.USUARIO, nombre)
        } finally {
            if (!registrado && auth.currentUser?.uid == usuario.uid) auth.signOut()
        }
    }

    override suspend fun enviarRestablecimiento(correo: String) {
        auth.sendPasswordResetEmail(correo).await()
    }

    override suspend fun sesionActual(): Sesion? = auth.currentUser?.let { leerSesion(it) }

    override suspend fun confirmarAdministrador(uid: String): Boolean {
        if (auth.currentUser?.uid != uid) return false
        val documento = firestore.collection("usuarios").document(uid).get(Source.SERVER).await()
        return auth.currentUser?.uid == uid && documento.get("rol") == "admin"
    }

    override fun cerrarSesion() = auth.signOut()

    private suspend fun leerSesion(usuario: FirebaseUser): Sesion {
        val documento = firestore.collection("usuarios").document(usuario.uid).get().await()
        val rol = when (documento.get("rol")) {
            "admin" -> Rol.ADMIN
            "usuario" -> Rol.USUARIO
            else -> throw PerfilSinRolException()
        }
        val nombre = (documento.get("nombre") as? String)?.trim()?.takeIf { it.isNotEmpty() }
            ?: usuario.displayName?.trim()?.takeIf { it.isNotEmpty() }
        return Sesion(usuario.uid, usuario.email.orEmpty(), rol, nombre)
    }
}

class PerfilSinRolException : IllegalStateException()
internal class CuentaYaRegistradaException : IllegalStateException()
