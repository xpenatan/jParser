package com.github.xpenatan.jParser.gradle

import com.github.xpenatan.jParser.builder.bundle.NativeArchiveInput
import com.github.xpenatan.jParser.builder.bundle.NativeBundleBuilder
import com.github.xpenatan.jParser.builder.bundle.NativeBundleRequest
import com.github.xpenatan.jParser.builder.bundle.NativeComponentBuilder
import com.github.xpenatan.jParser.builder.bundle.NativeComponentRequest
import com.github.xpenatan.jParser.builder.bundle.NativeResourcesPublicationRequest
import com.github.xpenatan.jParser.builder.bundle.NativeResourcesPublicationVerifier
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.nio.file.Files

@DisableCachingByDefault(because = "Packages native toolchain outputs into a validated resource JAR")
abstract class JParserNativeComponentTask : DefaultTask() {
    @get:OutputFile
    abstract val outputJar: RegularFileProperty

    @get:Internal
    lateinit var resources: JParserResourcesExtension

    @get:Internal
    lateinit var variant: JParserResourceVariant

    @TaskAction
    fun buildComponent() {
        val request = NativeComponentRequest()
        request.outputJar = outputJar.get().asFile.toPath()
        request.componentId = resources.componentId.get()
        request.componentVersion = resources.componentVersion.get()
        request.role = resources.role.get()
        request.target = variant.target.get()
        request.bridge = variant.bridge.get()
        request.variantName = variant.backend.get()
        request.buildType = variant.buildType.get()
        request.minimumJavaVersion = variant.minimumJavaVersion.get()
        request.minimumPlatformVersion = variant.minimumPlatformVersion.get()
        request.runtimeAbi = variant.runtimeAbi.get()
        request.toolchainId = variant.toolchainId.get()
        request.toolchainVersion = variant.toolchainVersion.get()
        request.cRuntime = variant.cRuntime.get()
        request.cppRuntime = variant.cppRuntime.get()
        request.webModuleName = if(
            request.target.operatingSystem ==
            com.github.xpenatan.jParser.builder.bundle.NativeTarget.OperatingSystem.WEB
        ) {
            variant.webModuleName.orNull ?: resources.componentId.get()
        }
        else {
            ""
        }
        request.implementationArchive = variant.implementationArchive.get().asFile.toPath()
        if(variant.bridgeArchive.isPresent) {
            request.bridgeArchive = variant.bridgeArchive.get().asFile.toPath()
        }
        variant.dependencyArchives.forEach { dependency ->
            request.dependencyArchives.add(
                NativeArchiveInput(
                    dependency.name,
                    dependency.file.get().asFile.toPath(),
                    dependency.linkMode
                )
            )
        }
        request.systemLibraries.addAll(variant.systemLibraries.get())
        request.frameworks.addAll(variant.frameworks.get())
        request.dynamicDependencies.addAll(variant.dynamicDependencies.get())
        request.linkerOptions.addAll(variant.linkerOptions.get())
        request.exportedSymbols.addAll(variant.exportedSymbols.get())
        request.webIDLFiles.addAll(variant.webIDLFiles.files.sortedBy { it.absolutePath }.map { it.toPath() })
        request.webHeaderDirectories.addAll(
            variant.webHeaderDirectories.files.sortedBy { it.absolutePath }.map { it.toPath() }
        )
        request.licenseFiles.addAll(
            (resources.licenses.files + variant.licenses.files)
                .distinctBy { it.absoluteFile.normalize().path }
                .sortedBy { it.absolutePath }
                .map { it.toPath() }
        )
        NativeComponentBuilder.build(request)
    }
}

@DisableCachingByDefault(because = "Validates native task provenance without producing an output")
abstract class JParserVerifyResourceInputsTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val nativeInputs: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val producerOutputs: ConfigurableFileCollection

    @get:Input
    abstract val requireProvenance: Property<Boolean>

    @TaskAction
    fun verifyInputs() {
        if(!requireProvenance.get()) {
            return
        }
        val roots = producerOutputs.files
            .map { file -> file.toPath().toAbsolutePath().normalize() }
        if(roots.isEmpty()) {
            throw GradleException(
                "Release jParser resource variants must declare native producer tasks with builtBy(...)"
            )
        }
        val unproduced = nativeInputs.files
            .map { file -> file.toPath().toAbsolutePath().normalize() }
            .filter { input ->
                roots.none { root ->
                    input == root || (Files.isDirectory(root) && input.startsWith(root))
                }
            }
        if(unproduced.isNotEmpty()) {
            throw GradleException(
                "jParser resource inputs are not outputs of their declared native producer tasks: " +
                    unproduced.joinToString()
            )
        }
    }
}

@DisableCachingByDefault(because = "Invokes the selected native platform linker")
abstract class JParserNativeBundleTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val componentJars: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Internal
    lateinit var bundle: JParserNativeBundleSpec

    @get:Internal
    lateinit var componentConfigurations: List<Configuration>

    @TaskAction
    fun buildBundle() {
        val request = NativeBundleRequest()
        request.bundleName = bundle.bundleName.get()
        request.outputDirectory = outputDirectory.get().asFile.toPath()
        request.target = bundle.target.get()
        request.buildType = bundle.buildType.get()
        request.webOutput = bundle.webOutput.get()
        request.androidApiLevel = bundle.androidApiLevel.get()
        request.minimumMacOSVersion = bundle.minimumMacOSVersion.get()
        request.minimumIOSVersion = bundle.minimumIOSVersion.get()
        request.linkerExecutable = bundle.linkerExecutable.get()
        request.visualCppEnvironment = bundle.visualCppEnvironment.get()
        request.androidNdkHome = bundle.androidNdkHome.get()
        request.emscriptenRoot = bundle.emscriptenRoot.get()
        request.pythonExecutable = bundle.pythonExecutable.get()
        request.environment.putAll(bundle.environment.get())
        request.keepTemporaryFiles = bundle.keepTemporaryFiles.get()
        componentConfigurations.forEach { configuration ->
            val files = configuration.resolve()
            if(files.size != 1) {
                throw GradleException(
                    "jParser bundle component configuration ${configuration.name} resolved " +
                        "${files.size} files; exactly one classified _resources JAR is required"
                )
            }
            request.componentJars.add(files.single().toPath())
        }
        NativeBundleBuilder.build(request)
    }
}

@DisableCachingByDefault(because = "Validates the publication matrix without producing an output")
abstract class JParserVerifyResourcesPublicationTask : DefaultTask() {
    @get:Input
    abstract val componentId: Property<String>

    @get:Input
    abstract val componentVersion: Property<String>

    @get:Input
    abstract val declaredClassifiers: ListProperty<String>

    @get:Input
    abstract val requireCompleteMatrix: Property<Boolean>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val resourceJars: ConfigurableFileCollection

    @TaskAction
    fun verifyPublication() {
        val request = NativeResourcesPublicationRequest()
        request.componentId = componentId.get()
        request.componentVersion = componentVersion.get()
        request.declaredClassifiers.addAll(declaredClassifiers.get())
        request.requireCompleteMatrix = requireCompleteMatrix.get()
        request.resourceJars.addAll(resourceJars.files.sortedBy { it.absolutePath }.map { it.toPath() })
        NativeResourcesPublicationVerifier.verify(request)
    }
}
