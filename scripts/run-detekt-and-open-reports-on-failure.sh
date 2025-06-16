#!/bin/sh
./gradlew $1 || (echo "$2 failed. Launching the browser..." && open $PWD/app/build/reports/detekt/detekt.html)