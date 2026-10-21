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
