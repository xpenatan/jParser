package com.github.xpenatan.jParser.gradle

import com.github.xpenatan.jParser.builder.tool.DefaultBuildTargetConfig
import com.github.xpenatan.jParser.builder.tool.JParserBuildRequest
import com.github.xpenatan.jParser.builder.tool.JParserBuildRunner
import com.github.xpenatan.jParser.builder.tool.TeaVMCConsumerConfig
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
    abstract val targetVariant: Property<String>

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
        val modulePrefix = required(extension.modulePrefix.orNull, "modulePrefix", allowEmpty = true)
        request.params.modulePrefix = modulePrefix
        request.params.packageName = required(extension.packageName.orNull, "packageName")
        val modulePath = normalizeProjectPath(extension.modulePath.orNull ?: project.projectDir.parentFile.absolutePath)
        request.params.modulePath = modulePath
        request.params.moduleBuildSuffix = extension.moduleBuildSuffix.orNull
        val runtimeHelperMode = extension.runtimeHelperMode.get()
        request.params.cppSourcePath = if(runtimeHelperMode) {
            extension.cppSourcePath.orNull?.takeIf { it.isNotBlank() }?.let { path ->
                normalizeCppSourcePath(path, modulePath, modulePrefix, request.params.moduleBuildSuffix)
            }
        }
        else {
            normalizeCppSourcePath(
                required(extension.cppSourcePath.orNull, "cppSourcePath"),
                modulePath,
                modulePrefix,
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
        request.params.teaVMCLinkage = extension.teaVMCLinkage.get()

        request.keepGeneratedCommandComments = extension.keepGeneratedCommandComments.get()
        request.finalClass = extension.finalClass.get()
        extension.finalClassOverrides.get().forEach(request::setFinalClass)
        request.idlRenaming = extension.idlRenaming.orNull
        request.jniSymbolNameMode = extension.jniSymbolNameMode.orNull
        request.ffmSymbolNameMode = extension.ffmSymbolNameMode.orNull
        request.teaVMCSymbolNameMode = extension.teaVMCSymbolNameMode.orNull
        request.ffmLogMethod = extension.ffmLogMethod.get()
        request.ffmDefaultCritical = extension.ffmDefaultCritical.get()
        request.additionalIDLPaths.addAll(extension.additionalIDLPaths.get().map(::normalizeProjectPath))
        request.additionalIDLRefPaths.addAll(extension.additionalIDLRefPaths.get().map(::normalizeProjectPath))
        request.additionalSourceDirs.addAll(extension.additionalSourceDirs.get().map(::normalizeProjectPath))

        configureTargetConfig(request, request.targetConfig)
        configureTeaVMCConsumers(request)
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
        config.sourceLanguage = extension.sourceLanguage.get()
        config.cStandard = extension.cStandard.get()
        config.webMainModuleName = extension.webMainModuleName.get()
        config.webSideModule = extension.webSideModule.get()
        config.webForcedInclude = extension.webForcedInclude.orNull?.let(::normalizeProjectPath)
        config.webMainModule = extension.webMainModule.get()
        config.webExportedFunctions.addAll(extension.webExportedFunctions.get())
        config.webExportedRuntimeMethods.addAll(extension.webExportedRuntimeMethods.get())
        config.androidApiLevel = extension.androidApiLevel.get()
        config.androidTargets.clear()
        extension.androidTargets.get().forEach { target ->
            config.androidTargets.add(target)
        }
        copyHooks(extension.native, config.globalHooks)
        val activeVariant = findActiveVariant()
        val skipBaseTargetHooks = activeVariant != null && !activeVariant.includeBaseTargetHooks.get()
        extension.native.targets.forEach { hooks ->
            if(!(skipBaseTargetHooks && hooks.name == targetArg.get())) {
                copyNamedTargetHooks(hooks, config)
            }
        }
        activeVariant?.let { variant ->
            copyHooks(variant, config.target(targetArg.get()))
            copyAndroidTargetHooks(variant.androidTargets, targetArg.get(), config)
            setOutputDirectoryPrefix(config.target(targetArg.get()), normalizeOutputDirectoryPrefix(
                variant.outputDirectoryPrefix.orNull ?: variant.variantName.get()
            ))
        }
        extension.dependencies.forEach { dependency ->
            configureDependencyReference(dependency, request, config)
            request.additionalIDLRefPaths.addAll(dependency.idlRefPaths.get().map(::normalizeProjectPath))
            copyHooks(dependency.native, config.globalHooks)
            dependency.native.targets.forEach { hooks ->
                copyNamedTargetHooks(hooks, config)
            }
        }
    }

    private fun configureTeaVMCConsumers(request: JParserBuildRequest) {
        extension.native.targets.forEach { target ->
            addTeaVMCConsumer(request, target.name, "default", target.consumer)
        }
        extension.native.variants.forEach { variant ->
            addTeaVMCConsumer(
                request,
                required(variant.targetName.orNull, "native variant ${variant.name} targetName"),
                required(variant.variantName.orNull, "native variant ${variant.name} variantName"),
                variant.consumer
            )
        }
    }

    private fun addTeaVMCConsumer(
        request: JParserBuildRequest,
        targetName: String,
        variantName: String,
        source: JParserTeaVMCConsumerHooks
    ) {
        if(!source.enabled.get()) {
            return
        }
        if(!targetName.endsWith("_teavm_c")) {
            throw GradleException("jParser TeaVM C consumer metadata cannot be declared for target '$targetName'")
        }

        val consumer = TeaVMCConsumerConfig()
        consumer.targetName = targetName
        consumer.variantName = variantName
        consumer.selectorResources.addAll(source.selectorResources.get().map(::normalizeConsumerResourcePath))
        consumer.headerDirs.addAll(source.headerDirs.get().map(::normalizeConsumerResourcePath))
        consumer.compileDefinitions.addAll(source.compileDefinitions.get().map(::requiredConsumerValue))
        consumer.compileOptions.addAll(source.compileFlags.get().map(::requiredConsumerValue))
        source.staticLibraries.forEach { library ->
            consumer.staticLibraries.add(TeaVMCConsumerConfig.StaticLibrary(
                normalizeConsumerResourcePath(library.resourcePath),
                library.overrideVariable.trim()
            ))
        }
        consumer.staticLinkLibraries.addAll(source.staticLinkLibraries.get().map(::requiredConsumerValue))
        consumer.staticLinkOptions.addAll(source.staticLinkerFlags.get().map(::requiredConsumerValue))
        request.teaVMCConsumers.add(consumer)
    }

    private fun normalizeConsumerResourcePath(value: String): String {
        val normalized = value.trim().replace('\\', '/').trim('/')
        if(normalized.isEmpty() || normalized.startsWith("../") || "/../" in normalized || normalized == "..") {
            throw GradleException("jParser TeaVM C consumer resource paths must stay inside the packaged platform directory: '$value'")
        }
        if(File(value).isAbsolute || isPortableAbsolute(value)) {
            throw GradleException("jParser TeaVM C consumer resource paths must be relative: '$value'")
        }
        return normalized
    }

    private fun requiredConsumerValue(value: String): String {
        if(value.isBlank()) {
            throw GradleException("jParser TeaVM C consumer values must not be blank")
        }
        return value
    }

    private fun findActiveVariant(): JParserNativeTargetVariantHooks? {
        val variantName = targetVariant.orNull?.takeIf { it.isNotBlank() } ?: return null
        val targetName = targetArg.get()
        return extension.native.variants.firstOrNull { variant ->
            variant.targetName.orNull == targetName && variant.variantName.orNull == variantName
        } ?: throw GradleException("No jParser native target variant '$variantName' configured for target '$targetName'")
    }

    private fun configureDependencyReference(
        dependency: JParserDependencyExtension,
        request: JParserBuildRequest,
        config: DefaultBuildTargetConfig
    ) {
        val projectReference = resolveJParserProjectReference(project, dependency)
        val libName = dependency.referenceLibName.orNull?.takeIf { it.isNotBlank() }
            ?: projectReference?.libName?.takeIf { it.isNotBlank() }
            ?: return
        val referencePackageName = dependency.referencePackageName.orNull?.takeIf { it.isNotBlank() }
            ?: projectReference?.packageName?.takeIf { it.isNotBlank() }
            ?: libName.substring(0, 1).lowercase() + libName.substring(1)
        val moduleBuildDirectory = projectReference?.builderProject?.projectDir ?: run {
            val modulePath = normalizeProjectPath(required(
                dependency.referenceModulePath.orNull,
                "dependency.${dependency.name}.referenceModulePath"
            ))
            val modulePrefix = dependency.referenceModulePrefix.orNull ?: "lib"
            File(modulePath, resolveModuleName(
                modulePrefix,
                dependency.referenceModuleBuildSuffix.orNull,
                "-build"
            ))
        }
        val moduleBuildPath = moduleBuildDirectory.absolutePath.replace('\\', '/')
        val nativeBuildPath = "$moduleBuildPath/build/c++"
        val idlName = projectReference?.idlName?.takeIf { it.isNotBlank() } ?: libName

        request.additionalIDLRefPaths.add("$moduleBuildPath/src/main/cpp/$idlName.idl")
        config.globalHooks.headerDirs.add("$moduleBuildPath/src/main/cpp/source")
        config.globalHooks.headerDirs.add("$moduleBuildPath/src/main/cpp/custom")
        projectReference?.cppSourcePath?.takeIf { it.isNotBlank() }?.let { sourcePath ->
            val sourceDirectory = if(File(sourcePath).isAbsolute) {
                File(sourcePath)
            }
            else {
                projectReference?.builderProject?.file(sourcePath) ?: project.file(sourcePath)
            }
            config.globalHooks.headerDirs.add(sourceDirectory.absolutePath.replace('\\', '/'))
        }
        projectReference?.coreProject?.let { coreProject ->
            request.additionalJavaClassPaths.add(
                coreProject.layout.buildDirectory.dir("classes/java/main").get().asFile.absolutePath
            )
        } ?: request.additionalJavaImportPackages.add(referencePackageName)

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

    private fun copyNamedTargetHooks(source: JParserNamedTargetHooks, config: DefaultBuildTargetConfig) {
        copyHooks(source, config.target(source.name))
        copyAndroidTargetHooks(source.androidTargets, source.name, config)
    }

    private fun copyAndroidTargetHooks(
        source: Iterable<JParserAndroidTargetHooks>,
        targetName: String,
        config: DefaultBuildTargetConfig
    ) {
        source.forEach { androidHooks ->
            val androidTarget = AndroidTarget.Target.valueOf(androidHooks.name)
            copyHooks(androidHooks, config.target("$targetName:${androidTarget.name}"))
        }
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
        source.outputDirectoryPrefix.orNull?.let { setOutputDirectoryPrefix(target, normalizeOutputDirectoryPrefix(it)) }
    }

    private fun normalizeOutputDirectoryPrefix(value: String): String {
        return value.trim().replace('\\', '/').trim('/')
    }

    private fun setOutputDirectoryPrefix(targetHooks: DefaultBuildTargetConfig.TargetHooks, value: String) {
        targetHooks.outputDirectoryPrefix = value
    }

    private fun required(value: String?, name: String, allowEmpty: Boolean = false): String {
        if(value == null) {
            throw GradleException("jParser.$name must be configured for task $path")
        }
        if(!allowEmpty && value.isBlank()) {
            throw GradleException("jParser.$name must be configured for task $path")
        }
        if(allowEmpty && value.isNotEmpty() && value.isBlank()) {
            throw GradleException("jParser.$name must not contain only whitespace for task $path")
        }
        return value
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
            val moduleBuildDir = File(modulePath, resolveModuleName(modulePrefix, moduleBuildSuffix, "-build"))
            return File(moduleBuildDir, value.trimStart('/', '\\')).absolutePath.replace('\\', '/')
        }
        return normalizeProjectPath(value)
    }

    private fun resolveModuleName(modulePrefix: String, moduleSuffix: String?, defaultSuffix: String): String {
        val prefix = modulePrefix.trim()
        val suffix = resolveModuleSuffix(moduleSuffix, defaultSuffix)
        if(prefix.isEmpty()) {
            return suffix.trimStart('-', '/', '\\')
        }
        return prefix + suffix
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
