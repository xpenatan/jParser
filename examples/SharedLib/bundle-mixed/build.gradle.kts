import com.github.xpenatan.jParser.builder.bundle.NativeBridge
import com.github.xpenatan.jParser.builder.bundle.NativeTarget
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    `java-library`
    alias(libs.plugins.jParser)
}

dependencies {
    api(project(":examples:SharedLib:libA:shared:LibA-jni")) {
        isTransitive = false
    }
    api(project(
        path = ":examples:SharedLib:libB:desktop:LibB-desktop-ffm",
        configuration = "fatModeClasses"
    ))
    api(project(
        path = ":jParser:runtime:desktop:runtime-desktop-ffm",
        configuration = "fatModeClasses"
    ))
    api(project(":jParser:api:api-core"))
    api(project(":jParser:loader:loader-core"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaFfm.get())
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.javaFfm.get()))
    }
}

val host = DefaultNativePlatform.host()
val desktopTarget = NativeTarget.of(
    NativeTarget.OperatingSystem.valueOf(host.operatingSystem.toFamilyName().uppercase()),
    NativeTarget.Architecture.valueOf(
        host.architecture.name.uppercase().replace("-", "_").replace("AARCH64", "ARM64")
    )
)

jParser {
    bundle("desktopMixed") {
        bundleName.set("SharedLibFatMixed")
        target.set(desktopTarget)
        component("runtime", project(":jParser:runtime:resources"), NativeBridge.FFM)
        component("libA", project(":examples:SharedLib:libA:resources"), NativeBridge.JNI)
        component("libB", project(":examples:SharedLib:libB:resources"), NativeBridge.FFM)
    }
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("SharedLib-bundle-mixed")
    from(tasks.matching { task -> task.name == "jParserBundleDesktopMixed" })
}
