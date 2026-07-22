package com.github.xpenatan.jParser.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

class JParserGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply(JavaPlugin::class.java)

        val extension = project.extensions.create<JParserExtension>("jParser", project, project.objects)
        val buildTasks = linkedMapOf<String, TaskProvider<JParserBuildTask>>()
        buildTasks[""] = registerBuildTask(
            project,
            extension,
            "jParser_generate",
            "",
            project.provider { resolveGenerateTargets(extension).map { it.arg } },
            "Generate jParser Java sources for all configured APIs."
        )

        project.afterEvaluate {
            registerNativeBuildTasks(project, extension, buildTasks)
            registerVariantBuildTasks(project, extension)
            configureTaskDependencies(project, extension, buildTasks)
        }
    }

    private fun registerNativeBuildTasks(
        project: Project,
        extension: JParserExtension,
        buildTasks: MutableMap<String, TaskProvider<JParserBuildTask>>
    ) {
        val targetHookNames = extension.native.targets.names.toSet()
        val variantTargetNames = extension.native.variants
            .mapNotNull { variant -> variant.targetName.orNull?.takeIf { it.isNotBlank() } }
            .toSet()
        val hasExplicitNativeTargets = targetHookNames.isNotEmpty() || variantTargetNames.isNotEmpty()
        buildTargets.forEach { target ->
            val targetArg = target.targetArg
            if(targetArg in variantTargetNames) {
                return@forEach
            }
            if(hasExplicitNativeTargets && targetArg !in targetHookNames) {
                return@forEach
            }
            if(!hasExplicitNativeTargets && !isBuildTargetEnabledByModule(extension, target.target)) {
                return@forEach
            }
            buildTasks[targetArg] = registerBuildTask(
                project,
                extension,
                "jParser_build_$targetArg",
                targetArg,
                project.provider { target.args },
                target.description
            )
        }
    }

    private fun isBuildTargetEnabledByModule(
        extension: JParserExtension,
        target: JParserTargets
    ): Boolean {
        return when(target) {
            JParserTargets.WEB_WASM -> extension.moduleWebSuffix.isPresent
            JParserTargets.WINDOWS64_JNI,
            JParserTargets.LINUX64_JNI,
            JParserTargets.MAC64_JNI,
            JParserTargets.MAC_ARM_JNI,
            JParserTargets.ANDROID_JNI,
            JParserTargets.IOS_JNI -> extension.moduleJNISuffix.isPresent
            JParserTargets.WINDOWS64_FFM,
            JParserTargets.LINUX64_FFM,
            JParserTargets.MAC64_FFM,
            JParserTargets.MAC_ARM_FFM -> extension.moduleFFMSuffix.isPresent
            JParserTargets.WINDOWS64_TEAVM_C,
            JParserTargets.LINUX64_TEAVM_C,
            JParserTargets.MAC64_TEAVM_C,
            JParserTargets.MAC_ARM_TEAVM_C,
            JParserTargets.ANDROID_TEAVM_C,
            JParserTargets.IOS_TEAVM_C -> extension.moduleCSuffix.isPresent
        }
    }

    private fun registerBuildTask(
        project: Project,
        extension: JParserExtension,
        taskName: String,
        targetArg: String,
        args: Provider<List<String>>,
        taskDescription: String,
        targetVariant: String = ""
    ): TaskProvider<JParserBuildTask> {
        return project.tasks.register<JParserBuildTask>(taskName) {
            group = TASK_GROUP
            description = taskDescription
            this.extension = extension
            this.targetArg.set(targetArg)
            this.targetVariant.set(targetVariant)
            this.buildArgs.convention(args)
            this.generateCore.set(true)
        }
    }

    private fun resolveGenerateTargets(extension: JParserExtension): List<JParserGenerationTarget> {
        val targetNames = extension.native.targets.names + extension.native.variants.mapNotNull { it.targetName.orNull }
        val targets = mutableListOf<JParserGenerationTarget>()

        if(extension.moduleJNISuffix.isPresent || targetNames.any { it.endsWith("_jni") }) {
            targets.add(JParserGenerationTarget.JNI)
        }
        if(extension.moduleFFMSuffix.isPresent || targetNames.any { it.endsWith("_ffm") }) {
            targets.add(JParserGenerationTarget.FFM)
        }
        if(extension.moduleWebSuffix.isPresent || targetNames.contains(JParserTargets.WEB_WASM.targetName)) {
            targets.add(JParserGenerationTarget.WEB)
        }
        if(extension.moduleCSuffix.isPresent || targetNames.any { it.endsWith("_teavm_c") }) {
            targets.add(JParserGenerationTarget.TEAVM_C)
        }

        return targets
    }

    private fun registerVariantBuildTasks(
        project: Project,
        extension: JParserExtension
    ) {
        extension.native.variants.forEach { variant ->
            val targetName = variant.targetName.orNull?.takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("jParser native target variant '${variant.name}' is missing targetName")
            val variantName = variant.variantName.orNull?.takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("jParser native target variant '${variant.name}' is missing variantName")
            val target = JParserTargets.fromTargetName(targetName)
                ?: throw IllegalArgumentException("jParser native target variant '${variant.name}' has unknown targetName '$targetName'")
            val taskProvider = registerBuildTask(
                project,
                extension,
                "jParser_build_${targetName}_${variantName}",
                targetName,
                project.provider { listOf(target.generationTarget.arg, targetName) },
                "Build jParser $targetName native library variant '$variantName'.",
                variantName
            )
            taskProvider.configure {
                dependsOn(extension.native.taskDependencies)
                if(variant.includeBaseTargetHooks.get()) {
                    extension.native.targets.findByName(targetName)?.let { hooks ->
                        dependsOn(hooks.taskDependencies)
                    }
                }
                dependsOn(variant.taskDependencies)
                extension.dependencies.forEach { dependency ->
                    dependsOn(dependency.taskDependencies)
                    dependency.referenceProjectPath.orNull?.takeIf { it.isNotBlank() }?.let { projectPath ->
                        dependsOn("$projectPath:jParser_build_$targetName")
                    }
                    if(dependency.referenceProjectPath.orNull.isNullOrBlank()) {
                        dependency.referenceIncludedBuildName.orNull?.takeIf { it.isNotBlank() }?.let { buildName ->
                            dependsOn(project.gradle.includedBuild(buildName).task(":jParser_build_$targetName"))
                        }
                    }
                    dependsOn(dependency.native.taskDependencies)
                    if(variant.includeBaseTargetHooks.get()) {
                        dependency.native.targets.findByName(targetName)?.let { hooks ->
                            dependsOn(hooks.taskDependencies)
                        }
                    }
                }
            }
        }
    }

    private fun configureTaskDependencies(
        project: Project,
        extension: JParserExtension,
        buildTasks: Map<String, TaskProvider<JParserBuildTask>>
    ) {
        buildTasks.forEach { (targetArg, taskProvider) ->
            taskProvider.configure {
                dependsOn(extension.native.taskDependencies)
                extension.native.targets.findByName(targetArg)?.let { hooks ->
                    dependsOn(hooks.taskDependencies)
                }
                extension.dependencies.forEach { dependency ->
                    dependsOn(dependency.taskDependencies)
                    val taskName = if(targetArg.isBlank()) {
                        "jParser_generate"
                    }
                    else {
                        "jParser_build_$targetArg"
                    }
                    dependency.referenceProjectPath.orNull?.takeIf { it.isNotBlank() }?.let { projectPath ->
                        dependsOn("$projectPath:$taskName")
                    }
                    if(dependency.referenceProjectPath.orNull.isNullOrBlank()) {
                        dependency.referenceIncludedBuildName.orNull?.takeIf { it.isNotBlank() }?.let { buildName ->
                            dependsOn(project.gradle.includedBuild(buildName).task(":$taskName"))
                        }
                    }
                    dependsOn(dependency.native.taskDependencies)
                    dependency.native.targets.findByName(targetArg)?.let { hooks ->
                        dependsOn(hooks.taskDependencies)
                    }
                }
            }
        }
    }

    private data class BuildTarget(
        val target: JParserTargets,
        val description: String
    ) {
        val targetArg: String = target.targetName
        val args: List<String> = listOf(target.generationTarget.arg, targetArg)
    }

    private companion object {
        const val TASK_GROUP = "jParser"
        val buildTargets = listOf(
            BuildTarget(JParserTargets.WEB_WASM, "Build jParser TeaVM web WASM side module."),
            BuildTarget(JParserTargets.WINDOWS64_JNI, "Build jParser Windows x64 JNI native library."),
            BuildTarget(JParserTargets.LINUX64_JNI, "Build jParser Linux x64 JNI native library."),
            BuildTarget(JParserTargets.MAC64_JNI, "Build jParser macOS x64 JNI native library."),
            BuildTarget(JParserTargets.MAC_ARM_JNI, "Build jParser macOS ARM JNI native library."),
            BuildTarget(JParserTargets.ANDROID_JNI, "Build jParser Android JNI native libraries."),
            BuildTarget(JParserTargets.IOS_JNI, "Build jParser iOS JNI native library."),
            BuildTarget(JParserTargets.WINDOWS64_FFM, "Build jParser Windows x64 FFM native library."),
            BuildTarget(JParserTargets.LINUX64_FFM, "Build jParser Linux x64 FFM native library."),
            BuildTarget(JParserTargets.MAC64_FFM, "Build jParser macOS x64 FFM native library."),
            BuildTarget(JParserTargets.MAC_ARM_FFM, "Build jParser macOS ARM FFM native library."),
            BuildTarget(JParserTargets.WINDOWS64_TEAVM_C, "Build jParser Windows x64 TeaVM C native library."),
            BuildTarget(JParserTargets.LINUX64_TEAVM_C, "Build jParser Linux x64 TeaVM C native library."),
            BuildTarget(JParserTargets.MAC64_TEAVM_C, "Build jParser macOS x64 TeaVM C native library."),
            BuildTarget(JParserTargets.MAC_ARM_TEAVM_C, "Build jParser macOS ARM TeaVM C native library."),
            BuildTarget(JParserTargets.ANDROID_TEAVM_C, "Build jParser Android TeaVM C native libraries."),
            BuildTarget(JParserTargets.IOS_TEAVM_C, "Build jParser iOS TeaVM C static library slices.")
        )
    }
}
