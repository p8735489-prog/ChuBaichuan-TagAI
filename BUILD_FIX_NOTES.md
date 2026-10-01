# Build fix — 2026-08-30

The supplied CI log failed in `:app:compileReleaseKotlin`.

## Reported failures
- stale `showFirstRunOnboarding` / `onboardingPage` references
- stale `animateFloat()` reference
- cascading `prefs`, `recreate`, `finishAffinity`, and arithmetic overload errors

The uploaded source already contains the corrected language/privacy/intro flow and uses
`animateFloatAsState`, so the CI failure log was from an older/stale source revision.

## Changes in this package
1. Removed checked-in local `127.0.0.1:18080` Gradle proxy settings.
2. Added `tools/ci-preflight.sh` to reject the exact stale symbols from the failed build.
3. Changed GitHub Actions to run the preflight check and `./gradlew clean assembleRelease`
   instead of falling back to another Gradle installation.
4. GitHub Actions now explicitly checks out a clean working tree and prints the commit SHA.

A local build could not be completed in this environment because Gradle 8.11.1 was not
cached and outbound access to services.gradle.org is unavailable. The GitHub runner should
perform the authoritative clean build.
