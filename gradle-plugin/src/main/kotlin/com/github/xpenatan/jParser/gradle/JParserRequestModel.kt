package com.github.xpenatan.jParser.gradle

import java.util.Properties

internal class JParserBuildRequest {
    val params = JParserBuildParams()
    val targetConfig = DefaultBuildTargetConfig()
    val additionalIDLPaths = mutableListOf<String>()
    val additionalIDLRefPaths = mutableListOf<String>()
    val additionalSourceDirs = mutableListOf<String>()
    val additionalJavaImportPackages = mutableListOf<String>()
    val additionalJavaClassPaths = mutableListOf<String>()
    val teaVMCConsumers = mutableListOf<TeaVMCConsumerConfig>()

    var keepGeneratedCommandComments = false
    var idlRenaming: IDLRenaming? = null
    var jniSymbolNameMode: JParserSymbolNameMode? = null
    var ffmSymbolNameMode: JParserSymbolNameMode? = null
    var teaVMCSymbolNameMode: JParserSymbolNameMode? = null
    var ffmLogMethod = false
    var ffmDefaultCritical = false
    var generateCore = true

    fun toProperties(): Properties {
        val properties = Properties()
        properties.value("jparser.generateCore", generateCore)
        properties.value("jparser.libName", params.libName)
        properties.value("jparser.idlName", params.idlName ?: "")
        properties.value("jparser.webModuleName", params.webModuleName)
        properties.value("jparser.packageName", params.packageName)
        properties.value("jparser.modulePrefix", params.modulePrefix)
        properties.value("jparser.cppSourcePath", params.cppSourcePath)
        properties.value("jparser.modulePath", params.modulePath)
        properties.value("jparser.moduleBaseSuffix", params.moduleBaseSuffix)
        properties.value("jparser.moduleBuildSuffix", params.moduleBuildSuffix)
        properties.value("jparser.moduleCoreSuffix", params.moduleCoreSuffix)
        properties.value("jparser.moduleJNISuffix", params.moduleJNISuffix)
        properties.value("jparser.moduleWebSuffix", params.moduleWebSuffix)
        properties.value("jparser.moduleFFMSuffix", params.moduleFFMSuffix)
        properties.value("jparser.moduleCSuffix", params.moduleCSuffix)
        properties.value("jparser.teaVMCLinkage", params.teaVMCLinkage)

        properties.value("jparser.keepGeneratedCommandComments", keepGeneratedCommandComments)
        properties.value("jparser.jniSymbolNameMode", jniSymbolNameMode)
        properties.value("jparser.ffmSymbolNameMode", ffmSymbolNameMode)
        properties.value("jparser.teaVMCSymbolNameMode", teaVMCSymbolNameMode)
        properties.value("jparser.ffmLogMethod", ffmLogMethod)
        properties.value("jparser.ffmDefaultCritical", ffmDefaultCritical)

        properties.value("jparser.addRuntimeHelperIDL", targetConfig.addRuntimeHelperIDL)
        properties.value("jparser.runtimeHelperMode", targetConfig.runtimeHelperMode)
        properties.value("jparser.windowsDebugBuild", targetConfig.windowsDebugBuild)
        properties.value("jparser.jniCppStandard", targetConfig.jniCppStandard)
        properties.value("jparser.ffmCppStandard", targetConfig.ffmCppStandard)
        properties.value("jparser.teaVMCCppStandard", targetConfig.teaVMCCppStandard)
        properties.value("jparser.webCppStandard", targetConfig.webCppStandard)
        properties.value("jparser.sourceLanguage", targetConfig.sourceLanguage)
        properties.value("jparser.cStandard", targetConfig.cStandard)
        properties.value("jparser.webMainModuleName", targetConfig.webMainModuleName)
        properties.value("jparser.webSideModule", targetConfig.webSideModule)
        properties.value("jparser.webForcedInclude", targetConfig.webForcedInclude)
        properties.value("jparser.webMainModule", targetConfig.webMainModule)
        properties.lines("jparser.webExportedFunctions", targetConfig.webExportedFunctions)
        properties.lines("jparser.webExportedRuntimeMethods", targetConfig.webExportedRuntimeMethods)
        properties.value("jparser.androidApiLevel", targetConfig.androidApiLevel)
        properties.value("jparser.androidTargets", targetConfig.androidTargets.joinToString(",") { it.name })

        properties.lines("jparser.additionalIDLPaths", additionalIDLPaths)
        properties.lines("jparser.additionalIDLRefPaths", additionalIDLRefPaths)
        properties.lines("jparser.additionalSourceDirs", additionalSourceDirs)
        properties.lines("jparser.additionalJavaImportPackages", additionalJavaImportPackages)
        properties.lines("jparser.additionalJavaClassPaths", additionalJavaClassPaths)

        properties.hooks("jparser.native", targetConfig.globalHooks)
        val targetNames = linkedSetOf<String>()
        targetConfig.targetHooks.forEach { (targetKey, hooks) ->
            val separator = targetKey.indexOf(':')
            val targetName = if(separator == -1) targetKey else targetKey.substring(0, separator)
            val propertySuffix = if(separator == -1) {
                targetName
            }
            else {
                "$targetName.${targetKey.substring(separator + 1)}"
            }
            targetNames.add(targetName)
            properties.hooks("jparser.native.$propertySuffix", hooks)
        }
        properties.value("jparser.native.targets", targetNames.joinToString(","))

        properties.value("jparser.teaVMCConsumers.count", teaVMCConsumers.size)
        teaVMCConsumers.forEachIndexed { consumerIndex, consumer ->
            val prefix = "jparser.teaVMCConsumers.$consumerIndex"
            properties.value("$prefix.targetName", consumer.targetName)
            properties.value("$prefix.variantName", consumer.variantName)
            properties.lines("$prefix.selectorResources", consumer.selectorResources)
            properties.lines("$prefix.headerDirs", consumer.headerDirs)
            properties.lines("$prefix.compileDefinitions", consumer.compileDefinitions)
            properties.lines("$prefix.compileOptions", consumer.compileOptions)
            properties.lines("$prefix.staticLinkLibraries", consumer.staticLinkLibraries)
            properties.lines("$prefix.staticLinkOptions", consumer.staticLinkOptions)
            properties.value("$prefix.staticLibraries.count", consumer.staticLibraries.size)
            consumer.staticLibraries.forEachIndexed { libraryIndex, library ->
                properties.value("$prefix.staticLibraries.$libraryIndex.resourcePath", library.resourcePath)
                properties.value("$prefix.staticLibraries.$libraryIndex.overrideVariable", library.overrideVariable)
            }
        }
        return properties
    }
}

internal class JParserBuildParams {
    var libName: String? = null
    var idlName: String? = null
    var webModuleName: String? = null
    var packageName: String? = null
    var modulePrefix: String? = null
    var cppSourcePath: String? = null
    var modulePath: String? = null
    var moduleBaseSuffix: String? = null
    var moduleBuildSuffix: String? = null
    var moduleCoreSuffix: String? = null
    var moduleJNISuffix: String? = null
    var moduleWebSuffix: String? = null
    var moduleFFMSuffix: String? = null
    var moduleCSuffix: String? = null
    var teaVMCLinkage: TeaVMCLinkage = TeaVMCLinkage.STATIC
}

internal class DefaultBuildTargetConfig {
    var addRuntimeHelperIDL = true
    var windowsDebugBuild = false
    var runtimeHelperMode = false
    var jniCppStandard = "c++11"
    var ffmCppStandard = "c++11"
    var teaVMCCppStandard = "c++17"
    var webCppStandard = "c++11"
    var sourceLanguage = SourceLanguage.CPP
    var cStandard = "c17"
    var webMainModuleName = "runtime"
    var webSideModule = 2
    var webForcedInclude: String? = null
    var webMainModule = false
    val webExportedFunctions = mutableListOf<String>()
    val webExportedRuntimeMethods = mutableListOf<String>()
    var androidApiLevel = AndroidTarget.ApiLevel.Android_10_29
    val androidTargets = mutableListOf(
        AndroidTarget.Target.x86,
        AndroidTarget.Target.x86_64,
        AndroidTarget.Target.armeabi_v7a,
        AndroidTarget.Target.arm64_v8a
    )
    val globalHooks = TargetHooks()
    internal val targetHooks = linkedMapOf<String, TargetHooks>()

    fun target(targetArg: String): TargetHooks {
        return targetHooks.getOrPut(targetArg, ::TargetHooks)
    }

    class TargetHooks {
        val headerDirs = mutableListOf<String>()
        val cppIncludes = mutableListOf<String>()
        val cppExcludes = mutableListOf<String>()
        val compileFlags = mutableListOf<String>()
        val linkerFlags = mutableListOf<String>()
        val staticLinkerInputs = mutableListOf<String>()
        val sharedLinkerInputs = mutableListOf<String>()
        val forcedIncludes = mutableListOf<String>()
        val webExportedFunctions = mutableListOf<String>()
        val webExportedRuntimeMethods = mutableListOf<String>()
        var includeDefaultSources: Boolean? = null
        var includeCustomSources: Boolean? = null
        var webSideModule: Int? = null
        var webMainModuleName: String? = null
        var outputDirectoryPrefix: String? = null
    }
}

internal class TeaVMCConsumerConfig {
    var targetName: String = ""
    var variantName: String = ""
    val selectorResources = mutableListOf<String>()
    val headerDirs = mutableListOf<String>()
    val compileDefinitions = mutableListOf<String>()
    val compileOptions = mutableListOf<String>()
    val staticLibraries = mutableListOf<StaticLibrary>()
    val staticLinkLibraries = mutableListOf<String>()
    val staticLinkOptions = mutableListOf<String>()

    data class StaticLibrary(
        val resourcePath: String,
        val overrideVariable: String
    )
}

private fun Properties.value(name: String, value: Any?) {
    if(value != null) {
        setProperty(name, if(value is Enum<*>) value.name else value.toString())
    }
}

private fun Properties.lines(name: String, values: Iterable<String>) {
    val encoded = values.joinToString("\n")
    if(encoded.isNotEmpty()) {
        setProperty(name, encoded)
    }
}

private fun Properties.hooks(prefix: String, hooks: DefaultBuildTargetConfig.TargetHooks) {
    lines("$prefix.headerDirs", hooks.headerDirs)
    lines("$prefix.cppIncludes", hooks.cppIncludes)
    lines("$prefix.cppExcludes", hooks.cppExcludes)
    lines("$prefix.compileFlags", hooks.compileFlags)
    lines("$prefix.linkerFlags", hooks.linkerFlags)
    lines("$prefix.staticLinkerInputs", hooks.staticLinkerInputs)
    lines("$prefix.sharedLinkerInputs", hooks.sharedLinkerInputs)
    lines("$prefix.forcedIncludes", hooks.forcedIncludes)
    lines("$prefix.webExportedFunctions", hooks.webExportedFunctions)
    lines("$prefix.webExportedRuntimeMethods", hooks.webExportedRuntimeMethods)
    value("$prefix.includeDefaultSources", hooks.includeDefaultSources)
    value("$prefix.includeCustomSources", hooks.includeCustomSources)
    value("$prefix.webSideModule", hooks.webSideModule)
    value("$prefix.webMainModuleName", hooks.webMainModuleName)
    value("$prefix.outputDirectoryPrefix", hooks.outputDirectoryPrefix)
}
