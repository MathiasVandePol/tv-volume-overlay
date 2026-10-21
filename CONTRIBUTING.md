# Contributing

Please include the TV model, Android version, receiver/soundbar, ARC/eARC connection and affected
app in compatibility reports. Do not post credentials, network addresses, full device logs or
screenshots containing personal information.

Run `./gradlew testReleaseUnitTest lintRelease assembleDebug` before submitting a change.
Volume feedback must remain read-only: no key interception, screen-content retrieval, audio-focus
requests or volume adjustment. Use the public AudioManager routing API and show the reported scale.
Keep UI lightweight and usable with a D-pad. Validate changes on an actual TV where possible;
unit tests alone do not establish HDMI-CEC or protected-video compatibility.

`./gradlew connectedDebugAndroidTest` runs renderer/settings checks on a connected Android 13+
emulator or test device. These include all appearance combinations, launcher icon recovery, and
accessibility capabilities. The full-window test temporarily enables this app’s service on the
test device via instrumentation shell permissions and restores the original accessibility settings.
It checks the real overlay window, timeout and unchanged media volume/mute state. Foreground detection may use activity package/class metadata only;
never retrieve event sources, node trees or screen text. Third-party test libraries are test-only;
the app itself has no runtime library dependencies.

## Releases

Maintain a dedicated Android release keystore outside the repository and configure these GitHub
Actions secrets:

- `ANDROID_KEYSTORE_BASE64`: base64-encoded keystore, without line breaks.
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

The release workflow materializes the keystore only in the runner's temporary directory and removes
it afterwards. A `vMAJOR.MINOR.PATCH` tag sets versionName and versionCode; minor/patch must be below
100. Update release notes and test the signed candidate on a TV before pushing the tag.
Verify the published APK's checksum and certificate when checking a release.
