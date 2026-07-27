import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import java.util.zip.ZipFile

plugins {
    java
}

sourceSets["test"].java.srcDir(
    rootProject.file("examples/SharedLib/app/core/src/test/java")
)

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
}

val bundleOutput = project(":examples:SharedLib:bundle")
    .layout.buildDirectory.dir("native/jni")
val bundleTask = ":examples:SharedLib:bundle:SharedLib_build_bundle_desktop_jni"
val isMacOs = DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX

dependencies {
    implementation(project(":examples:SharedLib:app:core"))

    // These modules contain generated binding/runtime classes only. The desktop
    // wrapper artifacts that package standalone DLL/SO/DYLIB files are omitted.
    implementation(project(":examples:SharedLib:libA:shared:LibA-jni")) {
        isTransitive = false
    }
    implementation(project(":examples:SharedLib:libB:shared:LibB-jni")) {
        isTransitive = false
    }
    implementation(project(":jParser:runtime:shared:runtime-jni")) {
        isTransitive = false
    }
    implementation(project(":jParser:api:api-core"))
    implementation(project(":jParser:loader:loader-core"))

    testImplementation(libs.junit)
    testRuntimeOnly(files(bundleOutput))
}

tasks.test {
    group = "verification"
    description = "Load and exercise the JNI-only fat bundle through one bundle loader call."
    dependsOn(bundleTask)
    useJUnit()
    systemProperty("java.awt.headless", "true")
    systemProperty("jparser.nativeBundle", "SharedLibFatJni")
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
