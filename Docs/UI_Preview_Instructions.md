# 📱 How to Preview Your Jukwa Android UI

This guide contains the corrected steps and paths to view the UI/UX of the Jukwa app in Android Studio.

---

## Step 1: Open Your Project in Android Studio

1.  Open **Android Studio**.
2.  Click **File** -> **Open**.
3.  Navigate to: `D:\Github Local\Baraza-la-Jukwa\android`
4.  Click **OK**.
5.  **Wait for Gradle to sync**. You will see "Build: completed successfully" in the bottom status bar when it's ready.

---

## Step 2: Navigate to the UI Screen Files

All UI screens are located in the `ke.jukwa` package. You can find them in the Project Explorer (Left Side) under:

```
android
└── app
    └── src
        └── main
            └── kotlin
                └── ke
                    └── jukwa
                        ├── ui
                        │   └── home
                        │       └── HomeScreen.kt (Main Dashboard)
                        └── presentation
                            ├── report
                            │   └── ReportScreen.kt (Incident Reporting)
                            ├── myreports
                            │   └── MyReportsScreen.kt (Your History)
                            ├── baraza
                            │   └── BarazaScreen.kt (Government Accountability)
                            └── settings
                                └── SettingsScreen.kt (App Settings)
```

---

## Step 3: View the Live Jetpack Compose Preview

1.  Open any of the `.kt` files listed above (e.g., `HomeScreen.kt`).
2.  Look at the **TOP RIGHT** corner of the editor window.
3.  Click on the **Split** or **Design** icon to show the preview panel.

### What you can see in each file:

| File | Preview Composable Name | Description |
| :--- | :--- | :--- |
| `HomeScreen.kt` | `HomeScreenPreview` | The main map dashboard and quick metrics. |
| `ReportScreen.kt` | `ReportScreenPreview` | The incident reporting form with category selection. |
| `MyReportsScreen.kt` | `MyReportsScreenPreview` | A list of reported incidents with status indicators. |
| `BarazaScreen.kt` | `BarazaScreenPreview` | The Baraza module entry screen. |
| `SettingsScreen.kt` | `SettingsScreenPreview` | The app settings screen. |

---

## Step 4: Interact with the Preview

In the Preview panel, you can:
- **Switch to Interactive Mode**: Click the small "pointing finger" icon or the "Play" button on a preview to interact with buttons and sliders.
- **Change Device**: Use the dropdown at the top of the preview panel to see how it looks on a Pixel 6, a tablet, etc.
- **Toggle Dark Mode**: Use the "Theme" dropdown to switch between light and dark themes.

---

## Troubleshooting

### Preview Not Showing
- Make sure the project has finished syncing with Gradle.
- Click **Build** -> **Make Project** (Ctrl+F9) to ensure all components are compiled.
- If you see a "Rendering Problems" error, click the **Refresh** button (circular arrow) in the preview panel.

### Missing Icons or Images
- Previews use local resources. If an icon is missing, it might be because the build hasn't completed or there's a rendering limitation in the Preview panel. Usually, a "Make Project" fixes this.
