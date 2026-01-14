### 0.6.0

_Released 2026 Jan 14_

#### Overview

The modules providing support for integrating [Omittable](https://github.com/Osmerion/Omittable) with third-party
libraries and frameworks have been split off into their own repositories to simplify development of current and future
integrations.

This repository now only contains the code for the `com.osmerion.omittable:omittable-swagger-core` artifact.

#### Improvements

- Updated to [omittable-jackson 1.0.0](https://github.com/Osmerion/omittable-jackson/releases/tag/v1.0.0).
- The `OmittableModelConveter` now only marks non-omittable properties as required in the generated schema.
