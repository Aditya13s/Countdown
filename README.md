# ⏳ Countdown

A minimal, Material 3 Android app to track countdowns to your upcoming events — exams, birthdays, travel, and more.

## Screenshots

> _Screenshots coming soon. Build the app to see the UI._

## Features

- **📅 Google Calendar Import** — Import events directly from your synced Google Calendar. Select only the ones you want to track — no duplicate management needed.
- **⏱ Live Countdown** — Real-time ticker showing days, hours, minutes, and seconds to each event
- **🎨 Minimal Material 3 UI** — Clean, abstract card designs inspired by Pixel app aesthetics
- **🏠 Home Screen Widget** — Configurable widget with 3 background styles and adjustable font size
- **📌 Pin Events** — Pin important events to the top of the list
- **🔔 Reminders** — Get notified 1, 3, or 7 days before an event
- **🔍 Search & Sort** — Filter events by name, category, or note; sort by date or name
- **🎯 Rich Customization** — 8 colors, 30 emojis, 8 categories per event
- **📊 Progress Bars** — Visual timeline showing how much time has elapsed (toggleable)
- **🌙 Dark Mode** — Full light/dark/system theme support
- **📤 Share** — Share your countdown with friends

## How to Use

1. **Add Events**: Tap **＋** to create a new event with name, date, emoji, color, and category
2. **Import from Calendar**: Tap the menu → **Import from Calendar** to pull in Google Calendar events
3. **Track**: Events display a live countdown on the main screen
4. **Widget**: Long-press your home screen → Widgets → Countdown to add a home screen widget
5. **Settings**: Customize theme, sort order, reminders, widget style, and display options

## Building

Open in Android Studio (Hedgehog or later) and build the `app` module.

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Language**: Kotlin
- **UI**: Material 3 (Material Components 1.11.0)

## Permissions

| Permission | Purpose |
|---|---|
| `READ_CALENDAR` | Import events from Google Calendar |
| `POST_NOTIFICATIONS` | Event reminder notifications |
| `RECEIVE_BOOT_COMPLETED` | Reschedule reminders after reboot |
