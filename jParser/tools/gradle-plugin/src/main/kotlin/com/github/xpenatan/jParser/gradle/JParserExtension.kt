package com.github.xpenatan.jParser.gradle

import com.github.xpenatan.jParser.builder.tool.JParserSymbolNameMode
import com.github.xpenatan.jParser.builder.targets.SourceLanguage
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

open class JParserExtension @Inject constructor(
    private val project: Project,
    private val objects: ObjectFactory
) {
    val libName: Property<String> = objects.property(String::class.java)
    val modulePrefix: Property<String> = objects.property(String::class.java)
    val packageName: Property<String> = objects.property(String::class.java)
    val cppSourcePath: Property<String> = objects.property(String::class.java)

    val idlName: Property<String> = objects.property(String::class.java)
    val webModuleName: Property<String> = objects.property(String::class.java)
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
    val ffmLogMethod: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val ffmDefaultCritical: Property<Boolean> = objects.property(Boolean::class.java).convention(false)

    val jniCppStandard: Property<String> = objects.property(String::class.java).convention("c++11")
    val ffmCppStandard: Property<String> = objects.property(String::class.java).convention("c++11")
    val teaVMCCppStandard: Property<String> = objects.property(String::class.java).convention("c++17")
    val webCppStandard: Property<String> = objects.property(String::class.java).convention("c++11")
    val sourceLanguage: Property<SourceLanguage> = objects.property(SourceLanguage::class.java).convention(SourceLanguage.CPP)
    val cStandard: Property<String> = objects.property(String::class.java).convention("c17")
    val webMainModuleName: Property<String> = objects.property(String::class.java).convention("runtime")
    val webSideModule: Property<Int> = objects.property(Int::class.javaObjectType).convention(2)
    val webForcedInclude: Property<String> = objects.property(String::class.java)
    val webMainModule: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
    val webExportedFunctions: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
    val webExportedRuntimeMethods: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())

    val androidApiLevel: Property<String> = objects.property(String::class.java).convention("Android_10_29")
    val androidTargets: ListProperty<String> = objects.listProperty(String::class.java)
        .convention(listOf("x86", "x86_64", "armeabi_v7a", "arm64_v8a"))

    val additionalIDLPaths: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
    val additionalIDLRefPaths: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
    val additionalSourceDirs: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())

    val native: JParserNativeHooks = objects.newInstance(JParserNativeHooks::class.java, objects)
    val dependencies: NamedDomainObjectContainer<JParserDependencyExtension> =
        objects.domainObjectContainer(JParserDependencyExtension::class.java) { name ->
            objects.newInstance(JParserDependencyExtension::class.java, name, objects)
        }

    fun additionalIDLPath(path: String) {
        additionalIDLPaths.add(path)
    }

    fun additionalIDLRefPath(path: String) {
        additionalIDLRefPaths.add(path)
    }

    fun additionalSourceDir(path: String) {
        additionalSourceDirs.add(path)
    }

    fun runtimeHelper() {
        runtimeHelperMode.set(true)
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

open class JParserNativeHooks @Inject constructor(
    objects: ObjectFactory
) : JParserTargetHooks(objects) {
    val targets: NamedDomainObjectContainer<JParserNamedTargetHooks> =
        objects.domainObjectContainer(JParserNamedTargetHooks::class.java) { name ->
            objects.newInstance(JParserNamedTargetHooks::class.java, name, objects)
        }

    fun target(name: String, action: Action<in JParserNamedTargetHooks>) {
        targets.create(name, action)
    }
}

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

    fun idlRefPath(path: String) {
        idlRefPaths.add(path)
    }

    fun dependsOn(vararg tasks: Any) {
        taskDependencies.addAll(tasks)
    }

    fun native(action: Action<in JParserNativeHooks>) {
        action.execute(native)
    }
}

open class JParserNamedTargetHooks @Inject constructor(
    private val targetName: String,
    objects: ObjectFactory
) : JParserTargetHooks(objects), Named {
    override fun getName(): String = targetName
}

open class JParserTargetHooks @Inject constructor(
    objects: ObjectFactory
) {
    val headerDirs: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
    val cppIncludes: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
    val cppExcludes: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
    val compileFlags: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
    val linkerFlags: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
    val staticLinkerInputs: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
    val sharedLinkerInputs: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
    val forcedIncludes: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
    val webExportedFunctions: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
    val webExportedRuntimeMethods: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
    val includeDefaultSources: Property<Boolean> = objects.property(Boolean::class.java)
    val includeCustomSources: Property<Boolean> = objects.property(Boolean::class.java)
    val webSideModule: Property<Int> = objects.property(Int::class.javaObjectType)
    val webMainModuleName: Property<String> = objects.property(String::class.java)
    val taskDependencies = mutableListOf<Any>()

    fun headerDir(value: String) {
        headerDirs.add(value)
    }

    fun cppInclude(value: String) {
        cppIncludes.add(value)
    }

    fun cppExclude(value: String) {
        cppExcludes.add(value)
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

    fun sharedLinkerInput(value: String) {
        sharedLinkerInputs.add(value)
    }

    fun forcedInclude(value: String) {
        forcedIncludes.add(value)
    }

    fun webExportedFunction(value: String) {
        webExportedFunctions.add(value)
    }

    fun webExportedRuntimeMethod(value: String) {
        webExportedRuntimeMethods.add(value)
    }

    fun dependsOn(vararg tasks: Any) {
        taskDependencies.addAll(tasks)
    }
}
