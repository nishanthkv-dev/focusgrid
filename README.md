# Focus Now 🚀 — Student Operating System for Android

**Focus Now** is a modern, production-ready Android application designed specifically for students to manage **study planning, college scheduling, productivity tracking, app blocking, sleep tracking, to-do lists, study timers, analytics, streaks, and academic goal tracking** in one unified mobile application.

Built with **Jetpack Compose**, **Material 3**, **Room Database**, **DataStore**, **WorkManager**, and **Kotlin Coroutines**. It is completely **offline-first** and requires no external backend.

---

## 📸 Overview & Architecture

```
                                  +---------------------------------------+
                                  |         Focus Now Mobile App          |
                                  +---------------------------------------+
                                                     |
         +-------------------+-----------------------+---------------------+-------------------+
         |                   |                       |                     |                   |
+-----------------+ +-----------------+ +-------------------------+ +---------------+ +-----------------+
|  Daily Tasks    | | College Planner | | Study Timer & Blocker   | | Sleep Tracker | | Productivity OS |
| & Streaks Engine| | & Smart Planner | | (Focus & Pomodoro + FGS)| | & Correlation | | & Goals Manager |
+-----------------+ +-----------------+ +-------------------------+ +---------------+ +-----------------+
         |                   |                       |                     |                   |
         +-------------------+-----------------------+---------------------+-------------------+
                                                     |
                                  +---------------------------------------+
                                  |      Room Database & DataStore        |
                                  |     (Full Local Offline Storage)      |
                                  +---------------------------------------+
                                                     |
                                  +---------------------------------------+
                                  |   Local JSON Backup / Export / Import |
                                  +---------------------------------------+
```

---

## ✨ Key Features

### 1. 🎯 Comprehensive Home Dashboard
* **Today's Progress Ring**: Circular study target completion, time remaining, and status indicators.
* **Student Overview**: Name, college, department, active streak count, and daily productivity score.
* **Quick Actions Grid**: Direct 1-tap navigation to Study Timer, Tasks, Plan Day, College Timetable, Sleep Log, Analytics, App Blocker, and Exams.
* **Live Class & Priority Tasks Preview**: Shows today's upcoming lectures and urgent to-dos.

### 2. 📝 Daily To-Do System
* Categorized by **College, GATE, Placement, Coding, Project, Personal, Other**.
* Priority levels: **Low, Medium, High, Urgent**.
* Tabbed views: **Today, Upcoming, Completed, Overdue**.
* Recurring task support (**Daily, Weekly, Monthly, Custom**).
* Importance starring, reordering, and streak updating on completion.

### 3. 🏛️ College Day Planner & Timetable
* Dedicated day/week/month schedule viewer.
* Add recurring lectures/labs repeating on specific days of the week until semester end.
* Automatic future timetable generation.

### 4. 📅 Personal Daily Routine & Conflict Detector
* 24-hour timeline routine editor (Wake up, exercise, study, college, coding, dinner, GATE prep, revision, sleep).
* **Conflict Detection Engine**: Highlights schedule overlaps between college classes, study sessions, and routine activities.

### 5. ⚡ Smart Daily Plan ("Plan My Day")
* Synthesizes college timetable, sleep schedule, pending tasks, study targets, and upcoming exams into an optimized conflict-free daily timetable.
* Option to review and directly apply suggested schedule into active routines.

### 6. ⏱️ Study Timer & Persistent Focus
* **Focus Timer** (25m, 50m, 90m, custom) and **Pomodoro Timer** (study/break intervals with round counter).
* Background Foreground Service (`StudyTimerService`) with persistent status notification.
* Survives app kills, device reboots, and screen rotations using timestamp-based persistence.

### 7. 🔒 Application Blocking & Focus Lock Screen
* Integrated **Accessibility Blocker Service** (`AppBlockerAccessibilityService`) and **UsageStatsManager**.
* Reads installed applications on device (not hardcoded).
* Configurable **Blocking Profiles** (e.g. *GATE Mode*, *Coding Mode*, *Deep Focus*).
* Beautiful Focus Lock overlay (`BlockOverlayActivity`) displaying remaining study time, current subject, and a return-to-study action.

### 8. 📊 Study & Sleep Analytics + Correlation
* 7-day and 30-day interactive bar charts for study and sleep activity.
* Subject-wise and category-wise percentage distribution.
* Productivity highlights: Longest study session, most productive day, and peak time slot.
* **Sleep + Study Correlation Engine**: Computes correlation strictly from recorded data (*"Your study time was highest on days when you slept 7–8 hours"*).

### 9. 🏆 Streaks & Academic Goal Tracker
* Multi-track streak system: **Study Streak, Task Streak, Sleep Streak, Overall Productivity Streak**.
* Long-term milestone goals with interactive subtask checklists (e.g., *Complete DSA*, *GATE CS Syllabus*).

### 10. 🎯 Academic Deadlines & Countdown Manager
* Manage Exams, Assignments, Projects, Hackathons, Placement tests.
* Live countdown badges (*"DBMS Exam — 12 days left"*, *"GATE — 45 days left"*).

### 11. 💾 Local JSON Backup & Restore
* Complete offline export of all student data, timetable, goals, sessions, and streaks to formatted JSON.
* 1-click restore functionality and data reset tools.

---

## 🛠️ Tech Stack

* **Platform**: Android (minSdk 26, targetSdk 35, compileSdk 35)
* **Language**: Kotlin 2.0.21
* **UI Framework**: Jetpack Compose with Material 3
* **Local Database**: Room 2.6.1 + KSP
* **Preferences**: Jetpack DataStore Preferences
* **Background Tasks**: WorkManager & Foreground Service
* **Serialization**: Google Gson
* **Architecture**: MVVM + Clean Repository Pattern
* **CI/CD**: GitHub Actions (JDK 21, automated unit testing, debug APK assembly, and artifact upload)

---

## 🚀 Building and Running the App

### Prerequisites
* JDK 21
* Android SDK (API 35)
* Android Studio Ladybug (or newer)

### 1. Build and Run Unit Tests
```bash
./gradlew test
```

### 2. Build Debug APK Locally
```bash
./gradlew assembleDebug
```
The resulting APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🤖 GitHub Actions CI/CD & APK Download

Every push or pull request to `main` triggers GitHub Actions workflows (`.github/workflows/android-build.yml`):
1. Sets up JDK 21 and Android SDK environment.
2. Runs all unit tests.
3. Assembles the debug APK.
4. Uploads the build artifact as **`Engineer360-APK`**.

### How to Download the APK from GitHub:
1. Navigate to the **Actions** tab on the GitHub repository.
2. Select the latest successful workflow run (**Build Focus Now Android APK**).
3. Scroll down to the **Artifacts** section at the bottom.
4. Download the **`Engineer360-APK`** zip archive containing `app-debug.apk`.
5. Transfer and install on your Android device!

---

## 🔒 Android Permissions Setup (App Blocking)

Focus Now works fully offline. To enable the optional app blocking features during focus timers:
1. **Accessibility Service**: Go to *Settings > Accessibility > Focus Now Blocker Service* and enable it.
2. **Usage Access**: Go to *Settings > Apps > Special app access > Usage access > Focus Now* and grant access.
3. **Display over other apps**: Allow Focus Now to show the focus screen over blocked distraction apps.

*Note: If permissions are not granted, the app continues to work smoothly for study timing, planning, sleep tracking, and task management.*

---

## 📄 License
This project is licensed under the MIT License.
