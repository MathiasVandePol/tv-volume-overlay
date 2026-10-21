# TV Volume Overlay

A small, free volume display for Android TV apps when sound goes to an HDMI ARC/eARC receiver.

Some TVs send volume commands to the receiver but show no volume bar inside Netflix or other
built-in apps. TV Volume Overlay displays the level Android receives back from the audio system.
It works with your existing launcher.

[**Download the latest APK**](https://github.com/MathiasVandePol/tv-volume-overlay/releases/latest)

![TV Volume Overlay on an actual Android TV](docs/images/tv-volume-overlay.png)

## Requirements

- Android TV / Google TV running **Android 13 or newer**.
- Media audio routed through **HDMI ARC or eARC**.
- A receiver/soundbar and TV firmware that expose the current volume through Android's audio APIs.

Tested on Xiaomi **MiTV-MOEU0**, Android 14, with a Denon **AVR-X3400H**.
Other models are welcome to try it; compatibility depends on the TV firmware and receiver.
This version does not display volume for optical, Bluetooth, TV speakers or a TV box's ordinary HDMI output.

## Install

1. Download the APK from [Releases](https://github.com/MathiasVandePol/tv-volume-overlay/releases/latest) and install it on the TV.
2. Open **TV Volume Overlay** from the TV's app list.
3. Select **Open Accessibility settings**, then enable **TV Volume Overlay**.
4. Use your TV remote's volume buttons, including while watching a video.

The small **Volume** panel appears near the upper-right corner and disappears about two seconds
after the last volume change. Muting displays **Muted**. English and Dutch interfaces are included.
The number uses Android's reported range, such as `21 / 100`; it is not a dB reading.

To sideload with ADB, after connecting your TV:

```sh
adb install -r TV-Volume-Overlay-v1.0.0.apk
```

If an Android TV 14 installer waits for an on-screen confirmation, accept it on the TV, or push
the APK and install it through a separate shell call:

```sh
adb push TV-Volume-Overlay-v1.0.0.apk /data/local/tmp/tv-volume-overlay.apk
adb shell pm install -r /data/local/tmp/tv-volume-overlay.apk
adb shell rm /data/local/tmp/tv-volume-overlay.apk
```

If Android blocks enabling a sideloaded accessibility service, look in the app's information page
for **Allow restricted settings**. Availability and wording differ between TV firmware versions.
After sideloading, switch network/USB debugging off again; the app does not need ADB to keep working.

## Why Accessibility?

Android permits an enabled accessibility service to host a window above other apps. This service
uses that window type only to show the volume. It cannot read screen content and does not request
key-event filtering. Volume long-presses remain handled by Android and HDMI-CEC.

The app:

- Has **no Internet permission**, ads, analytics, accounts or runtime third-party libraries.
- Does not change volume, intercept remote keys or request audio focus.
- Reacts to Android volume/mute notifications; it does not poll the receiver or network.
- Checks the active media route before showing a panel; merely having an HDMI cable connected is not enough.
- Removes the panel after about 1.9 seconds, and when its service stops.

## Turn it off

Disable **TV Volume Overlay** in the TV's Accessibility settings, or uninstall the app.
No launcher, HDMI-CEC or audio settings are changed by the app.

## Build

Install JDK 17 and Android SDK 35, then run:

```sh
./gradlew testReleaseUnitTest lintRelease assembleDebug
```

The debug APK is in `app/build/outputs/apk/debug/`. A debug build uses a different signing key from
the published release; uninstall one before installing the other.

For a signed release, copy `keystore.properties.example` to `keystore.properties`, fill in your
own keystore details and run `./gradlew assembleRelease`. Keystores and local properties are ignored
by Git. Release maintainers can alternatively supply the four `ANDROID_*` environment variables
listed in the release workflow.

Pushing a `vMAJOR.MINOR.PATCH` tag runs the signed release workflow after its tests and lint pass.
It publishes the APK and `SHA256SUMS`. The repository uses a dedicated release key stored as GitHub
Actions secrets; never commit signing keys or passwords. See [CONTRIBUTING.md](CONTRIBUTING.md).

## Troubleshooting

**No panel:** verify that the service is enabled, audio is actually routed to ARC/eARC, and the
receiver reports its volume. Some firmware does not expose those reports to normal apps.

**Panel visible on Home but not over video:** protected playback or firmware window rules may
restrict overlays. Please report the TV, Android version, receiver and affected app. On the tested
MiTV/Denon setup the panel was confirmed visible during Netflix playback with the real remote.

**Two bars when using an AVR HDMI input:** the receiver may draw its own volume display on its
video output while this app also shows a panel. Automatic hiding on TV input screens is planned;
version 1.0.0 does not yet suppress that case. Disable this app temporarily if you prefer the
receiver's own display on those inputs.

**Two bars inside Android apps:** disable another overlay or an integrated overlay in your launcher.

**Volume changes only a few steps when held:** this app deliberately requests no key filtering.
Check whether another enabled accessibility/remapping service requests key filtering. On the tested
TV, removing key filtering restored continuous volume changes.

## Planned

- Automatically hide the overlay on TV input screens to avoid a duplicate AVR volume bar.
- Configurable screen position.
- Horizontal/vertical layouts and selectable themes.

These options are not included in version 1.0.0 yet.

## License

[MIT](LICENSE). Contributions and compatibility reports are welcome. See [NOTICE.md](NOTICE.md)
for the project's origins and Gradle attribution.
