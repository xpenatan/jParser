import com.github.xpenatan.jParser.builder.bundle.NativeBridge
import com.github.xpenatan.jParser.builder.bundle.NativeTarget
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform

plugins {
    `java-library`
    alias(libs.plugins.jParser)
}

dependencies {
    api(project(":examples:SharedLib:libA:shared:LibA-jni"))
    api(project(":examples:SharedLib:libB:shared:LibB-jni"))
}

java {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.javaMain.get())
}

val host = DefaultNativePlatform.host()
val desktopTarget = NativeTarget.of(
    NativeTarget.OperatingSystem.valueOf(host.operatingSystem.toFamilyName().uppercase()),
    NativeTarget.Architecture.valueOf(
        host.architecture.name.uppercase().replace("-", "_").replace("AARCH64", "ARM64")
    )
)

jParser {
    bundle("desktopJni") {
        bundleName.set("SharedLibFatJni")
        target.set(desktopTarget)
        component("runtime", project(":jParser:runtime:resources"), NativeBridge.JNI)
        component("libA", project(":examples:SharedLib:libA:resources"), NativeBridge.JNI)
        component("libB", project(":examples:SharedLib:libB:resources"), NativeBridge.JNI)
    }
}

tasks.named<Jar>("jar") {
    archiveBaseName.set("SharedLib-bundle-jni")
    from(tasks.matching { task -> task.name == "jParserBundleDesktopJni" })
}
