package com.beautyplanner.client

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object Locales {
    var currentLanguage by mutableStateOf("ru")
        private set

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val strings = mutableMapOf<String, Map<String, String>>()
    private val loadMutex = Mutex()
    private var initialized = false

    suspend fun init(defaultLanguage: String = "ru") {
        loadMutex.withLock {
            if (initialized) {
                currentLanguage = normalizeLang(defaultLanguage)
                ensureLoaded("en")
                ensureLoaded(currentLanguage)
                return
            }

            currentLanguage = normalizeLang(defaultLanguage)
            ensureLoaded("en")
            ensureLoaded(currentLanguage)
            initialized = true
        }
    }

    suspend fun onLanguageChanged(langCode: String) {
        val normalized = normalizeLang(langCode)

        loadMutex.withLock {
            ensureLoaded("en")
            ensureLoaded(normalized)
            currentLanguage = normalized
        }
    }

    fun t(key: String): String {
        val lang = normalizeLang(currentLanguage)
        return strings[lang]?.get(key)
            ?: strings["en"]?.get(key)
            ?: key
    }

    private fun normalizeLang(raw: String?): String {
        val value = raw?.trim().orEmpty()
        if (value.isBlank() || value == "system") return "en"

        val supported = setOf("ru", "en")

        val candidates = buildList {
            add(value)
            add(value.lowercase())
            add(value.replace('_', '-'))
            add(value.lowercase().replace('_', '-'))

            if (value.contains("-")) add(value.substringBefore("-").lowercase())
            if (value.contains("_")) add(value.substringBefore("_").lowercase())
        }.distinct()

        for (candidate in candidates) {
            if (candidate in supported) return candidate
        }

        return "en"
    }

    private suspend fun ensureLoaded(lang: String) {
        val normalized = normalizeLang(lang)
        if (strings.containsKey(normalized)) return
        strings[normalized] = loadLang(normalized)
    }

    private fun loadLang(lang: String): Map<String, String> {
        val text = loadLocaleResourceText("locales/$lang.json") ?: return emptyMap()

        return try {
            val root = json.parseToJsonElement(text).jsonObject
            buildMap(root.size) {
                for ((k, v) in root) {
                    put(k, v.jsonPrimitive.content)
                }
            }
        } catch (_: Throwable) {
            emptyMap()
        }
    }
}