# Contributing to Quran Reader App

First off, thank you for considering contributing to Quran Reader App! It's people like you that make this app better for the Muslim community worldwide.

## 📋 Table of Contents
- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
- [Development Setup](#development-setup)
- [Coding Guidelines](#coding-guidelines)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)

## 🤝 Code of Conduct

This project and everyone participating in it is governed by respect, kindness, and Islamic values. By participating, you are expected to uphold this code.

### Our Standards
- Be respectful and considerate
- Welcome newcomers warmly
- Focus on what is best for the community
- Show empathy towards others
- Accept constructive criticism gracefully

## 🎯 How Can I Contribute?

### Reporting Bugs
Before creating bug reports, please check existing issues. When creating a bug report, include:

- **Clear title and description**
- **Steps to reproduce**
- **Expected behavior**
- **Actual behavior**
- **Screenshots** (if applicable)
- **Device information** (Android version, device model)
- **App version**

### Suggesting Enhancements
Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, include:

- **Clear title and description**
- **Use case** - why is this enhancement useful?
- **Mockups or examples** (if applicable)
- **Alternative solutions** you've considered

### Your First Code Contribution
Unsure where to begin? Look for issues labeled:
- `good first issue` - simple issues for beginners
- `help wanted` - issues that need attention
- `documentation` - improvements to documentation

### Pull Requests
- Fill in the required template
- Follow the coding guidelines
- Include screenshots for UI changes
- Update documentation if needed
- Add tests if applicable

## 🛠️ Development Setup

### Prerequisites
```bash
# Required
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34
- Git

# Recommended
- Android device or emulator
- Stable internet connection
```

### Setup Steps

1. **Fork the repository**
   ```bash
   # Click "Fork" on GitHub
   ```

2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/quran-reader-app.git
   cd quran-reader-app
   ```

3. **Add upstream remote**
   ```bash
   git remote add upstream https://github.com/ORIGINAL_OWNER/quran-reader-app.git
   ```

4. **Open in Android Studio**
   - Launch Android Studio
   - Open the project
   - Wait for Gradle sync

5. **Create a branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

6. **Make your changes**
   - Write code
   - Test thoroughly
   - Commit changes

7. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

8. **Create Pull Request**
   - Go to GitHub
   - Click "New Pull Request"
   - Fill in the template

## 📝 Coding Guidelines

### Kotlin Style
Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)

```kotlin
// Good
fun calculateProgress(current: Int, total: Int): Float {
    return current.toFloat() / total.toFloat()
}

// Bad
fun calc(c: Int, t: Int): Float {
    return c.toFloat()/t.toFloat()
}
```

### Compose Best Practices

#### State Management
```kotlin
// Good - Hoist state
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    MyContent(uiState = uiState)
}

// Bad - State in UI
@Composable
fun MyScreen() {
    var state by remember { mutableStateOf(State()) }
    // Business logic here
}
```

#### Naming Conventions
```kotlin
// Composables - PascalCase
@Composable
fun QuranPageView() { }

// Functions - camelCase
fun loadQuranPage() { }

// Constants - UPPER_SNAKE_CASE
const val MAX_PAGES = 604

// Variables - camelCase
val currentPage = 1
```

#### File Organization
```kotlin
// 1. Package declaration
package com.quranreader.custom.ui.screens.reading

// 2. Imports (grouped and sorted)
import androidx.compose.foundation.*
import androidx.compose.material3.*
import androidx.compose.runtime.*

// 3. Constants
private const val ANIMATION_DURATION = 220

// 4. Data classes
data class ReadingUiState(...)

// 5. Main composable
@Composable
fun ReadingScreen(...) { }

// 6. Helper composables
@Composable
private fun PageContent(...) { }

// 7. Preview composables
@Preview
@Composable
private fun ReadingScreenPreview() { }
```

### Documentation

#### KDoc Comments
```kotlin
/**
 * Loads a Quran page from assets.
 *
 * @param pageNumber The page number (1-604)
 * @return Bitmap of the page or null if not found
 * @throws IllegalArgumentException if pageNumber is out of range
 */
fun loadQuranPage(pageNumber: Int): Bitmap? {
    require(pageNumber in 1..604) { "Invalid page number" }
    // Implementation
}
```

#### Inline Comments
```kotlin
// Good - Explain WHY, not WHAT
// Use binary search for better performance with large datasets
val index = list.binarySearch(item)

// Bad - Obvious comment
// Set the page number to 1
val pageNumber = 1
```

### Testing

#### Unit Tests
```kotlin
@Test
fun `calculateProgress returns correct percentage`() {
    // Given
    val current = 50
    val total = 100
    
    // When
    val result = calculateProgress(current, total)
    
    // Then
    assertEquals(0.5f, result, 0.001f)
}
```

#### Compose Tests
```kotlin
@Test
fun `clicking bookmark button adds bookmark`() {
    composeTestRule.setContent {
        ReadingScreen(...)
    }
    
    composeTestRule
        .onNodeWithContentDescription("Add Bookmark")
        .performClick()
    
    // Verify bookmark was added
}
```

## 📦 Commit Guidelines

### Commit Message Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### Examples
```bash
# Good commits
feat(reading): add fullscreen mode
fix(bookmarks): resolve duplicate bookmark issue
docs(readme): update installation instructions
style(theme): format theme files
refactor(viewmodel): simplify state management
test(repository): add unit tests for QuranRepository
chore(deps): update Compose to 1.5.4

# Bad commits
update stuff
fix bug
changes
wip
```

### Commit Message Rules
- Use present tense ("add feature" not "added feature")
- Use imperative mood ("move cursor to..." not "moves cursor to...")
- First line should be 50 characters or less
- Reference issues and pull requests when relevant
- Explain WHAT and WHY, not HOW

## 🔄 Pull Request Process

### Before Submitting
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] No new warnings
- [ ] Tests added/updated
- [ ] All tests pass
- [ ] UI tested on device/emulator
- [ ] Both languages tested (EN/ID)
- [ ] All themes tested

### PR Template
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
How has this been tested?

## Screenshots
If applicable, add screenshots

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-reviewed
- [ ] Commented complex code
- [ ] Updated documentation
- [ ] No new warnings
- [ ] Added tests
- [ ] Tests pass
- [ ] UI tested
- [ ] Languages tested
- [ ] Themes tested
```

### Review Process
1. **Automated Checks**: CI must pass
2. **Code Review**: At least one approval required
3. **Testing**: Reviewer tests changes
4. **Merge**: Maintainer merges PR

### After Merge
- Delete your branch
- Update your fork
- Celebrate! 🎉

## 🌍 Localization

### Adding a New Language

1. **Create strings file**
   ```bash
   # For Arabic (ar)
   app/src/main/res/values-ar/strings.xml
   ```

2. **Translate all strings**
   ```xml
   <resources>
       <string name="app_name">قارئ القرآن</string>
       <!-- All other strings -->
   </resources>
   ```

3. **Update Language enum**
   ```kotlin
   enum class AppLanguage(val code: String, val displayName: String) {
       ENGLISH("en", "English"),
       INDONESIAN("id", "Bahasa Indonesia"),
       ARABIC("ar", "العربية") // Add new language
   }
   ```

4. **Test thoroughly**
   - All screens
   - All features
   - RTL layout (for Arabic, Urdu, etc.)

## 🎨 UI/UX Guidelines

### Design Principles
- **Simplicity**: Keep UI clean and uncluttered
- **Consistency**: Follow Material Design 3
- **Accessibility**: Support TalkBack, large text
- **Performance**: Smooth 60fps animations
- **Responsiveness**: Support all screen sizes

### Adding a New Theme

1. **Define colors**
   ```kotlin
   private val MyThemeLightColors = lightColorScheme(
       primary = Color(0xFF...),
       // Other colors
   )
   
   private val MyThemeDarkColors = darkColorScheme(
       primary = Color(0xFF...),
       // Other colors
   )
   ```

2. **Add to theme system**
   ```kotlin
   when (themeId) {
       "my_theme_light" -> MyThemeLightColors
       "my_theme_dark" -> MyThemeDarkColors
       // Other themes
   }
   ```

3. **Update settings UI**
   ```kotlin
   ThemeOptionRow(
       name = "My Theme",
       lightId = "my_theme_light",
       darkId = "my_theme_dark",
       // Colors
   )
   ```

## 🐛 Debugging Tips

### Common Issues

#### Gradle Sync Failed
```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

#### Compose Preview Not Working
```kotlin
// Add preview annotation
@Preview(showBackground = true)
@Composable
fun MyPreview() {
    QuranReaderTheme {
        MyScreen()
    }
}
```

#### Hilt Injection Failed
```kotlin
// Ensure @HiltAndroidApp on Application
@HiltAndroidApp
class QuranReaderApp : Application()

// Ensure @AndroidEntryPoint on Activity
@AndroidEntryPoint
class MainActivity : ComponentActivity()

// Ensure @HiltViewModel on ViewModel
@HiltViewModel
class MyViewModel @Inject constructor() : ViewModel()
```

## 📚 Resources

### Learning
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Android Architecture Guide](https://developer.android.com/topic/architecture)

### Tools
- [Android Studio](https://developer.android.com/studio)
- [Compose Preview](https://developer.android.com/jetpack/compose/tooling)
- [Layout Inspector](https://developer.android.com/studio/debug/layout-inspector)
- [Profiler](https://developer.android.com/studio/profile)

## 💬 Communication

### Channels
- **GitHub Issues**: Bug reports, feature requests
- **GitHub Discussions**: Questions, ideas, general discussion
- **Pull Requests**: Code contributions

### Response Time
- Issues: Within 48 hours
- Pull Requests: Within 1 week
- Discussions: Best effort

## 🙏 Recognition

Contributors will be:
- Listed in CONTRIBUTORS.md
- Mentioned in release notes
- Credited in the app (if significant contribution)

## ❓ Questions?

If you have questions:
1. Check existing documentation
2. Search closed issues
3. Ask in GitHub Discussions
4. Create a new issue

---

**Thank you for contributing to Quran Reader App!** 

May Allah reward you for your efforts in serving the Muslim community. 🤲

*"The best of people are those who bring most benefit to others."* - Prophet Muhammad ﷺ
