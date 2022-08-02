# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].


## [0.4.0] - 2022-08-02

### Added
- `ZIO.provide`
- `ZIO.fromFunction`

### Changed
- `ZIO[+E, +A]` $\to$ `ZIO[R, +E, +A]`

### Removed
- `ZIO.Thunk`

## [0.3.0] - 2022-08-01

### Added
- `ZIO.catchAll`
- `ZIO.mapError`

### Changed
- `ZIO[A]` $\to$ `ZIO[+E, +A]`


## [0.2.0] - 2022-08-01

### Added
- `zio` object

### Changed
- Initial program


## [0.1.0] - 2022-08-01
- Initial release


<!-- Links -->
[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
[Semantic Versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[unreleased]: https://github.com/jaacko-torus/diy-zio/compare/v0.4.0...HEAD
[0.4.0]: https://github.com/jaacko-torus/diy-zio/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/jaacko-torus/diy-zio/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/jaacko-torus/diy-zio/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/jaacko-torus/diy-zio/releases/tag/v0.1.0