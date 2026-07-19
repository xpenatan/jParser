package com.github.xpenatan.jParser.gradle

import com.github.xpenatan.jParser.builder.tool.JParserBuildRequest
import com.github.xpenatan.jParser.builder.tool.TeaVMCLinkage
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JParserBuildTaskTest {

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
