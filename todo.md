Core Functionality:
[X] Parse text-based workout plan input. (Parser created)
[X] Create ExerciseDefinition entries (database persistence added; find-or-create in parser).
[X] Associate provided images with ExerciseDefinitions based on imageIdentifier. (Handled by ExerciseImage composable; uses DB definition)
[X] Display the structured workout plan (Days -> Exercises).
[X] Show exercise details: name, image, description (if any), sets/reps from the plan. (Display uses DB definition)

Timers & Execution:
[X] Implement an exercise timer during workout execution. (Basic timer logic done)
[X] Implement a rest timer between sets/exercises. (Basic timer logic done)
[X] Send notifications when timers complete.
    [X] Implement Workout Execution Screen Structure & Navigation (Initial structure done)
    [X] Implement Timer Logic in ViewModel
    [X] Implement Control Button Logic in UI
    [X] Implement Set/Exercise Progression Logic (Basic version done)
    [X] Implement Rep/Weight Logging UI & Logic (UI and ViewModel state done)

Progress Tracking:
[X] Allow users to log actual reps and weight for each set performed. (UI and temporary logging done)
    [X] Implement database saving for SetLog
[X] Store SetLog data persistently (e.g., using Room database). (Initial DataStore setup done for WorkoutPlan, Room setup for SetLog done)
    [X] Implement actual saving/loading of SetLog data (Saving done, loading/display needed) <- Note: Loading/Display is done via History screen
[X] Display historical performance for exercises.

Calculators:
[X] UI for BMI Calculator.
[X] UI for Calorie (TDEE) Calculator.
[X] UI for Body Fat Calculator.

UI/UX:
[X] Design a modern and beautiful UI using Jetpack Compose.
    [X] Applied custom Theme (Colors, Typography).
    [X] Refined WorkoutPlanScreen (Display Cards, Input Area).
    [X] Refined WorkoutExecutionScreen (Layout, Timer, Controls).
    [X] Refined ProgressHubScreen and ExerciseHistoryScreen (List items).
    [X] Refine Calculator Screens UI.
    [X] Enhanced workout execution flow with progress indicators and summary stats.
    [X] Implemented consistent design language across all screens.
    [X] Created reusable UI components for common elements (ErrorCard, LoadingSpinner).
    [X] Added visual feedback for user actions and state changes.
[X] Incorporate smooth animations and transitions.
[X] Create navigation between plan viewing, workout execution, and progress tracking. (Initial Setup Done)
[X] Create navigation for accessing the calculators. (Initial Setup Done)
[X] Refine Navigation (e.g., Top App Bar titles, back navigation)
[X] Make the UI/UX more stylish and modern with cool animations and transitions.

Optimizations & Enhancements:
[X] Fix compile errors and crashes in workout execution flow.
[X] Optimize workout execution performance for smoother transitions.
[X] Fix body part image loading in workout plan display.
  [X] Updated MuscleGroup enum to exactly match asset filenames
  [X] Simplified image loading logic in BodyPartImage component
  [X] Added better fallback mechanisms and error handling
  [X] Improved muscle group parsing with additional variations
  [X] Implemented split-view for displaying multiple muscle groups
  [X] Added proper aspect ratio handling to prevent image stretching
[ ] Implement comprehensive error handling across the app.
[ ] Add accessibility features for better inclusivity.

Workout Plan Display Enhancements:
[X] Redesign workout plan display to be more stylish and user-friendly
[X] Implement weekday-based workout display showing what's scheduled for current day
[X] Show estimated session duration prominently for each workout day
[X] Add visual indicators for current day's workout ("Today's Workout")
[X] Display muscle-specific images based on workout focus:
    [X] Show primary muscle group image (chest, back, legs, etc.)
    [X] Support combined muscle groups (e.g., chest and arms) by showing primary muscle
    [X] Use images from app/src/main/assets/body_images/ for muscle visualization
    [X] Fixed image loading to properly handle all muscle group types
[X] Add workout day swapping functionality:
    [X] Allow users to switch between two workout days (e.g., Monday/Wednesday)
    [X] Maintain workout plan integrity while allowing flexible scheduling
    [X] Update UI to reflect changes immediately after swapping
[X] Improve navigation between workout days with intuitive controls

Potential Future Features (Added as possibilities):
[ ] Add food nutrition tracking feature for daily and monthly progress with calorie, protein, carb, and fat intake goals displayed as graphs.
[ ] Add a dashboard that displays the current day's food progress and workout schedule with a sleek, modern design.
[ ] Enhance workout summary screen after completion with more detailed analytics.
[ ] Implement advanced charts/graphs for progress visualization.
[ ] Create ability to edit workout plans within the app.
[ ] Add user profiles and settings.
[ ] Implement backup/restore functionality for plans and logs.
[ ] Add exercise database search/filtering.
[ ] Add customizable timer sounds/notifications.
[ ] Implement dark/light theme toggle and additional theme options.