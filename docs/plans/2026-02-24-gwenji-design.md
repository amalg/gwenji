# Gwenji - Emoji-Based AAC App Design

## Overview

Gwenji is an emoji-based Augmentative and Alternative Communication (AAC) Android app designed for a barely verbal 11-year-old. She is comfortable with phones and tablets, navigates apps fluidly, and searches for content using emojis. The app bridges emoji expression to spoken language, with the goal of helping her learn and eventually use real words independently.

The name "Gwenji" is a blend of her name and the word emoji.

## Target Devices

- Primary: Android phone
- Secondary: Samsung tablets (wall-mounted around the house)
- Min SDK: 26 (Android 8.0)

## Core Concept

The user taps emojis to build a sentence. The app assembles natural English from the emoji sequence using context-aware word mappings. She taps "Say it!" to hear the sentence spoken aloud via text-to-speech, with word-by-word highlighting synced to both the text display and the emoji strip.

This is a language learning tool, not just a communication tool. Every interaction exposes her to real words associated with her emoji choices.

## Approach: Rule-Based with LLM-Assisted Authoring

Sentence assembly is rule-based at runtime: each emoji has predefined word mappings, and simple positional context rules produce natural sentences. No LLM runs on-device or in the cloud at runtime.

An LLM (Claude) is used at design time to author the emoji dictionary -- choosing appropriate solo phrases, chain words, and context rules to produce natural-sounding output.

This approach was chosen because:
- Predictability is a feature: consistent output builds trust and aids word learning
- Instant response with no loading or latency
- Fully offline capable
- Simple to iterate on by editing a JSON dictionary

## Emoji Dictionary Structure

Each emoji entry:

```json
{
  "emoji": "...",
  "category": "feelings",
  "solo": "I feel sad",
  "chain": {
    "default": "sad",
    "after_i": "feel sad",
    "after_want": "to cry"
  },
  "tags": ["emotion", "negative", "crying"],
  "grammar_role": "descriptor"
}
```

### Context Rules

- **solo**: Used when this emoji is the only one selected. A complete thought.
- **chain.default**: Used in a multi-emoji sequence with no special context match.
- **chain.after_X**: Context-specific override triggered by the previous emoji's output word.

### Assembly Logic

1. Single emoji: use its `solo` phrase.
2. Multiple emojis: walk left to right. For each emoji, check if the previous emoji's output triggers a context key (e.g. `after_i`). Fall back to `chain.default`.
3. Join chain words with spaces.

### Example Chains

- `pointing-at-self` alone = "I need something" (solo)
- `pointing-at-self` + `sad face` = "I" + "feel sad" = "I feel sad"
- `pointing-at-self` + `hamburger` = "I" + "want a hamburger" = "I want a hamburger"
- `pointing-at-self` + `heart` + `mom` = "I" + "love" + "mom" = "I love mom"
- `sad face` alone = "I feel sad" (solo)

### Curated Emoji Set (~150 emojis)

Categories:
- **Feelings** (~25): happy, sad, angry, scared, tired, sick, hurt, love, etc.
- **Needs** (~20): hungry, thirsty, bathroom, sleep, help, medicine, etc.
- **People** (~15): me/I, you, mom, dad, brother, sister, friend, teacher, etc.
- **Actions** (~20): want, go, play, watch, eat, drink, stop, more, etc.
- **Places** (~15): home, school, store, park, car, outside, bed, etc.
- **Food/Drink** (~20): water, milk, juice, pizza, chicken, snack, etc.
- **Things** (~20): phone, TV, toy, book, blanket, etc.
- **Modifiers** (~15): yes, no, please, thank you, big, small, hot, cold, etc.

An "All" tab provides access to the full system emoji set for expressions not covered by the curated set.

### grammar_role Field

Included from day one to support future guided mode. Values: subject, verb, descriptor, object, modifier, greeting.

## UI Layout

Main screen has three vertical zones:

```
+----------------------------+
|   SENTENCE STRIP            |  Horizontal row of selected emojis
|   ...emoji chain...         |  Tap an emoji to remove it
|                             |
|   "Live text preview"       |  Updates as emojis are added/removed
+----------------------------+
|                             |
|   EMOJI PICKER              |  Vertical scrolling grid
|   Category tabs at top      |  Feelings | Needs | People | ...
|   ...emoji grid...          |
|                             |
+----------------------------+
|                             |
|       [ SAY IT! ]           |  Large, high-contrast button
|                             |
+----------------------------+
```

### Interaction Flow

1. App opens to main screen. Emoji picker shows curated set, Feelings category by default.
2. Category tabs along top of picker: icon + short label for each category.
3. Tap emoji to add to sentence strip. Live text preview updates instantly.
4. Tap emoji in sentence strip to remove it. Preview updates.
5. Tap "Say it!" -- text area enters speaking mode, TTS speaks, words highlight in sync.
6. Both the text words and corresponding emojis in the strip highlight during speech.
7. Sentence stays on screen after speaking. Clear with X button or modify.
8. Every spoken sentence is auto-saved to history.

### UX Details

- Live text preview: reinforces emoji-to-word connection before speaking
- "Say it!" button: always visible, large, thumb-friendly at bottom of screen
- Long-press emoji in picker: tooltip shows the word it would contribute
- Visual style: bright, friendly, high contrast, but not childish (she's 11)

## Text-to-Speech and Word Highlighting

- Uses Android's built-in `TextToSpeech` API (works offline with downloaded language packs)
- Default speech rate slightly slower than system default for clarity
- `UtteranceProgressListener.onRangeStart()` reports character offsets during speech
- Dual highlighting: current word in text + corresponding emoji in strip
- After speech completes, highlights briefly hold, then fade

## Hamburger Menu

Accessible from top corner of main screen. Contains:

- **History**: scrollable log of spoken sentences (newest first), showing emoji sequence, spoken text, and timestamp. Tap an entry to reload it into the sentence strip.
- **Settings**:
  - Voice selection (lists available TTS voices on device)
  - Speech rate slider
  - Mode toggle: Free Mode / Guided Mode (greyed out until guided mode is built)

## Data Model

### Bundled Asset: emoji_dictionary.json

```json
{
  "categories": [
    {
      "name": "Feelings",
      "icon": "heart_icon",
      "display_order": 1
    }
  ],
  "emojis": [
    {
      "emoji": "...",
      "category": "feelings",
      "solo": "I feel sad",
      "chain": {
        "default": "sad",
        "after_i": "feel sad"
      },
      "tags": ["emotion", "negative"],
      "grammar_role": "descriptor"
    }
  ]
}
```

### Local Database (Room): History

| Column         | Type    | Description                    |
|----------------|---------|--------------------------------|
| id             | Int     | Auto-increment primary key     |
| emoji_sequence | String  | Raw emoji characters           |
| spoken_text    | String  | The assembled sentence         |
| timestamp      | Long    | Epoch milliseconds             |

### DataStore Preferences

| Key              | Type   | Description                           |
|------------------|--------|---------------------------------------|
| selected_voice   | String | TTS voice identifier                  |
| speech_rate      | Float  | TTS speed multiplier                  |
| app_mode         | String | "free" or "guided" (future)           |

## Technology Stack

- Language: Kotlin
- UI: Jetpack Compose + Material 3
- TTS: Android TextToSpeech API
- Local DB: Room
- Preferences: DataStore
- Architecture: Single-activity, MVVM
- Build: Gradle with Kotlin DSL, version catalog
- No external dependencies beyond AndroidX/Compose

## Project Structure

```
com.dangerousthings.gwenji
├── ui/
│   ├── main/          Main screen (sentence strip, picker, say it button)
│   ├── menu/          Hamburger menu, history, settings
│   └── theme/         Colors, typography, theming
├── data/
│   ├── emoji/         Emoji dictionary loader, models
│   ├── history/       Room database, DAO
│   └── preferences/   DataStore wrapper
├── speech/            TTS manager, word highlighting logic
├── engine/            Sentence assembly engine (chain rules, context resolution)
└── model/             Shared data classes (EmojiEntry, SentenceChain, etc.)
```

## Future Exploration: Guided Mode

An advanced mode where emoji availability filters based on sentence structure:

- At sentence start, only "starter" emojis are visible (I, you, we, etc.)
- After selecting a subject, only verbs appear (feel, want, am, need, etc.)
- After a verb, relevant objects/descriptors appear (sad, hungry, pizza, home, etc.)
- Uses the `grammar_role` field to determine valid next picks
- Progressive TTS: each tap speaks the full sentence-so-far, not just the new word
- Eliminates the need for solo phrases since sentences are always constructed grammatically

This mode serves as a stronger language learning tool by teaching sentence structure through constrained choices.

### Additional Future Features

- Sharing constructed sentences via messaging apps
- Frequency statistics (most-used emojis and phrases)
- Export history to CSV/text for therapists or teachers
