#!/usr/bin/env bash
set -euo pipefail
python3 tools/tdd/SourceRegressionTest.py
kotlinc app/src/main/java/Shirakawa/LocalCueWord/TagRecordSupport.kt tools/tdd/TagRecordSupportTest.kt -include-runtime -d /tmp/lcw-tag-record-test.jar
java -jar /tmp/lcw-tag-record-test.jar
kotlinc app/src/main/java/Shirakawa/LocalCueWord/ui/theme/ThemeColorSupport.kt tools/tdd/ThemeColorSupportTest.kt -include-runtime -d /tmp/lcw-theme-test.jar
java -jar /tmp/lcw-theme-test.jar
