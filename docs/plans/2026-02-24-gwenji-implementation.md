# Gwenji Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build an emoji-based AAC Android app that lets a user chain emojis into sentences and speak them via TTS with word-by-word highlighting.

**Architecture:** Single-activity MVVM with Jetpack Compose. A bundled JSON dictionary maps emojis to context-aware words. A sentence assembly engine resolves emoji chains into English text. Android TTS speaks the result with synchronized word highlighting. Room stores history; DataStore stores preferences.

**Tech Stack:** Kotlin 2.1.10, AGP 8.13.0, Jetpack Compose + Material 3, Room, DataStore, Android TTS API. Min SDK 26, target SDK 35.

---

## Task 1: Project Scaffolding

**Files:**
- Create: `app/build.gradle.kts`
- Create: `build.gradle.kts` (root)
- Create: `settings.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `gradle.properties`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/dangerousthings/gwenji/MainActivity.kt`
- Create: `app/src/main/java/com/dangerousthings/gwenji/GwenjiApp.kt`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/themes.xml`

**Step 1: Create version catalog**

`gradle/libs.versions.toml`:
```toml
[versions]
agp = "8.13.0"
kotlin = "2.1.10"
composeBom = "2025.05.01"
coreKtx = "1.16.0"
lifecycleRuntime = "2.9.0"
activityCompose = "1.10.1"
room = "2.7.1"
datastore = "1.1.4"
ksp = "2.1.10-1.0.31"
navigation = "2.9.0"
serialization = "1.8.1"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntime" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntime" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "serialization" }

junit = { group = "junit", name = "junit", version = "4.13.2" }
androidx-test-ext-junit = { group = "androidx.test.ext", name = "junit", version = "1.2.1" }
androidx-test-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version = "3.6.1" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

**Step 2: Create root build.gradle.kts**

`build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}
```

**Step 3: Create settings.gradle.kts**

`settings.gradle.kts`:
```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolution {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Gwenji"
include(":app")
```

**Step 4: Create gradle.properties**

`gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

**Step 5: Create app/build.gradle.kts**

`app/build.gradle.kts`:
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.dangerousthings.gwenji"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dangerousthings.gwenji"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
```

**Step 6: Create AndroidManifest.xml**

`app/src/main/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".GwenjiApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Gwenji">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.Gwenji">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

**Step 7: Create Application class, MainActivity, strings, theme**

`app/src/main/java/com/dangerousthings/gwenji/GwenjiApp.kt`:
```kotlin
package com.dangerousthings.gwenji

import android.app.Application

class GwenjiApp : Application()
```

`app/src/main/java/com/dangerousthings/gwenji/MainActivity.kt`:
```kotlin
package com.dangerousthings.gwenji

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dangerousthings.gwenji.ui.theme.GwenjiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GwenjiTheme {
                // Main content will go here
            }
        }
    }
}
```

`app/src/main/res/values/strings.xml`:
```xml
<resources>
    <string name="app_name">Gwenji</string>
</resources>
```

`app/src/main/res/values/themes.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.Gwenji" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
```

**Step 8: Build the project**

Run: `cd app && ../gradlew.bat assembleDebug` (from project root)
Expected: BUILD SUCCESSFUL

**Step 9: Commit**

```bash
git add -A
git commit -m "feat: scaffold Android project with Compose and dependencies"
```

---

## Task 2: Data Models

**Files:**
- Create: `app/src/main/java/com/dangerousthings/gwenji/model/EmojiEntry.kt`
- Create: `app/src/main/java/com/dangerousthings/gwenji/model/Category.kt`
- Create: `app/src/main/java/com/dangerousthings/gwenji/model/EmojiDictionary.kt`
- Create: `app/src/main/java/com/dangerousthings/gwenji/model/GrammarRole.kt`
- Create: `app/src/test/java/com/dangerousthings/gwenji/model/EmojiEntryTest.kt`

**Step 1: Write tests for data model serialization**

`app/src/test/java/com/dangerousthings/gwenji/model/EmojiEntryTest.kt`:
```kotlin
package com.dangerousthings.gwenji.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class EmojiEntryTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `deserialize emoji entry with all fields`() {
        val jsonString = """
        {
            "emoji": "\uD83D\uDE22",
            "category": "feelings",
            "solo": "I feel sad",
            "chain": {
                "default": "sad",
                "after_i": "feel sad"
            },
            "tags": ["emotion", "negative"],
            "grammar_role": "descriptor"
        }
        """.trimIndent()

        val entry = json.decodeFromString<EmojiEntry>(jsonString)
        assertEquals("\uD83D\uDE22", entry.emoji)
        assertEquals("feelings", entry.category)
        assertEquals("I feel sad", entry.solo)
        assertEquals("sad", entry.chain["default"])
        assertEquals("feel sad", entry.chain["after_i"])
        assertEquals(listOf("emotion", "negative"), entry.tags)
        assertEquals(GrammarRole.DESCRIPTOR, entry.grammarRole)
    }

    @Test
    fun `deserialize emoji entry with minimal chain`() {
        val jsonString = """
        {
            "emoji": "\uD83D\uDC49",
            "category": "people",
            "solo": "I need something",
            "chain": {
                "default": "I"
            },
            "tags": ["self", "me"],
            "grammar_role": "subject"
        }
        """.trimIndent()

        val entry = json.decodeFromString<EmojiEntry>(jsonString)
        assertEquals("I", entry.chain["default"])
        assertEquals(1, entry.chain.size)
    }

    @Test
    fun `deserialize full dictionary`() {
        val jsonString = """
        {
            "categories": [
                {
                    "name": "Feelings",
                    "icon": "heart",
                    "display_order": 1
                }
            ],
            "emojis": [
                {
                    "emoji": "\uD83D\uDE22",
                    "category": "feelings",
                    "solo": "I feel sad",
                    "chain": { "default": "sad" },
                    "tags": ["emotion"],
                    "grammar_role": "descriptor"
                }
            ]
        }
        """.trimIndent()

        val dict = json.decodeFromString<EmojiDictionary>(jsonString)
        assertEquals(1, dict.categories.size)
        assertEquals("Feelings", dict.categories[0].name)
        assertEquals(1, dict.emojis.size)
    }
}
```

**Step 2: Run tests to verify they fail**

Run: `./gradlew.bat test --tests "com.dangerousthings.gwenji.model.EmojiEntryTest"`
Expected: FAIL (classes don't exist yet)

**Step 3: Implement data models**

`app/src/main/java/com/dangerousthings/gwenji/model/GrammarRole.kt`:
```kotlin
package com.dangerousthings.gwenji.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class GrammarRole {
    @SerialName("subject") SUBJECT,
    @SerialName("verb") VERB,
    @SerialName("descriptor") DESCRIPTOR,
    @SerialName("object") OBJECT,
    @SerialName("modifier") MODIFIER,
    @SerialName("greeting") GREETING
}
```

`app/src/main/java/com/dangerousthings/gwenji/model/Category.kt`:
```kotlin
package com.dangerousthings.gwenji.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val name: String,
    val icon: String,
    @SerialName("display_order") val displayOrder: Int
)
```

`app/src/main/java/com/dangerousthings/gwenji/model/EmojiEntry.kt`:
```kotlin
package com.dangerousthings.gwenji.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmojiEntry(
    val emoji: String,
    val category: String,
    val solo: String,
    val chain: Map<String, String>,
    val tags: List<String>,
    @SerialName("grammar_role") val grammarRole: GrammarRole
)
```

`app/src/main/java/com/dangerousthings/gwenji/model/EmojiDictionary.kt`:
```kotlin
package com.dangerousthings.gwenji.model

import kotlinx.serialization.Serializable

@Serializable
data class EmojiDictionary(
    val categories: List<Category>,
    val emojis: List<EmojiEntry>
)
```

**Step 4: Run tests to verify they pass**

Run: `./gradlew.bat test --tests "com.dangerousthings.gwenji.model.EmojiEntryTest"`
Expected: PASS (3 tests)

**Step 5: Commit**

```bash
git add -A
git commit -m "feat: add data models for emoji dictionary with serialization"
```

---

## Task 3: Sentence Assembly Engine

This is the core logic. Highly testable, no Android dependencies.

**Files:**
- Create: `app/src/main/java/com/dangerousthings/gwenji/engine/SentenceAssembler.kt`
- Create: `app/src/test/java/com/dangerousthings/gwenji/engine/SentenceAssemblerTest.kt`

**Step 1: Write comprehensive tests for the assembly engine**

`app/src/test/java/com/dangerousthings/gwenji/engine/SentenceAssemblerTest.kt`:
```kotlin
package com.dangerousthings.gwenji.engine

import com.dangerousthings.gwenji.model.EmojiEntry
import com.dangerousthings.gwenji.model.GrammarRole
import org.junit.Assert.assertEquals
import org.junit.Test

class SentenceAssemblerTest {

    private val assembler = SentenceAssembler()

    private val selfEmoji = EmojiEntry(
        emoji = "\uD83D\uDC49",
        category = "people",
        solo = "I need something",
        chain = mapOf("default" to "I"),
        tags = listOf("self"),
        grammarRole = GrammarRole.SUBJECT
    )

    private val sadEmoji = EmojiEntry(
        emoji = "\uD83D\uDE22",
        category = "feelings",
        solo = "I feel sad",
        chain = mapOf(
            "default" to "sad",
            "after_i" to "feel sad"
        ),
        tags = listOf("emotion"),
        grammarRole = GrammarRole.DESCRIPTOR
    )

    private val hamburgerEmoji = EmojiEntry(
        emoji = "\uD83C\uDF54",
        category = "food",
        solo = "I want a hamburger",
        chain = mapOf(
            "default" to "a hamburger",
            "after_i" to "want a hamburger",
            "after_want" to "a hamburger"
        ),
        tags = listOf("food"),
        grammarRole = GrammarRole.OBJECT
    )

    private val heartEmoji = EmojiEntry(
        emoji = "\u2764\uFE0F",
        category = "feelings",
        solo = "I love you",
        chain = mapOf(
            "default" to "love",
            "after_i" to "love"
        ),
        tags = listOf("love"),
        grammarRole = GrammarRole.VERB
    )

    private val momEmoji = EmojiEntry(
        emoji = "\uD83D\uDC69",
        category = "people",
        solo = "Mom",
        chain = mapOf("default" to "mom"),
        tags = listOf("family"),
        grammarRole = GrammarRole.OBJECT
    )

    @Test
    fun `single emoji returns solo phrase`() {
        val result = assembler.assemble(listOf(sadEmoji))
        assertEquals("I feel sad", result.text)
    }

    @Test
    fun `single self emoji returns solo phrase`() {
        val result = assembler.assemble(listOf(selfEmoji))
        assertEquals("I need something", result.text)
    }

    @Test
    fun `two emojis with context rule`() {
        val result = assembler.assemble(listOf(selfEmoji, sadEmoji))
        assertEquals("I feel sad", result.text)
    }

    @Test
    fun `two emojis food after self`() {
        val result = assembler.assemble(listOf(selfEmoji, hamburgerEmoji))
        assertEquals("I want a hamburger", result.text)
    }

    @Test
    fun `three emojis chain`() {
        val result = assembler.assemble(listOf(selfEmoji, heartEmoji, momEmoji))
        assertEquals("I love mom", result.text)
    }

    @Test
    fun `two emojis no context match falls to default`() {
        val result = assembler.assemble(listOf(heartEmoji, momEmoji))
        assertEquals("love mom", result.text)
    }

    @Test
    fun `empty list returns empty result`() {
        val result = assembler.assemble(emptyList())
        assertEquals("", result.text)
    }

    @Test
    fun `result contains word segments with emoji indices`() {
        val result = assembler.assemble(listOf(selfEmoji, sadEmoji))
        // "I feel sad" -> segment 0: "I" from emoji 0, segment 1: "feel sad" from emoji 1
        assertEquals(2, result.segments.size)
        assertEquals("I", result.segments[0].text)
        assertEquals(0, result.segments[0].emojiIndex)
        assertEquals("feel sad", result.segments[1].text)
        assertEquals(1, result.segments[1].emojiIndex)
    }

    @Test
    fun `result word segments have correct character offsets`() {
        val result = assembler.assemble(listOf(selfEmoji, sadEmoji))
        // "I feel sad"
        //  0         = "I"
        //  2         = "feel sad"
        assertEquals(0, result.segments[0].startOffset)
        assertEquals(1, result.segments[0].endOffset)
        assertEquals(2, result.segments[1].startOffset)
        assertEquals(10, result.segments[1].endOffset)
    }
}
```

**Step 2: Run tests to verify they fail**

Run: `./gradlew.bat test --tests "com.dangerousthings.gwenji.engine.SentenceAssemblerTest"`
Expected: FAIL (class doesn't exist)

**Step 3: Implement sentence assembler**

`app/src/main/java/com/dangerousthings/gwenji/engine/SentenceAssembler.kt`:
```kotlin
package com.dangerousthings.gwenji.engine

import com.dangerousthings.gwenji.model.EmojiEntry

data class WordSegment(
    val text: String,
    val emojiIndex: Int,
    val startOffset: Int,
    val endOffset: Int
)

data class AssemblyResult(
    val text: String,
    val segments: List<WordSegment>
)

class SentenceAssembler {

    fun assemble(emojis: List<EmojiEntry>): AssemblyResult {
        if (emojis.isEmpty()) {
            return AssemblyResult("", emptyList())
        }

        if (emojis.size == 1) {
            val solo = emojis[0].solo
            return AssemblyResult(
                text = solo,
                segments = listOf(
                    WordSegment(
                        text = solo,
                        emojiIndex = 0,
                        startOffset = 0,
                        endOffset = solo.length
                    )
                )
            )
        }

        val segments = mutableListOf<WordSegment>()
        var previousOutput: String? = null
        var currentOffset = 0

        for ((index, emoji) in emojis.withIndex()) {
            val word = resolveChainWord(emoji, previousOutput)
            val segment = WordSegment(
                text = word,
                emojiIndex = index,
                startOffset = currentOffset,
                endOffset = currentOffset + word.length
            )
            segments.add(segment)
            currentOffset += word.length + 1 // +1 for space separator
            previousOutput = word
        }

        val text = segments.joinToString(" ") { it.text }
        return AssemblyResult(text = text, segments = segments)
    }

    private fun resolveChainWord(emoji: EmojiEntry, previousOutput: String?): String {
        if (previousOutput != null) {
            // Check for context-specific keys based on the previous emoji's output
            // Try matching the first word of the previous output as a context key
            val contextKey = "after_${previousOutput.lowercase().split(" ").first()}"
            emoji.chain[contextKey]?.let { return it }
        }
        return emoji.chain["default"] ?: emoji.solo
    }
}
```

**Step 4: Run tests to verify they pass**

Run: `./gradlew.bat test --tests "com.dangerousthings.gwenji.engine.SentenceAssemblerTest"`
Expected: PASS (all 9 tests)

**Step 5: Commit**

```bash
git add -A
git commit -m "feat: implement sentence assembly engine with context-aware chain rules"
```

---

## Task 4: Emoji Dictionary Loader

**Files:**
- Create: `app/src/main/java/com/dangerousthings/gwenji/data/emoji/EmojiRepository.kt`
- Create: `app/src/main/assets/emoji_dictionary.json`
- Create: `app/src/test/java/com/dangerousthings/gwenji/data/emoji/EmojiRepositoryTest.kt`
- Create: `app/src/test/resources/test_dictionary.json`

**Step 1: Write test for dictionary loading**

`app/src/test/java/com/dangerousthings/gwenji/data/emoji/EmojiRepositoryTest.kt`:
```kotlin
package com.dangerousthings.gwenji.data.emoji

import com.dangerousthings.gwenji.model.EmojiDictionary
import com.dangerousthings.gwenji.model.GrammarRole
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parse test dictionary from resource`() {
        val input = javaClass.classLoader!!.getResourceAsStream("test_dictionary.json")!!
            .bufferedReader().readText()
        val dictionary = json.decodeFromString<EmojiDictionary>(input)

        assertTrue(dictionary.emojis.isNotEmpty())
        assertTrue(dictionary.categories.isNotEmpty())
    }

    @Test
    fun `emojis are grouped by category`() {
        val input = javaClass.classLoader!!.getResourceAsStream("test_dictionary.json")!!
            .bufferedReader().readText()
        val dictionary = json.decodeFromString<EmojiDictionary>(input)

        val byCategory = dictionary.emojis.groupBy { it.category }
        assertTrue(byCategory.containsKey("feelings"))
        assertTrue(byCategory.containsKey("people"))
    }

    @Test
    fun `categories are sorted by display order`() {
        val input = javaClass.classLoader!!.getResourceAsStream("test_dictionary.json")!!
            .bufferedReader().readText()
        val dictionary = json.decodeFromString<EmojiDictionary>(input)

        val sorted = dictionary.categories.sortedBy { it.displayOrder }
        assertEquals(sorted, dictionary.categories)
    }
}
```

**Step 2: Create test dictionary resource**

`app/src/test/resources/test_dictionary.json`:
```json
{
  "categories": [
    { "name": "People", "icon": "person", "display_order": 0 },
    { "name": "Feelings", "icon": "heart", "display_order": 1 },
    { "name": "Needs", "icon": "exclamation", "display_order": 2 },
    { "name": "Actions", "icon": "lightning", "display_order": 3 },
    { "name": "Food", "icon": "fork_knife", "display_order": 4 }
  ],
  "emojis": [
    {
      "emoji": "\uD83E\uDEF5",
      "category": "people",
      "solo": "I need something",
      "chain": { "default": "I" },
      "tags": ["self", "me", "I"],
      "grammar_role": "subject"
    },
    {
      "emoji": "\uD83D\uDE22",
      "category": "feelings",
      "solo": "I feel sad",
      "chain": { "default": "sad", "after_i": "feel sad" },
      "tags": ["emotion", "sad", "crying"],
      "grammar_role": "descriptor"
    },
    {
      "emoji": "\uD83D\uDE0A",
      "category": "feelings",
      "solo": "I feel happy",
      "chain": { "default": "happy", "after_i": "feel happy" },
      "tags": ["emotion", "happy", "smile"],
      "grammar_role": "descriptor"
    },
    {
      "emoji": "\uD83C\uDF54",
      "category": "food",
      "solo": "I want a hamburger",
      "chain": { "default": "a hamburger", "after_i": "want a hamburger", "after_want": "a hamburger" },
      "tags": ["food", "burger", "hungry"],
      "grammar_role": "object"
    },
    {
      "emoji": "\uD83D\uDCA4",
      "category": "needs",
      "solo": "I am tired",
      "chain": { "default": "tired", "after_i": "am tired" },
      "tags": ["tired", "sleep", "rest"],
      "grammar_role": "descriptor"
    },
    {
      "emoji": "\u2764\uFE0F",
      "category": "actions",
      "solo": "I love you",
      "chain": { "default": "love", "after_i": "love" },
      "tags": ["love", "heart"],
      "grammar_role": "verb"
    }
  ]
}
```

**Step 3: Run tests to verify they fail**

Run: `./gradlew.bat test --tests "com.dangerousthings.gwenji.data.emoji.EmojiRepositoryTest"`
Expected: FAIL (no test resource yet -- actually resource was created, so should pass)

**Step 4: Implement EmojiRepository**

`app/src/main/java/com/dangerousthings/gwenji/data/emoji/EmojiRepository.kt`:
```kotlin
package com.dangerousthings.gwenji.data.emoji

import android.content.Context
import com.dangerousthings.gwenji.model.Category
import com.dangerousthings.gwenji.model.EmojiDictionary
import com.dangerousthings.gwenji.model.EmojiEntry
import kotlinx.serialization.json.Json

class EmojiRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private lateinit var dictionary: EmojiDictionary

    fun load() {
        val input = context.assets.open("emoji_dictionary.json")
            .bufferedReader().readText()
        dictionary = json.decodeFromString<EmojiDictionary>(input)
    }

    fun getCategories(): List<Category> =
        dictionary.categories.sortedBy { it.displayOrder }

    fun getEmojisByCategory(categoryName: String): List<EmojiEntry> =
        dictionary.emojis.filter {
            it.category.equals(categoryName, ignoreCase = true)
        }

    fun getAllEmojis(): List<EmojiEntry> = dictionary.emojis

    fun searchEmojis(query: String): List<EmojiEntry> =
        dictionary.emojis.filter { entry ->
            entry.tags.any { tag -> tag.contains(query, ignoreCase = true) } ||
                entry.emoji.contains(query)
        }
}
```

**Step 5: Create initial emoji_dictionary.json asset**

`app/src/main/assets/emoji_dictionary.json` -- start with a starter set of ~30 emojis across all categories. We will expand this to ~150 in a later task. Use the same structure as the test dictionary but with more entries. For now, include at least 3-5 emojis per category to make the app functional.

Note: The full ~150 emoji dictionary will be authored with LLM assistance in Task 11.

**Step 6: Run tests to verify they pass**

Run: `./gradlew.bat test --tests "com.dangerousthings.gwenji.data.emoji.EmojiRepositoryTest"`
Expected: PASS (3 tests)

**Step 7: Commit**

```bash
git add -A
git commit -m "feat: add emoji repository and initial dictionary asset"
```

---

## Task 5: App Theme

**Files:**
- Create: `app/src/main/java/com/dangerousthings/gwenji/ui/theme/Color.kt`
- Create: `app/src/main/java/com/dangerousthings/gwenji/ui/theme/Type.kt`
- Create: `app/src/main/java/com/dangerousthings/gwenji/ui/theme/Theme.kt`

**Step 1: Create color palette**

`app/src/main/java/com/dangerousthings/gwenji/ui/theme/Color.kt`:
```kotlin
package com.dangerousthings.gwenji.ui.theme

import androidx.compose.ui.graphics.Color

// Primary: teal -- friendly, gender-neutral, high contrast
val Teal500 = Color(0xFF009688)
val Teal700 = Color(0xFF00796B)
val Teal200 = Color(0xFF80CBC4)

// Accent: coral -- warm, inviting, great for the "Say it!" button
val Coral500 = Color(0xFFFF6B6B)
val Coral700 = Color(0xFFE55656)

// Highlight: gold for word highlighting during TTS
val HighlightGold = Color(0xFFFFF176)

// Background
val BackgroundLight = Color(0xFFFAFAFA)
val SurfaceLight = Color(0xFFFFFFFF)

// Text
val TextPrimary = Color(0xFF212121)
val TextSecondary = Color(0xFF757575)
```

**Step 2: Create typography**

`app/src/main/java/com/dangerousthings/gwenji/ui/theme/Type.kt`:
```kotlin
package com.dangerousthings.gwenji.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val GwenjiTypography = Typography(
    // For the live text preview / spoken text
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    // Category tab labels
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    // "Say it!" button text
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    )
)
```

**Step 3: Create theme**

`app/src/main/java/com/dangerousthings/gwenji/ui/theme/Theme.kt`:
```kotlin
package com.dangerousthings.gwenji.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Teal500,
    primaryContainer = Teal200,
    onPrimary = SurfaceLight,
    secondary = Coral500,
    onSecondary = SurfaceLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

@Composable
fun GwenjiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = GwenjiTypography,
        content = content
    )
}
```

**Step 4: Build to verify compilation**

Run: `./gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add -A
git commit -m "feat: add Gwenji theme with teal/coral color scheme"
```

---

## Task 6: TTS Manager

**Files:**
- Create: `app/src/main/java/com/dangerousthings/gwenji/speech/TtsManager.kt`

**Step 1: Implement TTS manager**

`app/src/main/java/com/dangerousthings/gwenji/speech/TtsManager.kt`:
```kotlin
package com.dangerousthings.gwenji.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

data class SpeechProgress(
    val isSpeaking: Boolean = false,
    val currentCharStart: Int = -1,
    val currentCharEnd: Int = -1
)

class TtsManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _progress = MutableStateFlow(SpeechProgress())
    val progress: StateFlow<SpeechProgress> = _progress

    private var onDone: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.language = Locale.US
                tts?.setSpeechRate(0.85f) // slightly slower for learning
                setupListener()
            }
        }
    }

    private fun setupListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _progress.value = SpeechProgress(isSpeaking = true)
            }

            override fun onDone(utteranceId: String?) {
                _progress.value = SpeechProgress(isSpeaking = false)
                onDone?.invoke()
            }

            @Deprecated("Deprecated in API")
            override fun onError(utteranceId: String?) {
                _progress.value = SpeechProgress(isSpeaking = false)
            }

            override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                _progress.value = SpeechProgress(
                    isSpeaking = true,
                    currentCharStart = start,
                    currentCharEnd = end
                )
            }
        })
    }

    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isInitialized || text.isBlank()) return
        onDone = onComplete
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "gwenji_utterance")
    }

    fun stop() {
        tts?.stop()
        _progress.value = SpeechProgress()
    }

    fun getAvailableVoices(): List<Voice> {
        return tts?.voices?.filter { it.locale.language == "en" }?.toList() ?: emptyList()
    }

    fun setVoice(voice: Voice) {
        tts?.voice = voice
    }

    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
```

**Step 2: Build to verify compilation**

Run: `./gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add -A
git commit -m "feat: add TTS manager with word-level progress tracking"
```

---

## Task 7: Room Database for History

**Files:**
- Create: `app/src/main/java/com/dangerousthings/gwenji/data/history/HistoryEntry.kt`
- Create: `app/src/main/java/com/dangerousthings/gwenji/data/history/HistoryDao.kt`
- Create: `app/src/main/java/com/dangerousthings/gwenji/data/history/GwenjiDatabase.kt`

**Step 1: Create Room entity**

`app/src/main/java/com/dangerousthings/gwenji/data/history/HistoryEntry.kt`:
```kotlin
package com.dangerousthings.gwenji.data.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "emoji_sequence") val emojiSequence: String,
    @ColumnInfo(name = "spoken_text") val spokenText: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)
```

**Step 2: Create DAO**

`app/src/main/java/com/dangerousthings/gwenji/data/history/HistoryDao.kt`:
```kotlin
package com.dangerousthings.gwenji.data.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(entry: HistoryEntry)

    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<HistoryEntry>>

    @Query("DELETE FROM history")
    suspend fun deleteAll()
}
```

**Step 3: Create database**

`app/src/main/java/com/dangerousthings/gwenji/data/history/GwenjiDatabase.kt`:
```kotlin
package com.dangerousthings.gwenji.data.history

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryEntry::class], version = 1)
abstract class GwenjiDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: GwenjiDatabase? = null

        fun getDatabase(context: Context): GwenjiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GwenjiDatabase::class.java,
                    "gwenji_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

**Step 4: Build to verify compilation**

Run: `./gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 5: Commit**

```bash
git add -A
git commit -m "feat: add Room database for sentence history"
```

---

## Task 8: DataStore Preferences

**Files:**
- Create: `app/src/main/java/com/dangerousthings/gwenji/data/preferences/UserPreferences.kt`

**Step 1: Implement DataStore wrapper**

`app/src/main/java/com/dangerousthings/gwenji/data/preferences/UserPreferences.kt`:
```kotlin
package com.dangerousthings.gwenji.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gwenji_prefs")

class UserPreferences(private val context: Context) {

    private object Keys {
        val SELECTED_VOICE = stringPreferencesKey("selected_voice")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val APP_MODE = stringPreferencesKey("app_mode")
    }

    val selectedVoice: Flow<String?> = context.dataStore.data
        .map { it[Keys.SELECTED_VOICE] }

    val speechRate: Flow<Float> = context.dataStore.data
        .map { it[Keys.SPEECH_RATE] ?: 0.85f }

    val appMode: Flow<String> = context.dataStore.data
        .map { it[Keys.APP_MODE] ?: "free" }

    suspend fun setSelectedVoice(voiceName: String) {
        context.dataStore.edit { it[Keys.SELECTED_VOICE] = voiceName }
    }

    suspend fun setSpeechRate(rate: Float) {
        context.dataStore.edit { it[Keys.SPEECH_RATE] = rate }
    }

    suspend fun setAppMode(mode: String) {
        context.dataStore.edit { it[Keys.APP_MODE] = mode }
    }
}
```

**Step 2: Build to verify compilation**

Run: `./gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add -A
git commit -m "feat: add DataStore preferences for voice, speech rate, and mode"
```

---

## Task 9: Main Screen ViewModel

**Files:**
- Create: `app/src/main/java/com/dangerousthings/gwenji/ui/main/MainViewModel.kt`

**Step 1: Implement ViewModel**

`app/src/main/java/com/dangerousthings/gwenji/ui/main/MainViewModel.kt`:
```kotlin
package com.dangerousthings.gwenji.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dangerousthings.gwenji.data.emoji.EmojiRepository
import com.dangerousthings.gwenji.data.history.GwenjiDatabase
import com.dangerousthings.gwenji.data.history.HistoryEntry
import com.dangerousthings.gwenji.engine.AssemblyResult
import com.dangerousthings.gwenji.engine.SentenceAssembler
import com.dangerousthings.gwenji.model.Category
import com.dangerousthings.gwenji.model.EmojiEntry
import com.dangerousthings.gwenji.speech.SpeechProgress
import com.dangerousthings.gwenji.speech.TtsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val categories: List<Category> = emptyList(),
    val currentCategoryEmojis: List<EmojiEntry> = emptyList(),
    val selectedCategory: String = "",
    val sentenceEmojis: List<EmojiEntry> = emptyList(),
    val assemblyResult: AssemblyResult = AssemblyResult("", emptyList()),
    val speechProgress: SpeechProgress = SpeechProgress()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val emojiRepository = EmojiRepository(application)
    private val assembler = SentenceAssembler()
    private val ttsManager = TtsManager(application)
    private val database = GwenjiDatabase.getDatabase(application)
    private val historyDao = database.historyDao()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    val speechProgress: StateFlow<SpeechProgress> = ttsManager.progress

    init {
        emojiRepository.load()
        val categories = emojiRepository.getCategories()
        val firstCategory = categories.firstOrNull()
        _uiState.value = MainUiState(
            categories = categories,
            selectedCategory = firstCategory?.name ?: "",
            currentCategoryEmojis = firstCategory?.let {
                emojiRepository.getEmojisByCategory(it.name)
            } ?: emptyList()
        )
    }

    fun selectCategory(categoryName: String) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = categoryName,
            currentCategoryEmojis = emojiRepository.getEmojisByCategory(categoryName)
        )
    }

    fun addEmoji(emoji: EmojiEntry) {
        val updated = _uiState.value.sentenceEmojis + emoji
        val result = assembler.assemble(updated)
        _uiState.value = _uiState.value.copy(
            sentenceEmojis = updated,
            assemblyResult = result
        )
    }

    fun removeEmoji(index: Int) {
        val updated = _uiState.value.sentenceEmojis.toMutableList().apply {
            removeAt(index)
        }
        val result = assembler.assemble(updated)
        _uiState.value = _uiState.value.copy(
            sentenceEmojis = updated,
            assemblyResult = result
        )
    }

    fun clearSentence() {
        _uiState.value = _uiState.value.copy(
            sentenceEmojis = emptyList(),
            assemblyResult = AssemblyResult("", emptyList())
        )
    }

    fun speak() {
        val state = _uiState.value
        if (state.assemblyResult.text.isBlank()) return

        viewModelScope.launch {
            historyDao.insert(
                HistoryEntry(
                    emojiSequence = state.sentenceEmojis.joinToString("") { it.emoji },
                    spokenText = state.assemblyResult.text
                )
            )
        }

        ttsManager.speak(state.assemblyResult.text)
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
```

**Step 2: Build to verify compilation**

Run: `./gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add -A
git commit -m "feat: add MainViewModel with emoji selection, assembly, TTS, and history"
```

---

## Task 10: Main Screen UI -- Sentence Strip

**Files:**
- Create: `app/src/main/java/com/dangerousthings/gwenji/ui/main/SentenceStrip.kt`

**Step 1: Implement sentence strip composable**

`app/src/main/java/com/dangerousthings/gwenji/ui/main/SentenceStrip.kt`:
```kotlin
package com.dangerousthings.gwenji.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangerousthings.gwenji.engine.AssemblyResult
import com.dangerousthings.gwenji.model.EmojiEntry
import com.dangerousthings.gwenji.speech.SpeechProgress
import com.dangerousthings.gwenji.ui.theme.HighlightGold

@Composable
fun SentenceStrip(
    emojis: List<EmojiEntry>,
    assemblyResult: AssemblyResult,
    speechProgress: SpeechProgress,
    onRemoveEmoji: (Int) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Emoji row with clear button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 8.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(emojis) { index, emoji ->
                    val highlightedEmojiIndex = if (speechProgress.isSpeaking) {
                        assemblyResult.segments.firstOrNull { segment ->
                            speechProgress.currentCharStart >= segment.startOffset &&
                                speechProgress.currentCharStart < segment.endOffset
                        }?.emojiIndex ?: -1
                    } else -1

                    val bgColor by animateColorAsState(
                        targetValue = if (index == highlightedEmojiIndex) HighlightGold
                        else Color.Transparent,
                        label = "emojiBg"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .clickable { onRemoveEmoji(index) }
                            .padding(8.dp)
                    ) {
                        Text(
                            text = emoji.emoji,
                            fontSize = 36.sp
                        )
                    }
                }
            }

            if (emojis.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Live text preview with word highlighting
        if (assemblyResult.text.isNotBlank()) {
            val annotatedText = buildAnnotatedString {
                if (speechProgress.isSpeaking) {
                    val text = assemblyResult.text
                    var lastEnd = 0
                    for (segment in assemblyResult.segments) {
                        // Text before this segment (spaces)
                        if (segment.startOffset > lastEnd) {
                            append(text.substring(lastEnd, segment.startOffset))
                        }
                        val isHighlighted =
                            speechProgress.currentCharStart >= segment.startOffset &&
                                speechProgress.currentCharStart < segment.endOffset
                        if (isHighlighted) {
                            withStyle(SpanStyle(
                                background = HighlightGold,
                                fontWeight = FontWeight.Bold
                            )) {
                                append(text.substring(segment.startOffset, segment.endOffset))
                            }
                        } else {
                            append(text.substring(segment.startOffset, segment.endOffset))
                        }
                        lastEnd = segment.endOffset
                    }
                    if (lastEnd < text.length) {
                        append(text.substring(lastEnd))
                    }
                } else {
                    append(assemblyResult.text)
                }
            }

            Text(
                text = annotatedText,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
```

**Step 2: Build to verify compilation**

Run: `./gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add -A
git commit -m "feat: add SentenceStrip composable with dual highlighting"
```

---

## Task 11: Main Screen UI -- Emoji Picker

**Files:**
- Create: `app/src/main/java/com/dangerousthings/gwenji/ui/main/EmojiPicker.kt`

**Step 1: Implement emoji picker composable**

`app/src/main/java/com/dangerousthings/gwenji/ui/main/EmojiPicker.kt`:
```kotlin
package com.dangerousthings.gwenji.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangerousthings.gwenji.model.Category
import com.dangerousthings.gwenji.model.EmojiEntry
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EmojiPicker(
    categories: List<Category>,
    selectedCategory: String,
    emojis: List<EmojiEntry>,
    onCategorySelected: (String) -> Unit,
    onEmojiSelected: (EmojiEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = categories.indexOfFirst {
        it.name.equals(selectedCategory, ignoreCase = true)
    }.coerceAtLeast(0)

    Box(modifier = modifier) {
        // Category tabs
        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            edgePadding = 8.dp
        ) {
            categories.forEach { category ->
                Tab(
                    selected = category.name.equals(selectedCategory, ignoreCase = true),
                    onClick = { onCategorySelected(category.name) },
                    text = { Text(category.name) }
                )
            }
        }

        // Emoji grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 56.dp),
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                top = 56.dp, // offset for tab row
                bottom = 8.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(emojis, key = { it.emoji }) { emoji ->
                val tooltipState = rememberTooltipState()
                val scope = rememberCoroutineScope()

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(emoji.chain["default"] ?: emoji.solo)
                        }
                    },
                    state = tooltipState
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .combinedClickable(
                                onClick = { onEmojiSelected(emoji) },
                                onLongClick = { scope.launch { tooltipState.show() } }
                            )
                    ) {
                        Text(
                            text = emoji.emoji,
                            fontSize = 32.sp
                        )
                    }
                }
            }
        }
    }
}
```

**Step 2: Build to verify compilation**

Run: `./gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 3: Commit**

```bash
git add -A
git commit -m "feat: add EmojiPicker composable with category tabs and long-press tooltips"
```

---

## Task 12: Main Screen UI -- Assembled

**Files:**
- Create: `app/src/main/java/com/dangerousthings/gwenji/ui/main/MainScreen.kt`
- Modify: `app/src/main/java/com/dangerousthings/gwenji/MainActivity.kt`

**Step 1: Implement main screen composable**

`app/src/main/java/com/dangerousthings/gwenji/ui/main/MainScreen.kt`:
```kotlin
package com.dangerousthings.gwenji.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onMenuClick: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val speechProgress by viewModel.speechProgress.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gwenji") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sentence strip (top)
            SentenceStrip(
                emojis = uiState.sentenceEmojis,
                assemblyResult = uiState.assemblyResult,
                speechProgress = speechProgress,
                onRemoveEmoji = viewModel::removeEmoji,
                onClear = viewModel::clearSentence
            )

            // Emoji picker (middle, fills available space)
            EmojiPicker(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                emojis = uiState.currentCategoryEmojis,
                onCategorySelected = viewModel::selectCategory,
                onEmojiSelected = viewModel::addEmoji,
                modifier = Modifier.weight(1f)
            )

            // Say It! button (bottom)
            Button(
                onClick = viewModel::speak,
                enabled = uiState.sentenceEmojis.isNotEmpty() && !speechProgress.isSpeaking,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (speechProgress.isSpeaking) "Speaking..." else "Say it!",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
```

**Step 2: Wire MainScreen into MainActivity**

Update `MainActivity.kt` to use `MainScreen`:
```kotlin
package com.dangerousthings.gwenji

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dangerousthings.gwenji.ui.main.MainScreen
import com.dangerousthings.gwenji.ui.theme.GwenjiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GwenjiTheme {
                MainScreen(
                    onMenuClick = { /* TODO: open drawer */ }
                )
            }
        }
    }
}
```

**Step 3: Build to verify compilation**

Run: `./gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add -A
git commit -m "feat: assemble MainScreen with sentence strip, emoji picker, and Say It button"
```

---

## Task 13: Hamburger Menu with History and Settings

**Files:**
- Create: `app/src/main/java/com/dangerousthings/gwenji/ui/menu/MenuDrawer.kt`
- Create: `app/src/main/java/com/dangerousthings/gwenji/ui/menu/HistoryScreen.kt`
- Create: `app/src/main/java/com/dangerousthings/gwenji/ui/menu/SettingsScreen.kt`
- Create: `app/src/main/java/com/dangerousthings/gwenji/ui/menu/MenuViewModel.kt`
- Modify: `app/src/main/java/com/dangerousthings/gwenji/MainActivity.kt`

**Step 1: Create MenuViewModel**

`app/src/main/java/com/dangerousthings/gwenji/ui/menu/MenuViewModel.kt`:
```kotlin
package com.dangerousthings.gwenji.ui.menu

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dangerousthings.gwenji.data.history.GwenjiDatabase
import com.dangerousthings.gwenji.data.history.HistoryEntry
import kotlinx.coroutines.flow.Flow

class MenuViewModel(application: Application) : AndroidViewModel(application) {
    private val database = GwenjiDatabase.getDatabase(application)
    val history: Flow<List<HistoryEntry>> = database.historyDao().getAll()
}
```

**Step 2: Create HistoryScreen**

`app/src/main/java/com/dangerousthings/gwenji/ui/menu/HistoryScreen.kt`:
```kotlin
package com.dangerousthings.gwenji.ui.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dangerousthings.gwenji.data.history.HistoryEntry
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    historyFlow: Flow<List<HistoryEntry>>,
    onEntryClick: (HistoryEntry) -> Unit
) {
    val history by historyFlow.collectAsState(initial = emptyList())

    if (history.isEmpty()) {
        Text(
            text = "No history yet",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(history) { entry ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEntryClick(entry) }
                        .padding(16.dp)
                ) {
                    Text(
                        text = entry.emojiSequence,
                        fontSize = 24.sp
                    )
                    Text(
                        text = entry.spokenText,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = formatTimestamp(entry.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider()
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
```

**Step 3: Create SettingsScreen**

`app/src/main/java/com/dangerousthings/gwenji/ui/menu/SettingsScreen.kt`:
```kotlin
package com.dangerousthings.gwenji.ui.menu

import android.speech.tts.Voice
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    availableVoices: List<Voice>,
    selectedVoiceName: String?,
    speechRate: Float,
    onVoiceSelected: (Voice) -> Unit,
    onSpeechRateChanged: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Speech rate
        Text(
            text = "Speech Rate",
            style = MaterialTheme.typography.titleMedium
        )
        Slider(
            value = speechRate,
            onValueChange = onSpeechRateChanged,
            valueRange = 0.5f..1.5f,
            steps = 9,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Text(
            text = "%.1fx".format(speechRate),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Voice selection
        Text(
            text = "Voice",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (availableVoices.isEmpty()) {
            Text(
                text = "No voices available",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            availableVoices.forEach { voice ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = voice.name == selectedVoiceName,
                        onClick = { onVoiceSelected(voice) }
                    )
                    Text(
                        text = voice.name.replace("en-us-x-", "")
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        // Mode toggle (greyed out)
        Text(
            text = "Mode",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        Text(
            text = "Guided Mode (coming soon)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

**Step 4: Create MenuDrawer**

`app/src/main/java/com/dangerousthings/gwenji/ui/menu/MenuDrawer.kt`:
```kotlin
package com.dangerousthings.gwenji.ui.menu

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class MenuDestination { HISTORY, SETTINGS }

@Composable
fun MenuDrawer(
    content: @Composable (MenuDestination) -> Unit
) {
    var selected by remember { mutableStateOf(MenuDestination.HISTORY) }

    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Gwenji",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "History") },
                label = { Text("History") },
                selected = selected == MenuDestination.HISTORY,
                onClick = { selected = MenuDestination.HISTORY },
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                label = { Text("Settings") },
                selected = selected == MenuDestination.SETTINGS,
                onClick = { selected = MenuDestination.SETTINGS },
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Content area
            content(selected)
        }
    }
}
```

**Step 5: Update MainActivity to wire up drawer navigation**

Update `MainActivity.kt` to use `ModalNavigationDrawer` with the menu screens.

**Step 6: Build to verify compilation**

Run: `./gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 7: Commit**

```bash
git add -A
git commit -m "feat: add hamburger menu with history and settings screens"
```

---

## Task 14: Author Full Emoji Dictionary (~150 emojis)

**Files:**
- Modify: `app/src/main/assets/emoji_dictionary.json`

**Step 1: Author the full curated emoji dictionary**

Use LLM assistance (Claude) to author a complete emoji dictionary with ~150 entries across all categories. For each emoji, define:
- `solo` phrase (complete thought for standalone use)
- `chain` map with `default` and relevant `after_X` context overrides
- `tags` for searchability
- `grammar_role` for future guided mode

Follow these principles:
- Solo phrases should be the most common intent ("hamburger" solo = "I want a hamburger")
- Chain defaults should be the bare word/concept ("a hamburger")
- Context overrides should produce natural English ("after_i" = "want a hamburger")
- Tags should include synonyms and related concepts
- Cover the categories defined in the design: People, Feelings, Needs, Actions, Places, Food/Drink, Things, Modifiers

**Step 2: Validate JSON parses correctly**

Run: `./gradlew.bat test --tests "com.dangerousthings.gwenji.data.emoji.EmojiRepositoryTest"`
Expected: PASS

**Step 3: Commit**

```bash
git add app/src/main/assets/emoji_dictionary.json
git commit -m "feat: author full curated emoji dictionary with ~150 entries"
```

---

## Task 15: Integration Testing and Polish

**Files:**
- Various UI files may need small adjustments

**Step 1: Build full APK**

Run: `./gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 2: Install and test on device/emulator**

Run: `"C:\Users\amal\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r app/build/outputs/apk/debug/app-debug.apk`
Expected: Success

**Step 3: Manual testing checklist**

Test each of these scenarios:
1. App launches and shows emoji picker with category tabs
2. Tap emoji -> appears in sentence strip, text preview updates
3. Tap multiple emojis -> chain assembles correctly
4. Tap emoji in strip -> it is removed, text updates
5. Tap clear (X) -> strip and text clear
6. Tap "Say it!" -> TTS speaks, words highlight in sync with emojis
7. Open hamburger menu -> shows History and Settings
8. History shows previous sentences
9. Tap history entry -> reloads into sentence strip
10. Settings: change speech rate -> affects TTS speed
11. Settings: change voice -> affects TTS voice
12. Long-press emoji in picker -> shows word tooltip

**Step 4: Fix any issues found during testing**

Address any layout, behavior, or crash issues discovered.

**Step 5: Run all unit tests**

Run: `./gradlew.bat test`
Expected: ALL PASS

**Step 6: Commit any fixes**

```bash
git add -A
git commit -m "fix: polish and integration fixes from manual testing"
```

---

## Task Summary

| Task | Description | Dependencies |
|------|-------------|--------------|
| 1 | Project scaffolding | None |
| 2 | Data models | Task 1 |
| 3 | Sentence assembly engine | Task 2 |
| 4 | Emoji dictionary loader | Task 2 |
| 5 | App theme | Task 1 |
| 6 | TTS manager | Task 1 |
| 7 | Room database (history) | Task 1 |
| 8 | DataStore preferences | Task 1 |
| 9 | Main ViewModel | Tasks 3, 4, 6, 7 |
| 10 | Sentence strip UI | Tasks 5, 9 |
| 11 | Emoji picker UI | Tasks 5, 9 |
| 12 | Main screen assembly | Tasks 10, 11 |
| 13 | Menu/History/Settings UI | Tasks 7, 8 |
| 14 | Full emoji dictionary | Task 4 |
| 15 | Integration testing | All above |

Tasks 3-8 can be parallelized after Task 2 is complete.
Tasks 10-11 can be parallelized after Task 9.
Tasks 12-14 can be parallelized after their dependencies.
