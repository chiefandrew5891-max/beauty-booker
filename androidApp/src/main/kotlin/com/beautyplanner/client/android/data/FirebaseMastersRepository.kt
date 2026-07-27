package com.beautyplanner.client.android.data

import android.util.Log
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
        private const val TAG = "FirebaseMastersRepo"
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

            Log.d(TAG, "getCategories: result=${categoryMap.values.map { it.id }}")
            categoryMap.values.toList()
        }.getOrElse { error ->
            Log.e(TAG, "getCategories failed", error)
            emptyList()
        }
    }

    override suspend fun getMasters(
        categoryId: String?,
        query: String?
    ): List<MasterProfile> {
        return runCatching {
            var result = loadMastersUnsafe()

            Log.d(TAG, "getMasters: loaded=${result.size}, categoryId=$categoryId, query=$query")

            if (!categoryId.isNullOrBlank()) {
                result = result.filter { inferCategoryId(it.specialtyTitle) == categoryId }
                Log.d(TAG, "getMasters: after category filter=${result.size}")
            }

            if (!query.isNullOrBlank()) {
                val q = query.trim().lowercase()
                result = result.filter {
                    it.displayName.lowercase().contains(q) ||
                            it.specialtyTitle.lowercase().contains(q) ||
                            it.bio.lowercase().contains(q)
                }
                Log.d(TAG, "getMasters: after query filter=${result.size}")
            }

            result.forEach {
                Log.d(
                    TAG,
                    "getMasters item: id=${it.id}, name=${it.displayName}, spec=${it.specialtyTitle}, rating=${it.averageRating}"
                )
            }

            result
        }.getOrElse { error ->
            Log.e(TAG, "getMasters failed", error)
            emptyList()
        }
    }

    override suspend fun getFeaturedMasters(): List<MasterProfile> {
        return runCatching {
            val result = loadMastersUnsafe()
                .sortedWith(
                    compareByDescending<MasterProfile> { it.averageRating }
                        .thenByDescending { it.reviewCount }
                )
                .take(5)

            Log.d(TAG, "getFeaturedMasters: result=${result.size}")
            result
        }.getOrElse { error ->
            Log.e(TAG, "getFeaturedMasters failed", error)
            emptyList()
        }
    }

    override suspend fun getMasterById(masterId: String): MasterProfile? {
        return runCatching {
            Log.d(TAG, "getMasterById: masterId=$masterId")

            val snap = firestore
                .collection(MASTERS_COLLECTION)
                .document(masterId)
                .get()
                .await()

            Log.d(TAG, "getMasterById: exists=${snap.exists()} path=${snap.reference.path}")

            if (!snap.exists()) return null

            val data = snap.data ?: return null
            Log.d(TAG, "getMasterById: keys=${data.keys}")

            mapMaster(masterId, data)
        }.getOrElse { error ->
            Log.e(TAG, "getMasterById failed for id=$masterId", error)
            null
        }
    }

    override suspend fun getServicesForMaster(masterId: String): List<MasterService> {
        return runCatching {
            Log.d(TAG, "getServicesForMaster: masterId=$masterId")

            val snap = firestore
                .collection(MASTERS_COLLECTION)
                .document(masterId)
                .get()
                .await()

            Log.d(TAG, "getServicesForMaster: exists=${snap.exists()} path=${snap.reference.path}")

            if (!snap.exists()) return emptyList()

            val data = snap.data ?: return emptyList()
            val rawTemplates = data["serviceTemplates"] as? List<*> ?: return emptyList()

            val result = rawTemplates.mapIndexedNotNull { index, rawItem ->
                val item = rawItem as? Map<*, *> ?: return@mapIndexedNotNull null

                val id = item["id"]?.toString()?.trim().orEmpty()
                val rawTitle = item["title"]?.toString()?.trim().orEmpty()
                val defaultPrice = item["defaultPrice"]?.toString()?.trim().orEmpty()
                val isActive = when (val raw = item["isActive"]) {
                    is Boolean -> raw
                    is String -> raw.equals("true", ignoreCase = true)
                    is Number -> raw.toInt() != 0
                    else -> true
                }

                if (!isActive || rawTitle.isBlank()) return@mapIndexedNotNull null

                MasterService(
                    id = if (id.isNotBlank()) id else "svc_${masterId}_$index",
                    masterId = masterId,
                    titleRu = resolveServiceTitle(rawTitle),
                    descriptionRu = "",
                    durationMinutes = 60,
                    price = defaultPrice.toDoubleOrNull() ?: 0.0,
                    currency = "EUR"
                )
            }

            Log.d(TAG, "getServicesForMaster: result=${result.size}")
            result
        }.getOrElse { error ->
            Log.e(TAG, "getServicesForMaster failed for id=$masterId", error)
            emptyList()
        }
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

    private suspend fun loadMastersUnsafe(): List<MasterProfile> {
        Log.d(TAG, "loadMasters: reading collection='$MASTERS_COLLECTION'")

        val snap = firestore
            .collection(MASTERS_COLLECTION)
            .get()
            .await()

        Log.d(TAG, "loadMasters: documents=${snap.documents.size}")

        snap.documents.forEach { doc ->
            Log.d(
                TAG,
                "loadMasters raw doc: id=${doc.id}, exists=${doc.exists()}, keys=${doc.data?.keys}"
            )
        }

        val mapped = snap.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            mapMaster(doc.id, data)
        }

        Log.d(TAG, "loadMasters: mapped=${mapped.size}")

        mapped.forEach {
            Log.d(
                TAG,
                "loadMasters mapped item: id=${it.id}, name=${it.displayName}, spec=${it.specialtyTitle}"
            )
        }

        return mapped.filter { it.displayName.isNotBlank() }
    }

    private fun mapMaster(
        id: String,
        data: Map<String, Any?>
    ): MasterProfile? {
        val clientInteractionsEnabled = when (val raw = data["clientInteractionsEnabled"]) {
            is Boolean -> raw
            is String -> raw.equals("true", ignoreCase = true)
            is Number -> raw.toInt() != 0
            else -> false
        }

        Log.d(
            TAG,
            "mapMaster: id=$id, clientInteractionsEnabled=$clientInteractionsEnabled, ownerName=${data["ownerName"]}, specialization=${data["profileSpecialization"]}"
        )

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