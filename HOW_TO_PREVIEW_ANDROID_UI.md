# 📱 How to Preview Your Android UI/UX (Step-by-Step)

## What You're Trying To Do

You want to **see and review the UI/UX** of your Jukwa app to check:
- Design layouts
- Button placements
- Navigation flows
- Color schemes
- Typography
- User interactions

---

## The Quick Answer

Your UI is **NOT in a browser** (like http://localhost:8888).

It's in **Android Studio** in the **Jetpack Compose Preview panel**.

---

## Step 1: Download & Install Android Studio (If Not Already Done)

### Check If You Have It
```powershell
# Open PowerShell, type:
"C:\Program Files\Android\Android Studio\bin\studio64.exe" --version
```

If you see a version number → You have it, skip to Step 2.

If error → Download from: https://developer.android.com/studio

---

## Step 2: Open Your Android Project in Android Studio

### Open Android Studio
```powershell
# Or just click Android Studio in Windows Start menu
"C:\Program Files\Android\Android Studio\bin\studio64.exe"
```

### Open Your Project
1. Click **File** (top menu)
2. Click **Open** 
3. Navigate to: `D:\Github Local\Baraza-la-Jukwa\android`
4. Click **OK**

**Wait 2-3 minutes** for Gradle to sync (it downloads dependencies).

You'll see:
```
[Sync in progress...]
Gradle: Processing dependencies...
Syncing files...
[Sync completed]
```

---

## Step 3: Navigate to a UI Screen File

### Find the Android App Source Code
```
Project Explorer (Left Side)
└── android
    └── app
        └── src
            └── main
                └── java
                    └── [Your package name]
                        └── *.kt files (UI screens)
```

### Open Any .kt File
Example path:
```
android/app/src/main/java/com/kenyawebs/jukwa/screens/IncidentScreen.kt
```

Click on any `.kt` file (these are Kotlin files with UI code).

---

## Step 4: View the Live Jetpack Compose Preview

### Look at the RIGHT PANEL

When you open a `.kt` file with Jetpack Compose code, the right side of Android Studio automatically shows a **live preview** of that UI:

```
┌──────────────────────────────────────┬──────────────────────────┐
│ Code Editor (Left)                   │ Preview Panel (Right)    │
├──────────────────────────────────────┼──────────────────────────┤
│                                      │                          │
│ @Composable                          │  ┌──────────────────┐   │
│ fun IncidentScreen() {               │  │   [Phone Frame]  │   │
│   Column {                           │  │                  │   │
│     Text("Report Incident")          │  │  Report Incident │   │
│     Button(...) { ... }              │  │  [Submit Button] │   │
│   }                                  │  │                  │   │
│ }                                    │  │  ↑ LIVE PREVIEW! │   │
│                                      │  └──────────────────┘   │
└──────────────────────────────────────┴──────────────────────────┘
```

### Interact With the Preview

**In the preview panel, you can:**
- ✅ Click buttons and see what happens
- ✅ Type text in text fields
- ✅ Scroll through lists
- ✅ Tap on navigation items
- ✅ Change device orientation (portrait/landscape)
- ✅ See real-time updates as you edit code

### Edit Code, Watch Preview Update

Try this:
1. In the code editor, change a label text:
   ```kotlin
   // Change from:
   Text("Report Incident")
   // To:
   Text("Report Emergency")
   ```

2. Save (Ctrl+S)

3. **The preview updates instantly** (no rebuild needed!)

---

## Step 5: Switch Between Different UI Screens

### View All Available Screens

```
Project Explorer (Left)
└── android/app/src/main/java/...
    └── screens/
        ├── HomeScreen.kt
        ├── IncidentScreen.kt
        ├── IdentityScreen.kt
        ├── TrafficScreen.kt
        ├── EmergencyScreen.kt
        ├── NotificationScreen.kt
        ├── CommitmentScreen.kt
        └── [more screens]
```

### Switch Preview Between Screens

1. Click on `HomeScreen.kt` → preview shows home screen
2. Click on `IncidentScreen.kt` → preview shows incident report screen
3. Click on `TrafficScreen.kt` → preview shows traffic info screen
4. Etc.

Each file you click shows its UI instantly in the right panel.

---

## Step 6: View Different Device Sizes/Orientations

### In the Preview Panel Header

Look at the toolbar above the preview:

```
┌──────────────────────────────────────────┐
│ [Device ▼] [Pixel 6 Pro, API 34, ▼]    │
│ [Orientation ▼] [Portrait]              │
│ [Theme ▼] [Light] [Dark ▼]             │
└──────────────────────────────────────────┘
```

### Change Device
Click **Device dropdown** → Select different phones (Pixel 4, Pixel 5, Pixel 6 Pro, iPhone, Tablet, etc.)

### Change Orientation
Click **Orientation** → Switch between Portrait and Landscape

### Change Theme
Click **Theme** → Switch between Light and Dark mode

### Test Responsive Design
Try switching between different devices to see how your UI adapts.

---

## Step 7: Advanced: Run on Emulator or Real Phone

### Option A: Android Emulator (Simulated Phone)

1. **Create Virtual Device:**
   - Android Studio → **Tools** → **AVD Manager**
   - Click **Create Virtual Device**
   - Select device (Pixel 6, Pixel 6 Pro, etc.)
   - Click **Next** → **Next** → **Finish**

2. **Start Emulator:**
   - AVD Manager → Select device → Click play button ▶️
   - Wait 30 seconds for emulator to boot

3. **Install App:**
   - Green **Run** button (top toolbar)
   - Select the emulator
   - Click **OK**
   - APK installs and launches on emulator

4. **Test Full App:**
   - Now you're testing the complete app like a real user
   - Tap buttons, fill forms, navigate screens
   - All backend services are available at localhost:3001, 3002, etc.

### Option B: Real Android Phone

1. **Enable Developer Mode on your phone:**
   - Settings → About → Build Number (tap 7 times)
   - Settings → Developer Options → USB Debugging (turn on)

2. **Connect Phone via USB Cable:**
   - Plug in phone
   - Android Studio → Select your phone from device dropdown

3. **Click Green Run Button:**
   - APK installs directly to your phone
   - Test on real hardware

---

## Reference: File Structure of UI Code

```
D:\Github Local\Baraza-la-Jukwa\android\
├── app/
│   ├── src/
│   │   └── main/
│   │       └── java/
│   │           └── [com/kenyawebs/jukwa/]
│   │               ├── MainActivity.kt          ← Entry point
│   │               ├── screens/
│   │               │   ├── HomeScreen.kt       ← Home page UI
│   │               │   ├── IncidentScreen.kt   ← Incident report UI
│   │               │   ├── IdentityScreen.kt   ← Identity/profile UI
│   │               │   ├── TrafficScreen.kt    ← Traffic map UI
│   │               │   ├── EmergencyScreen.kt  ← Emergency response UI
│   │               │   └── [more screens]
│   │               ├── components/
│   │               │   ├── IncidentCard.kt     ← Reusable UI component
│   │               │   ├── MapView.kt
│   │               │   └── [more components]
│   │               ├── models/
│   │               │   ├── Incident.kt         ← Data models
│   │               │   ├── Traffic.kt
│   │               │   └── [more models]
│   │               ├── viewmodels/
│   │               │   ├── IncidentViewModel.kt ← State management
│   │               │   └── [more viewmodels]
│   │               └── ui/
│   │                   ├── theme/
│   │                   │   ├── Color.kt        ← Color scheme
│   │                   │   ├── Typography.kt   ← Fonts/text styles
│   │                   │   └── Theme.kt
│   │                   └── [more UI utilities]
│   └── build.gradle.kts                        ← Dependencies
└── build.gradle.kts
```

---

## What Each Screen Probably Has

Based on your "antigravity" notes, expect to find:

| Screen | Purpose | Preview Shows |
|--------|---------|---------------|
| **HomeScreen** | Main dashboard | Map, incident list, navigation tabs |
| **IncidentScreen** | Report incidents | Form to submit incident reports |
| **IdentityScreen** | User profile/auth | Login, profile, citizen vault access |
| **TrafficScreen** | Traffic monitoring | Map with traffic sensors, congestion |
| **EmergencyScreen** | Emergency response | Emergency alerts, hotline numbers |
| **NotificationScreen** | Notification feed | Recent notifications, alerts |
| **CommitmentScreen** | Citizen commitments | Track commitments, accountability |

---

## Troubleshooting Android Studio

### Issue: Preview Not Showing

**Problem:** You open a `.kt` file but no preview appears on right side.

**Solution:**
1. Look for tabs at top of right panel: `Preview` | `Design` | `Split`
2. Click **Preview** or **Design** tab
3. If still nothing, click: **View** → **Tool Windows** → **Preview**

### Issue: Gradle Sync Taking Too Long

**Problem:** Android Studio stuck at "Syncing..."

**Solution:**
1. Wait 5 minutes (first sync downloads ~1GB of dependencies)
2. If still stuck, click: **File** → **Invalidate Caches** → **Invalidate and Restart**
3. Or: **File** → **Sync Project with Gradle Files**

### Issue: Build Fails with Errors

**Problem:** Red error messages in code.

**Solution:**
1. Bottom panel → **Problems** tab
2. Fix errors shown (usually missing imports or syntax)
3. Or ask: `docker ai "help me fix this Android Studio error: [error message]"`

### Issue: Emulator Won't Start

**Problem:** AVD Manager shows device but won't launch.

**Solution:**
1. Make sure you have enough disk space (emulator needs ~10GB)
2. Try: **AVD Manager** → Right-click device → **Wipe Data** → Try again
3. Or update: **Tools** → **SDK Manager** → Update emulation packages

---

## TL;DR: 3-Minute Setup

1. **Open Android Studio** (Windows Start menu)
2. **File** → **Open** → `D:\Github Local\Baraza-la-Jukwa\android` → **OK**
3. **Wait for Gradle** sync (1-2 min)
4. **Click any `.kt` file** in `java/...` folder
5. **Look at RIGHT PANEL** → You see the UI preview
6. **Edit code** → Preview updates instantly
7. **Switch screens** → Click different `.kt` files to view different UIs

That's it! You're now viewing your actual UI/UX. 📱

---

## Next: Test the App

Once you've reviewed the UI in preview:

### Run on Emulator
```
1. Green Run button (top toolbar)
2. Select Android Emulator device
3. App launches, you test it
```

### Connect Backend
The app will connect to your running Docker services. However, because the Android Emulator runs as its own virtual machine, it cannot use `localhost` (which would point to the phone itself).

Instead, it uses the special bridge IP **`10.0.2.2`** to talk to your Windows Docker Host:
- Incident service: `http://10.0.2.2:3001`
- Traffic service: `http://10.0.2.2:3003`
- Identity service: `http://10.0.2.2:3006`
- Etc.

*(Note: Your `android/app/src/main/kotlin/ke/jukwa/di/NetworkModule.kt` is already configured correctly with these `10.0.2.2` addresses!)*

### See Real Data
The app will fetch real data from your backend services (or errors if something's not configured).

---

## One More Thing: What NOT to Do

❌ **DON'T expect a web browser UI** — this is a mobile app
❌ **DON'T look in http://localhost:3001** for UI — that's an API endpoint
❌ **DON'T use the Dozzle dashboard for UI review** — that's just logs
❌ **DON'T click Run without selecting an emulator/device** — nothing will happen

✅ **DO use Android Studio Preview panel** — instant, interactive, live
✅ **DO run on emulator for full app testing** — better than just preview
✅ **DO use backend APIs (http://localhost:3001) for API testing** — separate from UI
✅ **DO check logs in Dozzle when API calls fail** — debugging backend

---

You're ready! Open Android Studio and start reviewing your UI. 🚀
