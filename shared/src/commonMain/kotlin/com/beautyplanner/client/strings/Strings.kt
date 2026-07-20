package com.beautyplanner.client.strings

/**
 * Centralised UI string constants (Russian).
 *
 * Structured for easy future localisation — each constant corresponds to a
 * single translatable message key. To add a language, add an alternative
 * object/map for that locale and swap at startup.
 */
object Strings {

    // ── Auth screen ─────────────────────────────────────────────────────────
    const val AUTH_TITLE = "Вход"
    const val AUTH_SUBTITLE =
        "Войдите, чтобы записываться к мастерам, сохранять записи и оставлять отзывы"
    const val AUTH_SIGN_IN_GOOGLE = "Продолжить с Google"
    const val AUTH_SIGN_IN_APPLE = "Продолжить с Apple"
    const val AUTH_SIGN_IN_EMAIL = "Продолжить по email"
    const val AUTH_GUEST = "Продолжить как гость"

    // ── Complete Profile screen ──────────────────────────────────────────────
    const val COMPLETE_PROFILE_TITLE = "Придумайте никнейм"
    const val COMPLETE_PROFILE_SUBTITLE =
        "Это имя будет отображаться в ваших отзывах и оценках"
    const val COMPLETE_PROFILE_HINT = "Ваш никнейм"
    const val COMPLETE_PROFILE_BUTTON = "Продолжить"

    // ── Discover screen ──────────────────────────────────────────────────────
    const val DISCOVER_TITLE = "Найдите своего мастера"
    const val DISCOVER_SUBTITLE = "Выбирайте специалистов, услуги и удобное время"
    const val DISCOVER_SEARCH_HINT = "Поиск мастера или услуги"
    const val DISCOVER_FEATURED = "Рекомендуемые мастера"
    const val DISCOVER_ALL_MASTERS = "Все мастера"
    const val DISCOVER_CATEGORIES = "Категории"

    // ── Master profile / booking ─────────────────────────────────────────────
    const val BOOK_NOW = "Записаться"
    const val REVIEWS_TITLE = "Отзывы"
    const val LEAVE_REVIEW = "Оставить отзыв"
    const val REVIEW_SNOOZE = "Позже"

    // ── Review reminder popup — title variants ───────────────────────────────
    val REMINDER_TITLE_VARIANTS = listOf(
        "Как прошёл ваш визит?",
        "Поделитесь впечатлением о визите",
        "Оцените вашего специалиста"
    )

    // ── Review reminder popup — body variants ────────────────────────────────
    val REMINDER_BODY_VARIANTS = listOf(
        "Ваш отзыв поможет другим клиентам выбрать мастера",
        "Оставьте короткий отзыв о недавнем визите",
        "Поставьте оценку и расскажите, как всё прошло"
    )

    // ── Booking ──────────────────────────────────────────────────────────────
    const val BOOKING_SERVICES_TITLE = "Услуги"
    const val BOOKING_DATE_TIME_TITLE = "Выберите дату и время"
    const val BOOKING_FORM_TITLE = "Подтверждение записи"
    const val BOOKING_NOTE_HINT = "Примечание для мастера (необязательно)"
    const val BOOKING_CONFIRM_BUTTON = "Подтвердить запись"
    const val BOOKING_CONFIRMATION_TITLE = "Запись подтверждена!"
    const val BOOKING_CONFIRMATION_SUBTITLE = "Вы записаны. Ждём вас!"

    // ── Guest restriction messages ────────────────────────────────────────────
    const val GUEST_BOOKING_BLOCKED =
        "Чтобы записаться к мастеру, необходимо войти в аккаунт"
    const val GUEST_REVIEW_BLOCKED =
        "Чтобы оставить отзыв, необходимо войти в аккаунт"
    const val SIGN_IN_BUTTON = "Войти"

    // ── General ──────────────────────────────────────────────────────────────
    const val BACK = "Назад"
    const val LOADING = "Загрузка…"
    const val ERROR_GENERIC = "Что-то пошло не так. Попробуйте снова."
}
