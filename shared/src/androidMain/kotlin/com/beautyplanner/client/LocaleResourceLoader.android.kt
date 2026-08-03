package com.beautyplanner.client

actual fun loadLocaleResourceText(path: String): String? {
    val normalized = path.removePrefix("/")
    val classLoader = Thread.currentThread().contextClassLoader
        ?: Locales::class.java.classLoader
        ?: return null

    return runCatching {
        classLoader.getResourceAsStream(normalized)
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { it.readText() }
    }.getOrNull()
}