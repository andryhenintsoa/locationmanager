# locationmanager

<b>With this library you just need to provide a Configuration object with your requirements, and you will receive a location or a fail reason.</b>
 
This library requires quite a lot of lifecycle information to handle all the steps between onCreate - onResume - onPause - onDestroy - onActivityResult - onRequestPermissionsResult.
You can simply use one of [LocationBaseActivity][2], [LocationBaseFragment][3], [LocationBaseService][4] or you can manually call those methods as required.

[See the sample application][5] for detailed usage!

## Configuration

All those settings below are optional. Use only those you really want to customize. Please do not copy-paste this configuration directly. If you want to use pre-defined configurations, see [Configurations][6].

```kotlin 
val configuration = LocationConfiguration.Builder()
            .keepTracking(false)
            .askForPermission(
                PermissionConfiguration.Builder()
                    .rationaleMessage("Gimme the permission!")
                    .build()
            )
            .useGooglePlayServices(
                GooglePlayServicesConfiguration.Builder()
                    .locationRequest(
                        LocationRequest.create()
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setInterval(10 * 1000)
                            .setFastestInterval(5 * 1000)
                    )
                    .fallbackToDefault(true)
                    .askForGooglePlayServices(false)
                    .askForSettingsApi(true)
                    .failOnSettingsApiSuspended(false)
                    .ignoreLastKnowLocation(true)
                    .setWaitPeriod(10 * 1000)
                    .build()
            )
            .useDefaultProviders(
                DefaultProviderConfiguration.Builder()
                    .requiredTimeInterval((1 * 60 * 1000).toLong())
                    .requiredDistanceInterval(0)
                    .acceptableAccuracy(1.0f)
                    .acceptableTimePeriod((1 * 60 * 1000).toLong())
                    .gpsMessage("Turn on GPS?")
                    .setWaitPeriod(ProviderType.GPS, (10 * 1000).toLong())
                    .setWaitPeriod(ProviderType.NETWORK, (10 * 1000).toLong())
                    .build()
            )
            .build()
```

Library is modular enough to let you create your own way for Permission request, Dialog display, or even a whole LocationProvider process. (Custom LocationProvider implementation is described below in LocationManager section)

You can create your own [PermissionProvider][7] implementation and simply set it to [PermissionConfiguration][8], and then library will use your implementation. Your custom PermissionProvider implementation will receive your configuration requirements from PermissionConfiguration object once it's built. If you don't specify any PermissionProvider to PermissionConfiguration [DefaultPermissionProvider][9] will be used. If you don't specify PermissionConfiguration to LocationConfiguration [StubPermissionProvider][10] will be used instead.

You can create your own [DialogProvider][11] implementation to display `rationale message` or `gps request message` to user, and simply set them to required configuration objects. If you don't specify any [SimpleMessageDialogProvider][12] will be used as default.

## LocationManager

Ok, we have our configuration object up to requirements, now we need a manager configured with it.

```kotlin
// LocationManager MUST be initialized with Application context in order to prevent MemoryLeaks
      LocationManager locationManager = LocationManager.Builder(applicationContext)
            .configuration(locationConfiguration)
            .activity(this)
            .notify(this)
            .build()
```

LocationManager doesn't keep strong reference of your activity **OR** fragment in order not to cause any memory leak. They are required to ask for permission and/or GoogleApi - SettingsApi in case they need to be resolved.

You can create your own [LocationProvider][13] implementation and ask library to use it. If you don't set any, library will use [DispatcherLocationProvider][14], which will do all the stuff is described above, as default.

Enough, gimme the location now!

```kotlin
locationManager.get();
```

Done! Enjoy :)

## Logging

Library has a lot of log implemented, in order to make tracking the process easy, you can simply enable or disable it.
It is highly recommended to disable in release mode.

```kotlin 
LocationManager.enableLog(false);
```

For a more fine tuned logging, you can provide a custom Logger implementation to filter and delegate logs as you need it.

```kotlin
Logger myCustomLoggerImplementation = MyCustomLoggerImplementation();
LocationManager.setLogger(myCustomLoggerImplementation);
```

## Restrictions
If you are using LocationManager in a
- Fragment, you need to redirect your `onActivityResult` to fragment manually, because GooglePlayServices Api and SettingsApi calls `startActivityForResult` from activity. For the sample implementation please see [SampleFragmentActivity][15].
- Service, you need to have the permission already otherwise library will fail immediately with PermissionDenied error type. Because runtime permissions can be asked only from a fragment or an activity, not from a context. For the sample implementation please see [SampleService][16].

## AndroidManifest

Library requires 3 permission;
 - 2 of them `ACCESS_NETWORK_STATE` and `INTERNET` are not in `Dangerous Permissions` and they are required in order to use Network Provider. So if your configuration doesn't require them, you don't need to define them, otherwise they need to be defined.
 - The other one is `ACCESS_FINE_LOCATION` and it is marked as `Dangerous Permissions`, so you need to define it in Manifest and library will ask runtime permission for that if the application is running on Android M or higher OS  version. If you don't specify in Manifest, library will fail immediately with PermissionDenied when location is required.

```html
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />

<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

You might also need to consider information below from [the location guide page.][17]

<blockquote>
<b>Caution:</b> If your app targets Android 5.0 (API level 21) or higher, you must declare that your app uses the android.hardware.location.network or android.hardware.location.gps hardware feature in the manifest file, depending on whether your app receives location updates from NETWORK_PROVIDER or from GPS_PROVIDER. If your app receives location information from either of these location provider sources, you need to declare that the app uses these hardware features in your app manifest. On devices running versions prior to Android 5.0 (API 21), requesting the ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION permission includes an implied request for location hardware features. However, requesting those permissions does not automatically request location hardware features on Android 5.0 (API level 21) and higher.
</blockquote>


[1]: https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderClient
[2]: https://github.com/andryhenintsoa/locationmanager/tree/main/locationmanager/src/main/java/mg/henkinn/locationmanager/base/LocationBaseActivity.java
[3]: https://github.com/andryhenintsoa/locationmanager/tree/main/locationmanager/src/main/java/mg/henkinn/locationmanager/base/LocationBaseFragment.java
[4]: https://github.com/andryhenintsoa/locationmanager/tree/main/locationmanager/src/main/java/mg/henkinn/locationmanager/base/LocationBaseService.java
[5]: https://github.com/andryhenintsoa/locationmanager/tree/main/app
[6]: https://github.com/andryhenintsoa/locationmanager/tree/main/locationmanager/src/main/java/mg/henkinn/locationmanager/configuration/Configurations.java
[7]: https://github.com/andryhenintsoa/locationmanager/tree/main/locationmanager/src/main/java/mg/henkinn/locationmanager/providers/permissionprovider/PermissionProvider.java
[8]: https://github.com/andryhenintsoa/locationmanager/tree/main/locationmanager/src/main/java/mg/henkinn/locationmanager/configuration/PermissionConfiguration.java
[9]: https://github.com/andryhenintsoa/locationmanager/tree/main/locationmanager/src/main/java/mg/henkinn/locationmanager/providers/permissionprovider/DefaultPermissionProvider.java
[10]: https://github.com/andryhenintsoa/locationmanager/tree/main/locationmanager/src/main/java/mg/henkinn/locationmanager/providers/permissionprovider/StubPermissionProvider.java
[11]: https://github.com/andryhenintsoa/locationmanager/tree/main/locationmanager/src/main/java/mg/henkinn/locationmanager/providers/dialogprovider/DialogProvider.java
[12]: https://github.com/andryhenintsoa/locationmanager/tree/main/locationmanager/src/main/java/mg/henkinn/locationmanager/providers/dialogprovider/SimpleMessageDialogProvider.java
[13]: https://github.com/andryhenintsoa/locationmanager/tree/main/locationmanager/src/main/java/mg/henkinn/locationmanager/providers/locationprovider/LocationProvider.java
[14]: https://github.com/andryhenintsoa/locationmanager/tree/main/locationmanager/src/main/java/mg/henkinn/locationmanager/providers/locationprovider/DispatcherLocationProvider.java
[15]: https://github.com/andryhenintsoa/locationmanager/tree/main/app/src/main/java/mg/sparks/eqworks_kotlin/fragment/SampleFragmentActivity.java
[16]: https://github.com/andryhenintsoa/locationmanager/tree/main/app/src/main/java/mg/sparks/eqworks_kotlin/service/SampleService.java
[17]:https://developer.android.com/guide/topics/location/strategies.html
