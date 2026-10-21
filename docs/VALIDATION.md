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


# Version 1.1 development validation

New features: TV-input suppression, position/layout/theme controls, custom/hidden text label,
full/value-only/hidden numbers, size/duration controls, a full-screen overlay test, and a reversible hidden launcher alias. Settings include an
explicitly labelled sample preview that uses the same renderer as the real overlay.

Local checks completed:

- Ten JVM tests passed, including input suppression, popup immunity, custom-label validation and
  all number formats (also while muted).
- Five connected Android tests passed on an isolated emulator with a 1920×1080 TV device profile
  and an Android 37.1 Google APIs phone system image. This is an app/UI check, not TV firmware or ARC emulation.
- Rendered 216 combinations (3 sizes × 2 layouts × 3 themes × 3 number formats × label on/off × mute on/off)
  without a draw failure; checked text visibility and displayed numbers.
- Disabling the launcher alias removes the leanback launcher resolver; explicit setup still opens,
  and its settings checkbox restores the launcher entry.
- The real accessibility overlay test uses the settings button, checks its window type/title,
  confirms the five-second selection outlasts one second and the one-second selection disappears,
  and checks that media volume and mute state are unchanged. Original accessibility settings are restored.
- The settings preview fits its column while the full-screen test retains the chosen actual size.
- Installed service metadata exposes the setup activity, with capability mask zero, no key filter
  and no screen-content retrieval. The package filter is intentionally removed for activity metadata.
- Release unit tests, lint (zero errors; existing target-SDK warning only), signed release build and
  debug build passed. D-pad selection of vertical layout and the actual settings preview were inspected.

The TV was switched off during implementation. No new HDMI-CEC, HDMI-input switching, Netflix,
held-key, reboot persistence, or physical launcher behavior has been verified for this version.
The physical-device evidence above applies to **1.0.0 only**. Do not treat the development version
as a stable release until those device checks pass.

Pending physical TV checks:

- Discover the TV input viewer, hide on AVR HDMI input, restore in Home/Netflix, and check the
  suppression toggle. Popups and the overlay itself must not clear the detected input screen.
- Check all six screen positions, horizontal/vertical layouts and themes with the TV remote.
- Check custom/hidden labels and all three number formats, including mute and held volume keys.
- Hide the launcher icon; reopen through Accessibility → Settings and restore it. Verify after reboot.
- Recheck no key filtering, non-touchable/non-focusable overlay windows, and each selected timeout (1/2/3/5 seconds), including the explicitly labelled test overlay.
