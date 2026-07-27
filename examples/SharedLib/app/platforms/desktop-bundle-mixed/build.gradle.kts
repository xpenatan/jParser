import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import java.util.zip.ZipFile

plugins {
    java
}

sourceSets["test"].java.srcDir(
    rootProject.file("examples/SharedLib/app/core/src/test/java")
)

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaFfm.get()))
    }
}

val bundleOutput = project(":examples:SharedLib:bundle")
    .layout.buildDirectory.dir("native/mixed")
val bundleTask = ":examples:SharedLib:bundle:SharedLib_build_bundle_desktop_mixed"
val isMacOs = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX

dependencies {
    implementation(project(":examples:SharedLib:app:core"))

    // LibA exercises JNI while LibB exercises FFM. FFM is the one runtime
    // implementation because a mixed bundle requires Java 25.
    implementation(project(":examples:SharedLib:libA:shared:LibA-jni")) {
        isTransitive = false
    }
    implementation(project(
        path = ":examples:SharedLib:libB:desktop:LibB-desktop-ffm",
        configuration = "fatModeClasses"
    ))
    implementation(project(
        path = ":jParser:runtime:desktop:runtime-desktop-ffm",
        configuration = "fatModeClasses"
    ))
    implementation(project(":jParser:api:api-core"))
    implementation(project(":jParser:loader:loader-core"))

    testImplementation(libs.junit)
    testRuntimeOnly(files(bundleOutput))
}

tasks.test {
    group = "verification"
    description = "Load and exercise a mixed JNI/FFM fat bundle through one bundle loader call."
    dependsOn(bundleTask)
    useJUnit()
    systemProperty("java.awt.headless", "true")
    systemProperty("jparser.nativeBundle", "SharedLibFatMixed")
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaFfm.get()))
    })
    jvmArgs("--enable-native-access=ALL-UNNAMED")
    outputs.upToDateWhen { false }
    doFirst {
        val packagedNatives = classpath.files
            .filter { file -> file.extension.equals("jar", ignoreCase = true) }
            .flatMap { jar ->
                ZipFile(jar).use { zip ->
                    zip.entries().asSequence()
                        .map { entry -> entry.name.lowercase() }
                        .filter { entry ->
                            entry.endsWith(".dll") ||
                                entry.endsWith(".so") ||
                                entry.endsWith(".dylib")
                        }
                        .map { entry -> "${jar.name}!/$entry" }
                        .toList()
                }
            }
        check(packagedNatives.isEmpty()) {
            "Fat-mode test classpath contains standalone native libraries: $packagedNatives"
        }
    }
    testLogging {
        showStandardStreams = true
        events("passed", "skipped", "failed")
    }
    if(isMacOs) {
        jvmArgs("-XstartOnFirstThread")
    }
}
