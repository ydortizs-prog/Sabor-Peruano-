package com.tecsup.saborperuano.data.traduccion

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Traducción inglés → español en el propio celular con ML Kit.
 * El modelo de idioma se descarga una sola vez (con internet); después traduce sin conexión.
 */
class TraductorMlKit : Traductor {
    private val cliente = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.SPANISH)
            .build()
    )
    private val cache = ConcurrentHashMap<String, String>()
    private val preparando = Mutex()
    @Volatile private var modeloListo = false

    override suspend fun aEspanol(texto: String): String {
        if (texto.isBlank()) return texto
        cache[texto]?.let { return it }
        if (!prepararModelo()) return texto
        return try {
            texto.lines().map { linea ->
                val limpia = linea.trimEnd('\r')
                if (limpia.isBlank()) limpia else cliente.translate(limpia).await()
            }.joinToString("\n").also { cache[texto] = it }
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            texto
        }
    }

    private suspend fun prepararModelo(): Boolean = preparando.withLock {
        if (!modeloListo) {
            modeloListo = withTimeoutOrNull(TIEMPO_DESCARGA_MS) {
                try {
                    cliente.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
                    true
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    false
                }
            } ?: false
        }
        modeloListo
    }

    private companion object {
        const val TIEMPO_DESCARGA_MS = 30_000L
    }
}
