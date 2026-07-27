import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.Attribute
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import java.util.Locale
import javax.inject.Inject

plugins {
    base
    `maven-publish`
}

abstract class ComponentFactoryAccess @Inject constructor(
    val softwareComponentFactory: SoftwareComponentFactory
)

data class RuntimeResourceVariant(
    val classifier: String,
    val operatingSystem: String,
    val architecture: String,
    val abi: String = "",
    val environment: String = "",
    val bridge: String,
    val toolchain: String,
    val implementationPath: String,
    val bridgePath: String? = null,
    val nativeTask: String
)

val moduleName = "runtime_resources"
val runtimeBuilderPath = ":jParser:runtime:builder"
evaluationDependsOn(runtimeBuilderPath)
val runtimeBuilderProject = project(runtimeBuilderPath)
val runtimeBuilderSourceSets =
    runtimeBuilderProject.extensions.getByType(SourceSetContainer::class.java)
val repositoryRoot = rootProject.layout.projectDirectory

val allVariants = listOf(
    RuntimeResourceVariant(
        "windows-x86_64-jni", "windows", "x86_64", bridge = "jni", toolchain = "msvc",
        implementationPath = "jParser/runtime/builder/build/c++/libs/windows/vc/jni/runtime64_.lib",
        bridgePath = "jParser/runtime/builder/build/c++/libs/windows/vc/jni/runtime_bridge64_.lib",
        nativeTask = "$runtimeBuilderPath:runtime_helper_build_project_windows64_jni"
    ),
    RuntimeResourceVariant(
        "windows-x86_64-ffm", "windows", "x86_64", bridge = "ffm", toolchain = "msvc",
        implementationPath = "jParser/runtime/builder/build/c++/libs/windows/vc/ffm/runtime64_.lib",
        bridgePath = "jParser/runtime/builder/build/c++/libs/windows/vc/ffm/runtime_bridge64_.lib",
        nativeTask = "$runtimeBuilderPath:runtime_helper_build_project_windows64_ffm"
    ),
    RuntimeResourceVariant(
        "linux-x86_64-jni", "linux", "x86_64", bridge = "jni", toolchain = "gcc",
        implementationPath = "jParser/runtime/builder/build/c++/libs/linux/jni/libruntime64_.a",
        bridgePath = "jParser/runtime/builder/build/c++/libs/linux/jni/libruntime_bridge64_.a",
        nativeTask = "$runtimeBuilderPath:runtime_helper_build_project_linux64_jni"
    ),
    RuntimeResourceVariant(
        "linux-x86_64-ffm", "linux", "x86_64", bridge = "ffm", toolchain = "gcc",
        implementationPath = "jParser/runtime/builder/build/c++/libs/linux/ffm/libruntime64_.a",
        bridgePath = "jParser/runtime/builder/build/c++/libs/linux/ffm/libruntime_bridge64_.a",
        nativeTask = "$runtimeBuilderPath:runtime_helper_build_project_linux64_ffm"
    ),
    RuntimeResourceVariant(
        "macos-x86_64-jni", "macos", "x86_64", bridge = "jni", toolchain = "apple-clang",
        implementationPath = "jParser/runtime/builder/build/c++/libs/mac/jni/libruntime64_.a",
        bridgePath = "jParser/runtime/builder/build/c++/libs/mac/jni/libruntime_bridge64_.a",
        nativeTask = "$runtimeBuilderPath:runtime_helper_build_project_mac64_jni"
    ),
    RuntimeResourceVariant(
        "macos-x86_64-ffm", "macos", "x86_64", bridge = "ffm", toolchain = "apple-clang",
        implementationPath = "jParser/runtime/builder/build/c++/libs/mac/ffm/libruntime64_.a",
        bridgePath = "jParser/runtime/builder/build/c++/libs/mac/ffm/libruntime_bridge64_.a",
        nativeTask = "$runtimeBuilderPath:runtime_helper_build_project_mac64_ffm"
    ),
    RuntimeResourceVariant(
        "macos-arm64-jni", "macos", "arm64", bridge = "jni", toolchain = "apple-clang",
        implementationPath = "jParser/runtime/builder/build/c++/libs/mac/arm/jni/libruntime64_.a",
        bridgePath = "jParser/runtime/builder/build/c++/libs/mac/arm/jni/libruntime_bridge64_.a",
        nativeTask = "$runtimeBuilderPath:runtime_helper_build_project_macArm_jni"
    ),
    RuntimeResourceVariant(
        "macos-arm64-ffm", "macos", "arm64", bridge = "ffm", toolchain = "apple-clang",
        implementationPath = "jParser/runtime/builder/build/c++/libs/mac/arm/ffm/libruntime64_.a",
        bridgePath = "jParser/runtime/builder/build/c++/libs/mac/arm/ffm/libruntime_bridge64_.a",
        nativeTask = "$runtimeBuilderPath:runtime_helper_build_project_macArm_ffm"
    ),
    RuntimeResourceVariant(
        "android-x86-jni", "android", "x86", abi = "x86", bridge = "jni",
        toolchain = "android-ndk",
        implementationPath = "jParser/runtime/builder/build/c++/libs/android/x86/libruntime.a",
        bridgePath = "jParser/runtime/builder/build/c++/libs/android/x86/libruntime_bridge.a",
        nativeTask = "$runtimeBuilderPath:runtime_helper_build_project_android_jni"
    ),
    RuntimeResourceVariant(
        "android-x86_64-jni", "android", "x86_64", abi = "x86_64", bridge = "jni",
        toolchain = "android-ndk",
        implementationPath = "jParser/runtime/builder/build/c++/libs/android/x86_64/libruntime.a",
        bridgePath = "jParser/runtime/builder/build/c++/libs/android/x86_64/libruntime_bridge.a",
        nativeTask = "$runtimeBuilderPath:runtime_helper_build_project_android_jni"
    ),
    RuntimeResourceVariant(
        "android-armeabi-v7a-jni", "android", "armv7", abi = "armeabi-v7a", bridge = "jni",
        toolchain = "android-ndk",
        implementationPath = "jParser/runtime/builder/build/c++/libs/android/armeabi-v7a/libruntime.a",
        bridgePath = "jParser/runtime/builder/build/c++/libs/android/armeabi-v7a/libruntime_bridge.a",
        nativeTask = "$runtimeBuilderPath:runtime_helper_build_project_android_jni"
    ),
    RuntimeResourceVariant(
        "android-arm64-v8a-jni", "android", "arm64", abi = "arm64-v8a", bridge = "jni",
        toolchain = "android-ndk",
        implementationPath = "jParser/runtime/builder/build/c++/libs/android/arm64-v8a/libruntime.a",
        bridgePath = "jParser/runtime/builder/build/c++/libs/android/arm64-v8a/libruntime_bridge.a",
        nativeTask = "$runtimeBuilderPath:runtime_helper_build_project_android_jni"
    ),
    RuntimeResourceVariant(
        "ios-device-arm64-teavm-c", "ios", "arm64", environment = "device",
        bridge = "teavm-c", toolchain = "apple-clang",
        implementationPath = "jParser/runtime/builder/build/c++/libs/ios/device/arm64/teavm_c/libruntime_implementation64_.a",
        bridgePath = "jParser/runtime/builder/build/c++/libs/ios/device/arm64/teavm_c/libruntime_bridge64_.a",
        nativeTask = "$runtimeBuilderPath:runtime_helper_build_project_ios_teavm_c"
    ),
    RuntimeResourceVariant(
        "ios-simulator-arm64-teavm-c", "ios", "arm64", environment = "simulator",
        bridge = "teavm-c", toolchain = "apple-clang",
        implementationPath = "jParser/runtime/builder/build/c++/libs/ios/simulator/arm64/teavm_c/libruntime_implementation64_.a",
        bridgePath = "jParser/runtime/builder/build/c++/libs/ios/simulator/arm64/teavm_c/libruntime_bridge64_.a",
        nativeTask = "$runtimeBuilderPath:runtime_helper_build_project_ios_teavm_c"
    ),
    RuntimeResourceVariant(
        "ios-simulator-x86_64-teavm-c", "ios", "x86_64", environment = "simulator",
        bridge = "teavm-c", toolchain = "apple-clang",
        implementationPath = "jParser/runtime/builder/build/c++/libs/ios/simulator/x86_64/teavm_c/libruntime_implementation64_.a",
        bridgePath = "jParser/runtime/builder/build/c++/libs/ios/simulator/x86_64/teavm_c/libruntime_bridge64_.a",
        nativeTask = "$runtimeBuilderPath:runtime_helper_build_project_ios_teavm_c"
    ),
    RuntimeResourceVariant(
        "web", "web", "wasm32", bridge = "web", toolchain = "emscripten",
        implementationPath = "jParser/runtime/builder/build/c++/libs/emscripten/runtime_.a",
        nativeTask = "$runtimeBuilderPath:runtime_helper_build_project_web_wasm"
    )
)

val requestedTasks = gradle.startParameter.taskNames.map { it.lowercase(Locale.ROOT) }
val completePublication =
    providers.gradleProperty("jparser.runtimeResources.complete")
        .map(String::toBoolean)
        .orElse(false)
        .get() ||
        requestedTasks.any { taskName ->
            taskName.contains("preparerelease") ||
                taskName.contains("preparesnapshot") ||
                taskName.contains("publishrelease") ||
                taskName.contains("publishsnapshot") ||
                (taskName.contains("publishjparserresourcespublication") &&
                    !taskName.contains("mavenlocal"))
        }
val explicitlySelected = providers.gradleProperty("jparser.runtimeResources.variants")
    .orNull
    ?.split(',')
    ?.map(String::trim)
    ?.filter(String::isNotEmpty)
    ?.toSet()

fun hostClassifiers(): Set<String> {
    val os = System.getProperty("os.name", "").lowercase(Locale.ROOT)
    val architecture = System.getProperty("os.arch", "").lowercase(Locale.ROOT)
    return when {
        os.contains("win") -> setOf("windows-x86_64-jni", "windows-x86_64-ffm")
        os.contains("linux") -> setOf("linux-x86_64-jni", "linux-x86_64-ffm")
        os.contains("mac") && (architecture == "aarch64" || architecture == "arm64") ->
            setOf("macos-arm64-jni", "macos-arm64-ffm")
        os.contains("mac") -> setOf("macos-x86_64-jni", "macos-x86_64-ffm")
        else -> emptySet()
    }
}

val selectedClassifiers = if(completePublication) {
    allVariants.map { it.classifier }.toSet()
}
else {
    explicitlySelected ?: hostClassifiers()
}
val unknownClassifiers = selectedClassifiers - allVariants.map { it.classifier }.toSet()
require(unknownClassifiers.isEmpty()) {
    "Unknown jParser runtime resource classifiers: $unknownClassifiers"
}
val selectedVariants = allVariants.filter { it.classifier in selectedClassifiers }
require(selectedVariants.isNotEmpty()) {
    "No runtime resource variants are configured for this host; set " +
        "-Pjparser.runtimeResources.variants=<classifier,...>"
}

val nativeAttributes = mapOf(
    "operatingSystem" to Attribute.of(
        "com.github.xpenatan.jparser.operating-system",
        String::class.java
    ),
    "architecture" to Attribute.of(
        "com.github.xpenatan.jparser.architecture",
        String::class.java
    ),
    "abi" to Attribute.of("com.github.xpenatan.jparser.abi", String::class.java),
    "environment" to Attribute.of(
        "com.github.xpenatan.jparser.environment",
        String::class.java
    ),
    "bridge" to Attribute.of("com.github.xpenatan.jparser.bridge", String::class.java),
    "backend" to Attribute.of("com.github.xpenatan.jparser.backend", String::class.java),
    "buildType" to Attribute.of(
        "com.github.xpenatan.jparser.build-type",
        String::class.java
    ),
    "toolchain" to Attribute.of(
        "com.github.xpenatan.jparser.toolchain",
        String::class.java
    )
)
val componentFormat = Attribute.of(
    "com.github.xpenatan.jparser.component-format",
    Int::class.javaObjectType
)
val componentFactory = objects.newInstance(ComponentFactoryAccess::class.java)
    .softwareComponentFactory
val resourcesComponent = componentFactory.adhoc("jParserResources")
components.add(resourcesComponent)
val componentTasks = selectedVariants.associateWith { variant ->
    val taskSegment = variant.classifier.split(Regex("[^A-Za-z0-9]+"))
        .filter(String::isNotEmpty)
        .joinToString("") { word ->
            word.replaceFirstChar { character -> character.uppercase(Locale.ROOT) }
        }
    val outputJar = layout.buildDirectory.file(
        providers.provider {
            "jparser/resources/${variant.classifier}/" +
                "$moduleName-${project.version}-${variant.classifier}.jar"
        }
    )
    val task = tasks.register<JavaExec>("buildRuntimeResource$taskSegment") {
        group = "jParser"
        description = "Build runtime_resources classifier '${variant.classifier}'."
        dependsOn(runtimeBuilderProject.tasks.named("classes"))
        if(!completePublication) {
            dependsOn(variant.nativeTask)
        }
        classpath = runtimeBuilderSourceSets["main"].runtimeClasspath
        mainClass.set("BuildRuntimeResources")
        inputs.file(repositoryRoot.file(variant.implementationPath))
        variant.bridgePath?.let { path -> inputs.file(repositoryRoot.file(path)) }
        inputs.file(repositoryRoot.file("LICENSE"))
        if(variant.classifier == "web") {
            inputs.file(repositoryRoot.file(
                "jParser/runtime/base/src/main/resources/RuntimeHelper.idl"
            ))
            inputs.dir(repositoryRoot.dir("jParser/runtime/base/src/main/resources"))
            inputs.dir(repositoryRoot.dir("jParser/runtime/builder/src/main/cpp/custom"))
        }
        outputs.file(outputJar)
        doFirst {
            args = listOf(
                "build",
                repositoryRoot.asFile.absolutePath,
                variant.classifier,
                outputJar.get().asFile.absolutePath,
                project.version.toString()
            )
        }
    }

    val outgoing = configurations.create("runtimeResources${taskSegment}Elements") {
        isCanBeConsumed = true
        isCanBeResolved = false
        this.attributes.attribute(
            Usage.USAGE_ATTRIBUTE,
            objects.named(Usage::class.java, "jparser-native")
        )
        this.attributes.attribute(
            Category.CATEGORY_ATTRIBUTE,
            objects.named(Category::class.java, "jparser-native-resources")
        )
        this.attributes.attribute(
            Bundling.BUNDLING_ATTRIBUTE,
            objects.named(Bundling::class.java, Bundling.EXTERNAL)
        )
        this.attributes.attribute(
            LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
            objects.named(LibraryElements::class.java, LibraryElements.JAR)
        )
        this.attributes.attribute(nativeAttributes.getValue("operatingSystem"), variant.operatingSystem)
        this.attributes.attribute(nativeAttributes.getValue("architecture"), variant.architecture)
        this.attributes.attribute(nativeAttributes.getValue("abi"), variant.abi)
        this.attributes.attribute(nativeAttributes.getValue("environment"), variant.environment)
        this.attributes.attribute(nativeAttributes.getValue("bridge"), variant.bridge)
        this.attributes.attribute(nativeAttributes.getValue("backend"), "")
        this.attributes.attribute(componentFormat, 1)
        this.attributes.attribute(nativeAttributes.getValue("buildType"), "release")
        this.attributes.attribute(nativeAttributes.getValue("toolchain"), variant.toolchain)
    }
    outgoing.outgoing.artifact(outputJar) {
        classifier = variant.classifier
        extension = "jar"
        type = "jar"
        builtBy(task)
    }
    resourcesComponent.addVariantsFromConfiguration(outgoing) {
        mapToMavenScope("runtime")
        mapToOptional()
    }
    task
}

val verifyJParserResourcesPublication = tasks.register<JavaExec>(
    "verifyJParserResourcesPublication"
) {
    group = "verification"
    description = "Verify the selected runtime_resources classifier matrix."
    dependsOn(componentTasks.values)
    classpath = runtimeBuilderSourceSets["main"].runtimeClasspath
    mainClass.set("BuildRuntimeResources")
    inputs.files(componentTasks.values.map { task -> task.flatMap { it.outputs.files.elements } })
    doFirst {
        args = buildList {
            add("verify")
            add(project.version.toString())
            add(completePublication.toString())
            add(allVariants.joinToString(",") { variant -> variant.classifier })
            componentTasks.values.forEach { task ->
                add(task.get().outputs.files.singleFile.absolutePath)
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("jParserResources") {
            artifactId = moduleName
            from(resourcesComponent)
            pom.packaging = "pom"
            suppressAllPomMetadataWarnings()
        }
    }
}

tasks.matching { task ->
    task.name.startsWith("publishJParserResourcesPublication") ||
        task.name == "generateMetadataFileForJParserResourcesPublication" ||
        task.name == "generatePomFileForJParserResourcesPublication"
}.configureEach {
    dependsOn(verifyJParserResourcesPublication)
}
