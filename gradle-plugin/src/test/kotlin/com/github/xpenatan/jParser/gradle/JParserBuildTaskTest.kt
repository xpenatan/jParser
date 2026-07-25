package com.github.xpenatan.jParser.gradle

import com.github.xpenatan.jParser.builder.tool.JParserBuildRequest
import com.github.xpenatan.jParser.builder.tool.TeaVMCLinkage
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class JParserBuildTaskTest {

    @Test
    fun globalSourceSelectionFlowsIntoBuildRequest() {
        val project = ProjectBuilder.builder().build()
        val extension = JParserExtension(project, project.objects).apply {
            libName.set("TestLib")
            modulePrefix.set("")
            packageName.set("com.example.testlib")
            runtimeHelperMode.set(true)
            native {
                includeDefaultSources.set(false)
                includeCustomSources.set(true)
                cppInclude("explicit.cpp")
                target(JParserTargets.WINDOWS64_JNI) {}
            }
        }
        val task = project.tasks.register("testJParserSourceSelection", JParserBuildTask::class.java).get().apply {
            this.extension = extension
            buildArgs.set(emptyList())
            targetArg.set(JParserTargets.WINDOWS64_JNI.targetName)
            targetVariant.set("")
            generateCore.set(true)
        }

        val createRequest = JParserBuildTask::class.java.getDeclaredMethod("createRequest")
        createRequest.isAccessible = true
        val request = createRequest.invoke(task) as JParserBuildRequest

        assertEquals(false, request.targetConfig.globalHooks.includeDefaultSources)
        assertEquals(true, request.targetConfig.globalHooks.includeCustomSources)
        assertTrue(request.targetConfig.globalHooks.cppIncludes.single().endsWith("explicit.cpp"))
        assertEquals(
            null,
            request.targetConfig.target(JParserTargets.WINDOWS64_JNI.targetName).includeDefaultSources
        )
    }

    @Test
    fun projectReferenceInfersGeneratorInputsAndConsumerMetadata() {
        val rootDir = Files.createTempDirectory("jparser-project-reference").toFile()
        val root = ProjectBuilder.builder()
            .withName("root")
            .withProjectDir(rootDir)
            .build()
        val library = ProjectBuilder.builder()
            .withName("library")
            .withParent(root)
            .withProjectDir(File(rootDir, "library").apply(File::mkdirs))
            .build()
        val builder = ProjectBuilder.builder()
            .withName("builder")
            .withParent(library)
            .withProjectDir(File(library.projectDir, "builder").apply(File::mkdirs))
            .build()
        val core = ProjectBuilder.builder()
            .withName("core")
            .withParent(library)
            .withProjectDir(File(library.projectDir, "core").apply(File::mkdirs))
            .build()
        val consumer = ProjectBuilder.builder()
            .withName("consumer")
            .withParent(root)
            .withProjectDir(File(rootDir, "consumer").apply(File::mkdirs))
            .build()

        builder.pluginManager.apply(JParserGradlePlugin::class.java)
        builder.extensions.getByType(JParserExtension::class.java).apply {
            libName.set("Referenced")
            idlName.set("ReferencedApi")
            modulePrefix.set("")
            modulePath(library.projectDir)
            moduleBuildSuffix.set("builder")
            moduleCoreSuffix.set("core")
            packageName.set("com.example.referenced")
            cppSourcePath(File(library.projectDir, "native-source"))
        }

        val extension = JParserExtension(consumer, consumer.objects).apply {
            libName.set("Consumer")
            modulePrefix.set("")
            packageName.set("com.example.consumer")
            runtimeHelperMode.set(true)
            dependency("referenced") {
                referenceProject(":library:builder")
            }
        }
        val task = consumer.tasks.register("testProjectReference", JParserBuildTask::class.java).get().apply {
            this.extension = extension
            buildArgs.set(emptyList())
            targetArg.set(JParserTargets.WINDOWS64_JNI.targetName)
            targetVariant.set("")
            generateCore.set(true)
        }

        val createRequest = JParserBuildTask::class.java.getDeclaredMethod("createRequest")
        createRequest.isAccessible = true
        val request = createRequest.invoke(task) as JParserBuildRequest

        assertEquals(
            listOf(File(builder.projectDir, "src/main/cpp/ReferencedApi.idl").absolutePath.replace('\\', '/')),
            request.additionalIDLRefPaths
        )
        assertEquals(
            listOf(core.layout.buildDirectory.dir("classes/java/main").get().asFile.absolutePath),
            request.additionalJavaClassPaths
        )
        assertTrue(request.additionalJavaImportPackages.isEmpty())
        assertTrue(request.targetConfig.globalHooks.headerDirs.contains(
            File(library.projectDir, "native-source").absolutePath.replace('\\', '/')
        ))
        assertEquals(
            listOf(File(builder.projectDir, "build/c++/libs/windows/vc/jni/Referenced64.lib")
                .absolutePath.replace('\\', '/')),
            request.targetConfig.target(JParserTargets.WINDOWS64_JNI.targetName).staticLinkerInputs
        )
    }

    @Test
    fun teaVMCLinkageDefaultsToStaticAndFlowsIntoTaskRequest() {
        val project = ProjectBuilder.builder().build()
        val extension = JParserExtension(project, project.objects).apply {
            libName.set("TestLib")
            modulePrefix.set("")
            packageName.set("com.example.testlib")
            runtimeHelperMode.set(true)
        }
        val task = project.tasks.register("testJParserBuild", JParserBuildTask::class.java).get().apply {
            this.extension = extension
            buildArgs.set(emptyList())
            targetArg.set("")
            targetVariant.set("")
            generateCore.set(true)
        }

        assertEquals(TeaVMCLinkage.STATIC, extension.teaVMCLinkage.get())

        extension.teaVMCLinkage.set(TeaVMCLinkage.RUNTIME_LOADED)
        val createRequest = JParserBuildTask::class.java.getDeclaredMethod("createRequest")
        createRequest.isAccessible = true
        val request = createRequest.invoke(task) as JParserBuildRequest

        assertEquals(TeaVMCLinkage.RUNTIME_LOADED, request.params.teaVMCLinkage)
    }

    @Test
    fun compilerFlagsFlowFromTargetHooksWithoutPlatformSpecificPolicy() {
        val project = ProjectBuilder.builder().build()
        val extension = JParserExtension(project, project.objects).apply {
            libName.set("TestLib")
            modulePrefix.set("")
            packageName.set("com.example.testlib")
            runtimeHelperMode.set(true)
            native.target(JParserTargets.WINDOWS64_TEAVM_C) {
                compileFlag("/MD")
            }
        }
        val task = project.tasks.register("testJParserCompileFlags", JParserBuildTask::class.java).get().apply {
            this.extension = extension
            buildArgs.set(emptyList())
            targetArg.set(JParserTargets.WINDOWS64_TEAVM_C.targetName)
            targetVariant.set("")
            generateCore.set(true)
        }

        val createRequest = JParserBuildTask::class.java.getDeclaredMethod("createRequest")
        createRequest.isAccessible = true
        val request = createRequest.invoke(task) as JParserBuildRequest

        assertEquals(listOf("/MD"),
            request.targetConfig.target(JParserTargets.WINDOWS64_TEAVM_C.targetName).compileFlags)
        assertEquals(emptyList<String>(), request.targetConfig.globalHooks.compileFlags)
    }

    @Test
    fun teaVMCConsumerRequirementsFlowFromEveryConfiguredVariant() {
        val project = ProjectBuilder.builder().build()
        val extension = JParserExtension(project, project.objects).apply {
            libName.set("TestLib")
            modulePrefix.set("")
            packageName.set("com.example.testlib")
            runtimeHelperMode.set(true)
            native.targetVariant(JParserTargets.WINDOWS64_TEAVM_C, "wgpu") {
                consumer {
                    selectorResource("include/webgpu/wgpu.h")
                    headerDir("include")
                    staticLibrary("deps/wgpu_native.lib", "TESTLIB_WGPU_LIBRARY")
                    staticLinkLibrary("user32.lib")
                }
            }
            native.targetVariant(JParserTargets.WINDOWS64_TEAVM_C, "dawn") {
                consumer {
                    selectorResource("include/dawn/webgpu.h")
                    compileDefinition("TESTLIB_DAWN=1")
                    compileFlag("/Zc:preprocessor")
                    staticLibrary("deps/webgpu_dawn.lib")
                }
            }
        }
        val task = project.tasks.register("testJParserConsumers", JParserBuildTask::class.java).get().apply {
            this.extension = extension
            buildArgs.set(emptyList())
            targetArg.set("")
            targetVariant.set("")
            generateCore.set(true)
        }

        val createRequest = JParserBuildTask::class.java.getDeclaredMethod("createRequest")
        createRequest.isAccessible = true
        val request = createRequest.invoke(task) as JParserBuildRequest

        assertEquals(listOf("dawn", "wgpu"), request.teaVMCConsumers.map { it.variantName }.sorted())
        val wgpu = request.teaVMCConsumers.single { it.variantName == "wgpu" }
        assertEquals(listOf("include/webgpu/wgpu.h"), wgpu.selectorResources)
        assertEquals(listOf("include"), wgpu.headerDirs)
        assertEquals("deps/wgpu_native.lib", wgpu.staticLibraries.single().resourcePath)
        assertEquals("TESTLIB_WGPU_LIBRARY", wgpu.staticLibraries.single().overrideVariable)
        assertEquals(listOf("user32.lib"), wgpu.staticLinkLibraries)

        val dawn = request.teaVMCConsumers.single { it.variantName == "dawn" }
        assertEquals(listOf("TESTLIB_DAWN=1"), dawn.compileDefinitions)
        assertEquals(listOf("/Zc:preprocessor"), dawn.compileOptions)
        assertTrue(dawn.staticLibraries.single().overrideVariable.isEmpty())
    }
}
