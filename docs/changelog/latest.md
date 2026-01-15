### 0.7.0

_Released 2026 Jan 15_

#### Overview

This release contains another full rewrite of `OmittableModelConverter` to play more nicely with swagger-core's
conversion chain. The refactoring brings fixes for multiple leakages of omittable into the generated spec. Additionally,
required properties are resolved slightly more conservatively now to avoid false positives.
