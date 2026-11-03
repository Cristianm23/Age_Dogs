package com.example.agedogs.data.repository

import com.example.agedogs.data.model.Categoria
import com.example.agedogs.data.model.Inventario
import com.example.agedogs.data.model.Producto
import com.example.agedogs.data.remote.FirebaseProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MarketplaceRepository(
    private val firestore: FirebaseFirestore = FirebaseProvider.firestore
) {

    suspend fun obtenerCategorias(): Result<List<Categoria>> {
        return try {
            val snapshot = firestore.collection("categorias").get().await()
            val list = snapshot.documents.mapNotNull { it.toObject(Categoria::class.java) }
            if (list.isEmpty()) {
                seedCategoriasDefault()
                return obtenerCategorias()
            }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun seedCategoriasDefault() {
        val cats = listOf(
            Categoria("cat_concentrado", "Concentrado"),
            Categoria("cat_humedo", "Alimento húmedo"),
            Categoria("cat_snacks", "Snacks comestibles caninos")
        )
        for (cat in cats) {
            firestore.collection("categorias").document(cat.id).set(cat).await()
        }
    }

    suspend fun obtenerProductos(): Result<List<Pair<Producto, Inventario>>> {
        return try {
            val prodSnapshot = firestore.collection("productos").get().await()
            val invSnapshot = firestore.collection("inventario").get().await()

            val inventarioMap = invSnapshot.documents
                .mapNotNull { it.toObject(Inventario::class.java) }
                .associateBy { it.productoId }

            val productos = prodSnapshot.documents.mapNotNull { it.toObject(Producto::class.java) }
            val resultado = productos.map { prod ->
                val inv = inventarioMap[prod.id] ?: Inventario(productoId = prod.id, cantidadDisponible = 10, stockMinimo = 5)
                Pair(prod, inv)
            }

            Result.success(resultado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun guardarProducto(producto: Producto, stockInicial: Int): Result<Unit> {
        return try {
            if (!Categoria.CATEGORIAS_VALIDAS.contains(producto.categoriaId)) {
                return Result.failure(Exception("Categoría inválida. Solo se permiten alimentos para perros (${Categoria.CATEGORIAS_VALIDAS.joinToString()})."))
            }

            val docRef = if (producto.id.isBlank()) firestore.collection("productos").document() else firestore.collection("productos").document(producto.id)
            val prodToSave = producto.copy(id = docRef.id)

            docRef.set(prodToSave).await()

            val invRef = firestore.collection("inventario").document(prodToSave.id)
            val inventario = Inventario(productoId = prodToSave.id, cantidadDisponible = stockInicial, ultimaActualizacion = System.currentTimeMillis())
            invRef.set(inventario).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarInventario(productoId: String, nuevaCantidad: Int): Result<Unit> {
        return try {
            firestore.collection("inventario").document(productoId)
                .update(
                    mapOf(
                        "cantidadDisponible" to nuevaCantidad,
                        "ultimaActualizacion" to System.currentTimeMillis()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarProducto(productoId: String): Result<Unit> {
        return try {
            firestore.collection("productos").document(productoId).delete().await()
            firestore.collection("inventario").document(productoId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sembrarDatosDemostracion(): Result<Unit> {
        return try {
            seedCategoriasDefault()

            // 1. Borrar documentos anteriores para forzar reemplazo exacto
            val oldProds = firestore.collection("productos").get().await()
            for (doc in oldProds.documents) {
                firestore.collection("productos").document(doc.id).delete().await()
                firestore.collection("inventario").document(doc.id).delete().await()
            }

            // 2. Sembrar los 12 productos con URLs de empaques profesionales limpios
            val demoProductos = listOf(
                // --- ROYAL CANIN (4 productos) ---
                Producto(
                    id = "rc_1",
                    nombre = "Royal Canin Puppy Wet",
                    marca = "Royal canin",
                    descripcion = "Alimento húmedo en bolsita (pouch) en salsa para cachorros.",
                    presentacion = "0.15 KG",
                    precio = 11600,
                    categoriaId = "Alimento húmedo",
                    imagenUrl = "https://images.unsplash.com/photo-1541599540903-216a46ca1dc0?w=600",
                    activo = true
                ),
                Producto(
                    id = "rc_2",
                    nombre = "Royal Canin Medium Cachorro",
                    marca = "Royal canin",
                    descripcion = "Nutrición a medida para cachorros de razas medianas (2-12 meses).",
                    presentacion = "4 KG",
                    precio = 165599,
                    categoriaId = "Concentrado",
                    imagenUrl = "https://images.unsplash.com/photo-1608474846067-73595f68c4d3?w=600",
                    activo = true
                ),
                Producto(
                    id = "rc_3",
                    nombre = "Royal Canin Medium Adulto",
                    marca = "Royal canin",
                    descripcion = "Alimento equilibrado para perros adultos de talla mediana.",
                    presentacion = "4 KG",
                    precio = 152560,
                    categoriaId = "Concentrado",
                    imagenUrl = "https://images.unsplash.com/photo-1568640347023-a616a95bc3bd?w=600",
                    activo = true
                ),
                Producto(
                    id = "rc_4",
                    nombre = "ROYAL CANIN MINI ADULTO",
                    marca = "Royal canin",
                    descripcion = "Croqueta exclusiva y energía óptima para perros adultos mini.",
                    presentacion = "2 KG",
                    precio = 102000,
                    categoriaId = "Concentrado",
                    imagenUrl = "https://images.unsplash.com/photo-1589924691995-400dc9ecc119?w=600",
                    activo = true
                ),

                // --- PEDIGREE (4 productos) ---
                Producto(
                    id = "ped_1",
                    nombre = "Pedigree Comida Húmeda 18 Sobres / Latas",
                    marca = "Pedigree",
                    descripcion = "Pack múltiple de alimento húmedo balanceado para nutrición diaria.",
                    presentacion = "18 Unidades",
                    precio = 45000,
                    categoriaId = "Alimento húmedo",
                    imagenUrl = "https://images.unsplash.com/photo-1535930891776-0c2dfb7fda1a?w=600",
                    activo = true
                ),
                Producto(
                    id = "ped_2",
                    nombre = "Pedigree - Alimento Perro Adulto Raza Pequeña",
                    marca = "Pedigree",
                    descripcion = "Nutrición completa y balanceada para perros adultos de razas pequeñas.",
                    presentacion = "2 KG",
                    precio = 38950,
                    categoriaId = "Concentrado",
                    imagenUrl = "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=600",
                    activo = true
                ),
                Producto(
                    id = "ped_3",
                    nombre = "Pedigree Sobre Adulto Pollo en Filetes",
                    marca = "Pedigree",
                    descripcion = "Trocitos jugosos de pollo en filetes con ingredientes naturales.",
                    presentacion = "100 GR",
                    precio = 3500,
                    categoriaId = "Alimento húmedo",
                    imagenUrl = "https://images.unsplash.com/photo-1601758228041-f3b2795255f1?w=600",
                    activo = true
                ),
                Producto(
                    id = "ped_4",
                    nombre = "Alimento Seco Para Perro Pedigree Adulto Pancita Saludable",
                    marca = "Pedigree",
                    descripcion = "Enriquecido con fibras prebióticas, pavo, salmón y zanahoria.",
                    presentacion = "4 KG",
                    precio = 65000,
                    categoriaId = "Concentrado",
                    imagenUrl = "https://images.unsplash.com/photo-1576201836106-db1758fd1c97?w=600",
                    activo = true
                ),

                // --- HILL'S (4 productos) ---
                Producto(
                    id = "hill_1",
                    nombre = "Hills Science Diet Cachorros Razas Pequeñas",
                    marca = "Hills",
                    descripcion = "Desarrollo óptimo y croqueta pequeña para cachorros Small & Mini.",
                    presentacion = "4.5 LB",
                    precio = 120000,
                    categoriaId = "Concentrado",
                    imagenUrl = "https://images.unsplash.com/photo-1583512603805-3cc6b41f3edb?w=600",
                    activo = true
                ),
                Producto(
                    id = "hill_2",
                    nombre = "Hills Prescription Diet Z/D Food/Skin",
                    marca = "Hills",
                    descripcion = "Alimento clínico especializado para sensibilidades e intolerancias alimentarias.",
                    presentacion = "7.7 LB",
                    precio = 247840,
                    categoriaId = "Concentrado",
                    imagenUrl = "https://images.unsplash.com/photo-1517849845537-4d257902454a?w=600",
                    activo = true
                ),
                Producto(
                    id = "hill_3",
                    nombre = "Hills Perro W/D Multi-Benefit",
                    marca = "Hills",
                    descripcion = "Nutrición multibeneficio para el control de peso, glucosa y fibra.",
                    presentacion = "3.3 LB (1.58 KG)",
                    precio = 116300,
                    categoriaId = "Concentrado",
                    imagenUrl = "https://images.unsplash.com/photo-1537151608828-ea2b11777ee8?w=600",
                    activo = true
                ),
                Producto(
                    id = "hill_4",
                    nombre = "Hills Prescription Diet Perro Metabolic",
                    marca = "Hills",
                    descripcion = "Pérdida y mantenimiento de pesó clínicamente probado.",
                    presentacion = "17.6 LB",
                    precio = 226272,
                    categoriaId = "Concentrado",
                    imagenUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=600",
                    activo = true
                )
            )

            for (prod in demoProductos) {
                firestore.collection("productos").document(prod.id).set(prod).await()
                val inv = Inventario(productoId = prod.id, cantidadDisponible = (15..35).random(), stockMinimo = 5)
                firestore.collection("inventario").document(prod.id).set(inv).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
