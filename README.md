# Empty Activity

A minimal, clean, and modern **Jetpack Compose** starter template for Android development. This repository is designed to jumpstart new projects with a robust foundation and the latest Android standards.


## Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Minimum SDK**: 31 (Android 12)
- **Target/Compile SDK**: 36 (Android 15 / Vanilla Ice Cream)
- **Dependency Management**: Gradle Version Catalog
- **Architecture Components**: ViewModel, Lifecycle, Navigation

## Project Structure

```text
app/
├── src/main/kotlin/io/github/ttnguyen2505/emptyactivity/
│   ├── MainActivity.kt      # Entry point with a sample Compose UI
└── build.gradle.kts         # App-level build configuration
```

## Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/ttnguyen2505/EmptyActivity.git
   ```
2. **Open in Android Studio**:
   Use **Android Studio Ladybug** or newer for full compatibility.
3. **Build and Run**:
   Sync Gradle and run the `:app` module on an emulator or physical device.

## Testing

The project includes basic setups for both unit and instrumented tests:
- **Unit Tests**: Located in `app/src/test`.
- **Instrumented Tests**: Located in `app/src/androidTest` (including Compose UI tests).

## License

This project is open-source. Feel free to use it as a base for your own applications.
