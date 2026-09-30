# Changelog

This project adheres to [Semantic
Versioning](https://semver.org/spec/v2.0.0.html).

## Release 1.0 (2019-12-31)

Initial public release. SimpleCaptcha source was imported and tidied
up, including: Javadoc comments, visibility tightening, API pruning.


## Release 1.1 (2020-01-26)

### Added
- New `FastWordRenderer` can render image CAPTCHAs about 5X faster
  (with a reduction in configurability).

### Changed
- Several speed improvements to `ImageCaptcha` and
  `DefaultWordRenderer`.
- Minor improvements to documentation, including `README.md` and
  Javadocs.
- Minor code improvements suggested by PMD, FindBugs, SpotBugs and
  Checkstyle.
- Substitutes `Random` for `SecureRandom`.

### Fixed
- `ImageCaptcha.isCorrect()` returns `false` for a `null`
  argument. [#1](https://github.com/logicsquad/nanocaptcha/issues/1)


## Release 1.2 (2021-02-14)

### Changed
- Removed dependency on `com.jhlabs.filters`.
  [#4](https://github.com/logicsquad/nanocaptcha/issues/4)

### Security
- Updated JUnit 4.12 → 4.13.1.
  [#2](https://github.com/logicsquad/nanocaptcha/issues/2)


## Release 1.3 (2022-10-05)

### Added
- Added logging through SLF4J to `Sample`, with a dependency on the
  Log4j 2 binding, `log4j-slf4j-impl` 2.18.0.

### Fixed
- Inserted a `BufferedInputStream` into the `Sample(InputStream)`
  constructor to allow audio to be played from resources in the
  JAR. [#6](https://github.com/logicsquad/nanocaptcha/issues/6)


## Release 1.4 (2023-03-13)

### Added
- Improved support for alternate languages, and added German digit
  samples for audio
  CAPTCHAs. [#7](https://github.com/logicsquad/nanocaptcha/issues/7)
- Added support in `Builder` classes for setting content
  length. [#9](https://github.com/logicsquad/nanocaptcha/issues/9)
- Added support for randomising the y-offset in image CAPTCHAs, which
  improves variability in "tall"
  images. [#13](https://github.com/logicsquad/nanocaptcha/issues/13)

### Changed
- Replaced the dependency on `log4j-slf4j-impl` with `slf4j-api`
  2.0.6, so applications choose their own SLF4J
  binding. [#10](https://github.com/logicsquad/nanocaptcha/issues/10)


## Release 1.5 (2023-03-22)

### Fixed
- `WordRenderer` implementations now use built-in fonts by default: we
  were making assumptions about font availability that were rarely
  true. Ships with "Courier Prime" and "Public
  Sans". [#14](https://github.com/logicsquad/nanocaptcha/issues/14)
- `FastWordRenderer` was initialising static variables in its
  constructor. This has been moved out to a static
  block. [#15](https://github.com/logicsquad/nanocaptcha/issues/15)


## Release 2.0 (2023-12-27)

### Added
- Improved colour support in `WordRenderer`
  implementations. `DefaultWordRenderer` loses the deprecated
  `DefaultWordRenderer(List<Color> colors, List<Font> fonts)`
  constructor, and colours are now handled by additions to its
  `Builder` (via `AbstractWordRenderer.Builder`). `FastWordRenderer`
  benefits in the same way, and it is no longer restricted to a single
  colour. Colour options can now be supplied by the `Builder`'s
  `color()` and `randomColor()`
  methods. [#18](https://github.com/logicsquad/nanocaptcha/issues/18)
- Added two new `NoiseProducer` implementations:
  `GaussianNoiseProducer` and
  `SaltAndPepperNoiseProducer`. [#19](https://github.com/logicsquad/nanocaptcha/issues/19)
- Added new `create()` static factory method in both `ImageCaptcha`
  and `AudioCaptcha` to make the simplest case even
  simpler. [#12](https://github.com/logicsquad/nanocaptcha/issues/12)
- Added an SLF4J implementation for unit tests to
  use. [#20](https://github.com/logicsquad/nanocaptcha/issues/20)

### Changed
- Removed deprecated constructors in `RandomNumberVoiceProducer`,
  `DefaultWordRenderer` and
  `FastWordRenderer`. [#11](https://github.com/logicsquad/nanocaptcha/issues/11)


## Release 2.1 (2024-01-04)

### Added
- Added custom font support via `AbstractWordRenderer.Builder`, with
  methods analogous to recent additions for `Color` support (in
  2.0). (Note that while `DefaultWordRenderer` will honour custom
  fonts set, `FastWordRenderer` uses only the two built-in fonts.)
  [#21](https://github.com/logicsquad/nanocaptcha/issues/21)

### Fixed
- Reverted the visibility reduction of `AbstractWordRenderer.Builder`
  to public. (The change in 2.0 effectively completely broke usage of
  the `Builder`s in both `WordRenderer` implementations!)
  [#22](https://github.com/logicsquad/nanocaptcha/issues/22)


## Release 2.2 (2026-09-30)

### Added
- Added French to the languages for audio
  CAPTCHAs. [#28](https://github.com/logicsquad/nanocaptcha/issues/28)
- Added an `Automatic-Module-Name` of `net.logicsquad.nanocaptcha` to
  the JAR
  manifest. [#34](https://github.com/logicsquad/nanocaptcha/issues/34)
- The JAR now includes `LICENSE` and `NOTICE` under
  `META-INF/`. [#36](https://github.com/logicsquad/nanocaptcha/issues/36)
- Added `toWav()` and `writeWav()` to `Sample`, for sending audio
  CAPTCHAs to a
  browser. [#40](https://github.com/logicsquad/nanocaptcha/issues/40)
- Added `toPng()`, `writePng()` and `toDataUri()` to `ImageCaptcha`,
  for sending image CAPTCHAs to a
  browser. [#45](https://github.com/logicsquad/nanocaptcha/issues/45)
- Added `Sample(URL)` and `RandomNoiseProducer(List<Sample>)`, so you
  can load your own audio through your own classes, which also works
  on the module
  path. [#50](https://github.com/logicsquad/nanocaptcha/issues/50)
- Added a README section on using NanoCaptcha in a web application,
  and a note on what a CAPTCHA like this can and can't
  do. [#54](https://github.com/logicsquad/nanocaptcha/issues/54)

### Changed
- Modernised the build: it compiles with `--release 8` on current
  JDKs, CI tests on Java 8, 11, 17, 21 and 25 and in slim containers,
  releases go through the Central Publisher Portal, and static
  analysis and site reports are
  gone. [#30](https://github.com/logicsquad/nanocaptcha/issues/30)
  [#31](https://github.com/logicsquad/nanocaptcha/issues/31)
  [#32](https://github.com/logicsquad/nanocaptcha/issues/32)
  [#33](https://github.com/logicsquad/nanocaptcha/issues/33)
- Regenerated all the spoken digits with the Piper text-to-speech
  engine, from voices whose licences allow redistribution, using
  `scripts/generate-audio.py`. There are now three English voices, two
  German and one French, all at the same loudness, and `NOTICE` says
  where they come
  from. [#35](https://github.com/logicsquad/nanocaptcha/issues/35)
  [#43](https://github.com/logicsquad/nanocaptcha/issues/43)
- Replaced the background noises from SimpleCaptcha, whose source was
  unknown, with generated babble, radio static and rain, 15 seconds
  each. `radio_tuning.wav`, `restaurant.wav`, `swimming.wav` and
  `zombie.wav` are
  gone. [#36](https://github.com/logicsquad/nanocaptcha/issues/36)
  [#51](https://github.com/logicsquad/nanocaptcha/issues/51)
- `DefaultWordRenderer` and `FastWordRenderer` now throw an
  `IllegalArgumentException` for a character their font can't
  display, instead of drawing an empty
  box. [#38](https://github.com/logicsquad/nanocaptcha/issues/38)
- An `ImageCaptcha.Builder` now makes a single CAPTCHA: adding content
  a second time, or calling any method after `build()`, throws an
  `IllegalStateException`. [#48](https://github.com/logicsquad/nanocaptcha/issues/48)
- Rendering, noise, filters and the audio's choice of voice and noise
  now use `ThreadLocalRandom`, so threads making CAPTCHAs at the same
  time don't contend for shared
  generators. [#49](https://github.com/logicsquad/nanocaptcha/issues/49)
- Minor code tidy-ups, with no change in
  behaviour. [#52](https://github.com/logicsquad/nanocaptcha/issues/52)
- Updated `slf4j-api` 2.0.9 →
  2.0.20. [#74](https://github.com/logicsquad/nanocaptcha/pull/74)

### Deprecated
- Deprecated `ChineseContentProducer` and `ArabicContentProducer`,
  which the built-in fonts can't display. They will be removed in
  3.0. [#38](https://github.com/logicsquad/nanocaptcha/issues/38)
- Deprecated `AbstractWordRenderer.RAND`, which NanoCaptcha's own
  renderers no longer use. It will be removed in
  3.0. [#49](https://github.com/logicsquad/nanocaptcha/issues/49)
- Deprecated `Sample(String)` and `RandomNoiseProducer(String[])`,
  which only find resources that NanoCaptcha's own class loader and
  module can see. They will be removed in
  3.0. [#50](https://github.com/logicsquad/nanocaptcha/issues/50)

### Fixed
- If the built-in fonts can't be loaded, `AbstractWordRenderer` now
  fails straight away with a message explaining why, instead of with
  a `NullPointerException` later. The README has a new section on
  running in
  containers. [#37](https://github.com/logicsquad/nanocaptcha/issues/37)
- `RandomNumberVoiceProducer(Locale)` now matches on the language, so
  regional locales such as `de-AT` and `fr-CA` get their language's
  voices instead of English. The docs no longer say that the JVM's
  default `Locale` is
  used. [#39](https://github.com/logicsquad/nanocaptcha/issues/39)
- `Sample` could only be read once, and never closed its stream. It
  now reads its audio once and can be read any number of times, and
  the built-in clips are
  cached. [#40](https://github.com/logicsquad/nanocaptcha/issues/40)
- Audio was decoded with the low byte of each sample sign-extended,
  so about half the samples came out 256 steps too
  low. [#41](https://github.com/logicsquad/nanocaptcha/issues/41)
- `Mixer` now clips mixed audio instead of letting it wrap to the
  opposite polarity, and repeats background noise that's shorter than
  the voice. [#42](https://github.com/logicsquad/nanocaptcha/issues/42)
- `FastWordRenderer` no longer throws
  `ArrayIndexOutOfBoundsException` after rendering about a billion
  characters. [#44](https://github.com/logicsquad/nanocaptcha/issues/44)
- `AudioCaptcha.isCorrect()` now returns `false` for a `null`
  argument, as `ImageCaptcha.isCorrect()`
  does. [#46](https://github.com/logicsquad/nanocaptcha/issues/46)
- `addBorder()` now draws the whole left edge on images taller than
  they are
  wide. [#47](https://github.com/logicsquad/nanocaptcha/issues/47)
- A missing audio resource now throws an `IllegalArgumentException`
  naming it, instead of a `NullPointerException` with no
  message. [#50](https://github.com/logicsquad/nanocaptcha/issues/50)
- When the fonts can't load because the JVM has no writable temporary
  directory, the error now says so, instead of blaming missing
  fontconfig. [#77](https://github.com/logicsquad/nanocaptcha/issues/77)

### Security
- Content producers now choose CAPTCHA answers with a `SecureRandom`
  instead of a `java.util.Random`, whose next values can be worked out
  from enough earlier
  ones. [#49](https://github.com/logicsquad/nanocaptcha/issues/49)


## Release 2.3

### Changed
- Backgrounds, noise producers and filters are now tested against
  golden images, and `FishEyeImageFilter` uses `ThreadLocalRandom`
  like the other filters, instead of
  `Math.random()`. [#62](https://github.com/logicsquad/nanocaptcha/issues/62)

### Deprecated
- Deprecated `StretchImageFilter`, which can't stretch an image in
  place, so it draws a stretched part of the image over the rest. It
  will be removed in
  3.0. [#57](https://github.com/logicsquad/nanocaptcha/issues/57)

### Fixed
- `RippleImageFilter`, which `addFilter()` adds by default, drew the
  rippled image over the original, so the undistorted text still
  showed underneath. `ImageFilter.applyFilter()` now replaces the
  image with the filtered one, which makes the ripple much more
  visible, and changes how filtered CAPTCHAs
  look. [#56](https://github.com/logicsquad/nanocaptcha/issues/56)
- `StretchImageFilter` no longer smears the top rows of the image
  down the rest of it, which could leave the whole CAPTCHA a single
  colour. [#80](https://github.com/logicsquad/nanocaptcha/issues/80)
