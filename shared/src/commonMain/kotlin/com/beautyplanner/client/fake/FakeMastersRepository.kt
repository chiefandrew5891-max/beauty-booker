package com.beautyplanner.client.fake

import com.beautyplanner.client.domain.model.MasterCategory
import com.beautyplanner.client.domain.model.MasterProfile
import com.beautyplanner.client.domain.model.MasterService
import com.beautyplanner.client.domain.repository.MastersRepository

/**
 * In-memory fake masters repository for development and demo.
 * TODO: Replace with real backend API calls.
 */
class FakeMastersRepository : MastersRepository {

    private val categories = listOf(
        MasterCategory(id = "nails", titleRu = "Ногти"),
        MasterCategory(id = "hair", titleRu = "Волосы"),
        MasterCategory(id = "makeup", titleRu = "Макияж"),
        MasterCategory(id = "brows", titleRu = "Брови"),
        MasterCategory(id = "lashes", titleRu = "Ресницы")
    )

    private val masters = listOf(
        MasterProfile(
            id = "master-1",
            displayName = "Анна Коваль",
            avatarUrl = "https://i.pravatar.cc/150?img=1",
            categoryId = "nails",
            specialtyTitle = "Мастер маникюра",
            averageRating = 4.9f,
            reviewCount = 142,
            bio = "Профессиональный маникюр и педикюр. Работаю 7 лет."
        ),
        MasterProfile(
            id = "master-2",
            displayName = "Олена Бойко",
            avatarUrl = "https://i.pravatar.cc/150?img=5",
            categoryId = "hair",
            specialtyTitle = "Парикмахер-стилист",
            averageRating = 4.7f,
            reviewCount = 89,
            bio = "Стрижки, окрашивание, укладки. Консультация по выбору стиля."
        ),
        MasterProfile(
            id = "master-3",
            displayName = "Марина Петренко",
            avatarUrl = "https://i.pravatar.cc/150?img=9",
            categoryId = "makeup",
            specialtyTitle = "Визажист",
            averageRating = 4.8f,
            reviewCount = 211,
            bio = "Макияж для фотосессий, вечеринок и свадеб."
        ),
        MasterProfile(
            id = "master-4",
            displayName = "Катерина Шевченко",
            avatarUrl = "https://i.pravatar.cc/150?img=12",
            categoryId = "brows",
            specialtyTitle = "Мастер бровей",
            averageRating = 4.6f,
            reviewCount = 67,
            bio = "Архитектура, окрашивание и ламинирование бровей."
        ),
        MasterProfile(
            id = "master-5",
            displayName = "Ірина Мороз",
            avatarUrl = "https://i.pravatar.cc/150?img=16",
            categoryId = "lashes",
            specialtyTitle = "Мастер наращивания ресниц",
            averageRating = 4.9f,
            reviewCount = 303,
            bio = "Класика, об'єм, мегаоб'єм. Індивідуальний підхід."
        )
    )

    private val services = mapOf(
        "master-1" to listOf(
            MasterService("svc-1-1", "master-1", "Маникюр классический", durationMinutes = 60, price = 350.0),
            MasterService("svc-1-2", "master-1", "Маникюр + гель-лак", durationMinutes = 90, price = 550.0),
            MasterService("svc-1-3", "master-1", "Педикюр классический", durationMinutes = 75, price = 450.0)
        ),
        "master-2" to listOf(
            MasterService("svc-2-1", "master-2", "Стрижка женская", durationMinutes = 60, price = 400.0),
            MasterService("svc-2-2", "master-2", "Окрашивание волос", durationMinutes = 120, price = 900.0),
            MasterService("svc-2-3", "master-2", "Укладка", durationMinutes = 45, price = 300.0)
        ),
        "master-3" to listOf(
            MasterService("svc-3-1", "master-3", "Дневной макияж", durationMinutes = 60, price = 500.0),
            MasterService("svc-3-2", "master-3", "Вечерний макияж", durationMinutes = 90, price = 700.0),
            MasterService("svc-3-3", "master-3", "Свадебный макияж", durationMinutes = 120, price = 1200.0)
        ),
        "master-4" to listOf(
            MasterService("svc-4-1", "master-4", "Архитектура бровей", durationMinutes = 45, price = 350.0),
            MasterService("svc-4-2", "master-4", "Окрашивание бровей", durationMinutes = 30, price = 200.0)
        ),
        "master-5" to listOf(
            MasterService("svc-5-1", "master-5", "Классика (наращивание)", durationMinutes = 120, price = 700.0),
            MasterService("svc-5-2", "master-5", "Объём 2D-3D", durationMinutes = 150, price = 950.0),
            MasterService("svc-5-3", "master-5", "Коррекция", durationMinutes = 90, price = 600.0)
        )
    )

    override suspend fun getCategories(): List<MasterCategory> = categories

    override suspend fun getMasters(categoryId: String?, query: String?): List<MasterProfile> {
        var result = masters
        if (categoryId != null) {
            result = result.filter { it.categoryId == categoryId }
        }
        if (!query.isNullOrBlank()) {
            val q = query.lowercase()
            result = result.filter {
                it.displayName.lowercase().contains(q) ||
                        it.specialtyTitle.lowercase().contains(q)
            }
        }
        return result
    }

    override suspend fun getFeaturedMasters(): List<MasterProfile> =
        masters.sortedByDescending { it.averageRating }.take(3)

    override suspend fun getMasterById(masterId: String): MasterProfile? =
        masters.find { it.id == masterId }

    override suspend fun getServicesForMaster(masterId: String): List<MasterService> =
        services[masterId] ?: emptyList()
}
