# Run Tracker

# Project Overview and Architecture

MVVM Architecture, Navigation components, Room database with Coroutines and Dependency Injection with Dagger.

Architecture Overview: we will only use a single Activity with 5 fragments (single activity development).

TrackingService if for tracking the run with the app not displayed, it will be a foreground service.

MainViewModel: the view model of the Main Activity.

StatisticsViewModel: for separating the statistics state, because this data will only be used in this screen.

![architecture.png](doc/architecture.png)

# Project prerequisites

Migration from extensions to jetpack view binding: [https://developer.android.com/topic/libraries/view-binding/migration](https://developer.android.com/topic/libraries/view-binding/migration)
Migration from kapt to ksp: https://developer.android.com/build/migrate-to-ksp
Add safe args dependency: https://developer.android.com/guide/navigation/use-graph/safe-args

Added permissions in AndroidManifest.xml and also changed MainActivity launch mode to singleTask which means that only a single task of the activity will exist at a time.
Also added metadata needed Google Maps. Get your own API key from Google Cloud Console (https://developers.google.com/maps/documentation/android-sdk/get-api-key).
Get SHA-1 app fingerprint: https://stackoverflow.com/questions/27609442/how-to-get-the-sha-1-fingerprint-certificate-in-android-studio-for-debug-mode