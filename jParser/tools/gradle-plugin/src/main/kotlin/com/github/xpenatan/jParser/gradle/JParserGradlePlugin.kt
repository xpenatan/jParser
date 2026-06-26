package com.github.xpenatan.jParser.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
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
            listOf("gen_jni", "gen_ffm", "gen_web", "gen_teavm_c"),
            "Generate jParser Java sources for all configured APIs."
        )
        val targets = listOf(
            BuildTarget("web_wasm", listOf("web_wasm"), "Build jParser TeaVM web WASM side module."),
            BuildTarget("windows64_jni", listOf("windows64_jni"), "Build jParser Windows x64 JNI native library."),
            BuildTarget("linux64_jni", listOf("linux64_jni"), "Build jParser Linux x64 JNI native library."),
            BuildTarget("mac64_jni", listOf("mac64_jni"), "Build jParser macOS x64 JNI native library."),
            BuildTarget("macArm_jni", listOf("macArm_jni"), "Build jParser macOS ARM JNI native library."),
            BuildTarget("android_jni", listOf("android_jni"), "Build jParser Android JNI native libraries."),
            BuildTarget("ios_jni", listOf("ios_jni"), "Build jParser iOS JNI native library."),
            BuildTarget("windows64_ffm", listOf("windows64_ffm"), "Build jParser Windows x64 FFM native library."),
            BuildTarget("linux64_ffm", listOf("linux64_ffm"), "Build jParser Linux x64 FFM native library."),
            BuildTarget("mac64_ffm", listOf("mac64_ffm"), "Build jParser macOS x64 FFM native library."),
            BuildTarget("macArm_ffm", listOf("macArm_ffm"), "Build jParser macOS ARM FFM native library."),
            BuildTarget("windows64_teavm_c", listOf("windows64_teavm_c"), "Build jParser Windows x64 TeaVM C native library."),
            BuildTarget("linux64_teavm_c", listOf("linux64_teavm_c"), "Build jParser Linux x64 TeaVM C native library."),
            BuildTarget("mac64_teavm_c", listOf("mac64_teavm_c"), "Build jParser macOS x64 TeaVM C native library."),
            BuildTarget("macArm_teavm_c", listOf("macArm_teavm_c"), "Build jParser macOS ARM TeaVM C native library."),
            BuildTarget("android_teavm_c", listOf("android_teavm_c"), "Build jParser Android TeaVM C native libraries."),
            BuildTarget("ios_teavm_c", listOf("ios_teavm_c"), "Build jParser iOS TeaVM C native library.")
        )
        targets.forEach { target ->
            tasks[target.targetArg] = registerBuildTask(
                project,
                extension,
                "jParser_build_${target.targetArg}",
                target.targetArg,
                target.args,
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
        args: List<String>,
        taskDescription: String
    ): TaskProvider<JParserBuildTask> {
        return project.tasks.register<JParserBuildTask>(taskName) {
            group = TASK_GROUP
            description = taskDescription
            this.extension = extension
            this.targetArg.set(targetArg)
            this.buildArgs.set(args)
            this.generateCore.set(targetArg.isBlank())
        }
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
        val args: List<String>,
        val description: String
    )

    private companion object {
        const val TASK_GROUP = "jParser"
    }
}
