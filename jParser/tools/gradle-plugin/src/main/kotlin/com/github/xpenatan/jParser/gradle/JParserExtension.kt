package com.github.xpenatan.jParser.gradle

import com.github.xpenatan.jParser.builder.tool.JParserSymbolNameMode
import com.github.xpenatan.jParser.builder.targets.AndroidTarget
import com.github.xpenatan.jParser.builder.targets.SourceLanguage
import com.github.xpenatan.jParser.idl.IDLRenaming
import org.gradle.api.Action
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import java.io.File
import java.nio.file.Path
import javax.inject.Inject

/**
 * Gradle DSL exposed as `jParser { ... }`.
 *
 * The extension configures Java/API generation and the native build driver used
 * by `jParser_generate` and `jParser_build_*` tasks. Paths accepted by this DSL
 * may be absolute or project-relative unless the property documents a narrower
 * convention.
 */
open class JParserExtension @Inject constructor(
    private val project: Project,
    private val objects: ObjectFactory
) {
    /** Native library base name used for generated classes, native outputs, and loader names. */
    val libName: Property<String> = objects.property(String::class.java)

    /** Prefix used when resolving sibling modules. Empty is valid for layouts like `core` or `builder`. */
    val modulePrefix: Property<String> = objects.property(String::class.java)

    /** Java package used for generated binding classes. */
    val packageName: Property<String> = objects.property(String::class.java)

    /**
     * Source tree parsed by jParser and used as the default native source/header directory.
     *
     * For normal binding builds this is required. Runtime-helper builds may omit it because
     * they generate and compile only the runtime helper sources.
     */
    val cppSourcePath: Property<String> = objects.property(String::class.java)

    /** IDL file name without the `.idl` suffix. Defaults to [libName] outside runtime-helper mode. */
    val idlName: Property<String> = objects.property(String::class.java)

    /** Web module base name used by generated TeaVM web output. Defaults to [libName]. */
    val webModuleName: Property<String> = objects.property(String::class.java)

    /** Base directory that contains the generated output modules. Defaults to the parent project directory. */
    val modulePath: Property<String> = objects.property(String::class.java)
        .convention(project.layout.projectDirectory.asFile.parentFile.absolutePath)
    val moduleBaseSuffix: Property<String> = objects.property(String::class.java)
    val moduleBuildSuffix: Property<String> = objects.property(String::class.java)
    val moduleCoreSuffix: Property<String> = objects.property(String::class.java)
    val moduleJNISuffix: Property<String> = objects.property(String::class.java)
    val moduleWebSuffix: Property<String> = objects.property(String::class.java)
    val moduleFFMSuffix: Property<String> = objects.property(String::class.java)
    val moduleCSuffix: Property<String> = objects.property(String::class.java)

    val addRuntimeHelperIDL: Property<Boolean> = objects.property(Boolean::class.java).convention(true)
    val runtimeHelperMode: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val windowsDebugBuild: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val keepGeneratedCommandComments: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    val jniSymbolNameMode: Property<JParserSymbolNameMode> = objects.property(JParserSymbolNameMode::class.java)
    val ffmSymbolNameMode: Property<JParserSymbolNameMode> = objects.property(JParserSymbolNameMode::class.java)
    val teaVMCSymbolNameMode: Property<JParserSymbolNameMode> = objects.property(JParserSymbolNameMode::class.java)
    val idlRenaming: Property<IDLRenaming> = objects.property(IDLRenaming::class.java)
    val ffmLogMethod: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val ffmDefaultCritical: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    val jniCppStandard: Property<String> = objects.property(String::class.java).convention("c++11")
    val ffmCppStandard: Property<String> = objects.property(String::class.java).convention("c++11")
    val teaVMCCppStandard: Property<String> = objects.property(String::class.java).convention("c++17")
    val webCppStandard: Property<String> = objects.property(String::class.java).convention("c++11")

    /**
     * Language used for native library source compilation. Generated glue remains C++.
     *
     * Use [SourceLanguage.C] for C libraries whose implementation files should be compiled
     * as C, such as C17 libraries. The C++ standard properties still apply to generated
     * glue/link targets.
     */
    val sourceLanguage: Property<SourceLanguage> = objects.property(SourceLanguage::class.java).convention(SourceLanguage.CPP)

    /** C language standard used when [sourceLanguage] is [SourceLanguage.C]. */
    val cStandard: Property<String> = objects.property(String::class.java).convention("c17")

    /** Emscripten main module name linked by web side modules. */
    val webMainModuleName: Property<String> = objects.property(String::class.java).convention("runtime")

    /** Emscripten `SIDE_MODULE` value. The default `2` matches jParser runtime side-module builds. */
    val webSideModule: Property<Int> = objects.property(Int::class.javaObjectType).convention(2)

    /** Header forced into web compilation units before regular includes. */
    val webForcedInclude: Property<String> = objects.property(String::class.java)

    /** Builds this web target as the Emscripten main module instead of a side module. */
    val webMainModule: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    /** Extra Emscripten exported functions for web targets, for example `_malloc`. */
    val webExportedFunctions: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())

    /** Extra Emscripten exported runtime methods for web targets, for example `ccall`. */
    val webExportedRuntimeMethods: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())

    /** Android platform API level used by Android native targets. */
    val androidApiLevel: Property<AndroidTarget.ApiLevel> = objects.property(AndroidTarget.ApiLevel::class.java)
        .convention(AndroidTarget.ApiLevel.Android_10_29)

    /** Android ABIs built by the aggregate Android targets. */
    val androidTargets: ListProperty<AndroidTarget.Target> = objects.listProperty(AndroidTarget.Target::class.java)
        .convention(
            listOf(
                AndroidTarget.Target.x86,
                AndroidTarget.Target.x86_64,
                AndroidTarget.Target.armeabi_v7a,
                AndroidTarget.Target.arm64_v8a
            )
        )

    /** Additional IDL files parsed together with [idlName]. */
    val additionalIDLPaths: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())

    /** Additional IDL reference files used for type lookup without generating their APIs. */
    val additionalIDLRefPaths: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())

    /** Extra native source roots scanned by the compiler after default roots. */
    val additionalSourceDirs: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())

    val native: JParserNativeHooks = objects.newInstance(JParserNativeHooks::class.java, objects)
    val dependencies: NamedDomainObjectContainer<JParserDependencyExtension> =
        objects.domainObjectContainer(JParserDependencyExtension::class.java) { name ->
            objects.newInstance(JParserDependencyExtension::class.java, name, objects)
        }

    fun cppSourcePath(value: File) {
        cppSourcePath.set(value.toJParserPath())
    }

    fun cppSourcePath(value: Path) {
        cppSourcePath.set(value.toJParserPath())
    }

    fun cppSourcePath(value: Directory) {
        cppSourcePath.set(value.toJParserPath())
    }

    fun cppSourcePath(value: Provider<Directory>) {
        cppSourcePath.set(value.map { it.toJParserPath() })
    }

    fun modulePath(value: File) {
        modulePath.set(value.toJParserPath())
    }

    fun modulePath(value: Path) {
        modulePath.set(value.toJParserPath())
    }

    fun modulePath(value: Directory) {
        modulePath.set(value.toJParserPath())
    }

    fun modulePath(value: Provider<Directory>) {
        modulePath.set(value.map { it.toJParserPath() })
    }

    fun webForcedInclude(value: File) {
        webForcedInclude.set(value.toJParserPath())
    }

    fun webForcedInclude(value: Path) {
        webForcedInclude.set(value.toJParserPath())
    }

    fun webForcedInclude(value: RegularFile) {
        webForcedInclude.set(value.toJParserPath())
    }

    fun webForcedInclude(value: Provider<RegularFile>) {
        webForcedInclude.set(value.map { it.toJParserPath() })
    }

    fun additionalIDLPath(path: String) {
        additionalIDLPaths.add(path)
    }

    fun additionalIDLPath(path: File) {
        additionalIDLPaths.add(path.toJParserPath())
    }

    fun additionalIDLPath(path: Path) {
        additionalIDLPaths.add(path.toJParserPath())
    }

    fun additionalIDLPath(path: RegularFile) {
        additionalIDLPaths.add(path.toJParserPath())
    }

    fun additionalIDLPath(path: Provider<RegularFile>) {
        additionalIDLPaths.add(path.map { it.toJParserPath() })
    }

    fun additionalIDLRefPath(path: String) {
        additionalIDLRefPaths.add(path)
    }

    fun additionalIDLRefPath(path: File) {
        additionalIDLRefPaths.add(path.toJParserPath())
    }

    fun additionalIDLRefPath(path: Path) {
        additionalIDLRefPaths.add(path.toJParserPath())
    }

    fun additionalIDLRefPath(path: RegularFile) {
        additionalIDLRefPaths.add(path.toJParserPath())
    }

    fun additionalIDLRefPath(path: Provider<RegularFile>) {
        additionalIDLRefPaths.add(path.map { it.toJParserPath() })
    }

    fun additionalSourceDir(path: String) {
        additionalSourceDirs.add(path)
    }

    fun additionalSourceDir(path: File) {
        additionalSourceDirs.add(path.toJParserPath())
    }

    fun additionalSourceDir(path: Path) {
        additionalSourceDirs.add(path.toJParserPath())
    }

    fun additionalSourceDir(path: Directory) {
        additionalSourceDirs.add(path.toJParserPath())
    }

    fun additionalSourceDir(path: Provider<Directory>) {
        additionalSourceDirs.add(path.map { it.toJParserPath() })
    }

    fun runtimeHelper() {
        runtimeHelperMode.set(true)
    }

    fun idlRenaming(value: IDLRenaming) {
        idlRenaming.set(value)
    }

    fun webExportedFunction(value: String) {
        webExportedFunctions.add(value)
    }

    fun webExportedRuntimeMethod(value: String) {
        webExportedRuntimeMethods.add(value)
    }

    fun native(action: Action<in JParserNativeHooks>) {
        action.execute(native)
    }

    fun dependency(name: String, action: Action<in JParserDependencyExtension>) {
        dependencies.create(name, action)
    }
}

/** Global native hooks plus named target overrides. */
open class JParserNativeHooks @Inject constructor(
    objects: ObjectFactory
) : JParserTargetHooks(objects) {
    val targets: NamedDomainObjectContainer<JParserNamedTargetHooks> =
        objects.domainObjectContainer(JParserNamedTargetHooks::class.java) { name ->
            objects.newInstance(JParserNamedTargetHooks::class.java, name, objects)
        }
    val variants: NamedDomainObjectContainer<JParserNativeTargetVariantHooks> =
        objects.domainObjectContainer(JParserNativeTargetVariantHooks::class.java) { name ->
            objects.newInstance(JParserNativeTargetVariantHooks::class.java, name, objects)
        }

    fun target(target: JParserTargets, action: Action<in JParserNamedTargetHooks>) {
        targets.create(target.targetName, action)
    }

    fun targetVariant(target: JParserTargets, variantName: String, action: Action<in JParserNativeTargetVariantHooks>) {
        val targetName = target.targetName
        val normalizedVariantName = variantName.trim()
        require(normalizedVariantName.isNotEmpty()) { "jParser native target variant name must not be empty" }
        variants.create("${targetName}_${normalizedVariantName}") {
            this.targetName.set(targetName)
            this.variantName.set(normalizedVariantName)
            this.outputDirectoryPrefix.convention(normalizedVariantName)
            action.execute(this)
        }
    }
}

/** Declares another jParser library whose IDL, headers, and native outputs are consumed by this build. */
open class JParserDependencyExtension @Inject constructor(
    private val dependencyName: String,
    objects: ObjectFactory
) : Named {
    val idlRefPaths: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
    val referenceLibName: Property<String> = objects.property(String::class.java)
    val referencePackageName: Property<String> = objects.property(String::class.java)
    val referenceModulePath: Property<String> = objects.property(String::class.java)
    val referenceModulePrefix: Property<String> = objects.property(String::class.java).convention("lib")
    val referenceModuleBuildSuffix: Property<String> = objects.property(String::class.java).convention("-build")
    val referenceProjectPath: Property<String> = objects.property(String::class.java)
    val referenceIncludedBuildName: Property<String> = objects.property(String::class.java)
    val taskDependencies = mutableListOf<Any>()
    val native: JParserNativeHooks = objects.newInstance(JParserNativeHooks::class.java, objects)

    override fun getName(): String = dependencyName

    fun reference(
        libName: String,
        modulePath: String,
        packageName: String = "",
        modulePrefix: String = "lib",
        moduleBuildSuffix: String = "-build",
        projectPath: String = "",
        includedBuildName: String = ""
    ) {
        referenceLibName.set(libName)
        if(packageName.isNotBlank()) {
            referencePackageName.set(packageName)
        }
        referenceModulePath.set(modulePath)
        referenceModulePrefix.set(modulePrefix)
        referenceModuleBuildSuffix.set(moduleBuildSuffix)
        if(projectPath.isNotBlank()) {
            referenceProjectPath.set(projectPath)
        }
        if(includedBuildName.isNotBlank()) {
            referenceIncludedBuildName.set(includedBuildName)
        }
    }

    fun reference(
        libName: String,
        modulePath: File,
        packageName: String = "",
        modulePrefix: String = "lib",
        moduleBuildSuffix: String = "-build",
        projectPath: String = "",
        includedBuildName: String = ""
    ) {
        reference(libName, modulePath.toJParserPath(), packageName, modulePrefix, moduleBuildSuffix, projectPath, includedBuildName)
    }

    fun reference(
        libName: String,
        modulePath: Path,
        packageName: String = "",
        modulePrefix: String = "lib",
        moduleBuildSuffix: String = "-build",
        projectPath: String = "",
        includedBuildName: String = ""
    ) {
        reference(libName, modulePath.toJParserPath(), packageName, modulePrefix, moduleBuildSuffix, projectPath, includedBuildName)
    }

    fun reference(
        libName: String,
        modulePath: Directory,
        packageName: String = "",
        modulePrefix: String = "lib",
        moduleBuildSuffix: String = "-build",
        projectPath: String = "",
        includedBuildName: String = ""
    ) {
        reference(libName, modulePath.toJParserPath(), packageName, modulePrefix, moduleBuildSuffix, projectPath, includedBuildName)
    }

    fun referenceModulePath(value: File) {
        referenceModulePath.set(value.toJParserPath())
    }

    fun referenceModulePath(value: Path) {
        referenceModulePath.set(value.toJParserPath())
    }

    fun referenceModulePath(value: Directory) {
        referenceModulePath.set(value.toJParserPath())
    }

    fun referenceModulePath(value: Provider<Directory>) {
        referenceModulePath.set(value.map { it.toJParserPath() })
    }

    fun idlRefPath(path: String) {
        idlRefPaths.add(path)
    }

    fun idlRefPath(path: File) {
        idlRefPaths.add(path.toJParserPath())
    }

    fun idlRefPath(path: Path) {
        idlRefPaths.add(path.toJParserPath())
    }

    fun idlRefPath(path: RegularFile) {
        idlRefPaths.add(path.toJParserPath())
    }

    fun idlRefPath(path: Provider<RegularFile>) {
        idlRefPaths.add(path.map { it.toJParserPath() })
    }

    fun dependsOn(vararg tasks: Any) {
        taskDependencies.addAll(tasks)
    }

    fun native(action: Action<in JParserNativeHooks>) {
        action.execute(native)
    }
}

/** Native hooks for one jParser target name, such as `windows64_jni` or `web_wasm`. */
open class JParserNamedTargetHooks @Inject constructor(
    private val targetName: String,
    objects: ObjectFactory
) : JParserTargetHooks(objects), Named {
    val androidTargets: NamedDomainObjectContainer<JParserAndroidTargetHooks> =
        objects.domainObjectContainer(JParserAndroidTargetHooks::class.java) { name ->
            objects.newInstance(JParserAndroidTargetHooks::class.java, name, objects)
        }

    override fun getName(): String = targetName

    fun androidTarget(name: String, action: Action<in JParserAndroidTargetHooks>) {
        androidTargets.create(name, action)
    }

    fun androidTarget(target: AndroidTarget.Target, action: Action<in JParserAndroidTargetHooks>) {
        androidTarget(target.name, action)
    }
}

/** Variant-specific hooks for one native target, exposed as `jParser_build_<target>_<variant>`. */
open class JParserNativeTargetVariantHooks @Inject constructor(
    private val variantKey: String,
    objects: ObjectFactory
) : JParserTargetHooks(objects), Named {
    val targetName: Property<String> = objects.property(String::class.java)
    val variantName: Property<String> = objects.property(String::class.java)

    /**
     * Whether the base target hooks should be applied before this variant's hooks.
     *
     * Defaults to `false` because variants often swap mutually exclusive include/link inputs,
     * for example two native backend libraries for the same platform target.
     */
    val includeBaseTargetHooks: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    override fun getName(): String = variantKey
}

/** Android ABI-specific hooks under an Android target. */
open class JParserAndroidTargetHooks @Inject constructor(
    private val targetName: String,
    objects: ObjectFactory
) : JParserTargetHooks(objects), Named {
    override fun getName(): String = targetName
}

/**
 * Native build hooks shared by global, named-target, and Android ABI scopes.
 *
 * List properties are additive. Values from a named target are applied after the
 * global hooks, and Android ABI hooks are applied after the parent Android target.
 * Nullable boolean properties override the build-tool defaults only when set.
 */
open class JParserTargetHooks @Inject constructor(
    objects: ObjectFactory
) {
    /** Header search directories passed to the native compiler as include paths. */
    val headerDirs: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())

    /**
     * Native source include globs compiled by this target.
     *
     * These patterns are applied to generated sources, [JParserExtension.cppSourcePath],
     * `src/main/cpp/custom`, and any `additionalSourceDirs`. Use this when the target
     * needs explicit source files or platform-specific source subsets.
     */
    val cppIncludes: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())

    /** Native source globs removed after [cppIncludes] and automatic source globs are resolved. */
    val cppExcludes: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())

    /** Extra compiler flags for this scope. */
    val compileFlags: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())

    /** Extra linker flags for this scope. */
    val linkerFlags: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())

    /** Static libraries or object archives passed to the linker. */
    val staticLinkerInputs: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())

    /** Shared libraries passed to the linker. */
    val sharedLinkerInputs: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())

    /** Headers forced into each compilation unit before normal source includes. */
    val forcedIncludes: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())

    /** Extra Emscripten exported functions for web targets in this scope. */
    val webExportedFunctions: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())

    /** Extra Emscripten exported runtime methods for web targets in this scope. */
    val webExportedRuntimeMethods: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())

    /**
     * Controls whether jParser adds the automatic generated/default source glob.
     *
     * The default is `true`, which adds the parsed native source directory as a recursive
     * `**.cpp` include. Set this to `false` when every source should be selected explicitly
     * through [cppIncludes], such as prebuilt-link targets or highly platform-specific builds.
     */
    val includeDefaultSources: Property<Boolean> = objects.property(Boolean::class.java)

    /**
     * Controls whether jParser automatically compiles build-module custom sources.
     *
     * The default is `true`, which adds `.cpp` files directly under
     * `src/main/cpp/custom/` from the build module to the target. This folder is for
     * handwritten helper/wrapper sources that live beside the IDL, not upstream library
     * sources. Set this to `false` when those custom files should not be compiled for a
     * target, or when the target must opt in to specific files with [cppIncludes] to avoid
     * compiling stale, experimental, or platform-only helpers.
     */
    val includeCustomSources: Property<Boolean> = objects.property(Boolean::class.java)

    /** Emscripten `SIDE_MODULE` value for this target scope. */
    val webSideModule: Property<Int> = objects.property(Int::class.javaObjectType)

    /** Emscripten main module name consumed by this web side-module target. */
    val webMainModuleName: Property<String> = objects.property(String::class.java)

    /**
     * Prefix inserted under `build/c++/libs` and `build/c++/target` for this target.
     *
     * This is primarily used by native target variants so mutually exclusive builds of
     * the same platform target can coexist, for example `libs/wgpu/windows/vc/jni` and
     * `libs/dawn/windows/vc/jni`.
     */
    val outputDirectoryPrefix: Property<String> = objects.property(String::class.java)

    /** Extra Gradle task dependencies added to the generated jParser task for this scope. */
    val taskDependencies = mutableListOf<Any>()

    fun headerDir(value: String) {
        headerDirs.add(value)
    }

    fun headerDir(value: File) {
        headerDirs.add(value.toJParserPath())
    }

    fun headerDir(value: Path) {
        headerDirs.add(value.toJParserPath())
    }

    fun headerDir(value: Directory) {
        headerDirs.add(value.toJParserPath())
    }

    fun headerDir(value: Provider<Directory>) {
        headerDirs.add(value.map { it.toJParserPath() })
    }

    fun cppInclude(value: String) {
        cppIncludes.add(value)
    }

    fun cppInclude(value: File) {
        cppIncludes.add(value.toJParserPath())
    }

    fun cppInclude(value: Path) {
        cppIncludes.add(value.toJParserPath())
    }

    fun cppInclude(value: RegularFile) {
        cppIncludes.add(value.toJParserPath())
    }

    fun cppInclude(value: Provider<RegularFile>) {
        cppIncludes.add(value.map { it.toJParserPath() })
    }

    fun cppExclude(value: String) {
        cppExcludes.add(value)
    }

    fun cppExclude(value: File) {
        cppExcludes.add(value.toJParserPath())
    }

    fun cppExclude(value: Path) {
        cppExcludes.add(value.toJParserPath())
    }

    fun cppExclude(value: RegularFile) {
        cppExcludes.add(value.toJParserPath())
    }

    fun cppExclude(value: Provider<RegularFile>) {
        cppExcludes.add(value.map { it.toJParserPath() })
    }

    fun compileFlag(value: String) {
        compileFlags.add(value)
    }

    fun linkerFlag(value: String) {
        linkerFlags.add(value)
    }

    fun staticLinkerInput(value: String) {
        staticLinkerInputs.add(value)
    }

    fun staticLinkerInput(value: File) {
        staticLinkerInputs.add(value.toJParserPath())
    }

    fun staticLinkerInput(value: Path) {
        staticLinkerInputs.add(value.toJParserPath())
    }

    fun staticLinkerInput(value: RegularFile) {
        staticLinkerInputs.add(value.toJParserPath())
    }

    fun staticLinkerInput(value: Provider<RegularFile>) {
        staticLinkerInputs.add(value.map { it.toJParserPath() })
    }

    fun sharedLinkerInput(value: String) {
        sharedLinkerInputs.add(value)
    }

    fun sharedLinkerInput(value: File) {
        sharedLinkerInputs.add(value.toJParserPath())
    }

    fun sharedLinkerInput(value: Path) {
        sharedLinkerInputs.add(value.toJParserPath())
    }

    fun sharedLinkerInput(value: RegularFile) {
        sharedLinkerInputs.add(value.toJParserPath())
    }

    fun sharedLinkerInput(value: Provider<RegularFile>) {
        sharedLinkerInputs.add(value.map { it.toJParserPath() })
    }

    fun forcedInclude(value: String) {
        forcedIncludes.add(value)
    }

    fun forcedInclude(value: File) {
        forcedIncludes.add(value.toJParserPath())
    }

    fun forcedInclude(value: Path) {
        forcedIncludes.add(value.toJParserPath())
    }

    fun forcedInclude(value: RegularFile) {
        forcedIncludes.add(value.toJParserPath())
    }

    fun forcedInclude(value: Provider<RegularFile>) {
        forcedIncludes.add(value.map { it.toJParserPath() })
    }

    fun webExportedFunction(value: String) {
        webExportedFunctions.add(value)
    }

    fun webExportedRuntimeMethod(value: String) {
        webExportedRuntimeMethods.add(value)
    }

    fun outputDirectoryPrefix(value: String) {
        outputDirectoryPrefix.set(value)
    }

    fun dependsOn(vararg tasks: Any) {
        taskDependencies.addAll(tasks)
    }
}

private fun File.toJParserPath(): String {
    return path.replace('\\', '/')
}

private fun Path.toJParserPath(): String {
    return toFile().toJParserPath()
}

private fun Directory.toJParserPath(): String {
    return asFile.toJParserPath()
}

private fun RegularFile.toJParserPath(): String {
    return asFile.toJParserPath()
}
