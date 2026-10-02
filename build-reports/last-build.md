# Build report
date: Fri Oct  2 11:43:18 UTC 2026
commit: 84f2b95011cb9beaac56521b953e638da2ef0b6f
tests_outcome: success
assemble_outcome: success

## tests.log (tail)

Welcome to Gradle 8.11.1!

Here are the highlights of this release:
 - Parallel load and store for Configuration Cache
 - Java compilation errors at the end of the build output
 - Consolidated report for warnings and deprecations

For more details see https://docs.gradle.org/8.11.1/release-notes.html

To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.11.1/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 

> Configure project :android
Build was configured to prefer settings repositories over project repositories but repository 'Google' was added by build file 'android/build.gradle'
Build was configured to prefer settings repositories over project repositories but repository 'MavenRepo' was added by build file 'android/build.gradle'

> Configure project :core
Build was configured to prefer settings repositories over project repositories but repository 'Google' was added by build file 'core/build.gradle'
Build was configured to prefer settings repositories over project repositories but repository 'MavenRepo' was added by build file 'core/build.gradle'

> Task :core:compileJava
> Task :core:processResources NO-SOURCE
> Task :core:classes
> Task :core:compileTestJava
> Task :core:processTestResources NO-SOURCE
> Task :core:testClasses

> Task :core:test

ContentTest > sixChaptersInOrder PASSED

ContentTest > everyChapterHasEnoughToExplore PASSED

ContentTest > finalCodeIsDeducibleFromCollectedClues PASSED

ContentTest > lockedClosingLinesAreExact PASSED

ContentTest > everyPuzzleIsSolvable PASSED

ContentTest > everyReferencedAssetExists PASSED

ContentTest > contentValidates PASSED

ContentTest > letterIsARealLetter PASSED
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/tests-1790941325858.json

BUILD SUCCESSFUL in 34s
3 actionable tasks: 3 executed

## build.log (tail)
To honour the JVM settings for this build a single-use Daemon process will be forked. For more on this, please refer to https://docs.gradle.org/8.11.1/userguide/gradle_daemon.html#sec:disabling_the_daemon in the Gradle documentation.
Daemon will be stopped at the end of the build 

> Configure project :android
Build was configured to prefer settings repositories over project repositories but repository 'Google' was added by build file 'android/build.gradle'
Build was configured to prefer settings repositories over project repositories but repository 'MavenRepo' was added by build file 'android/build.gradle'

> Configure project :core
Build was configured to prefer settings repositories over project repositories but repository 'Google' was added by build file 'core/build.gradle'
Build was configured to prefer settings repositories over project repositories but repository 'MavenRepo' was added by build file 'core/build.gradle'

> Task :core:compileJava UP-TO-DATE
> Task :core:processResources NO-SOURCE
> Task :core:classes UP-TO-DATE
> Task :core:jar
> Task :android:preBuild UP-TO-DATE
> Task :android:preDebugBuild UP-TO-DATE
> Task :android:mergeDebugNativeDebugMetadata NO-SOURCE
> Task :android:javaPreCompileDebug
> Task :android:generateDebugResValues
> Task :android:checkDebugAarMetadata
> Task :android:mapDebugSourceSetPaths
> Task :android:generateDebugResources
> Task :android:packageDebugResources
> Task :android:createDebugCompatibleScreenManifests
> Task :android:extractDeepLinksDebug
> Task :android:mergeDebugResources
> Task :android:parseDebugLocalResources
> Task :android:processDebugMainManifest
> Task :android:processDebugManifest
> Task :android:mergeDebugShaders
> Task :android:compileDebugShaders NO-SOURCE
> Task :android:generateDebugAssets UP-TO-DATE
> Task :android:mergeDebugAssets
> Task :android:processDebugManifestForPackage
> Task :android:processDebugResources
> Task :android:compressDebugAssets
> Task :android:desugarDebugFileDependencies

> Task :android:compileDebugJavaWithJavac
Note: /home/runner/work/Lohen/Lohen/android/src/main/java/com/esteban/lohen/android/AndroidLauncher.java uses or overrides a deprecated API.
Note: Recompile with -Xlint:deprecation for details.

> Task :android:dexBuilderDebug
> Task :android:processDebugJavaRes NO-SOURCE
> Task :android:mergeDebugGlobalSynthetics
> Task :android:checkDebugDuplicateClasses
> Task :android:mergeDebugJavaResource
> Task :android:copyAndroidNatives
> Task :android:mergeDebugJniLibFolders
> Task :android:mergeProjectDexDebug
> Task :android:mergeLibDexDebug
> Task :android:mergeDebugNativeLibs
> Task :android:validateSigningDebug
> Task :android:writeDebugAppMetadata
> Task :android:writeDebugSigningConfigVersions

> Task :android:stripDebugDebugSymbols
Unable to strip the following libraries, packaging them as they are: libgdx-freetype.so, libgdx.so.

> Task :android:mergeExtDexDebug
> Task :android:packageDebug
> Task :android:createDebugApkListingFileRedirect
> Task :android:assembleDebug
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/assemble-1790941357088.json
[Incubating] Problems report is available at: file:///home/runner/work/Lohen/Lohen/build/reports/problems/problems-report.html

BUILD SUCCESSFUL in 44s
37 actionable tasks: 36 executed, 1 up-to-date

## apk
total 14540
-rw-r--r-- 1 runner runner 14882643 Oct  2 11:43 android-debug.apk
-rw-r--r-- 1 runner runner      405 Oct  2 11:43 output-metadata.json
8ebc728c00a709f50e5f479c4dd06db5080f404f445beb609df49926514b3c5a  dist/PourLohen-debug.apk
15M	dist/PourLohen-debug.apk
