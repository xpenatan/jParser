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
        val buildTasks = registerBuildTasks(project, extension)

        project.afterEvaluate {
            configureTaskDependencies(project, extension, buildTasks)
        }
    }

    private fun registerBuildTasks(
        project: Project,
        extension: JParserExtension
    ): Map<String, TaskProvider<JParserBuildTask>> {
        val tasks = linkedMapOf<String, TaskProvider<JParserBuildTask>>()
        tasks[""] = registerBuildTask(
            project,
            extension,
            "jParser_generate",
            "",
            project.provider { resolveGenerateArgs(extension) },
            "Generate jParser Java sources for all configured APIs."
        )
        val targets = listOf(
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
            BuildTarget(JParserTargets.IOS_TEAVM_C, "Build jParser iOS TeaVM C native library.")
        )
        targets.forEach { target ->
            tasks[target.targetArg] = registerBuildTask(
                project,
                extension,
                "jParser_build_${target.targetArg}",
                target.targetArg,
                project.provider { target.args },
                target.description
            )
        }
        return tasks
    }

    private fun registerBuildTask(
        project: Project,
        extension: JParserExtension,
        taskName: String,
        targetArg: String,
        args: Provider<List<String>>,
        taskDescription: String
    ): TaskProvider<JParserBuildTask> {
        return project.tasks.register<JParserBuildTask>(taskName) {
            group = TASK_GROUP
            description = taskDescription
            this.extension = extension
            this.targetArg.set(targetArg)
            this.buildArgs.convention(args)
            this.generateCore.set(targetArg.isBlank())
        }
    }

    private fun resolveGenerateArgs(extension: JParserExtension): List<String> {
        val targetNames = extension.native.targets.names
        val args = mutableListOf<String>()

        if(extension.moduleJNISuffix.isPresent || targetNames.any { it.endsWith("_jni") }) {
            args.add("gen_jni")
        }
        if(extension.moduleFFMSuffix.isPresent || targetNames.any { it.endsWith("_ffm") }) {
            args.add("gen_ffm")
        }
        if(extension.moduleWebSuffix.isPresent || targetNames.contains(JParserTargets.WEB_WASM)) {
            args.add("gen_web")
        }
        if(extension.moduleCSuffix.isPresent || targetNames.any { it.endsWith("_teavm_c") }) {
            args.add("gen_teavm_c")
        }

        return args
    }

    private fun configureTaskDependencies(
        project: Project,
        extension: JParserExtension,
        buildTasks: Map<String, TaskProvider<JParserBuildTask>>
    ) {
        buildTasks.forEach { (targetArg, taskProvider) ->
            taskProvider.configure {
                if(targetArg.isNotBlank()) {
                    dependsOn(buildTasks.getValue(""))
                }
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
        val targetArg: String,
        val description: String
    ) {
        val args: List<String> = listOf(targetArg)
    }

    private companion object {
        const val TASK_GROUP = "jParser"
    }
}
