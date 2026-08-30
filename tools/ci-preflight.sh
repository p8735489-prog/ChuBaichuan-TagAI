#!/usr/bin/env bash
set -euo pipefail

MAIN="app/src/main/java/com/kuzulabz/waifutaggercn/MainActivity.kt"
test -f "$MAIN"

# These symbols belonged to an older onboarding implementation and must not
# remain in the current MainActivity. The current flow uses language/privacy/intro dialogs.
if grep -nE '\b(showFirstRunOnboarding|onboardingPage)\b' "$MAIN"; then
  echo "::error::Stale onboarding symbols found in MainActivity.kt"
  exit 1
fi

# The Compose animation API used by this project is animateFloatAsState.
if grep -nE '[^A-Za-z]animateFloat\s*\(' "$MAIN"; then
  echo "::error::Unexpected animateFloat() call found; use animateFloatAsState()."
  exit 1
fi

grep -q 'import androidx.compose.animation.core.animateFloatAsState' "$MAIN"
echo "CI preflight passed: onboarding and animation references are consistent."
