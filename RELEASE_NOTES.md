TV Volume Overlay 1.1.0 adds appearance controls and optional automatic hiding on TV input screens.

Available in English and Dutch; the app follows your TV language and falls back to English.

- Hide the overlay on detected TV-input viewer screens to avoid the receiver’s duplicate bar.
- Choose six positions, horizontal/vertical layout and dark/light/black themes.
- Customize or hide the text label; show value + maximum, value only, or no number.
- Adjust background transparency from opaque to fully transparent, keeping text and bar visible.
- Choose small/normal/large size and a 1/2/3/5-second display duration.
- Preview locally or test the actual overlay on screen with a clear sample badge and no audio changes.
- Consistent setup buttons, a slimmer vertical panel and live service-status updates.
- Hide the launcher icon while retaining the service and an explicit setup/settings entry.
- Keep remote keys and audio untouched; use only activity metadata for input detection.

Input detection depends on firmware and can also hide on live TV using the same viewer app.
Settings, Netflix playback, mute, held volume and dismissal were checked on a Xiaomi MiTV-MOEU0
with Android 14 and a Denon AVR-X3400H. Other TV/receiver combinations are not verified.
See [validation details](https://github.com/MathiasVandePol/tv-volume-overlay/blob/main/docs/VALIDATION.md)
for test coverage and remaining device checks.

The README includes an English guided-installation prompt for coding assistants.

Android/CEC values are whole numbers and may differ from the receiver’s own scale; half steps are not available.
