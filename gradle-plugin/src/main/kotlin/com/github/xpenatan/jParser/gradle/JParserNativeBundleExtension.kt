package com.github.xpenatan.jParser.gradle

import com.github.xpenatan.jParser.builder.bundle.NativeArchiveLinkMode
import com.github.xpenatan.jParser.builder.bundle.NativeBridge
import com.github.xpenatan.jParser.builder.bundle.NativeBuildType
import com.github.xpenatan.jParser.builder.bundle.NativeComponentManifest
import com.github.xpenatan.jParser.builder.bundle.NativeComponentRole
import com.github.xpenatan.jParser.builder.bundle.NativeTarget
import com.github.xpenatan.jParser.builder.bundle.NativeWebOutput
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import java.io.File
import java.nio.file.Path
import javax.inject.Inject

object JParserNativeResourceAttributes {
    @JvmField
    val OPERATING_SYSTEM: Attribute<String> =
        Attribute.of("com.github.xpenatan.jparser.operating-system", String::class.java)

    @JvmField
    val ARCHITECTURE: Attribute<String> =
        Attribute.of("com.github.xpenatan.jparser.architecture", String::class.java)

    @JvmField
    val ABI: Attribute<String> =
        Attribute.of("com.github.xpenatan.jparser.abi", String::class.java)

    @JvmField
    val ENVIRONMENT: Attribute<String> =
        Attribute.of("com.github.xpenatan.jparser.environment", String::class.java)

    @JvmField
    val BRIDGE: Attribute<String> =
        Attribute.of("com.github.xpenatan.jparser.bridge", String::class.java)

    @JvmField
    val BACKEND: Attribute<String> =
        Attribute.of("com.github.xpenatan.jparser.backend", String::class.java)

    @JvmField
    val COMPONENT_FORMAT: Attribute<Int> =
        Attribute.of("com.github.xpenatan.jparser.component-format", Int::class.javaObjectType)

    @JvmField
    val BUILD_TYPE: Attribute<String> =
        Attribute.of("com.github.xpenatan.jparser.build-type", String::class.java)

    @JvmField
    val TOOLCHAIN: Attribute<String> =
        Attribute.of("com.github.xpenatan.jparser.toolchain", String::class.java)

    const val USAGE = "jparser-native"
    const val CATEGORY = "jparser-native-resources"
}

open class JParserResourcesExtension @Inject constructor(
    private val project: Project,
    private val objects: ObjectFactory
) {
    val componentId: Property<String> = objects.property(String::class.java)
    val componentVersion: Property<String> = objects.property(String::class.java)
    val resourcesArtifactId: Property<String> = objects.property(String::class.java)
    val role: Property<NativeComponentRole> = objects.property(NativeComponentRole::class.java)
        .convention(NativeComponentRole.BINDING)
    val licenses: ConfigurableFileCollection = objects.fileCollection()
    val declaredClassifiers: ListProperty<String> =
        objects.listProperty(String::class.java).convention(emptyList())
    val variants: NamedDomainObjectContainer<JParserResourceVariant> =
        objects.domainObjectContainer(JParserResourceVariant::class.java) { name ->
            objects.newInstance(JParserResourceVariant::class.java, name, project, objects)
        }

    fun license(value: Any) {
        licenses.from(value)
    }

    fun declaredClassifier(value: String) {
        declaredClassifiers.add(value)
    }

    fun variant(name: String, action: Action<in JParserResourceVariant>) {
        variants.create(name, action)
    }
}

open class JParserResourceVariant @Inject constructor(
    private val variantKey: String,
    private val project: Project,
    private val objects: ObjectFactory
) : Named {
    val target: Property<NativeTarget> = objects.property(NativeTarget::class.java)
    val bridge: Property<NativeBridge> = objects.property(NativeBridge::class.java)
    val backend: Property<String> = objects.property(String::class.java).convention("")
    val buildType: Property<NativeBuildType> = objects.property(NativeBuildType::class.java)
        .convention(NativeBuildType.RELEASE)
    val minimumJavaVersion: Property<Int> = objects.property(Int::class.javaObjectType)
        .convention(bridge.map { value -> if(value == NativeBridge.FFM) 25 else 8 })
    val minimumPlatformVersion: Property<String> =
        objects.property(String::class.java).convention("")
    val runtimeAbi: Property<String> = objects.property(String::class.java)
        .convention("jparser-runtime-1")
    val toolchainId: Property<String> = objects.property(String::class.java)
    val toolchainVersion: Property<String> = objects.property(String::class.java).convention("")
    val cRuntime: Property<String> = objects.property(String::class.java).convention("")
    val cppRuntime: Property<String> = objects.property(String::class.java).convention("")
    val webModuleName: Property<String> = objects.property(String::class.java)

    val implementationArchive: RegularFileProperty = objects.fileProperty()
    val bridgeArchive: RegularFileProperty = objects.fileProperty()
    val dependencyArchives = mutableListOf<JParserResourceDependencyArchive>()
    val systemLibraries: ListProperty<String> =
        objects.listProperty(String::class.java).convention(emptyList())
    val frameworks: ListProperty<String> =
        objects.listProperty(String::class.java).convention(emptyList())
    val dynamicDependencies: ListProperty<String> =
        objects.listProperty(String::class.java).convention(emptyList())
    val linkerOptions: ListProperty<String> =
        objects.listProperty(String::class.java).convention(emptyList())
    val exportedSymbols: ListProperty<String> =
        objects.listProperty(String::class.java).convention(emptyList())
    val webIDLFiles: ConfigurableFileCollection = objects.fileCollection()
    val webHeaderDirectories: ConfigurableFileCollection = objects.fileCollection()
    val licenses: ConfigurableFileCollection = objects.fileCollection()
    val taskDependencies = mutableListOf<Any>()

    override fun getName(): String = variantKey

    fun implementationArchive(value: Any) {
        implementationArchive.set(project.layout.file(project.provider { project.file(value) }))
    }

    fun bridgeArchive(value: Any) {
        bridgeArchive.set(project.layout.file(project.provider { project.file(value) }))
    }

    fun dependencyArchive(
        name: String,
        value: Any,
        linkMode: NativeArchiveLinkMode = NativeArchiveLinkMode.NORMAL
    ) {
        require(dependencyArchives.none { archive -> archive.name == name }) {
            "Duplicate jParser resource dependency archive name: $name"
        }
        dependencyArchives.add(
            JParserResourceDependencyArchive(
                name,
                project.layout.file(project.provider { project.file(value) }),
                linkMode
            )
        )
    }

    fun systemLibrary(value: String) {
        systemLibraries.add(value)
    }

    fun framework(value: String) {
        frameworks.add(value)
    }

    fun dynamicDependency(value: String) {
        dynamicDependencies.add(value)
    }

    fun linkerOption(value: String) {
        linkerOptions.add(value)
    }

    fun exportedSymbol(value: String) {
        exportedSymbols.add(value)
    }

    fun webIDL(value: Any) {
        webIDLFiles.from(value)
    }

    fun webHeaders(value: Any) {
        webHeaderDirectories.from(value)
    }

    fun license(value: Any) {
        licenses.from(value)
    }

    fun builtBy(vararg tasks: Any) {
        taskDependencies.addAll(tasks)
    }
}

data class JParserResourceDependencyArchive(
    val name: String,
    val file: Provider<RegularFile>,
    val linkMode: NativeArchiveLinkMode
)

open class JParserNativeBundleSpec @Inject constructor(
    private val bundleKey: String,
    private val project: Project,
    private val objects: ObjectFactory
) : Named {
    val bundleName: Property<String> = objects.property(String::class.java).convention(bundleKey)
    val target: Property<NativeTarget> = objects.property(NativeTarget::class.java)
    val buildType: Property<NativeBuildType> = objects.property(NativeBuildType::class.java)
        .convention(NativeBuildType.RELEASE)
    val outputDirectory: DirectoryProperty = objects.directoryProperty()
        .convention(project.layout.buildDirectory.dir("jparser/bundles/$bundleKey"))
    val webOutput: Property<NativeWebOutput> = objects.property(NativeWebOutput::class.java)
        .convention(NativeWebOutput.JAVASCRIPT_AND_WASM)
    val androidApiLevel: Property<Int> = objects.property(Int::class.javaObjectType).convention(28)
    val minimumMacOSVersion: Property<String> = objects.property(String::class.java).convention("10.13")
    val minimumIOSVersion: Property<String> = objects.property(String::class.java).convention("12.0")
    val linkerExecutable: Property<String> = objects.property(String::class.java).convention("")
    val visualCppEnvironment: Property<String> = objects.property(String::class.java).convention("")
    val androidNdkHome: Property<String> = objects.property(String::class.java).convention("")
    val emscriptenRoot: Property<String> = objects.property(String::class.java).convention("")
    val pythonExecutable: Property<String> = objects.property(String::class.java).convention("")
    val environment: MapProperty<String, String> =
        objects.mapProperty(String::class.java, String::class.java).convention(emptyMap())
    val keepTemporaryFiles: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val toolchainId: Property<String> = objects.property(String::class.java)
    val components: NamedDomainObjectContainer<JParserNativeBundleComponent> =
        objects.domainObjectContainer(JParserNativeBundleComponent::class.java) { name ->
            objects.newInstance(JParserNativeBundleComponent::class.java, name, objects)
        }

    override fun getName(): String = bundleKey

    fun outputDirectory(value: File) {
        outputDirectory.set(value)
    }

    fun outputDirectory(value: Path) {
        outputDirectory.set(value.toFile())
    }

    fun outputDirectory(value: Directory) {
        outputDirectory.set(value)
    }

    fun environment(name: String, value: String) {
        environment.put(name, value)
    }

    fun component(name: String, notation: Any, action: Action<in JParserNativeBundleComponent>) {
        components.create(name) {
            dependency(notation)
            action.execute(this)
        }
    }

    fun component(name: String, notation: Any, bridge: NativeBridge) {
        components.create(name) {
            dependency(notation)
            this.bridge.set(bridge)
        }
    }

    fun component(
        name: String,
        notation: Any,
        bridge: NativeBridge,
        action: Action<in JParserNativeBundleComponent>
    ) {
        components.create(name) {
            dependency(notation)
            this.bridge.set(bridge)
            action.execute(this)
        }
    }
}

open class JParserNativeBundleComponent @Inject constructor(
    private val componentKey: String,
    objects: ObjectFactory
) : Named {
    val bridge: Property<NativeBridge> = objects.property(NativeBridge::class.java)
    val backend: Property<String> = objects.property(String::class.java).convention("")
    val toolchainId: Property<String> = objects.property(String::class.java)
    internal var dependencyNotation: Any? = null

    override fun getName(): String = componentKey

    fun dependency(notation: Any) {
        dependencyNotation = notation
    }
}
