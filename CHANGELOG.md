# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].


## [0.9.0] - 2022-08-04

### Changed
- Type inference improvements
- Program improvements 


## [0.8.0] - 2022-08-04

### Added
- `ZLayer.requires`
- `ZLayer.identity`
- `ZLayer.>>>`
- `ZLayer.++`
- `ZIO.provideSomeLayer`
- `ZEnv.any`
- `console.Console.any`

### Changed
- Program uses new DSL


## [0.7.0] - 2022-08-03

### Added
- `ZLayer`
- `ZIO.zip`
- `ZIO.provideLayer`
- `ZEnv.live`

### Changed
- Made `console.Console` & `Runtime` use `ZLayer`
- Rewrote `Main` to use `ZLayer`


## [0.6.0] - 2022-08-02

### Added
- `ZIO.provideSome`
- `ZIO.provideCustom`
- `Has`

### Changed
- Made `console.Console` use `Has`


## [0.5.0] - 2022-08-02

### Added
- `ZIO.identity`
- `ZIO.environment`
- `ZIO.access`
- `ZIO.accessM`


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
[Unreleased]: https://github.com/jaacko-torus/diy-zio/compare/v0.9.0...HEAD
[0.9.0]: https://github.com/jaacko-torus/diy-zio/compare/v0.8.0...v0.9.0
[0.8.0]: https://github.com/jaacko-torus/diy-zio/compare/v0.7.0...v0.8.0
[0.7.0]: https://github.com/jaacko-torus/diy-zio/compare/v0.6.0...v0.7.0
[0.6.0]: https://github.com/jaacko-torus/diy-zio/compare/v0.5.0...v0.6.0
[0.5.0]: https://github.com/jaacko-torus/diy-zio/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/jaacko-torus/diy-zio/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/jaacko-torus/diy-zio/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/jaacko-torus/diy-zio/compare/v0.1.0...v0.2.0
[0.1.0]: https://github.com/jaacko-torus/diy-zio/releases/tag/v0.1.0