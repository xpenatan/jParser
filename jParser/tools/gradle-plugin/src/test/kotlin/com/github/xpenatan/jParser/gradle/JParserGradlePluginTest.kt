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
                moduleJNISuffix.set("-jni")
                moduleFFMSuffix.set("-ffm")
                moduleWebSuffix.set("-web")
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
        assertDoesNotContainTask(result.output, "jParser_build_android_teavm_c")
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
    fun supportsAndroidAbiHooksOnNativeTargetVariants() {
        val projectDir = createProject(
            """
            import com.github.xpenatan.jParser.builder.targets.AndroidTarget
            import com.github.xpenatan.jParser.builder.tool.JParserBuildRequest
            import com.github.xpenatan.jParser.gradle.JParserBuildTask
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
                        compileFlag("-DBASE_ROOT")
                        androidTarget(AndroidTarget.Target.arm64_v8a) {
                            compileFlag("-DBASE_ARM64")
                        }
                        androidTarget("x86_64") {
                            compileFlag("-DBASE_X86_64")
                        }
                    }
                    targetVariant(JParserTargets.ANDROID_JNI, "wgpu") {
                        compileFlag("-DWGPU_ROOT")
                        androidTarget(AndroidTarget.Target.arm64_v8a) {
                            compileFlag("-DWGPU_ARM64")
                        }
                        androidTarget("x86_64") {
                            compileFlag("-DWGPU_X86_64")
                        }
                    }
                    targetVariant(JParserTargets.ANDROID_JNI, "dawn") {
                        includeBaseTargetHooks.set(true)
                        compileFlag("-DDAWN_ROOT")
                        androidTarget(AndroidTarget.Target.arm64_v8a) {
                            compileFlag("-DDAWN_ARM64")
                        }
                    }
                }
            }

            tasks.register("verifyAndroidVariantHooks") {
                doLast {
                    check(tasks.findByName("jParser_build_android_jni") == null)

                    fun request(taskName: String): JParserBuildRequest {
                        val task = tasks.named<JParserBuildTask>(taskName).get()
                        val method = JParserBuildTask::class.java.getDeclaredMethod("createRequest")
                        method.isAccessible = true
                        return method.invoke(task) as JParserBuildRequest
                    }

                    fun compileFlags(request: JParserBuildRequest, targetName: String): List<String> {
                        return request.targetConfig.findTarget(targetName)?.compileFlags?.toList().orEmpty()
                    }

                    val wgpu = request("jParser_build_android_jni_wgpu")
                    check(compileFlags(wgpu, "android_jni") == listOf("-DWGPU_ROOT"))
                    check(compileFlags(wgpu, "android_jni:arm64_v8a") == listOf("-DWGPU_ARM64"))
                    check(compileFlags(wgpu, "android_jni:x86_64") == listOf("-DWGPU_X86_64"))
                    check(wgpu.targetConfig.findTarget("android_jni")?.outputDirectoryPrefix == "wgpu")

                    val dawn = request("jParser_build_android_jni_dawn")
                    check(compileFlags(dawn, "android_jni") == listOf("-DBASE_ROOT", "-DDAWN_ROOT"))
                    check(compileFlags(dawn, "android_jni:arm64_v8a") == listOf("-DBASE_ARM64", "-DDAWN_ARM64"))
                    check(compileFlags(dawn, "android_jni:x86_64") == listOf("-DBASE_X86_64"))
                    check(dawn.targetConfig.findTarget("android_jni")?.outputDirectoryPrefix == "dawn")

                    println("Verified Android variant ABI hook inheritance")
                }
            }
            """.trimIndent()
        )

        val result = runner(projectDir, "verifyAndroidVariantHooks", "--console=plain").build()

        assertContains(result.output, "Verified Android variant ABI hook inheritance")
    }

    @Test
    fun registersExplicitTeaVMCTargetWithoutModuleCSuffix() {
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
                    target(JParserTargets.ANDROID_TEAVM_C) {
                        compileFlag("-DTEST_TEAVM_C")
                    }
                }
            }
            """.trimIndent()
        )

        val result = runner(projectDir, "tasks", "--group", "jParser", "--all", "--console=plain").build()

        assertContains(result.output, "jParser_build_${JParserTargets.ANDROID_TEAVM_C}")
    }

    @Test
    fun registersOnlyExplicitNativeTargetsWhenAnyNativeTargetIsConfigured() {
        val projectDir = createProject(
            """
            import com.github.xpenatan.jParser.gradle.JParserTargets

            plugins {
                id("com.github.xpenatan.jparser")
            }

            jParser {
                libName.set("TestLib")
                modulePrefix.set("lib")
                moduleJNISuffix.set("-jni")
                moduleFFMSuffix.set("-ffm")
                moduleWebSuffix.set("-web")
                packageName.set("com.example.testlib")
                cppSourcePath.set("src/main/cpp/source/TestLib/src")

                native {
                    listOf(JParserTargets.WINDOWS64_JNI, JParserTargets.WINDOWS64_FFM).forEach { targetName ->
                        target(targetName) {
                            compileFlag("-DROOT_HOOK")
                        }
                        targetVariant(targetName, "wgpu") {
                            compileFlag("-DWGPU")
                        }
                        targetVariant(targetName, "dawn") {
                            compileFlag("-DDAWN")
                        }
                    }
                    target(JParserTargets.LINUX64_JNI) {}
                    target(JParserTargets.LINUX64_FFM) {}
                    target(JParserTargets.ANDROID_JNI) {}
                    target(JParserTargets.WEB_WASM) {}
                }
            }
            """.trimIndent()
        )

        val result = runner(projectDir, "tasks", "--group", "jParser", "--all", "--console=plain").build()

        assertContains(result.output, "jParser_build_${JParserTargets.WINDOWS64_JNI}_wgpu")
        assertContains(result.output, "jParser_build_${JParserTargets.WINDOWS64_JNI}_dawn")
        assertContains(result.output, "jParser_build_${JParserTargets.WINDOWS64_FFM}_wgpu")
        assertContains(result.output, "jParser_build_${JParserTargets.WINDOWS64_FFM}_dawn")
        assertContains(result.output, "jParser_build_${JParserTargets.LINUX64_JNI}")
        assertContains(result.output, "jParser_build_${JParserTargets.LINUX64_FFM}")
        assertContains(result.output, "jParser_build_${JParserTargets.ANDROID_JNI}")
        assertContains(result.output, "jParser_build_${JParserTargets.WEB_WASM}")
        assertDoesNotContainTask(result.output, "jParser_build_${JParserTargets.WINDOWS64_JNI}")
        assertDoesNotContainTask(result.output, "jParser_build_${JParserTargets.WINDOWS64_FFM}")
        assertDoesNotContainTask(result.output, "jParser_build_${JParserTargets.IOS_JNI}")
        assertDoesNotContainTask(result.output, "jParser_build_${JParserTargets.MAC64_JNI}")
        assertDoesNotContainTask(result.output, "jParser_build_${JParserTargets.ANDROID_TEAVM_C}")
        assertDoesNotContainTask(result.output, "jParser_build_${JParserTargets.LINUX64_TEAVM_C}")
    }

    @Test
    fun scopesNativeBuildTaskGenerationToItsBindingFamily() {
        val projectDir = createProject(
            """
            import com.github.xpenatan.jParser.gradle.JParserBuildTask
            import com.github.xpenatan.jParser.gradle.JParserTargets

            plugins {
                id("com.github.xpenatan.jparser")
            }

            jParser {
                libName.set("testlib")
                modulePrefix.set("lib")
                packageName.set("com.example.testlib")
                cppSourcePath.set("source")

                native {
                    target(JParserTargets.ANDROID_JNI) {}
                    target(JParserTargets.WINDOWS64_FFM) {}
                    target(JParserTargets.WEB_WASM) {}
                    target(JParserTargets.ANDROID_TEAVM_C) {}
                    targetVariant(JParserTargets.LINUX64_JNI, "wgpu") {}
                }
            }

            tasks.register("verifyScopedNativeBuildTasks") {
                doLast {
                    fun verifyBuildTask(taskName: String, expectedArgs: List<String>) {
                        val buildTask = tasks.getByName(taskName) as JParserBuildTask
                        check(buildTask.buildArgs.get() == expectedArgs) {
                            "Expected " + taskName + " buildArgs=" + expectedArgs +
                                " but was " + buildTask.buildArgs.get()
                        }
                        check(buildTask.generateCore.get()) {
                            "Expected " + taskName + " to generate core sources"
                        }
                        val dependencyPaths = buildTask.taskDependencies
                            .getDependencies(buildTask)
                            .map { it.path }
                        check(":jParser_generate" !in dependencyPaths) {
                            "Expected " + taskName + " to not depend on jParser_generate, dependencies=" + dependencyPaths
                        }
                    }

                    verifyBuildTask(
                        "jParser_build_android_jni",
                        listOf("gen_jni", "android_jni")
                    )
                    verifyBuildTask(
                        "jParser_build_windows64_ffm",
                        listOf("gen_ffm", "windows64_ffm")
                    )
                    verifyBuildTask(
                        "jParser_build_web_wasm",
                        listOf("gen_web", "web_wasm")
                    )
                    verifyBuildTask(
                        "jParser_build_android_teavm_c",
                        listOf("gen_teavm_c", "android_teavm_c")
                    )
                    verifyBuildTask(
                        "jParser_build_linux64_jni_wgpu",
                        listOf("gen_jni", "linux64_jni")
                    )

                    val generateTask = tasks.getByName("jParser_generate") as JParserBuildTask
                    check(
                        generateTask.buildArgs.get() ==
                            listOf("gen_jni", "gen_ffm", "gen_web", "gen_teavm_c")
                    ) {
                        "Unexpected jParser_generate buildArgs=" + generateTask.buildArgs.get()
                    }
                    check(generateTask.generateCore.get()) {
                        "Expected jParser_generate to generate core sources"
                    }

                    println("Verified family-scoped native build tasks")
                }
            }
            """.trimIndent()
        )

        val result = runner(projectDir, "verifyScopedNativeBuildTasks", "--console=plain").build()

        assertContains(result.output, "Verified family-scoped native build tasks")
    }

    @Test
    fun supportsTypedAndroidEnums() {
        val projectDir = createProject(
            """
            import com.github.xpenatan.jParser.builder.targets.AndroidTarget
            import com.github.xpenatan.jParser.gradle.JParserTargets

            plugins {
                id("com.github.xpenatan.jparser")
            }

            jParser {
                libName.set("testlib")
                modulePrefix.set("lib")
                modulePath.set(layout.projectDirectory.asFile.absolutePath)
                packageName.set("com.example.testlib")
                cppSourcePath.set("source")
                androidApiLevel.set(AndroidTarget.ApiLevel.Android_13_33)
                androidTargets.set(listOf(AndroidTarget.Target.arm64_v8a, AndroidTarget.Target.x86_64))

                native {
                    target(JParserTargets.ANDROID_JNI) {
                        androidTarget(AndroidTarget.Target.arm64_v8a) {
                            compileFlag("-DTEST_ARM64_ONLY")
                        }
                    }
                }
            }
            """.trimIndent()
        )
        File(projectDir, "lib-build/src/main/cpp").mkdirs()
        File(projectDir, "lib-build/src/main/cpp/testlib.idl").writeText(
            """
            interface TestObject {
                void TestObject();
            };
            """.trimIndent()
        )
        File(projectDir, "lib-base/src/main/java").mkdirs()
        File(projectDir, "source").mkdirs()

        runner(projectDir, "jParser_generate", "--console=plain").build()

        assertGeneratedClass(projectDir, "lib-core/src/main/java", "TestObject.java")
        assertGeneratedClass(projectDir, "lib-jni/src/main/java", "TestObject.java")
    }

    @Test
    fun supportsGradleFilePathInputs() {
        val projectDir = createProject(
            """
            import com.github.xpenatan.jParser.gradle.JParserTargets

            plugins {
                id("com.github.xpenatan.jparser")
            }

            jParser {
                libName.set("testlib")
                modulePrefix.set("")
                modulePath(layout.projectDirectory)
                moduleBuildSuffix.set("builder")
                moduleCoreSuffix.set("core")
                packageName.set("com.example.testlib")
                cppSourcePath(file("source").toPath())
                webForcedInclude(layout.projectDirectory.file("include/web_force.h"))
                additionalIDLPath(layout.projectDirectory.file("builder/src/main/cpp/extra.idl"))
                additionalIDLRefPath(file("builder/src/main/cpp/ref.idl"))
                additionalSourceDir(layout.buildDirectory.dir("generated-native"))

                dependency("dep") {
                    referenceLibName.set("dep")
                    referenceModulePath(layout.projectDirectory.dir("dep"))
                    idlRefPath(layout.projectDirectory.file("dep/builder/src/main/cpp/dep.idl"))
                }

                native {
                    target(JParserTargets.WINDOWS64_JNI) {
                        headerDir(layout.projectDirectory.dir("include"))
                        cppInclude(layout.projectDirectory.file("source/native.cpp"))
                        cppExclude(file("source/skip.cpp"))
                        staticLinkerInput(layout.projectDirectory.file("libs/native.lib"))
                        sharedLinkerInput(file("libs/native.dll").toPath())
                        forcedInclude(layout.projectDirectory.file("include/force.h"))
                    }
                }
            }
            """.trimIndent()
        )

        val result = runner(projectDir, "tasks", "--group", "jParser", "--all", "--console=plain").build()

        assertContains(result.output, "jParser_build_${JParserTargets.WINDOWS64_JNI}")
    }

    @Test
    fun supportsNativeTargetVariants() {
        val projectDir = createProject(
            """
            import com.github.xpenatan.jParser.gradle.JParserTargets

            plugins {
                id("com.github.xpenatan.jparser")
            }

            jParser {
                libName.set("testlib")
                modulePrefix.set("lib")
                modulePath.set(layout.projectDirectory.asFile.absolutePath)
                packageName.set("com.example.testlib")
                cppSourcePath.set("source")

                native {
                    targetVariant(JParserTargets.WINDOWS64_JNI, "wgpu") {
                        headerDir("include/wgpu")
                    }
                    targetVariant(JParserTargets.WINDOWS64_JNI, "dawn") {
                        headerDir("include/dawn")
                    }
                    targetVariant(JParserTargets.LINUX64_JNI, "wgpu") {
                        headerDir("include/linux-wgpu")
                    }
                }
            }
            """.trimIndent()
        )
        File(projectDir, "lib-build/src/main/cpp").mkdirs()
        File(projectDir, "lib-build/src/main/cpp/testlib.idl").writeText(
            """
            interface TestObject {
                void TestObject();
            };
            """.trimIndent()
        )
        File(projectDir, "lib-base/src/main/java").mkdirs()
        File(projectDir, "source").mkdirs()

        val result = runner(projectDir, "tasks", "--group", "jParser", "--all", "--console=plain").build()

        assertContains(result.output, "jParser_build_${JParserTargets.WINDOWS64_JNI}_wgpu")
        assertContains(result.output, "jParser_build_${JParserTargets.WINDOWS64_JNI}_dawn")
        assertContains(result.output, "jParser_build_${JParserTargets.LINUX64_JNI}_wgpu")
        assertDoesNotContainTask(result.output, "jParser_build_${JParserTargets.WINDOWS64_JNI}")
        assertDoesNotContainTask(result.output, "jParser_build_${JParserTargets.LINUX64_JNI}")

        runner(projectDir, "jParser_generate", "--console=plain").build()
        assertGeneratedClass(projectDir, "lib-jni/src/main/java", "TestObject.java")
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
    fun generateSkipsUnusedPlatformModules() {
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
        assertFalse(File(projectDir, "shared/c/src/main/java").exists())
    }

    @Test
    fun generateIncludesPlatformFromConfiguredNativeTarget() {
        val projectDir = createProject(
            """
            import com.github.xpenatan.jParser.gradle.JParserTargets

            plugins {
                id("com.github.xpenatan.jparser")
            }

            jParser {
                libName.set("testlib")
                modulePrefix.set("lib")
                modulePath.set(layout.projectDirectory.asFile.absolutePath)
                packageName.set("com.example.testlib")
                cppSourcePath.set("source")

                native {
                    target(JParserTargets.WINDOWS64_TEAVM_C) {}
                }
            }
            """.trimIndent()
        )
        File(projectDir, "lib-build/src/main/cpp").mkdirs()
        File(projectDir, "lib-build/src/main/cpp/testlib.idl").writeText(
            """
            interface TestObject {
                void TestObject();
            };
            """.trimIndent()
        )
        File(projectDir, "lib-base/src/main/java").mkdirs()
        File(projectDir, "source").mkdirs()

        runner(projectDir, "jParser_generate", "--console=plain").build()

        assertGeneratedClass(projectDir, "lib-core/src/main/java", "TestObject.java")
        assertGeneratedClass(projectDir, "lib-c/core/src/main/java", "TestObject.java")
        assertFalse(File(projectDir, "lib-jni/src/main/java").exists())
        assertFalse(File(projectDir, "lib-ffm/src/main/java").exists())
        assertFalse(File(projectDir, "lib-web/src/main/java").exists())
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
                [BindTo="DoThing"] void DoThing__1(TestObject other);
            };

            interface CallbackObject {
            };

            [JSImplementation="CallbackObject"]
            interface CallbackObjectImpl {
                void CallbackObjectImpl();
                void OnCallback();
            };
            CallbackObjectImpl implements CallbackObject;
            """.trimIndent()
        )
        File(projectDir, "base/src/main/java").mkdirs()
        File(projectDir, "source").mkdirs()

        runner(projectDir, "jParser_generate", "--console=plain").build()

        val generated = findGeneratedClass(projectDir, "core/src/main/java", "TestObject.java").readText()
        assertTrue(generated.contains("public void doThing("))
        assertFalse(generated.contains("public void DoThing("))

        val generatedCallback = findGeneratedClass(projectDir, "core/src/main/java", "CallbackObject.java").readText()
        assertTrue(generatedCallback.contains("protected void onCallback("))
        assertTrue(generatedCallback.contains("private void internal_OnCallback("))
        assertFalse(generatedCallback.contains("internal_onCallback("))

        val generatedWeb = findGeneratedClass(projectDir, "web/wasm/src/main/java", "TestObject.java").readText()
        assertTrue(generatedWeb.contains("jsObj.DoThing();"))
        assertTrue(generatedWeb.contains("jsObj.DoThing__1(other_addr);"))
        assertFalse(generatedWeb.contains("jsObj.doThing();"))
        assertFalse(generatedWeb.contains("jsObj.doThing(other_addr);"))

        val generatedWebCallback = findGeneratedClass(projectDir, "web/wasm/src/main/java", "CallbackObject.java").readText()
        assertTrue(generatedWebCallback.contains("protected void onCallback("))
        assertTrue(generatedWebCallback.contains("private void internal_OnCallback("))
        assertTrue(generatedWebCallback.contains("CallbackObjectImpl.OnCallback = OnCallback;"))
        assertTrue(generatedWebCallback.contains("public interface OnCallback"))
        assertFalse(generatedWebCallback.contains("CallbackObjectImpl.onCallback = onCallback;"))
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

    private fun assertDoesNotContainTask(output: String, taskName: String) {
        val hasTaskLine = output.lineSequence().any { line ->
            line == taskName || line.startsWith("$taskName -")
        }
        assertFalse("Expected output to not contain task '$taskName'.\n$output", hasTaskLine)
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
