package com.github.xpenatan.jParser.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.io.File

internal data class JParserProjectReference(
    val builderProject: Project,
    val coreProject: Project?,
    val libName: String?,
    val idlName: String?,
    val packageName: String?,
    val cppSourcePath: String?
)

internal fun resolveJParserProjectReference(
    consumerProject: Project,
    dependency: JParserDependencyExtension
): JParserProjectReference? {
    val projectPath = dependency.referenceProjectPath.orNull?.takeIf { it.isNotBlank() } ?: return null
    val builderProject = consumerProject.project(projectPath)
    // Gradle can load the same external plugin through project-specific classloaders.
    // Resolve the public DSL by name/getters so cross-project references remain reliable.
    val extension = builderProject.extensions.findByName("jParser") ?: return null
    val modulePath = (extension.providerValue("modulePath") as? String)
        ?.let { value -> if(File(value).isAbsolute) File(value) else builderProject.file(value) }
        ?: builderProject.projectDir.parentFile
    val coreDirectory = File(
        modulePath,
        resolveReferenceModuleName(
            extension.providerValue("modulePrefix") as? String ?: "",
            extension.providerValue("moduleCoreSuffix") as? String,
            "-core"
        )
    ).canonicalFile
    val coreProject = consumerProject.rootProject.allprojects.firstOrNull { candidate ->
        candidate.projectDir.canonicalFile == coreDirectory
    }
    return JParserProjectReference(
        builderProject = builderProject,
        coreProject = coreProject,
        libName = extension.providerValue("libName") as? String,
        idlName = extension.providerValue("idlName") as? String,
        packageName = extension.providerValue("packageName") as? String,
        cppSourcePath = extension.providerValue("cppSourcePath") as? String
    )
}

internal fun Any.beanValue(name: String): Any? {
    val getterName = "get" + name.replaceFirstChar { it.uppercaseChar() }
    return javaClass.methods.firstOrNull { method ->
        method.name == getterName && method.parameterCount == 0
    }?.invoke(this)
}

internal fun Any.providerValue(name: String): Any? {
    return (beanValue(name) as? Provider<*>)?.orNull
}

private fun resolveReferenceModuleName(prefixValue: String, suffixValue: String?, defaultSuffix: String): String {
    val prefix = prefixValue.trim()
    val suffix = suffixValue?.trim().takeUnless { it.isNullOrEmpty() } ?: defaultSuffix
    val normalizedSuffix = if(suffix.startsWith("-")) suffix else "-$suffix"
    return if(prefix.isEmpty()) {
        normalizedSuffix.trimStart('-', '/', '\\')
    }
    else {
        prefix + normalizedSuffix
    }
}
