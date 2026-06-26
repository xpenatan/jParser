package com.github.xpenatan.jParser.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class JParserGradlePluginTest {

    @Test
    fun registersGeneratedTasks() {
        val projectDir = createProject(
            """
            import com.github.xpenatan.jParser.builder.tool.JParserSymbolNameMode

            plugins {
                id("com.github.xpenatan.jparser")
            }

            jParser {
                libName.set("TestLib")
                modulePrefix.set("lib")
                packageName.set("com.example.testlib")
                cppSourcePath.set("src/main/cpp/source/TestLib/src")
                jniSymbolNameMode.set(JParserSymbolNameMode.OBFUSCATED)
                ffmSymbolNameMode.set(JParserSymbolNameMode.OBFUSCATED)
                teaVMCSymbolNameMode.set(JParserSymbolNameMode.OBFUSCATED)
            }
            """.trimIndent()
        )

        val result = runner(projectDir, "tasks", "--group", "jParser", "--all", "--console=plain").build()

        assertContains(result.output, "jParser_generate")
        assertContains(result.output, "jParser_build_web_wasm")
        assertContains(result.output, "jParser_build_windows64_jni")
        assertContains(result.output, "jParser_build_linux64_ffm")
        assertContains(result.output, "jParser_build_android_teavm_c")
    }

    @Test
    fun failsWithUsefulMessageWhenRequiredDslIsMissing() {
        val projectDir = createProject(
            """
            plugins {
                id("com.github.xpenatan.jparser")
            }
            """.trimIndent()
        )

        val result = runner(projectDir, "jParser_generate", "--stacktrace").buildAndFail()

        assertContains(result.output, "jParser.libName must be configured")
    }

    private fun createProject(buildFile: String): File {
        val projectDir = Files.createTempDirectory("jparser-gradle-plugin-test").toFile()
        File(projectDir, "settings.gradle.kts").writeText("")
        File(projectDir, "build.gradle.kts").writeText(buildFile)
        return projectDir
    }

    private fun runner(projectDir: File, vararg args: String): GradleRunner {
        return GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(*args)
            .withPluginClasspath()
    }

    private fun assertContains(output: String, expected: String) {
        assertTrue("Expected output to contain '$expected'.\n$output", output.contains(expected))
    }
}
