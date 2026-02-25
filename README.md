# Gwenji

An emoji-based AAC (Augmentative and Alternative Communication) Android app that translates emoji chains into spoken English.

Gwenji lets users build sentences by tapping emojis from a curated set, then speaks the assembled sentence aloud with word-by-word highlighting. It is designed as both a communication tool and a language learning aid -- every interaction exposes the user to real words associated with their emoji choices.

## Features

- **150 curated emojis** across 8 categories: People, Feelings, Needs, Actions, Places, Food, Things, Modifiers
- **Context-aware sentence assembly** -- emojis chain together into natural English using positional rules
- **Text-to-speech** with synchronized word-by-word highlighting on both the text and the emoji strip
- **Category-tabbed emoji picker** with vertical scrolling grid and long-press word tooltips
- **Sentence history** log accessible from the navigation drawer
- **Settings** for voice selection and speech rate
- **Fully offline** -- no internet required for core functionality

## How It Works

Each emoji has context-aware word mappings:

- **Solo**: A complete thought when used alone (e.g. sad face = "I feel sad")
- **Chain**: Context-dependent words when used in a sequence (e.g. "I" + sad face = "I feel sad", where the sad emoji contributes "feel sad" after "I")

The sentence assembly engine walks the emoji chain left to right, resolving each emoji's contribution based on what came before it.

## Tech Stack

- Kotlin
- Jetpack Compose + Material 3
- Android TextToSpeech API
- Room (sentence history)
- DataStore (preferences)
- kotlinx.serialization (emoji dictionary)
- Single-activity MVVM architecture

## Building

Open in Android Studio Narwhal (2025.1.4) or build from command line:

```
./gradlew assembleDebug
```

Requires Android SDK with min SDK 26 (Android 8.0), target SDK 35.

## Future Plans

- **Guided Mode**: Filtered emoji availability based on sentence structure -- each emoji pick narrows the next options to grammatically valid choices, teaching sentence construction through constrained selection
- **Sharing**: Send constructed sentences via messaging apps
- **Usage analytics**: Frequency stats and history export for therapists or teachers

## License

All rights reserved.
