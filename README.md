# 🚀 Delivery Buddy
### Floating Delivery Assistant for Ekart Field X

[![Android Min SDK](https://img.shields.io/badge/Min%20SDK-24%252B-brightgreen.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-orange.svg)](https://developer.android.com/jetpack/compose)
[![Navigation 3](https://img.shields.io/badge/Navigation-3.0-purple.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📱 Overview

**Delivery Buddy** is an advanced, productivity-boosting companion app designed specifically for delivery executives using **Ekart Field X**. It runs seamlessly over your delivery workflow as a floating overlay assistant, parsing runsheets, optimizing multi-stop delivery routes with custom start and end addresses, tracking delivery success & COD collections, calculating fuel costs & mileage, and automatically detecting nearby petrol pumps with instant route recalculation.

---

## ✨ Key Features

- 🛸 **Floating Overlay Assistant**: A persistent, draggable picture-in-picture floating widget that gives delivery executives quick access to active orders, next stop details, and status updates without leaving Ekart Field X.
- 📋 **Intelligent Runsheet Parser**: Automatically parses raw clipboard text, shared runsheets, or text snippets to instantly extract order IDs, customer names, delivery addresses, phone numbers, and COD amounts.
- 🗺️ **Advanced Route Optimization**: Optimizes multi-stop delivery sequences with customizable start and end addresses, minimizing travel distance, fuel consumption, and transit time.
- ✅ **Delivery Success & Status Tracking**: Seamlessly track delivery outcomes—mark orders as *Delivered*, *Undelivered*, *Return*, or *COD Collected* with real-time summary statistics.
- 🔄 **Intelligent Order Sorting**: Sort deliveries dynamically by optimized sequence, priority, status, or distance.
- ⛽ **Fuel Cost & Mileage Calculator**: Input your vehicle mileage and fuel price to track fuel consumption, trip expenses, and cost-per-delivery metrics.
- 📊 **Comprehensive Delivery History**: Review past runsheets, completed routes, earnings summaries, distance traveled, and success rates over days and weeks.
- ⛽ **Nearby Petrol Pump Detection & Recalculation**: Automatically scans for nearby petrol pumps along or near your active route when fuel is running low, offering one-tap route recalculation.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/) (100% modern coroutines & flows)
- **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 (M3) Design System & Dynamic Color support
- **Navigation**: [Jetpack Navigation 3](https://androidx.tech/artifacts/navigation3/navigation3-runtime/) (`androidx.navigation3`) with type-safe `@Serializable` routes
- **Adaptive Layouts**: Compose Material Adaptive (`ListDetailPaneScaffold`) for seamless phone, foldable, and tablet support
- **Architecture**: **MVVM (Model-View-ViewModel)** Architecture pattern with Repository pattern, StateFlow, and Lifecycle-aware ViewModels
- **Background Services**: Android Foreground Service & Window Manager for Floating Overlay management
- **Persistence**: Room Database & DataStore Preferences for robust local caching

---

## 📂 Project Structure & Architecture

```text
deliverybuddy/
├── app/
    ├── build.gradle.kts
    └── src/
        └── main/
            ├── AndroidManifest.xml
            └── java/com/example/deliverybuddy/
                ├── data/              # Repositories & Runsheet Parser
                ├── model/             # Data models (Address, Runsheet, RoutePlan, PetrolPump, etc.)
                ├── service/           # FloatingWidgetService (Floating Overlay UI & WindowManager)
                ├── ui/
                │   ├── navigation/    # Navigation 3 Graph & NavKeys
                │   ├── screens/       # RunsheetList, RunsheetDetail, RoutePlan, History, Settings
                │   ├── theme/         # Material 3 Theme, Color, Type
                │   └── viewmodel/     # DeliveryViewModel (StateFlow & Business Logic)
                └── MainActivity.kt
├── website/                         # Landing Page & APK Distribution
    ├── index.html                     # Responsive Tailwind CSS Landing Page
    ├── vercel.json                    # Vercel deployment configuration
    ├── DEPLOY.md                      # Deployment Guide
    └── delivery-buddy.apk             # Distribution APK file
└── README.md
```

### Architecture Details
- **Data Layer**: `DeliveryRepository` manages local state, parsing logic, and runsheet persistence. `RunsheetParser` utilizes robust regex and NLP pattern matching to extract structured order data from unstructured delivery texts.
- **UI Layer**: Built entirely with Jetpack Compose using Material 3 Expressive components. Navigation is handled via Jetpack Navigation 3 `NavDisplay` and typed `NavKey` serializables.
- **Floating Overlay Service**: `FloatingWidgetService` runs as an independent foreground service utilizing Android's `WindowManager` to display an interactive, draggable floating bubble that expands into a quick-access delivery control panel.

---

## 🌐 Website & Landing Page / APK Distribution

Delivery Buddy includes a dedicated, responsive landing page located in the `website/` directory, built with Tailwind CSS and designed for instant APK distribution.

- **Files in `website/`**:
  - `index.html`: Professional landing page featuring hero section, feature showcase, interactive mockups, and a direct download button for `delivery-buddy.apk`.
  - `vercel.json`: Vercel routing and deployment config.
  - `DEPLOY.md`: Step-by-step instructions for building the APK and publishing to Vercel or GitHub Pages.
  - `delivery-buddy.apk`: Production-ready installation file for delivery executives.

To deploy the landing page:
```bash
cd website
npx vercel
```

---

## ⚙️ Build & Run Instructions

### Prerequisites
- Android Studio Ladybug or newer / IntelliJ IDEA
- JDK 11 or higher
- Android SDK (Compile SDK 37, Min SDK 24)

### Clone & Open Project
```bash
git clone https://github.com/uju11/deliver-buddy.git
cd deliverybuddy
```
Open the project folder in Android Studio.

### Build via Gradle (Terminal)
To build the debug APK:
```bash
./gradlew :app:assembleDebug
```
The built APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

To run unit tests:
```bash
./gradlew :app:testDebugUnitTest
```

### Installation on Android Device
1. Enable **Developer Options** and **USB Debugging** on your Android device.
2. Connect your device or start an emulator.
3. Install and run via Android Studio (`Shift + F10`) or install the generated `app-debug.apk` directly on your device. Ensure "Display over other apps" permission is granted for the floating overlay feature.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
