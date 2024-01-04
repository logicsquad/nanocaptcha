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

### Fixed
- Inserted a `BufferedInputStream` into the `Sample(InputStream)`
  constructor to allow audio to be played from resources in the
  JAR. [#6](https://github.com/logicsquad/nanocaptcha/issues/6)

### Security
- Updated SLF4J 2.9.0 → 2.18.0.


## Release 1.4 (2023-03-12)

### Added
- Improved support for alternate languages, and added German digit
  samples for audio
  CAPTCHAs. [#7](https://github.com/logicsquad/nanocaptcha/issues/7)
- Added support in `Builder` classes for setting content
  length. [#9](https://github.com/logicsquad/nanocaptcha/issues/9)
- Added support for randomising the y-offset in image CAPTCHAs, which
  improves variability in "tall"
  images. [#13](https://github.com/logicsquad/nanocaptcha/issues/13)


## Release 1.5 (2023-02-22)

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
