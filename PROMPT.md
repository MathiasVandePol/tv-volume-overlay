# Install TV Volume Overlay with a coding assistant

Copy the prompt below into a coding assistant with shell access. Fill in what you know;
the assistant can help you find the rest. You will need your TV remote to approve debugging
and enable the accessibility service.

```text
Help me install TV Volume Overlay on my Android TV / Google TV.

Official project: https://github.com/MathiasVandePol/tv-volume-overlay
TV brand and model: [fill in, or ask me]
Android version: [if known]
Receiver or soundbar: [fill in, or ask me]
Audio connection: [HDMI ARC/eARC, or ask me]
TV IP address and debugging port: [if known]
Computer OS: [if known]

Read the official README and PROMPT.md first. Install the latest stable release from this
repository, preserving my existing apps, launcher, accessibility services and audio settings.
Do not run a TV debloating script or replace my launcher.

1. Check compatibility before installing. The app needs Android 13 or newer and an active
   HDMI ARC/eARC media-audio route to a receiver/soundbar whose volume is reported to Android.
   Explain that optical, Bluetooth, TV speakers and a TV box's ordinary HDMI output are not
   supported. Ask for missing essential details together. Do not promise compatibility based
   only on an HDMI cable being connected.

2. Check whether Android SDK Platform-Tools are already installed. If needed, help me install
   them from Google's official site for my OS. Explain how to enable Developer options:
   Settings > System > About, or Device Preferences > About, then press Android TV OS build /
   Build seven times. Return to Developer options and enable USB/ADB debugging and, if offered,
   network debugging. Menu names differ by TV. Have me accept the TV's RSA authorization prompt.
   Do not assume the TV's USB accessory ports support an ADB cable.

3. Connect only to the TV I identify. Use its displayed network-debugging address and port;
   port 5555 is common but not universal. For Wireless debugging, use adb pair with the pairing
   port/code and adb connect with the separate connection port shown on the TV. Verify status
   device with adb devices. If unauthorized, guide me to approve the prompt. Use adb -s SERIAL
   for every device command so another phone, emulator or TV is never modified. Never expose
   ADB through router port forwarding or disable ADB authentication.

4. Resolve the latest non-draft, non-prerelease release from the official GitHub repository.
   Download its TV-Volume-Overlay-vVERSION.apk and SHA256SUMS from that same release over HTTPS.
   Verify the APK against SHA256SUMS; stop on a mismatch. Where Android build tools are available,
   also verify the APK signature and the dedicated release certificate SHA-256 fingerprint:
   50eb34a8ade765d49376b64c83d34a6f55287c43d7a6b4f88e3a37cd868981c2.
   Do not use third-party APK mirrors, development artifacts or a newly signed local build.

5. Check whether be.mathiasvandepol.tvvolume is already installed, then install/update with
   adb -s SERIAL install -r APK_PATH. Preserve its data. If this TV's installer requires a
   separate step, push the verified APK to /data/local/tmp, run pm install -r in a separate
   shell call, check for Success and remove the temporary file. If there is a signing-key
   conflict, explain it and ask before uninstalling anything; do not clear data automatically.
   Stop if installation fails.

6. Open the setup screen with:
   adb -s SERIAL shell am start -n be.mathiasvandepol.tvvolume/.MainActivity
   Guide me to Open Accessibility settings > TV Volume Overlay > Enable. If Android blocks a
   sideloaded service, help me locate Allow restricted settings in the app's system-info page
   where available. Let me enable the service with the remote; do not overwrite the list of
   enabled services. The app needs no Internet permission or separate Draw over other apps grant.
   Explain that Accessibility provides the overlay window and activity metadata for optional
   TV-input hiding, without reading screen content or intercepting keys.

7. Open Appearance and settings. Help me choose position, orientation, theme, size, transparency,
   duration, label and number format. Use Test overlay on screen first: it shows labelled sample
   data and never changes audio. Then have me test real volume up/down, mute and briefly holding
   a volume button with my remote, including inside a streaming app. Ask whether the panel
   appears and disappears normally. Never inject a long burst of volume-up commands.
   The sample can work without ARC; do not treat it as proof of live receiver feedback.

8. Explain Hide on TV input screens: it avoids a duplicate receiver bar on detected input-viewer
   apps, depends on firmware and may also hide on live TV. If I want to hide the launcher icon,
   first explain the recovery path: TV Settings > Accessibility > TV Volume Overlay > Settings
   where supported, or the explicit MainActivity command above. Restore the icon with:
   adb -s SERIAL shell pm enable be.mathiasvandepol.tvvolume/.LauncherEntry
   Do not hide the icon unless I choose to.

9. Explain that volume numbers come from Android/CEC as whole numbers. They may differ from
   the receiver's own scale and cannot reproduce half steps or dB values. Do not invent offsets
   or decimals, change HDMI-CEC/audio settings, enable key filtering or add receiver-network
   integrations to compensate. If live feedback fails, inspect only relevant diagnostics and
   report the actual limitation; do not claim the app works just because installation succeeded.

10. When testing is finished, guide me to turn USB/network/wireless debugging off, revoke USB
    debugging authorizations and remove the wireless pairing if no longer needed. The app works
    without debugging. For network ADB, confirm the debugging port refuses a connection when
    possible; a timeout or No route to host is inconclusive. Do not publish my IP, pairing code,
    device logs or screenshots containing personal information.

Finish with the installed version, checks actually completed, any unresolved compatibility issue,
and how to disable the app in Accessibility or uninstall it. Do the installation work rather than
only providing a plan, but pause for physical TV approvals or missing connection details as needed.
```
