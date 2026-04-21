# 📖 Quran Reader App

A modern, beautiful Android Quran reading application built with **100% Jetpack Compose** and **Material Design 3**.

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Material3](https://img.shields.io/badge/Design-Material%203-orange.svg)](https://m3.material.io/)
[![Min API](https://img.shields.io/badge/Min%20API-21-yellow.svg)](https://developer.android.com/about/versions/lollipop)

## ✨ Features

### 📱 Core Features
- **Mushaf Reading Mode**: High-quality Quran page images (604 pages) with smooth horizontal paging
- **Multiple Navigation Options**: 
  - Browse by **Juz** (30 divisions)
  - Browse by **Surah** (114 chapters with Arabic & English names)
  - Browse by **Hizb** (60 divisions, 240 quarters)
- **Bookmarks**: Save and manage your favorite pages with notes
- **Reading Sessions**: Create, track, and manage multiple reading sessions with progress tracking
- **Search**: Find surahs, juz, or pages quickly
- **Audio Recitation**: Listen to Quran recitation with repeat options (framework ready)
- **Fullscreen Mode**: Immersive reading with auto-hide UI controls

### 🎨 Design & Themes
- **5 Beautiful Themes**:
  - 🟢 **Zamrud Islami** (Islamic Green)
  - 🔵 **Teal & Dusk** (Ocean Blue)
  - 🟡 **Amber Masjid** (Golden Mosque)
  - 🟣 **Indigo Malam** (Night Purple)
  - 🎨 **Material You** (Dynamic colors - Android 12+)
- **Light & Dark Mode**: Each theme supports both light and dark variants
- **Modern UI**: Clean, intuitive interface following Material Design 3 guidelines
- **Smooth Animations**: 220ms scale + fade transitions throughout the app

### 🌍 Localization
- **Bilingual Support**: 
  - 🇬🇧 English
  - 🇮🇩 Bahasa Indonesia
- **Dynamic Language Switching**: Change language with optional app restart
- **Localized Content**: All UI elements, labels, and messages fully translated
- **100+ String Resources**: Complete translation coverage

### 📊 Reading Statistics
- Track total pages read
- Monitor reading time (in minutes)
- View session progress
- Reading history

### 🎯 Session Management
- Create multiple named sessions
- Set reading goals (target pages)
- Track progress per session
- Extend sessions dynamically
- Complete or delete sessions
- Configurable session limits

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose (Zero XML layouts)
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture
- **Dependency Injection**: Hilt
- **Database**: Room
- **Async**: Kotlin Coroutines & Flow
- **Storage**: DataStore Preferences
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Coil
- **Background Tasks**: WorkManager

### Project Structure
```
app/
├── data/
│   ├── audio/          # Audio service (Bound Service)
│   ├── download/       # Download manager & worker
│   ├── local/          # Room database (DAOs)
│   ├── model/          # Data models
│   ├── page/           # Quran page provider
│   ├── preferences/    # User preferences (DataStore)
│   ├── remote/         # API services & downloaders
│   └── repository/     # Data repositories
├── di/                 # Dependency injection (Hilt modules)
├── ui/
│   ├── components/     # Reusable UI components
│   ├── navigation/     # Navigation graph with animations
│   ├── screens/        # App screens
│   │   ├── home/       # Dashboard & home screen
│   │   ├── reading/    # Mushaf reader screen
│   │   ├── bookmarks/  # Bookmarks management
│   │   ├── juz/        # Juz/Surah/Hizb navigation
│   │   ├── search/     # Search functionality
│   │   ├── session/    # Session management
│   │   ├── settings/   # App settings
│   │   ├── download/   # Download manager UI
│   │   └── onboarding/ # First launch onboarding
│   ├── theme/          # Material 3 theme system
│   └── viewmodel/      # ViewModels
└── utils/              # Utility classes (LocaleManager, etc.)
```

### Architecture Diagram
```
┌─────────────────────────────────────────┐
│           UI Layer (Compose)            │
│  ┌─────────────────────────────────┐   │
│  │  Screens & Components           │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│         ViewModel Layer (Hilt)          │
│  ┌─────────────────────────────────┐   │
│  │  ViewModels + StateFlows        │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│        Repository Layer (Domain)        │
│  ┌─────────────────────────────────┐   │
│  │  QuranRepository                │   │
│  │  BookmarkRepository             │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│          Data Layer (Room)              │
│  ┌─────────────────────────────────┐   │
│  │  Local DB + Remote API          │   │
│  │  DataStore + Assets             │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

## 🚀 Getting Started

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: 17
- **Android SDK**: 34
- **Minimum Android**: API 21 (Lollipop 5.0)
- **Target Android**: API 34 (Android 14)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/quran-reader-app.git
   cd quran-reader-app
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run on device/emulator**
   - Connect your Android device (with USB debugging enabled) or start an emulator
   - Click the "Run" button in Android Studio
   - Or use command line:
   ```bash
   ./gradlew installDebug
   ```

### Build Variants
```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing config)
./gradlew assembleRelease

# Install debug on connected device
./gradlew installDebug
```

## 📦 Dependencies

### Core Dependencies
```gradle
// Compose BOM
androidx.compose:compose-bom:2023.10.01

// Compose UI
androidx.compose.ui:ui
androidx.compose.material3:material3
androidx.compose.material:material-icons-extended

// Activity & Navigation
androidx.activity:activity-compose:1.8.1
androidx.navigation:navigation-compose:2.7.5

// Lifecycle
androidx.lifecycle:lifecycle-runtime-ktx:2.6.2
androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2
```

### Architecture Components
```gradle
// Hilt (Dependency Injection)
com.google.dagger:hilt-android:2.48
androidx.hilt:hilt-navigation-compose:1.1.0

// Room (Database)
androidx.room:room-runtime:2.6.1
androidx.room:room-ktx:2.6.1

// DataStore (Preferences)
androidx.datastore:datastore-preferences:1.0.0
```

### Networking & Image Loading
```gradle
// Retrofit
com.squareup.retrofit2:retrofit:2.9.0
com.squareup.retrofit2:converter-gson:2.9.0

// OkHttp
com.squareup.okhttp3:okhttp:4.12.0
com.squareup.okhttp3:logging-interceptor:4.12.0

// Coil (Image Loading)
io.coil-kt:coil-compose:2.5.0
```

### Background Tasks
```gradle
// WorkManager
androidx.work:work-runtime-ktx:2.9.0
androidx.hilt:hilt-work:1.1.0
```

## 🎯 Key Features Explained

### 1. Navigation System
The app provides **three ways** to navigate the Quran:

#### 📚 Juz (30 divisions)
- Traditional 30-part division of the Quran
- Each Juz contains approximately 20 pages
- Perfect for monthly reading plans (1 Juz per day)
- Page ranges clearly displayed

#### 📖 Surah (114 chapters)
- Complete list of all 114 surahs
- Shows both Arabic (السورة) and English names
- Displays ayah count for each surah
- Makki/Madani classification badges
- Color-coded for easy identification

#### 🌙 Hizb (60 divisions, 240 quarters)
- Traditional memorization units
- 2 Hizb per Juz (60 total)
- 4 quarters per Hizb (240 total)
- Each quarter ≈ 2.5 pages
- Shows parent Juz and starting page

### 2. Theme System
- **4 Custom Color Palettes**: Each carefully designed for optimal reading
- **Material You Dynamic Theming**: Colors adapt to your wallpaper (Android 12+)
- **Light & Dark Variants**: All themes support both modes
- **Smooth Theme Switching**: Instant theme changes without restart
- **Persistent Selection**: Theme preference saved across app launches

### 3. Reading Sessions
- **Multiple Sessions**: Create and manage multiple reading sessions simultaneously
- **Named Sessions**: Give each session a meaningful name
- **Goal Setting**: Set target pages for each session
- **Progress Tracking**: Visual progress indicators
- **Extend Sessions**: Add more pages to your goal dynamically
- **Session Limits**: Configurable limits for new and continue reading
- **Complete/Delete**: Mark sessions as complete or remove them

### 4. Localization System
- **Automatic Locale Detection**: Applies saved language on app start
- **Restart Dialog**: User-friendly prompt to restart after language change
- **Complete Coverage**: All UI elements translated
- **Context-Aware**: Strings adapt to current language
- **Easy to Extend**: Add new languages by creating `values-XX/strings.xml`

## 📱 Screenshots

[Screenshots will be added here]

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### How to Contribute

1. **Fork the project**
2. **Create your feature branch**
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. **Commit your changes**
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/AmazingFeature
   ```
5. **Open a Pull Request**

### Contribution Guidelines
- Follow Kotlin coding conventions
- Use Jetpack Compose best practices
- Write meaningful commit messages
- Add comments for complex logic
- Update documentation if needed
- Test your changes thoroughly

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Quran Text & Data**: [Tanzil.net](https://tanzil.net/)
- **Quran Pages**: Mushaf Madinah (604 pages)
- **Audio Recitations**: Various reciters
- **Icons**: Material Design Icons
- **Fonts**: 
  - Uthmanic Hafs Ver 12
  - Kitab Font
  - OpenDyslexic
  - Quran Titles Font
- **Inspiration**: [quran/quran_android](https://github.com/quran/quran_android)

## 📞 Contact & Support

- **Issues**: [GitHub Issues](https://github.com/yourusername/quran-reader-app/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/quran-reader-app/discussions)
- **Email**: your.email@example.com

## 🗺️ Roadmap

### Planned Features
- [ ] **Tafsir Integration**: Add Quran commentary from multiple sources
- [ ] **Multiple Translations**: Support for 20+ languages
- [ ] **Advanced Search**: Search by text, surah, juz with filters
- [ ] **Reading Reminders**: Customizable daily reading notifications
- [ ] **Verse Sharing**: Share verses as images or text
- [ ] **Night Reading Mode**: Custom brightness and color temperature
- [ ] **Font Customization**: Adjustable font sizes and styles
- [ ] **Offline Audio**: Download and manage audio recitations
- [ ] **Tajweed Rules**: Visual tajweed highlighting
- [ ] **Word-by-Word**: Translation and transliteration
- [ ] **Memorization Mode**: Tools for memorizing Quran
- [ ] **Reading Goals**: Weekly and monthly reading targets
- [ ] **Cloud Sync**: Sync bookmarks and progress across devices
- [ ] **Widgets**: Home screen widgets for quick access
- [ ] **Wear OS Support**: Quran reader for smartwatches

### Future Enhancements
- [ ] Tablet optimization with dual-pane layout
- [ ] Landscape mode optimization
- [ ] Accessibility improvements (TalkBack, large text)
- [ ] Performance optimizations
- [ ] Reduced app size
- [ ] More themes and customization options

## 📊 Project Stats

- **Lines of Code**: ~15,000+
- **Screens**: 10+
- **Components**: 50+
- **ViewModels**: 8
- **Repositories**: 2
- **Database Tables**: 4
- **String Resources**: 100+
- **Themes**: 5
- **Languages**: 2

## 🔧 Technical Details

### Minimum Requirements
- Android 5.0 (API 21) or higher
- 100 MB free storage
- Internet connection (for initial download)

### Recommended
- Android 12 (API 31) or higher (for Material You)
- 200 MB free storage
- Stable internet connection

### Permissions
- **INTERNET**: Download Quran pages and audio
- **FOREGROUND_SERVICE**: Background downloads
- **POST_NOTIFICATIONS**: Reading reminders (Android 13+)

## 🌟 Why This App?

- **100% Jetpack Compose**: Modern, declarative UI
- **Zero XML Layouts**: Pure Kotlin UI code
- **Material Design 3**: Latest design guidelines
- **Clean Architecture**: Maintainable and testable
- **Offline First**: Read without internet after initial download
- **Open Source**: Free and transparent
- **No Ads**: Clean, distraction-free reading experience
- **Privacy Focused**: No tracking, no analytics
- **Lightweight**: Optimized for performance
- **Beautiful**: Carefully crafted UI/UX

---

<div align="center">

**Made with ❤️ for the Muslim community**

*"Read in the name of your Lord who created"* - Quran 96:1

[⭐ Star this repo](https://github.com/yourusername/quran-reader-app) • [🐛 Report Bug](https://github.com/yourusername/quran-reader-app/issues) • [✨ Request Feature](https://github.com/yourusername/quran-reader-app/issues)

</div>
