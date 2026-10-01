# Build report
date: Thu Oct  1 22:37:17 UTC 2026
commit: f9f16884c272cb6de6ed7615e0621c20e4458c7b
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

SmokeTest > placeholder PASSED
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/tests-1790894176506.json

BUILD SUCCESSFUL in 31s
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
> Task :android:mergeDebugResources
> Task :android:createDebugCompatibleScreenManifests
> Task :android:extractDeepLinksDebug
> Task :android:parseDebugLocalResources
> Task :android:processDebugMainManifest
> Task :android:processDebugManifest
> Task :android:mergeDebugShaders
> Task :android:compileDebugShaders NO-SOURCE
> Task :android:generateDebugAssets UP-TO-DATE
> Task :android:mergeDebugAssets
> Task :android:compressDebugAssets
> Task :android:processDebugManifestForPackage
> Task :android:processDebugResources
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
gradle/actions: Writing build results to /home/runner/work/_temp/.gradle-actions/build-results/assemble-1790894205822.json
[Incubating] Problems report is available at: file:///home/runner/work/Lohen/Lohen/build/reports/problems/problems-report.html

BUILD SUCCESSFUL in 33s
37 actionable tasks: 36 executed, 1 up-to-date

## apk
total 5048
-rw-r--r-- 1 runner runner 5162967 Oct  1 22:37 android-debug.apk
-rw-r--r-- 1 runner runner     405 Oct  1 22:37 output-metadata.json
5b1c6d4206ef19209b6b443391b0c4b6974cfc8f00e1b1c8416f768b8046b296  dist/PourLohen-debug.apk
5.0M	dist/PourLohen-debug.apk
