# 📐 Responsive Design System

## Overview
Sistem dimensi yang konsisten untuk memastikan UI terlihat proporsional di semua ukuran layar (phone, tablet 7", tablet 10"+).

## 🎯 Tujuan
- ✅ Konsistensi UI di semua DPI
- ✅ Automatic scaling untuk tablet
- ✅ Mudah maintain dan update
- ✅ Type-safe dengan Kotlin
- ✅ Compose-friendly API

## 📱 Screen Size Breakpoints

### Phone (Default)
- **Qualifier**: `values/`
- **Screen Width**: < 600dp
- **Devices**: Phones, small tablets
- **Scaling**: 1.0x (base)

### Tablet 7"
- **Qualifier**: `values-sw600dp/`
- **Screen Width**: 600dp - 719dp
- **Devices**: 7" tablets, large phones
- **Scaling**: ~1.2x

### Tablet 10"+
- **Qualifier**: `values-sw720dp/`
- **Screen Width**: ≥ 720dp
- **Devices**: 10" tablets, large tablets
- **Scaling**: ~1.5x

## 📏 Dimension Categories

### 1. Spacing & Padding
Consistent spacing throughout the app:

| Name | Phone | Tablet 7" | Tablet 10" | Usage |
|------|-------|-----------|------------|-------|
| `spacing_xs` | 4dp | 6dp | 8dp | Minimal spacing |
| `spacing_small` | 8dp | 12dp | 16dp | Small gaps |
| `spacing_medium` | 12dp | 16dp | 20dp | Medium gaps |
| `spacing_default` | 16dp | 24dp | 32dp | Default spacing |
| `spacing_large` | 24dp | 32dp | 40dp | Large gaps |
| `spacing_xl` | 32dp | 48dp | 56dp | Extra large |
| `spacing_xxl` | 48dp | 64dp | 72dp | Maximum spacing |

### 2. Icon Sizes
Scalable icon dimensions:

| Name | Phone | Tablet 7" | Tablet 10" | Usage |
|------|-------|-----------|------------|-------|
| `icon_size_small` | 16dp | 20dp | 24dp | Small icons |
| `icon_size_medium` | 24dp | 28dp | 32dp | Default icons |
| `icon_size_large` | 32dp | 40dp | 48dp | Large icons |
| `icon_size_xl` | 48dp | 56dp | 64dp | Extra large icons |

### 3. Button Heights
Touch-friendly button sizes:

| Name | Phone | Tablet 7" | Tablet 10" | Usage |
|------|-------|-----------|------------|-------|
| `button_height_small` | 40dp | 48dp | 56dp | Compact buttons |
| `button_height_medium` | 48dp | 56dp | 64dp | Default buttons |
| `button_height_large` | 56dp | 64dp | 72dp | Prominent buttons |

### 4. Card Elevation
Material Design elevation levels:

| Name | Phone | Tablet 7" | Tablet 10" | Usage |
|------|-------|-----------|------------|-------|
| `card_elevation_default` | 2dp | 3dp | 4dp | Default cards |
| `card_elevation_raised` | 4dp | 6dp | 8dp | Raised cards |
| `card_elevation_high` | 8dp | 12dp | 16dp | Floating elements |

### 5. Corner Radius
Rounded corners for modern UI:

| Name | Phone | Tablet 7" | Tablet 10" | Usage |
|------|-------|-----------|------------|-------|
| `corner_radius_small` | 4dp | 6dp | 8dp | Small elements |
| `corner_radius_medium` | 8dp | 12dp | 16dp | Default radius |
| `corner_radius_large` | 12dp | 16dp | 20dp | Large cards |
| `corner_radius_xl` | 16dp | 20dp | 24dp | Extra large |
| `corner_radius_xxl` | 24dp | 28dp | 32dp | Maximum radius |

### 6. Screen-Specific Dimensions

#### Home Screen
| Name | Phone | Tablet 7" | Tablet 10" |
|------|-------|-----------|------------|
| `home_circular_progress_size` | 280dp | 360dp | 420dp |
| `home_card_min_height` | 120dp | 140dp | 160dp |

#### Reading Screen
| Name | Phone | Tablet 7" | Tablet 10" |
|------|-------|-----------|------------|
| `reading_progress_size` | 280dp | 360dp | 420dp |
| `reading_button_height` | 56dp | 64dp | 72dp |

#### Navigation Cards (Juz/Surah/Hizb)
| Name | Phone | Tablet 7" | Tablet 10" |
|------|-------|-----------|------------|
| `navigation_card_badge_size` | 56dp | 72dp | 88dp |
| `navigation_card_min_height` | 80dp | 100dp | 120dp |

#### Bottom Navigation
| Name | Phone | Tablet 7" | Tablet 10" |
|------|-------|-----------|------------|
| `bottom_nav_height` | 80dp | 96dp | 112dp |
| `bottom_nav_icon_size` | 24dp | 28dp | 32dp |

#### Other Components
| Name | Phone | Tablet 7" | Tablet 10" |
|------|-------|-----------|------------|
| `top_bar_height` | 64dp | 72dp | 80dp |
| `bookmark_card_height` | 100dp | 120dp | 140dp |
| `session_card_min_height` | 120dp | 140dp | 160dp |

## 🔧 Usage in Compose

### Method 1: Using AppDimensions (Recommended)
```kotlin
import com.quranreader.custom.ui.theme.AppDimensions

@Composable
fun MyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimensions.spacingDefault)
    ) {
        Icon(
            Icons.Default.Home,
            contentDescription = null,
            modifier = Modifier.size(AppDimensions.iconSizeMedium)
        )
        
        Spacer(Modifier.height(AppDimensions.spacingLarge))
        
        Button(
            onClick = { },
            modifier = Modifier.height(AppDimensions.buttonHeightMedium),
            shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium)
        ) {
            Text("Click Me")
        }
    }
}
```

### Method 2: Using LocalDimensions
```kotlin
import com.quranreader.custom.ui.theme.LocalDimensions

@Composable
fun MyCard() {
    val dimens = LocalDimensions
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimens.spacingDefault),
        shape = RoundedCornerShape(dimens.cornerRadiusLarge),
        elevation = CardDefaults.cardElevation(
            defaultElevation = dimens.cardElevationDefault
        )
    ) {
        // Card content
    }
}
```

### Method 3: Direct Resource Access
```kotlin
@Composable
fun MyComponent() {
    val context = LocalContext.current
    val spacing = context.resources.getDimension(R.dimen.spacing_default).dp
    
    Box(modifier = Modifier.padding(spacing)) {
        // Content
    }
}
```

## 📐 Design Principles

### 1. Proportional Scaling
- Phone: Base size (1.0x)
- Tablet 7": ~1.2x scaling
- Tablet 10": ~1.5x scaling

### 2. Touch Targets
- Minimum touch target: 48dp (phone)
- Scaled appropriately for tablets
- Comfortable spacing between interactive elements

### 3. Visual Hierarchy
- Consistent spacing creates clear hierarchy
- Larger screens = more breathing room
- Maintains readability at all sizes

### 4. Content Density
- Phone: Compact, efficient use of space
- Tablet: More spacious, comfortable reading
- Large Tablet: Maximum comfort, reduced eye strain

## 🎨 Best Practices

### DO ✅
- Use `AppDimensions` for all spacing and sizing
- Test on multiple screen sizes
- Use semantic names (e.g., `spacingDefault` not `16dp`)
- Maintain consistent spacing throughout app
- Scale icons and buttons proportionally

### DON'T ❌
- Hardcode dp values in Compose code
- Use different spacing for similar elements
- Ignore tablet optimization
- Mix hardcoded and resource dimensions
- Forget to test on tablets

## 🧪 Testing

### Test Devices
1. **Phone**: Pixel 5 (393dp width)
2. **Tablet 7"**: Nexus 7 (600dp width)
3. **Tablet 10"**: Pixel Tablet (840dp width)

### Test Checklist
- [ ] All screens render correctly on phone
- [ ] All screens render correctly on 7" tablet
- [ ] All screens render correctly on 10" tablet
- [ ] Touch targets are adequate on all sizes
- [ ] Text is readable on all sizes
- [ ] Spacing looks consistent
- [ ] Icons scale appropriately
- [ ] Buttons are properly sized
- [ ] Cards have correct proportions

## 📊 Scaling Formula

### General Formula
```
tablet_value = phone_value × scaling_factor
```

### Scaling Factors
- **Tablet 7"**: 1.2x - 1.5x
- **Tablet 10"**: 1.5x - 2.0x

### Example
```
Phone spacing: 16dp
Tablet 7": 16dp × 1.5 = 24dp
Tablet 10": 16dp × 2.0 = 32dp
```

## 🔄 Migration Guide

### Before (Hardcoded)
```kotlin
Column(
    modifier = Modifier.padding(16.dp)
) {
    Icon(
        Icons.Default.Home,
        modifier = Modifier.size(24.dp)
    )
    Spacer(Modifier.height(24.dp))
}
```

### After (Responsive)
```kotlin
Column(
    modifier = Modifier.padding(AppDimensions.spacingDefault)
) {
    Icon(
        Icons.Default.Home,
        modifier = Modifier.size(AppDimensions.iconSizeMedium)
    )
    Spacer(Modifier.height(AppDimensions.spacingLarge))
}
```

## 📱 Device Examples

### Phones
- Pixel 5: 393dp × 851dp
- Samsung Galaxy S21: 384dp × 854dp
- iPhone 13 Pro: 390dp × 844dp

### Tablets 7"
- Nexus 7: 600dp × 960dp
- Samsung Tab A7: 600dp × 960dp

### Tablets 10"+
- Pixel Tablet: 840dp × 1340dp
- iPad Pro 11": 834dp × 1194dp
- Samsung Tab S8: 800dp × 1280dp

## 🎯 Benefits

### For Developers
- ✅ Type-safe dimension access
- ✅ Autocomplete in IDE
- ✅ Easy to maintain
- ✅ Centralized configuration
- ✅ Compose-friendly API

### For Users
- ✅ Consistent experience across devices
- ✅ Optimized for their screen size
- ✅ Comfortable touch targets
- ✅ Readable text at all sizes
- ✅ Professional appearance

### For Design
- ✅ Maintains design system
- ✅ Scalable across devices
- ✅ Easy to update globally
- ✅ Consistent spacing
- ✅ Professional polish

## 📚 Resources

### Android Documentation
- [Supporting Different Screen Sizes](https://developer.android.com/guide/topics/large-screens/support-different-screen-sizes)
- [Dimension Resources](https://developer.android.com/guide/topics/resources/more-resources#Dimension)
- [Material Design Layout](https://m3.material.io/foundations/layout/understanding-layout/overview)

### Tools
- Android Studio Layout Inspector
- Device Preview in Compose
- Resizable Emulator

---

**Status**: ✅ Implemented and Ready
**Version**: 1.0.0
**Last Updated**: 2024
