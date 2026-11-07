package com.tecsup.saborperuano.data.repository

import com.tecsup.saborperuano.domain.model.Item
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class ItemRepositoryImpl : ItemRepository {
    private val recetas = listOf(
        Item("aji-de-gallina", "Ají de gallina", "Pollo deshilachado en crema de ají amarillo."),
        Item("lomo-saltado", "Lomo saltado", "Carne salteada con cebolla, tomate y papas fritas."),
        Item("causa-limena", "Causa limeña", "Capas de papa amarilla sazonadas con limón y ají."),
        Item("arroz-con-pollo", "Arroz con pollo", "Arroz al culantro con pollo y verduras.")
    )
    private val catalogo = MutableStateFlow<List<Item>>(emptyList())

    override val items = catalogo.asStateFlow()
    override val favoritos = items.map { lista -> lista.filter(Item::esFavorito) }

    override suspend fun refrescar(): Result<Int> {
        catalogo.update { actuales ->
            val idsFavoritos = actuales.filter(Item::esFavorito).map(Item::id).toSet()
            recetas.map { it.copy(esFavorito = it.id in idsFavoritos) }
        }
        return Result.success(recetas.size)
    }

    override suspend fun obtenerDetalle(id: String): Result<Item> =
        catalogo.value.firstOrNull { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Receta no encontrada."))

    override suspend fun alternarFavorito(id: String) {
        catalogo.update { lista ->
            lista.map { if (it.id == id) it.copy(esFavorito = !it.esFavorito) else it }
        }
    }

    override suspend fun cantidadGuardada(): Int = catalogo.value.size

    override suspend fun cantidadFavoritos(): Int = catalogo.value.count(Item::esFavorito)

    override suspend fun limpiarCache() {
        catalogo.value = emptyList()
    }
}
