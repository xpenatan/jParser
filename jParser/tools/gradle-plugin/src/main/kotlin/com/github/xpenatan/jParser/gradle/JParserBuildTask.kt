package com.github.xpenatan.jParser.gradle

import com.github.xpenatan.jParser.builder.tool.DefaultBuildTargetConfig
import com.github.xpenatan.jParser.builder.tool.JParserBuildRequest
import com.github.xpenatan.jParser.builder.tool.JParserBuildRunner
import com.github.xpenatan.jParser.builder.targets.AndroidTarget
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "Generates Java sources and invokes external native toolchains")
abstract class JParserBuildTask : DefaultTask() {
    @get:Input
    abstract val buildArgs: ListProperty<String>

    @get:Input
    abstract val targetArg: Property<String>

    @get:Input
    abstract val generateCore: Property<Boolean>

    @get:Internal
    lateinit var extension: JParserExtension

    @TaskAction
    fun build() {
        val request = createRequest()
        JParserBuildRunner.build(request, *buildArgs.get().toTypedArray())
    }

    private fun createRequest(): JParserBuildRequest {
        val request = JParserBuildRequest()
        request.generateCore = generateCore.get()
        request.params.libName = required(extension.libName.orNull, "libName")
        request.params.modulePrefix = required(extension.modulePrefix.orNull, "modulePrefix")
        request.params.packageName = required(extension.packageName.orNull, "packageName")
        request.params.modulePath = normalizeProjectPath(extension.modulePath.orNull ?: project.projectDir.parentFile.absolutePath)
        request.params.moduleBuildSuffix = extension.moduleBuildSuffix.orNull
        val runtimeHelperMode = extension.runtimeHelperMode.get()
        request.params.cppSourcePath = if(runtimeHelperMode) {
            extension.cppSourcePath.orNull?.takeIf { it.isNotBlank() }?.let { path ->
                normalizeCppSourcePath(path, request.params.modulePath, request.params.modulePrefix, request.params.moduleBuildSuffix)
            }
        }
        else {
            normalizeCppSourcePath(
                required(extension.cppSourcePath.orNull, "cppSourcePath"),
                request.params.modulePath,
                request.params.modulePrefix,
                request.params.moduleBuildSuffix
            )
        }
        request.params.idlName = extension.idlName.orNull?.takeIf { it.isNotBlank() }
            ?: if(runtimeHelperMode) null else request.params.libName
        request.params.webModuleName = extension.webModuleName.orNull ?: request.params.libName
        request.params.moduleBaseSuffix = extension.moduleBaseSuffix.orNull
        request.params.moduleCoreSuffix = extension.moduleCoreSuffix.orNull
        request.params.moduleJNISuffix = extension.moduleJNISuffix.orNull
        request.params.moduleWebSuffix = extension.moduleWebSuffix.orNull
        request.params.moduleFFMSuffix = extension.moduleFFMSuffix.orNull
        request.params.moduleCSuffix = extension.moduleCSuffix.orNull

        request.keepGeneratedCommandComments = extension.keepGeneratedCommandComments.get()
        request.jniSymbolNameMode = extension.jniSymbolNameMode.orNull
        request.ffmSymbolNameMode = extension.ffmSymbolNameMode.orNull
        request.teaVMCSymbolNameMode = extension.teaVMCSymbolNameMode.orNull
        request.ffmLogMethod = extension.ffmLogMethod.get()
        request.ffmDefaultCritical = extension.ffmDefaultCritical.get()
        request.additionalIDLPaths.addAll(extension.additionalIDLPaths.get().map(::normalizeProjectPath))
        request.additionalIDLRefPaths.addAll(extension.additionalIDLRefPaths.get().map(::normalizeProjectPath))
        request.additionalSourceDirs.addAll(extension.additionalSourceDirs.get().map(::normalizeProjectPath))

        configureTargetConfig(request, request.targetConfig)
        return request
    }

    private fun configureTargetConfig(request: JParserBuildRequest, config: DefaultBuildTargetConfig) {
        config.addRuntimeHelperIDL = extension.addRuntimeHelperIDL.get()
        config.runtimeHelperMode = extension.runtimeHelperMode.get()
        config.windowsDebugBuild = extension.windowsDebugBuild.get()
        config.jniCppStandard = extension.jniCppStandard.get()
        config.ffmCppStandard = extension.ffmCppStandard.get()
        config.teaVMCCppStandard = extension.teaVMCCppStandard.get()
        config.webCppStandard = extension.webCppStandard.get()
        config.webMainModuleName = extension.webMainModuleName.get()
        config.webSideModule = extension.webSideModule.get()
        config.webForcedInclude = extension.webForcedInclude.orNull?.let(::normalizeProjectPath)
        config.webMainModule = extension.webMainModule.get()
        config.webExportedFunctions.addAll(extension.webExportedFunctions.get())
        config.webExportedRuntimeMethods.addAll(extension.webExportedRuntimeMethods.get())
        config.androidApiLevel = AndroidTarget.ApiLevel.valueOf(extension.androidApiLevel.get())
        config.androidTargets.clear()
        extension.androidTargets.get().forEach { target ->
            config.androidTargets.add(AndroidTarget.Target.valueOf(target))
        }
        copyHooks(extension.native, config.globalHooks)
        extension.native.targets.forEach { hooks ->
            copyHooks(hooks, config.target(hooks.name))
        }
        extension.dependencies.forEach { dependency ->
            configureDependencyReference(dependency, request, config)
            request.additionalIDLRefPaths.addAll(dependency.idlRefPaths.get().map(::normalizeProjectPath))
            copyHooks(dependency.native, config.globalHooks)
            dependency.native.targets.forEach { hooks ->
                copyHooks(hooks, config.target(hooks.name))
            }
        }
    }

    private fun configureDependencyReference(
        dependency: JParserDependencyExtension,
        request: JParserBuildRequest,
        config: DefaultBuildTargetConfig
    ) {
        val libName = dependency.referenceLibName.orNull?.takeIf { it.isNotBlank() } ?: return
        val referencePackageName = dependency.referencePackageName.orNull?.takeIf { it.isNotBlank() }
            ?: libName.substring(0, 1).lowercase() + libName.substring(1)
        request.additionalJavaImportPackages.add(referencePackageName)
        val modulePath = normalizeProjectPath(required(dependency.referenceModulePath.orNull, "dependency.${dependency.name}.referenceModulePath"))
        val modulePrefix = dependency.referenceModulePrefix.orNull?.takeIf { it.isNotBlank() } ?: "lib"
        val moduleBuildSuffix = resolveModuleSuffix(dependency.referenceModuleBuildSuffix.orNull, "-build")
        val moduleBuildPath = File(modulePath, modulePrefix + moduleBuildSuffix).absolutePath.replace('\\', '/')
        val nativeBuildPath = "$moduleBuildPath/build/c++"

        request.additionalIDLRefPaths.add("$moduleBuildPath/src/main/cpp/$libName.idl")
        config.globalHooks.headerDirs.add("$moduleBuildPath/src/main/cpp/source")
        config.globalHooks.headerDirs.add("$moduleBuildPath/src/main/cpp/custom")

        config.target("windows64_jni").staticLinkerInputs.add("$nativeBuildPath/libs/windows/vc/jni/${libName}64.lib")
        config.target("windows64_ffm").staticLinkerInputs.add("$nativeBuildPath/libs/windows/vc/ffm/${libName}64.lib")
        config.target("windows64_teavm_c").staticLinkerInputs.add("$nativeBuildPath/libs/windows/vc/teavm_c/${libName}64.lib")

        config.target("linux64_jni").sharedLinkerInputs.add("$nativeBuildPath/libs/linux/jni/lib${libName}64.so")
        config.target("linux64_ffm").sharedLinkerInputs.add("$nativeBuildPath/libs/linux/ffm/lib${libName}64.so")
        config.target("linux64_teavm_c").sharedLinkerInputs.add("$nativeBuildPath/libs/linux/teavm_c/lib${libName}64.so")

        config.target("mac64_jni").sharedLinkerInputs.add("$nativeBuildPath/libs/mac/jni/lib${libName}64.dylib")
        config.target("mac64_ffm").sharedLinkerInputs.add("$nativeBuildPath/libs/mac/ffm/lib${libName}64.dylib")
        config.target("mac64_teavm_c").sharedLinkerInputs.add("$nativeBuildPath/libs/mac/teavm_c/lib${libName}64.dylib")

        config.target("macArm_jni").sharedLinkerInputs.add("$nativeBuildPath/libs/mac/arm/jni/lib${libName}arm64.dylib")
        config.target("macArm_ffm").sharedLinkerInputs.add("$nativeBuildPath/libs/mac/arm/ffm/lib${libName}arm64.dylib")
        config.target("macArm_teavm_c").sharedLinkerInputs.add("$nativeBuildPath/libs/mac/arm/teavm_c/lib${libName}arm64.dylib")

        config.target("android_jni").sharedLinkerInputs.add("$nativeBuildPath/libs/android/{androidAbi}/lib$libName.so")
        config.target("android_teavm_c").sharedLinkerInputs.add("$nativeBuildPath/libs/android/{androidAbi}/teavm_c/lib$libName.so")
    }

    private fun copyHooks(source: JParserTargetHooks, target: DefaultBuildTargetConfig.TargetHooks) {
        target.headerDirs.addAll(source.headerDirs.get().map(::normalizeProjectPath))
        target.cppIncludes.addAll(source.cppIncludes.get().map(::normalizeProjectPath))
        target.cppExcludes.addAll(source.cppExcludes.get().map(::normalizeProjectPath))
        target.compileFlags.addAll(source.compileFlags.get())
        target.linkerFlags.addAll(source.linkerFlags.get())
        target.staticLinkerInputs.addAll(source.staticLinkerInputs.get().map(::normalizeProjectPath))
        target.sharedLinkerInputs.addAll(source.sharedLinkerInputs.get().map(::normalizeProjectPath))
        target.forcedIncludes.addAll(source.forcedIncludes.get().map(::normalizeProjectPath))
        target.webExportedFunctions.addAll(source.webExportedFunctions.get())
        target.webExportedRuntimeMethods.addAll(source.webExportedRuntimeMethods.get())
        source.includeDefaultSources.orNull?.let { target.includeDefaultSources = it }
        source.includeCustomSources.orNull?.let { target.includeCustomSources = it }
        source.webSideModule.orNull?.let { target.webSideModule = it }
        source.webMainModuleName.orNull?.let { target.webMainModuleName = it }
    }

    private fun required(value: String?, name: String): String {
        return value?.takeIf { it.isNotBlank() }
            ?: throw GradleException("jParser.$name must be configured for task $path")
    }

    private fun normalizeProjectPath(value: String): String {
        if(value.startsWith("-I")) {
            val path = value.substring(2)
            return "-I" + normalizeProjectPath(path)
        }
        if(value.startsWith("-include")) {
            val path = value.substring("-include".length)
            return "-include" + normalizeProjectPath(path)
        }
        val file = File(value)
        if(file.isAbsolute) {
            return file.absolutePath.replace('\\', '/')
        }
        val trimmed = value.trimStart('/', '\\')
        return project.file(trimmed).absolutePath.replace('\\', '/')
    }

    private fun normalizeCppSourcePath(value: String, modulePath: String, modulePrefix: String, moduleBuildSuffix: String?): String {
        val file = File(value)
        if(isPortableAbsolute(value) || (file.isAbsolute && file.exists())) {
            return file.absolutePath.replace('\\', '/')
        }
        if(value.startsWith("/") || value.startsWith("\\")) {
            val moduleBuildDir = File(modulePath, modulePrefix + resolveModuleSuffix(moduleBuildSuffix, "-build"))
            return File(moduleBuildDir, value.trimStart('/', '\\')).absolutePath.replace('\\', '/')
        }
        return normalizeProjectPath(value)
    }

    private fun resolveModuleSuffix(moduleSuffix: String?, defaultSuffix: String): String {
        val normalized = moduleSuffix?.trim()
        if(normalized.isNullOrEmpty()) {
            return defaultSuffix
        }
        return if(normalized.startsWith("-")) normalized else "-$normalized"
    }

    private fun isPortableAbsolute(value: String): Boolean {
        return value.matches(Regex("^[A-Za-z]:[\\\\/].*")) || value.startsWith("\\\\")
    }
}
