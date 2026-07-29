package com.beautyplanner.client.strings

data class LanguageOption(
    val code: String,
    val label: String
)

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

    const val AUTH_EMAIL_LABEL = "Email"
    const val AUTH_PASSWORD_LABEL = "Пароль"
    const val AUTH_EMAIL_SIGN_IN = "Войти по email"
    const val AUTH_EMAIL_REGISTER = "Зарегистрироваться"
    const val AUTH_SWITCH_TO_REGISTER = "Нет аккаунта? Зарегистрироваться"
    const val AUTH_SWITCH_TO_SIGN_IN = "Уже есть аккаунт? Войти"
    const val ERROR_EMPTY_EMAIL = "Введите email"
    const val ERROR_EMPTY_PASSWORD = "Введите пароль"

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

    // ── Main navigation / tabs ────────────────────────────────────────────────
    const val NAV_HOME = "Главная"
    const val NAV_APPOINTMENTS = "Записи"
    const val NAV_FAVORITES = "Избранное"
    const val NAV_PROFILE = "Профиль"

    // ── Appointments ──────────────────────────────────────────────────────────
    const val APPOINTMENTS_TITLE = "Мои записи"
    const val APPOINTMENTS_EMPTY = "Пока нет записей"
    const val APPOINTMENT_UPCOMING = "Скоро"
    const val APPOINTMENT_COMPLETED = "Завершено"
    const val APPOINTMENT_CANCELLED = "Отменено"

    // ── Profile / Settings ────────────────────────────────────────────────────
    const val PROFILE_TITLE = "Профиль"
    const val FAVORITES_TITLE = "Избранные мастера"
    const val FAVORITES_EMPTY = "Добавляйте мастеров в избранное, чтобы они были всегда под рукой"
    const val SETTINGS_TITLE = "Настройки"
    const val SETTINGS_THEME = "Тема приложения"
    const val SETTINGS_LANGUAGE = "Язык приложения"
    const val SETTINGS_THEME_SYSTEM = "Системная"
    const val SETTINGS_THEME_LIGHT = "Светлая"
    const val SETTINGS_THEME_DARK = "Тёмная"
    const val SETTINGS_CLOSE = "Закрыть"
    const val PROFILE_GUEST_MODE = "Гостевой режим"
    const val PROFILE_CLIENT_MODE = "Клиентский аккаунт"
    const val PROFILE_SETTINGS_HINT = "Открывайте настройки через иконку в правом верхнем углу."

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

    // ── Language options (prepared for broad localisation) ───────────────────
    val LANGUAGE_OPTIONS = listOf(
        LanguageOption("system", "Системный"),
        LanguageOption("ru", "Русский"),
        LanguageOption("en", "English"),
        LanguageOption("uk", "Українська"),
        LanguageOption("pl", "Polski"),
        LanguageOption("de", "Deutsch"),
        LanguageOption("fr", "Français"),
        LanguageOption("es", "Español"),
        LanguageOption("it", "Italiano"),
        LanguageOption("pt", "Português"),
        LanguageOption("tr", "Türkçe"),
        LanguageOption("ar", "العربية"),
        LanguageOption("hi", "हिन्दी"),
        LanguageOption("zh-Hans", "简体中文"),
        LanguageOption("zh-Hant", "繁體中文"),
        LanguageOption("ja", "日本語"),
        LanguageOption("ko", "한국어"),
        LanguageOption("vi", "Tiếng Việt"),
        LanguageOption("th", "ไทย"),
        LanguageOption("id", "Bahasa Indonesia"),
        LanguageOption("ms", "Bahasa Melayu"),
        LanguageOption("nl", "Nederlands"),
        LanguageOption("sv", "Svenska"),
        LanguageOption("no", "Norsk"),
        LanguageOption("da", "Dansk"),
        LanguageOption("fi", "Suomi"),
        LanguageOption("cs", "Čeština"),
        LanguageOption("sk", "Slovenčina"),
        LanguageOption("hu", "Magyar"),
        LanguageOption("ro", "Română"),
        LanguageOption("bg", "Български"),
        LanguageOption("el", "Ελληνικά")
    )
}
