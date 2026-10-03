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


## Release 2.3 (2026-10-03)

Image CAPTCHAs look different in this release:

- The ripple that `addFilter()` adds is much stronger, since the
  undistorted text no longer shows
  underneath. [#56](https://github.com/logicsquad/nanocaptcha/issues/56)
- In images other than the default 200 × 50, the text is sized to
  fit. [#59](https://github.com/logicsquad/nanocaptcha/issues/59)
- `GaussianNoiseProducer`'s grain is grey, rather than faintly
  coloured. [#60](https://github.com/logicsquad/nanocaptcha/issues/60)
- `DefaultWordRenderer` varies the shape and position of every
  glyph. [#75](https://github.com/logicsquad/nanocaptcha/issues/75)

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
- Deprecated `FastWordRenderer`, which is built for speed that CAPTCHAs
  don't need: `DefaultWordRenderer` renders far more of them a second
  than an application asks for. Its CAPTCHAs are also weaker, since
  each character in each font is always the same bitmap. Use
  `DefaultWordRenderer` instead. It will be removed in
  3.0. [#82](https://github.com/logicsquad/nanocaptcha/issues/82)

### Fixed
- `RippleImageFilter`, which `addFilter()` adds by default, drew the
  rippled image over the original, so the undistorted text still
  showed underneath. `ImageFilter.applyFilter()` now replaces the
  image with the filtered one, which makes the ripple much more
  visible, and changes how filtered CAPTCHAs
  look. [#56](https://github.com/logicsquad/nanocaptcha/issues/56)
- With `randomiseYOffset()`, `DefaultWordRenderer` and
  `FastWordRenderer` now choose a new height for the text each time
  they render, anywhere it fits in the image. The height was chosen
  once per renderer, and could push glyphs off the top of the
  image. [#58](https://github.com/logicsquad/nanocaptcha/issues/58)
- `DefaultWordRenderer` and `FastWordRenderer` now size the built-in
  fonts to the image: 40 pt in the default height of 50 pixels, and in
  proportion otherwise. Text too wide for the image, such as
  `addContent(10)` at the default size, shrinks to fit instead of
  running off the right-hand edge, which made the CAPTCHA
  unsolvable. Fonts you supply keep their size unless they have to
  shrink. [#59](https://github.com/logicsquad/nanocaptcha/issues/59)
- `GaussianNoiseProducer` added noise to the alpha channel, which made
  opaque pixels partly transparent. It now draws each pixel's noise
  as a white or black speckle over the image, so opaque pixels stay
  opaque, and the grain is grey rather than faintly coloured. It
  still shows over any
  background. [#60](https://github.com/logicsquad/nanocaptcha/issues/60)
- `StretchImageFilter` no longer smears the top rows of the image
  down the rest of it, which could leave the whole CAPTCHA a single
  colour. [#80](https://github.com/logicsquad/nanocaptcha/issues/80)

### Security
- `DefaultWordRenderer` now gives each glyph a small random rotation,
  scale, vertical shift and sub-pixel position, chosen for each
  CAPTCHA, and lets neighbouring glyphs overlap slightly. It drew
  each character in each built-in font as the same bitmap, so the
  default CAPTCHA could be read by matching 46 templates. Drawing
  each glyph from its outline is slower: in a quick benchmark, about
  19,000 CAPTCHAs a second, down from
  160,000. [#75](https://github.com/logicsquad/nanocaptcha/issues/75)
- Audio CAPTCHAs now play each digit at a random volume, with a
  random gap of up to a quarter of a second after it, and
  `RandomNoiseProducer` starts its noise at a random point. The audio
  was the bundled clips end to end, with the noise from its start at
  a fixed volume, so the noise could be subtracted and the clips
  matched. [#75](https://github.com/logicsquad/nanocaptcha/issues/75)


## Release 3.0

Code that builds its own CAPTCHAs needs changing for this release. A
`Builder` now builds a factory, which makes each CAPTCHA, so where 2.x
had

    ImageCaptcha captcha = new ImageCaptcha.Builder(200, 50).addContent().build();

3.0 has

    // Once, when the application starts
    ImageCaptcha.Factory captchas = new ImageCaptcha.Factory.Builder(200, 50).addContent().build();

    // For each CAPTCHA, on any thread
    ImageCaptcha captcha = captchas.create();

The same goes for
`AudioCaptcha`. [#64](https://github.com/logicsquad/nanocaptcha/issues/64)

The background producers have moved to the
`net.logicsquad.nanocaptcha.image.background` package, so imports of
them need `image.backgrounds.` changing to
`image.background.`. [#92](https://github.com/logicsquad/nanocaptcha/issues/92)

### Added
- Added a README example of an audio CAPTCHA in a language NanoCaptcha
  doesn't include, from a `VoiceProducer` of your own and a recording
  of each digit. [#67](https://github.com/logicsquad/nanocaptcha/issues/67)
- Added `ImageCaptcha.Factory` and `AudioCaptcha.Factory`, whose
  `create()` makes a new CAPTCHA, with new content and randomness,
  each time it's called. A factory can't be changed, and it's safe to
  share between threads, so a web application can build one when it
  starts and use it for every request. Producers, renderers and
  filters of your own that it uses need to be thread-safe
  too. [#64](https://github.com/logicsquad/nanocaptcha/issues/64)
- NanoCaptcha is now a named module, `net.logicsquad.nanocaptcha`,
  with a `module-info.java`, rather than an automatic module named in
  the JAR manifest. It exports every package, and requires only
  `java.desktop`, which modules that require NanoCaptcha get too,
  since its API uses types such as
  `BufferedImage`. [#66](https://github.com/logicsquad/nanocaptcha/issues/66)

### Changed
- NanoCaptcha now needs Java 17 or later: it compiles with
  `--release 17`, and CI tests on Java 17, 21 and 25. On Java 8 to
  16, use 2.3. [#63](https://github.com/logicsquad/nanocaptcha/issues/63)
- NanoCaptcha's fonts and sounds are now under
  `/net/logicsquad/nanocaptcha/` in the JAR, rather than at `/fonts/`
  and `/sounds/`, where another JAR's files at the same paths could
  take their place. [#79](https://github.com/logicsquad/nanocaptcha/issues/79)
- `Sample` no longer logs before it throws. Audio that Java Sound
  can't read, such as an MP3 file, now throws an
  `IllegalArgumentException`, as audio in the wrong format does, and a
  stream that fails throws an `UncheckedIOException`, rather than a
  `RuntimeException`. The messages say what the audio needs to
  be. [#65](https://github.com/logicsquad/nanocaptcha/issues/65)
- `ImageCaptcha.toString()` and `AudioCaptcha.toString()` now give the
  length of the answer, as in `content=5 characters`, rather than the
  answer itself, which went wherever the description did: into logs,
  error pages and templates. Use `getContent()` for the
  answer. [#70](https://github.com/logicsquad/nanocaptcha/issues/70)
- Renamed `Mixer` to `AudioMixer`, which doesn't clash with
  `javax.sound.sampled.Mixer`. A `NoiseProducer` of your own that uses
  it needs the new
  name. [#70](https://github.com/logicsquad/nanocaptcha/issues/70)
- Image CAPTCHAs now have an opaque light grey background
  (`Color.LIGHT_GRAY`) unless another is added, including those from
  `ImageCaptcha.create()`, so they show on dark pages and can be
  written as JPEG. For a transparent image, add a
  `TransparentBackgroundProducer`. [#68](https://github.com/logicsquad/nanocaptcha/issues/68)
- `isCorrect()` now ignores case, which mobile keyboards often change,
  and whitespace at either end of the answer, which autofill can add.
  `isCorrect(answer, false)` compares exactly, as `isCorrect()` did
  before. The built-in content producers' answers are lowercase or
  digits, so ignoring case costs nothing with
  them. [#69](https://github.com/logicsquad/nanocaptcha/issues/69)
- `ImageCaptcha.Builder` is now `ImageCaptcha.Factory.Builder`, and
  `AudioCaptcha.Builder` is now `AudioCaptcha.Factory.Builder`. A
  `Builder`'s `build()` returns a factory, rather than a CAPTCHA, and
  the factory's `create()` makes each CAPTCHA, as shown above. A
  `Builder` keeps only its configuration, so calling a method after
  `build()` no longer throws an `IllegalStateException`, and changing
  a `Builder` doesn't change a factory it has already built. An
  exception from a producer, renderer or filter, such as a font that
  can't display the content, now comes from `create()`, rather than
  from the method that added
  it. [#64](https://github.com/logicsquad/nanocaptcha/issues/64)
- The audio `NoiseProducer`'s `addNoise()` now takes the spoken
  digits as one `Sample`, already joined, rather than a list of them
  to join first. A `NoiseProducer` of your own can drop its
  `AudioMixer.concatenate()`
  call. [#86](https://github.com/logicsquad/nanocaptcha/issues/86)
- Renamed `Sample.SC_AUDIO_FORMAT` to `Sample.FORMAT`. The `SC_` was
  from SimpleCaptcha, and `AUDIO_` repeated
  `AudioFormat`. [#91](https://github.com/logicsquad/nanocaptcha/issues/91)
- Renamed the `net.logicsquad.nanocaptcha.image.backgrounds` package to
  `net.logicsquad.nanocaptcha.image.background`, to match `filter`,
  `noise` and `renderer`. [#92](https://github.com/logicsquad/nanocaptcha/issues/92)

### Removed
- Removed `ChineseContentProducer` and `ArabicContentProducer`,
  deprecated in 2.2. For content in another script, write a
  `ContentProducer` of your own, and give `DefaultWordRenderer` a font
  that can display it. [#81](https://github.com/logicsquad/nanocaptcha/issues/81)
- Removed `StretchImageFilter`, deprecated in 2.3. Use
  `RippleImageFilter` or `ShearImageFilter`
  instead. [#81](https://github.com/logicsquad/nanocaptcha/issues/81)
- Removed `FastWordRenderer`, deprecated in 2.3. Use
  `DefaultWordRenderer`
  instead. [#81](https://github.com/logicsquad/nanocaptcha/issues/81)
- Removed `AbstractWordRenderer`, whose only subclass was
  `DefaultWordRenderer`, and with it `AbstractWordRenderer.RAND`,
  deprecated in 2.2. `DefaultWordRenderer.Builder`'s methods now
  return `DefaultWordRenderer.Builder`, so `build()` gives a
  `DefaultWordRenderer` after any of them. For a renderer of your
  own, implement `WordRenderer`, which has one method, and take any
  randomness from
  `ThreadLocalRandom.current()`. [#78](https://github.com/logicsquad/nanocaptcha/issues/78)
  [#85](https://github.com/logicsquad/nanocaptcha/issues/85)
- Removed `Sample(String)` and `RandomNoiseProducer(String[])`,
  deprecated in 2.2, which only found resources that NanoCaptcha's
  own class loader and module could see. Use `Sample(URL)`, with a URL
  from your own class's `getResource()`, and
  `RandomNoiseProducer(List<Sample>)`. [#79](https://github.com/logicsquad/nanocaptcha/issues/79)
- Removed the dependency on `slf4j-api`, so NanoCaptcha has no runtime
  dependencies at all, and on the module path it no longer needs
  `--add-modules org.slf4j`. [#65](https://github.com/logicsquad/nanocaptcha/issues/65)
- Removed the `net.logicsquad.nanocaptcha.Builder` interface, which
  nothing took, and with it the `net.logicsquad.nanocaptcha` package,
  which held nothing
  else. [#87](https://github.com/logicsquad/nanocaptcha/issues/87)
- Removed the system properties that changed NanoCaptcha's defaults:
  `net.logicsquad.nanocaptcha.image.ImageCaptcha.defaultX` and
  `defaultY`, for the size of `ImageCaptcha.create()`'s image, and
  `net.logicsquad.nanocaptcha.audio.producer.RandomNumberVoiceProducer.defaultLanguage`,
  for the language of `AudioCaptcha.create()` and
  `new RandomNumberVoiceProducer()`. Those now always make a
  200 × 50 image and use English, as does an unsupported language.
  For anything else, build a factory, with
  `new RandomNumberVoiceProducer(locale)` for another
  language. [#88](https://github.com/logicsquad/nanocaptcha/issues/88)
- Removed `FiveLetterFirstNameContentProducer`. It chose its answer
  from 7,235 first names, so a guess was right once in 7,235 tries,
  against once in about 6.4 million for five characters from
  `LatinContentProducer`, which replaces
  it. [#89](https://github.com/logicsquad/nanocaptcha/issues/89)
- Removed `addBackground()` without an argument. It added the light
  grey background that an image gets anyway, so it only undid an
  earlier `addBackground(BackgroundProducer)`: leave the background
  out instead. [#90](https://github.com/logicsquad/nanocaptcha/issues/90)

### Fixed
- `ShearImageFilter` no longer crashes the JVM on Alpine when it
  shears an opaque image, and no longer leaves a ghost of the original
  on a transparent one, such as the Builder's. It moves the pixels
  itself, rather than with `Graphics.copyArea()`, which copied in
  place and drew over the pixels underneath. Each glyph is now
  sheared rather than drawn twice, which changes how sheared CAPTCHAs
  look. [#84](https://github.com/logicsquad/nanocaptcha/issues/84)
- `RandomNumberVoiceProducer`, and renderers from
  `DefaultWordRenderer.Builder`, are now safe to share between
  threads. The producer worked out its clips the first time it was
  used, which another thread could see half done, and `randomColor()`
  and `randomFont()` kept the caller's lists, which could change
  afterwards. [#64](https://github.com/logicsquad/nanocaptcha/issues/64)
- `SquigglesBackgroundProducer` drew the same squiggles on every image
  of a given size, so an attacker who had seen one background had seen
  them all, and could subtract it. The ellipses' size, spacing and
  dashes, and where they start, now vary a little with each
  background. [#94](https://github.com/logicsquad/nanocaptcha/issues/94)
