package com.github.xpenatan.jParser.gradle

import com.github.xpenatan.jParser.builder.tool.JParserBuildRequest
import com.github.xpenatan.jParser.builder.tool.TeaVMCLinkage
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
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
}
