package com.github.xpenatan.jParser.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert.assertFalse
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

    @Test
    fun exposesTargetConstantsToBuildScripts() {
        val projectDir = createProject(
            """
            import com.github.xpenatan.jParser.gradle.JParserTargets

            plugins {
                id("com.github.xpenatan.jparser")
            }

            jParser {
                libName.set("TestLib")
                modulePrefix.set("lib")
                packageName.set("com.example.testlib")
                cppSourcePath.set("src/main/cpp/source/TestLib/src")

                native {
                    target(JParserTargets.WEB_WASM) {
                        compileFlag("-msimd128")
                    }
                    target(JParserTargets.WINDOWS64_JNI) {
                        compileFlag("/MP2")
                    }
                }
            }
            """.trimIndent()
        )

        val result = runner(projectDir, "tasks", "--group", "jParser", "--all", "--console=plain").build()

        assertContains(result.output, "jParser_build_${JParserTargets.WEB_WASM}")
        assertContains(result.output, "jParser_build_${JParserTargets.WINDOWS64_JNI}")
    }

    @Test
    fun supportsAndroidAbiTargetHooks() {
        val projectDir = createProject(
            """
            import com.github.xpenatan.jParser.gradle.JParserTargets

            plugins {
                id("com.github.xpenatan.jparser")
            }

            jParser {
                libName.set("TestLib")
                modulePrefix.set("lib")
                packageName.set("com.example.testlib")
                cppSourcePath.set("src/main/cpp/source/TestLib/src")

                native {
                    target(JParserTargets.ANDROID_JNI) {
                        androidTarget("armeabi_v7a") {
                            compileFlag("-DTEST_ARMV7_ONLY")
                        }
                    }
                    target(JParserTargets.ANDROID_TEAVM_C) {
                        androidTarget("armeabi_v7a") {
                            compileFlag("-DTEST_ARMV7_C_ONLY")
                        }
                    }
                }
            }
            """.trimIndent()
        )

        val result = runner(projectDir, "tasks", "--group", "jParser", "--all", "--console=plain").build()

        assertContains(result.output, "jParser_build_${JParserTargets.ANDROID_JNI}")
        assertContains(result.output, "jParser_build_${JParserTargets.ANDROID_TEAVM_C}")
    }

    @Test
    fun supportsPrefixlessModuleLayout() {
        val projectDir = createProject(
            """
            plugins {
                id("com.github.xpenatan.jparser")
            }

            jParser {
                libName.set("testlib")
                modulePrefix.set("")
                modulePath.set(layout.projectDirectory.asFile.absolutePath)
                moduleBuildSuffix.set("builder")
                moduleCoreSuffix.set("core")
                moduleJNISuffix.set("shared/jni")
                moduleFFMSuffix.set("desktop/ffm")
                moduleWebSuffix.set("web/wasm")
                moduleCSuffix.set("shared/c")
                packageName.set("com.example.testlib")
                cppSourcePath.set("source")
            }
            """.trimIndent()
        )
        File(projectDir, "builder/src/main/cpp").mkdirs()
        File(projectDir, "builder/src/main/cpp/testlib.idl").writeText(
            """
            interface TestObject {
                void TestObject();
            };
            """.trimIndent()
        )
        File(projectDir, "base/src/main/java").mkdirs()
        File(projectDir, "source").mkdirs()

        runner(projectDir, "jParser_generate", "--console=plain").build()

        assertGeneratedClass(projectDir, "core/src/main/java", "TestObject.java")
        assertGeneratedClass(projectDir, "shared/jni/src/main/java", "TestObject.java")
        assertGeneratedClass(projectDir, "desktop/ffm/src/main/java", "TestObject.java")
        assertGeneratedClass(projectDir, "web/wasm/src/main/java", "TestObject.java")
        assertGeneratedClass(projectDir, "shared/c/src/main/java", "TestObject.java")
        assertFalse(File(projectDir, "-core").exists())
        assertFalse(File(projectDir, "testlib-core").exists())
    }

    @Test
    fun supportsIDLMethodNameRenaming() {
        val projectDir = createProject(
            """
            import com.github.xpenatan.jParser.idl.IDLRenaming

            plugins {
                id("com.github.xpenatan.jparser")
            }

            jParser {
                libName.set("testlib")
                modulePrefix.set("")
                modulePath.set(layout.projectDirectory.asFile.absolutePath)
                moduleBuildSuffix.set("builder")
                moduleCoreSuffix.set("core")
                moduleJNISuffix.set("shared/jni")
                moduleFFMSuffix.set("desktop/ffm")
                moduleWebSuffix.set("web/wasm")
                moduleCSuffix.set("shared/c")
                packageName.set("com.example.testlib")
                cppSourcePath.set("source")
                idlRenaming(object : IDLRenaming {
                    override fun getIDLMethodName(methodName: String): String {
                        if(methodName.isEmpty()) {
                            return methodName
                        }
                        return methodName.substring(0, 1).lowercase() + methodName.substring(1)
                    }
                })
            }
            """.trimIndent()
        )
        File(projectDir, "builder/src/main/cpp").mkdirs()
        File(projectDir, "builder/src/main/cpp/testlib.idl").writeText(
            """
            interface TestObject {
                void TestObject();
                void DoThing();
            };
            """.trimIndent()
        )
        File(projectDir, "base/src/main/java").mkdirs()
        File(projectDir, "source").mkdirs()

        runner(projectDir, "jParser_generate", "--console=plain").build()

        val generated = findGeneratedClass(projectDir, "core/src/main/java", "TestObject.java").readText()
        assertTrue(generated.contains("public void doThing("))
        assertFalse(generated.contains("public void DoThing("))
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

    private fun assertGeneratedClass(projectDir: File, moduleJavaDir: String, className: String) {
        findGeneratedClass(projectDir, moduleJavaDir, className)
    }

    private fun findGeneratedClass(projectDir: File, moduleJavaDir: String, className: String): File {
        val rootDir = File(projectDir, moduleJavaDir)
        val generated = rootDir.walkTopDown().firstOrNull { it.isFile && it.name == className }
        assertTrue("Expected $className under ${rootDir.absolutePath}", generated != null)
        return generated!!
    }
}
