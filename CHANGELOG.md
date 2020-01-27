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
