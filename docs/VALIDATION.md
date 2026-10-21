# Version 1.0.0 validation

Tested on a physical Xiaomi MiTV-MOEU0, Android 14, with a Denon AVR-X3400H over HDMI 2 ARC/eARC.

- Local signed release build, three unit tests and Android lint passed (zero lint errors).
- Standalone accessibility service enabled and bound; key-filter capability is zero and Android's
  InputFilterEnabled remains false.
- Confirmed one overlay window, owned by this standalone package, above its setup screen and the
  original Google TV home screen with the custom launcher helper temporarily disabled.
- Overlay is non-focusable and non-touchable, and removes itself after about two seconds.
- The user confirmed the standalone app works during Netflix playback, with mute and held volume keys.
- The integrated overlay was removed from the user's custom launcher to prevent duplicate app overlays.
- A real TV screenshot of this standalone app is included in the README.

Known issue: the receiver can also display its own OSD when its HDMI input is viewed, causing two
bars. That case is not suppressed in 1.0.0. Position/layout/theme options have not been implemented.
Other TV/receiver combinations have not been verified.


# Version 1.1.0 validation

New features: TV-input suppression, position/layout/theme controls, custom/hidden text label,
full/value-only/hidden numbers, size/duration controls, a full-screen overlay test, and a reversible hidden launcher alias. Settings include an
explicitly labelled sample preview that uses the same renderer as the real overlay.

Local checks completed:

- Eleven JVM tests passed, including input suppression, popup immunity, custom-label validation and
  all number formats (also while muted).
- Six connected Android tests passed on an isolated emulator with a 1920×1080 TV device profile
  and an Android 37.1 Google APIs phone system image. This is an app/UI check, not TV firmware or ARC emulation.
- Rendered 216 combinations (3 sizes × 2 layouts × 3 themes × 3 number formats × label on/off × mute on/off)
  without a draw failure; checked text visibility and displayed numbers.
- Disabling the launcher alias removes the leanback launcher resolver; explicit setup still opens,
  and its settings checkbox restores the launcher entry.
- The real accessibility overlay test uses the settings button, checks its window type/title,
  confirms the five-second selection outlasts one second and the one-second selection disappears,
  and checks that media volume and mute state are unchanged. Original accessibility settings are restored.
- Transparency is checked at 0%, 50% and 100% in each theme using actual rendered pixels; text/bar remain visible.
- The settings preview fits its column while the full-screen test retains the chosen actual size.
- Installed service metadata exposes the setup activity, with capability mask zero, no key filter
  and no screen-content retrieval. The package filter is intentionally removed for activity metadata.
- Release unit tests, lint (zero errors; existing target-SDK warning only), signed release build and
  debug build passed. D-pad selection of vertical layout and the actual settings preview were inspected.

Physical TV checks completed on 21 October 2026:

- Installed the signed 1.1.0-dev candidate over 1.0.0 on the Xiaomi/Denon setup. The accessibility
  service remains bound with capability mask zero, alongside the existing launcher helper.
- The actual sample window appears and disappears at the default timeout. Actual reported volume
  was also observed in the app-owned accessibility overlay above its own setup/settings screens.
- Discovered the TV-input viewer. During four checks on the AVR HDMI input, Android received
  volume updates while no app overlay window appeared. This is evidence for suppression on this TV.
- The user confirmed that position, orientation, theme, transparency, text/number settings, preview
  and the test button work with the real remote.
- The user confirmed visibility during Netflix video playback, mute, held volume and automatic dismissal.
- Matched both setup buttons, reduced vertical side padding from 18dp to 8dp, and made vertical
  width depend on visible text/number choices (104dp full scale, 80dp label/value only, 48dp bar only,
  before the chosen size factor). The latest UI was visually checked on the TV.
- Fixed the setup status staying stale after an APK update: it now follows accessibility-service
  state changes. The existing connected window test now also covers service binding while setup stays open.
- Re-ran all 11 JVM and 6 isolated emulator tests, release lint and signed release build after the UI changes.
- Both README screenshots are fresh, unedited captures of this version on the physical TV.

Device checks not yet completed (release limitations):

- Hide the launcher icon, reopen through this TV’s Accessibility settings and restore it; verify after reboot.
- Check preferences/service behavior after reboot and repeated input/route changes.
- A controlled on/off comparison for the reported audible volume-control latency has not been completed.
  The settings/Netflix confirmations above do not establish that this latency issue is fixed.

Android/CEC precision limitation: the user reported a difference between the overlay and AVR display,
and confirmed that the AVR supports half steps. Local evidence shows integer CEC reports copied into
Android’s integer media-volume state, but contains no simultaneous front-panel reading to determine
this particular discrepancy. The user chose to retain Android/CEC without a network integration.
The settings now clarify this limitation; no guessed calibration/decimal precision is introduced.
