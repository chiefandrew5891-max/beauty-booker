package com.beautyplanner.client.data

import com.beautyplanner.client.domain.model.MasterCategory
import com.beautyplanner.client.domain.model.MasterProfile
import com.beautyplanner.client.domain.model.MasterService
import com.beautyplanner.client.domain.repository.MastersRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseMastersRepository(
    private val firestore: FirebaseFirestore
) : MastersRepository {

    companion object {
        private const val MASTERS_COLLECTION = "masters"
    }

    override suspend fun getCategories(): List<MasterCategory> {
        return runCatching {
            val masters = loadMastersUnsafe()
            val categoryMap = linkedMapOf<String, MasterCategory>()

            masters.forEach { master ->
                val categoryId = inferCategoryId(master.specialtyTitle)
                if (!categoryMap.containsKey(categoryId)) {
                    categoryMap[categoryId] = MasterCategory(
                        id = categoryId,
                        titleRu = categoryTitleRu(categoryId)
                    )
                }
            }

            categoryMap.values.toList()
        }.getOrElse {
            emptyList()
        }
    }

    override suspend fun getMasters(
        categoryId: String?,
        query: String?
    ): List<MasterProfile> {
        return runCatching {
            var result = loadMastersUnsafe()

            if (!categoryId.isNullOrBlank()) {
                result = result.filter { inferCategoryId(it.specialtyTitle) == categoryId }
            }

            if (!query.isNullOrBlank()) {
                val q = query.trim().lowercase()
                result = result.filter {
                    it.displayName.lowercase().contains(q) ||
                            it.specialtyTitle.lowercase().contains(q) ||
                            it.bio.lowercase().contains(q)
                }
            }

            result
        }.getOrElse {
            emptyList()
        }
    }

    override suspend fun getFeaturedMasters(): List<MasterProfile> {
        return runCatching {
            loadMastersUnsafe()
                .sortedWith(
                    compareByDescending<MasterProfile> { it.averageRating }
                        .thenByDescending { it.reviewCount }
                )
                .take(5)
        }.getOrElse {
            emptyList()
        }
    }

    override suspend fun getMasterById(masterId: String): MasterProfile? {
        return runCatching {
            val snap = firestore
                .collection(MASTERS_COLLECTION)
                .document(masterId)
                .get()
                .await()

            if (!snap.exists()) return null
            val data = snap.data ?: return null

            mapMaster(masterId, data)
        }.getOrElse {
            null
        }
    }

    override suspend fun getServicesForMaster(masterId: String): List<MasterService> {
        return runCatching {
            val snap = firestore
                .collection(MASTERS_COLLECTION)
                .document(masterId)
                .get()
                .await()

            if (!snap.exists()) return emptyList()

            val data = snap.data ?: return emptyList()
            val rawTemplates = data["serviceTemplates"] as? List<*> ?: return emptyList()

            rawTemplates.mapIndexedNotNull { index, rawItem ->
                val item = rawItem as? Map<*, *> ?: return@mapIndexedNotNull null

                val id = item["id"]?.toString()?.trim().orEmpty()
                val rawTitle = item["title"]?.toString()?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: item["name"]?.toString()?.trim()
                        ?.takeIf { it.isNotBlank() }
                    ?: item["serviceId"]?.toString()?.trim()
                        ?.takeIf { it.isNotBlank() }
                    ?: id.takeIf { it.isNotBlank() }
                    ?: "Услуга ${index + 1}"

                val description = item["description"]?.toString()?.trim().orEmpty()

                val price = when (val raw = item["defaultPrice"]) {
                    is Number -> raw.toDouble()
                    is String -> raw.trim().replace(",", ".").toDoubleOrNull() ?: 0.0
                    else -> 0.0
                }

                val currency = item["currency"]?.toString()?.trim().orEmpty()

                val durationMinutes = when (val raw = item["durationMinutes"]) {
                    is Number -> raw.toInt()
                    is String -> raw.toIntOrNull() ?: 60
                    else -> 60
                }

                val isActive = when (val raw = item["isActive"]) {
                    is Boolean -> raw
                    is String -> raw.equals("true", ignoreCase = true)
                    is Number -> raw.toInt() != 0
                    null -> true
                    else -> true
                }

                if (!isActive) return@mapIndexedNotNull null

                MasterService(
                    id = if (id.isNotBlank()) id else "svc_${masterId}_$index",
                    masterId = masterId,
                    titleRu = resolveServiceTitle(rawTitle),
                    descriptionRu = description,
                    durationMinutes = durationMinutes,
                    price = price,
                    currency = currency.ifBlank { "UAH" }
                )
            }
        }.getOrElse {
            emptyList()
        }
    }

    private suspend fun loadMastersUnsafe(): List<MasterProfile> {
        val snap = firestore
            .collection(MASTERS_COLLECTION)
            .get()
            .await()

        return snap.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            mapMaster(doc.id, data)
        }.filter { it.displayName.isNotBlank() }
    }

    private fun mapMaster(
        id: String,
        data: Map<String, Any?>
    ): MasterProfile? {
        val clientInteractionsEnabled = when (val raw = data["clientInteractionsEnabled"]) {
            is Boolean -> raw
            is String -> raw.equals("true", ignoreCase = true)
            is Number -> raw.toInt() != 0
            null -> true
            else -> true
        }

        if (!clientInteractionsEnabled) return null

        val displayName = data["ownerName"]?.toString()?.trim().orEmpty()
        val specialtyTitle = data["profileSpecialization"]?.toString()?.trim().orEmpty()
        val profileRating = data["profileRating"]?.toString()?.toFloatOrNull() ?: 0f
        val avatarUrl = data["profileAvatarUrl"]?.toString()?.trim().orEmpty()
        val avatarBase64 = data["profileAvatarBase64"]?.toString()?.trim().orEmpty()

        return MasterProfile(
            id = id,
            displayName = displayName.ifBlank { "Мастер" },
            avatarUrl = avatarUrl.ifBlank {
                if (avatarBase64.isNotBlank()) {
                    "data:image/png;base64,$avatarBase64"
                } else {
                    null
                }
            },
            categoryId = inferCategoryId(specialtyTitle),
            specialtyTitle = specialtyTitle.ifBlank { "Специалист" },
            averageRating = profileRating,
            reviewCount = 0,
            bio = specialtyTitle
        )
    }

    private fun resolveServiceTitle(rawTitle: String): String {
        return when (rawTitle) {
            "service_gel_polish" -> "Гель-лак"
            "service_gel_strengthening" -> "Укрепление гелем"
            "service_nail_extensions" -> "Наращивание ногтей"
            "service_lash_extensions" -> "Наращивание ресниц"
            "service_correction" -> "Коррекция"
            "service_repair" -> "Ремонт"
            else -> rawTitle
        }
    }

    private fun inferCategoryId(specialty: String): String {
        val s = specialty.lowercase()

        return when {
            "маник" in s || "ногт" in s -> "nails"
            "парик" in s || "волос" in s || "hair" in s -> "hair"
            "визаж" in s || "маки" in s || "makeup" in s -> "makeup"
            "бров" in s || "brow" in s -> "brows"
            "ресниц" in s || "lash" in s -> "lashes"
            "космет" in s -> "other"
            else -> "other"
        }
    }

    private fun categoryTitleRu(categoryId: String): String {
        return when (categoryId) {
            "nails" -> "Ногти"
            "hair" -> "Волосы"
            "makeup" -> "Макияж"
            "brows" -> "Брови"
            "lashes" -> "Ресницы"
            else -> "Другое"
        }
    }
}