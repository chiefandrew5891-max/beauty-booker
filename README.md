# Beauty Planner Client

**Beauty Planner Client** — клиентское мобильное приложение-компаньон экосистемы Beauty Planner,
позволяющее конечным пользователям находить мастеров, записываться на услуги и оставлять отзывы.

Платформа: **Kotlin Multiplatform (Android + iOS)**

---

## Что реализовано (текущий скаффолд)

| Модуль | Статус |
|---|---|
| Shared domain-модели | ✅ |
| Интерфейсы репозиториев | ✅ |
| Фейковые (in-memory) реализации | ✅ |
| Строки UI на русском | ✅ |
| Android: Auth экран | ✅ |
| Android: Complete Profile экран | ✅ |
| Android: Discover экран (поиск, категории, карусель, список) | ✅ |
| Android: Master Profile экран | ✅ |
| Android: Services экран | ✅ |
| Android: Date & Time экран | ✅ |
| Android: Booking Form экран | ✅ |
| Android: Booking Confirmation экран | ✅ |
| Android: Reviews экран | ✅ |
| Android: Leave Review экран | ✅ |
| Android: Review Reminder Dialog | ✅ |
| iOS: базовая SwiftUI-заглушка | ✅ |

---

## Поток аутентификации

```
Auth Screen
    ├── Sign in with Google / Apple / Email  →  [CompleteProfile (если нет никнейма)]  →  Discover
    └── Continue as Guest  →  Discover (гостевой режим)
```

- После входа, если у пользователя нет никнейма/публичного имени, отображается экран **Complete Profile**.
- Никнейм используется в отзывах и оценках.

---

## Ограничения гостевого режима

| Действие | Гость | Аутентифицированный |
|---|---|---|
| Просмотр мастеров | ✅ | ✅ |
| Просмотр отзывов | ✅ | ✅ |
| Запись к мастеру | ❌ | ✅ |
| Оставить отзыв | ❌ | ✅ |

---

## Система отзывов — правила

- Отзывы разрешены только после **завершённой записи**.
- **Один отзыв на одну запись** (ограничение через `appointmentId`).
- Имя автора отзыва — это `ClientProfile.nickname` (публичное имя).
- Мастер может скрыть отзыв из публичной ленты (`isHiddenByMaster = true`), но **не может удалить** его и не может изменить оценку.

---

## Напоминания об отзывах

После завершённой записи без поданного отзыва клиент видит всплывающее окно-напоминание.

Поведение:
- Кнопка **"Оставить отзыв"** — переход к форме отзыва.
- Кнопка **"Позже"** — откладывает напоминание (snooze), увеличивает `snoozeCount`.
- Текст напоминания меняется в зависимости от `snoozeCount`, чтобы не быть однообразным.

---

## Shared domain-модели

| Модель | Описание |
|---|---|
| `MasterProfile` | Профиль мастера (имя, аватар, специализация, рейтинг) |
| `MasterService` | Услуга мастера (цена, длительность) |
| `AvailableSlot` | Доступный слот записи |
| `BookingRequest` | Запрос на запись |
| `ClientProfile` | Профиль клиента (никнейм, гостевой/не гостевой) |
| `MasterReview` | Отзыв клиента о записи |
| `ReviewSubmission` | Данные для отправки отзыва |
| `PendingReviewPrompt` | Ожидающее напоминание об отзыве (с поддержкой snooze) |
| `MasterCategory` | Категория мастеров (id + человекочитаемое название) |

---

## Что сейчас фейковое / заглушки

Все текущие реализации репозиториев содержат данные в памяти:

- `AuthRepository` — имитирует вход через Google/Apple/Email, возвращает фиктивный профиль.
- `MastersRepository` — 5 заранее заданных мастеров.
- `BookingRepository` — 5 слотов, запись хранится в памяти.
- `ReviewsRepository` — несколько примеров отзывов + одно напоминание.
- `ClientProfileRepository` — хранит профили в памяти.

### Что нужно заменить позже

- `AuthRepository` → **Firebase Auth** (Google Sign-In, Apple Sign-In, Email/Password).
- `MastersRepository`, `BookingRepository`, `ReviewsRepository` → **реальный backend API**.
- `ClientProfileRepository` → **Firebase Firestore** или backend profile storage.
- iOS UI → полноценный SwiftUI-интерфейс, зеркалирующий Android-флоу.

---

## Структура проекта

```
beauty-planner-client/
├── composeApp/               # KMP UI/app-flow модуль
│   └── src/
│       ├── commonMain/       # общие auth flow, theme, root app state
│       ├── androidMain/      # Android-specific navigation/data implementations
│       ├── iosMain/          # iOS entry points / placeholders for ongoing migration
│       └── commonTest/       # общие тесты
├── shared/                   # временно хранит domain models, repository interfaces, strings
├── androidApp/               # Android host/bootstrap layer
│   └── src/main/kotlin/com/beautyplanner/client/android/MainActivity.kt
└── iosApp/                   # iOS host/app layer
    └── iosApp/               # BeautyPlannerClientApp.swift + ContentView.swift
```

### Что уже переехало в `composeApp`

- общий auth flow (`AuthScreen`, `CompleteProfileScreen`)
- общее app/root state orchestration (`BeautyBookerApp`)
- общая тема Compose
- Android-specific navigation и backend wiring теперь собираются через `composeApp` source sets

### Что пока временно остаётся Android-only

- discover / booking / reviews / master screens
- Firebase-backed master + booking implementations
- iOS post-auth screens (в `iosMain` пока только host seam / placeholder)

---

## Быстрый старт (Android)

1. Откройте корневую папку в **Android Studio Hedgehog** или новее.
2. Дождитесь синхронизации Gradle.
3. Запустите конфигурацию `androidApp`.

> Если Gradle Wrapper отсутствует, выполните:  
> `gradle wrapper --gradle-version=8.6`

## Быстрый старт (iOS)

1. Откройте `iosApp/iosApp.xcodeproj` в Xcode 15+.
2. Выберите симулятор и нажмите Run.
3. iOS-реализация сейчас является заглушкой; полная интеграция с KMP — следующий шаг.
