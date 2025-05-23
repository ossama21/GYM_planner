# GYM Planner - Workout & Fitness Tracker

A comprehensive fitness companion app that helps you plan workouts, track progress, monitor nutrition, and achieve your fitness goals.

![App Banner](screenshots/app_banner.png)

## Features

### 📋 Workout Planning
- Create and customize workout plans tailored to your goals
- Organize exercises by muscle groups
- Visual exercise guides with GIF animations
- Rest day scheduling
- Estimated workout duration calculation

### 🏋️ Workout Execution
- Step-by-step exercise guidance
- Timer for rest periods
- Track sets, reps, and weights
- Body part visualization showing targeted muscles

### 📊 Progress Tracking
- Track weight and body measurements
- Visualize progress with detailed charts
- Compare workout performance over time
- Set and monitor fitness goals

### 🍎 Nutrition Tracking
- Log daily food intake
- Track calories, macros, and nutrients
- TDEE (Total Daily Energy Expenditure) calculator
- Weight management planning

### ⚙️ Personalization
- Customizable themes
- User profile with health metrics
- Notification preferences
- Workout reminders

## Screenshots

### Home Screen & Navigation
![Home Screen](screenshots/home_screen.png) | ![Navigation Drawer](screenshots/navigation_drawer.png)
:-------------------------:|:-------------------------:
Main dashboard | App navigation menu

### Workout Planning
![Workout List](screenshots/workout_list.png) | ![Workout Editor](screenshots/workout_editor.png)
:-------------------------:|:-------------------------:
Workout plan overview | Creating and editing workouts

### Exercise Library
![Exercise List](screenshots/exercise_list.png) | ![Exercise Details](screenshots/exercise_details.png)
:-------------------------:|:-------------------------:
Browse available exercises | Exercise detail with animation

### Workout Execution
![Workout Start](screenshots/workout_start.png) | ![Exercise Execution](screenshots/exercise_execution.png)
:-------------------------:|:-------------------------:
Starting a workout | Performing exercises with guidance

### Progress Tracking
![Weight Chart](screenshots/weight_chart.png) | ![Workout History](screenshots/workout_history.png)
:-------------------------:|:-------------------------:
Weight progress visualization | Workout history and achievements

### Nutrition Tracking
![Nutrition Dashboard](screenshots/nutrition_dashboard.png) | ![Food Entry](screenshots/food_entry.png)
:-------------------------:|:-------------------------:
Nutrition overview | Adding food entries

### Settings & Personalization
![Settings](screenshots/settings.png) | ![Theme Options](screenshots/theme_options.png)
:-------------------------:|:-------------------------:
App settings | Theme customization

## Installation

### Requirements
- Android 6.0 (Marshmallow) or higher
- ~50MB free storage space

### Install from Google Play
<a href='https://play.google.com/store/apps/details?id=com.H_Oussama.gymplanner'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' height='80px'/></a>

### Manual Installation
1. Download the latest APK from the [Releases](https://github.com/username/GYM_planner/releases) page
2. Enable installation from unknown sources in your device settings
3. Open the APK file and follow the installation instructions

## Development Setup

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or newer
- JDK 11 or newer
- Android SDK 31 (Android 12) or newer

### Clone and Build
```bash
# Clone the repository
git clone https://github.com/username/GYM_planner.git

# Navigate to the project directory
cd GYM_planner

# Build the debug version
./gradlew assembleDebug
```

### Run the App
- Connect an Android device or start an emulator
- Run the app using Android Studio or with the command:
```bash
./gradlew installDebug
```

## Technologies Used

- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture principles
- **Dependency Injection**: Hilt
- **Database**: Room
- **Asynchronous Programming**: Kotlin Coroutines & Flow
- **Image Loading**: Coil
- **Charts**: MPAndroidChart
- **JSON Parsing**: Kotlinx Serialization
- **Persistence**: SharedPreferences, Room Database

## Contributing

Contributions are welcome! If you'd like to contribute, please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please ensure your code follows the project's coding style and includes appropriate tests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgements

- Exercise GIFs provided by [ExerciseDB](https://exercisedb.p.rapidapi.com/)
- Icons from [Material Design Icons](https://materialdesignicons.com/)
- Special thanks to all contributors and testers

---

## Contact

Developer: H_Oussama
Email: youremail@example.com
GitHub: [Your GitHub Profile](https://github.com/username) 