package com.github.xpenatan.jParser.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class JParserNativeBundlePluginTest {
    @Test
    fun publishesPomOnlyResourcesCoordinateAndResolvesVariantAndClassifier() {
        val producer = Files.createTempDirectory("jparser-resources-producer").toFile()
        val repository = File(producer, "repository")
        File(producer, "settings.gradle.kts").writeText("")
        archive(File(producer, "implementation.lib"), byteArrayOf(0x4c, 0x01, 1, 2))
        archive(File(producer, "bridge.lib"), byteArrayOf(0x4c, 0x01, 3, 4))
        File(producer, "LICENSE").writeText("test license")
        File(producer, "web.idl").writeText("interface Sample {};")
        File(producer, "include").mkdirs()
        File(producer, "include/sample.h").writeText("#pragma once")
        File(producer, "build.gradle.kts").writeText(
            """
            import com.github.xpenatan.jParser.builder.bundle.NativeBridge
            import com.github.xpenatan.jParser.builder.bundle.NativeTarget

            plugins {
                `maven-publish`
                id("com.github.xpenatan.jParser")
            }

            group = "com.example"
            version = "1.0.0"

            val nativeInputs = tasks.register("nativeInputs") {
                outputs.files(
                    file("implementation.lib"),
                    file("bridge.lib")
                )
            }

            jParser {
                resources {
                    componentId.set("sample")
                    resourcesArtifactId.set("sample_resources")
                    license(file("LICENSE"))
                    declaredClassifier("windows-x86_64-jni-wgpu")
                    declaredClassifier("windows-x86_64-ffm-dawn")
                    declaredClassifier("linux-x86_64-ffm")
                    declaredClassifier("android-arm64-v8a-jni-wgpu")
                    declaredClassifier("ios-device-arm64-teavm-c")
                    declaredClassifier("web")
                    variant("windowsWgpu") {
                        target.set(
                            NativeTarget.of(
                                NativeTarget.OperatingSystem.WINDOWS,
                                NativeTarget.Architecture.X86_64
                            )
                        )
                        bridge.set(NativeBridge.JNI)
                        backend.set("wgpu")
                        toolchainId.set("msvc")
                        toolchainVersion.set("19")
                        cRuntime.set("ucrt")
                        cppRuntime.set("msvc")
                        implementationArchive(file("implementation.lib"))
                        bridgeArchive(file("bridge.lib"))
                        builtBy(nativeInputs)
                        exportedSymbol("Java_sample_init")
                    }
                    variant("windowsDawn") {
                        target.set(
                            NativeTarget.of(
                                NativeTarget.OperatingSystem.WINDOWS,
                                NativeTarget.Architecture.X86_64
                            )
                        )
                        bridge.set(NativeBridge.FFM)
                        backend.set("dawn")
                        toolchainId.set("msvc")
                        cRuntime.set("ucrt")
                        cppRuntime.set("msvc")
                        implementationArchive(file("implementation.lib"))
                        bridgeArchive(file("bridge.lib"))
                        builtBy(nativeInputs)
                    }
                    variant("linuxFfm") {
                        target.set(
                            NativeTarget.of(
                                NativeTarget.OperatingSystem.LINUX,
                                NativeTarget.Architecture.X86_64
                            )
                        )
                        bridge.set(NativeBridge.FFM)
                        toolchainId.set("gcc")
                        cRuntime.set("glibc")
                        cppRuntime.set("libstdc++")
                        implementationArchive(file("implementation.lib"))
                        bridgeArchive(file("bridge.lib"))
                        builtBy(nativeInputs)
                    }
                    variant("androidArm64") {
                        target.set(
                            NativeTarget.android(
                                NativeTarget.Architecture.ARM64,
                                "arm64-v8a"
                            )
                        )
                        bridge.set(NativeBridge.JNI)
                        backend.set("wgpu")
                        toolchainId.set("android-ndk")
                        cRuntime.set("bionic")
                        cppRuntime.set("libc++")
                        implementationArchive(file("implementation.lib"))
                        bridgeArchive(file("bridge.lib"))
                        builtBy(nativeInputs)
                    }
                    variant("iosDevice") {
                        target.set(
                            NativeTarget.ios(
                                NativeTarget.Architecture.ARM64,
                                "device"
                            )
                        )
                        bridge.set(NativeBridge.TEAVM_C)
                        toolchainId.set("apple-clang")
                        cRuntime.set("libsystem")
                        cppRuntime.set("libc++")
                        implementationArchive(file("implementation.lib"))
                        bridgeArchive(file("bridge.lib"))
                        builtBy(nativeInputs)
                    }
                    variant("web") {
                        target.set(
                            NativeTarget.web(NativeTarget.Architecture.WASM32)
                        )
                        bridge.set(NativeBridge.WEB)
                        toolchainId.set("emscripten")
                        cRuntime.set("emscripten")
                        cppRuntime.set("libc++")
                        implementationArchive(file("implementation.lib"))
                        webIDL(file("web.idl"))
                        webHeaders(file("include"))
                        builtBy(nativeInputs)
                    }
                }
            }

            publishing {
                repositories {
                    maven {
                        name = "test"
                        url = uri("${repository.toURI()}")
                    }
                }
            }
            """.trimIndent()
        )

        runner(
            producer,
            "publishJParserResourcesPublicationToTestRepository",
            "--console=plain",
            "--stacktrace"
        ).build()

        val versionDir = File(repository, "com/example/sample_resources/1.0.0")
        val pom = File(versionDir, "sample_resources-1.0.0.pom")
        val module = File(versionDir, "sample_resources-1.0.0.module")
        val classifiers = listOf(
            "windows-x86_64-jni-wgpu",
            "windows-x86_64-ffm-dawn",
            "linux-x86_64-ffm",
            "android-arm64-v8a-jni-wgpu",
            "ios-device-arm64-teavm-c",
            "web"
        )
        assertTrue(pom.isFile)
        assertTrue(module.isFile)
        classifiers.forEach { classifier ->
            assertTrue(
                File(
                    versionDir,
                    "sample_resources-1.0.0-$classifier.jar"
                ).isFile
            )
        }
        assertFalse(File(versionDir, "sample_resources-1.0.0.jar").exists())
        assertTrue(pom.readText().contains("<packaging>pom</packaging>"))
        val metadata = module.readText()
        assertTrue(metadata.contains("com.github.xpenatan.jparser.operating-system"))
        classifiers.forEach { classifier ->
            assertTrue(metadata.contains(classifier))
        }
        assertTrue(metadata.contains("\"wgpu\""))
        assertTrue(metadata.contains("\"dawn\""))
        assertTrue(metadata.contains("\"org.gradle.usage\": \"jparser-native\""))
        assertTrue(
            metadata.contains(
                "\"org.gradle.category\": \"jparser-native-resources\""
            )
        )

        val consumer = Files.createTempDirectory("jparser-resources-consumer").toFile()
        File(consumer, "settings.gradle.kts").writeText("")
        File(consumer, "build.gradle.kts").writeText(
            """
            import com.github.xpenatan.jParser.builder.bundle.NativeBridge
            import com.github.xpenatan.jParser.builder.bundle.NativeTarget

            plugins {
                id("com.github.xpenatan.jParser")
            }

            repositories {
                maven { url = uri("${repository.toURI()}") }
            }

            jParser {
                bundle("game") {
                    target.set(
                        NativeTarget.of(
                            NativeTarget.OperatingSystem.WINDOWS,
                            NativeTarget.Architecture.X86_64
                        )
                    )
                    component("sample", "com.example:sample_resources:1.0.0", NativeBridge.JNI) {
                        backend.set("wgpu")
                    }
                }
                bundle("windowsFfm") {
                    target.set(
                        NativeTarget.of(
                            NativeTarget.OperatingSystem.WINDOWS,
                            NativeTarget.Architecture.X86_64
                        )
                    )
                    component(
                        "sample",
                        "com.example:sample_resources:1.0.0",
                        NativeBridge.FFM
                    ) {
                        backend.set("dawn")
                    }
                }
                bundle("linuxFfm") {
                    target.set(
                        NativeTarget.of(
                            NativeTarget.OperatingSystem.LINUX,
                            NativeTarget.Architecture.X86_64
                        )
                    )
                    component(
                        "sample",
                        "com.example:sample_resources:1.0.0",
                        NativeBridge.FFM
                    )
                }
                bundle("android") {
                    target.set(
                        NativeTarget.android(
                            NativeTarget.Architecture.ARM64,
                            "arm64-v8a"
                        )
                    )
                    component(
                        "sample",
                        "com.example:sample_resources:1.0.0",
                        NativeBridge.JNI
                    ) {
                        backend.set("wgpu")
                    }
                }
                bundle("ios") {
                    target.set(
                        NativeTarget.ios(
                            NativeTarget.Architecture.ARM64,
                            "device"
                        )
                    )
                    component(
                        "sample",
                        "com.example:sample_resources:1.0.0",
                        NativeBridge.TEAVM_C
                    )
                }
                bundle("web") {
                    target.set(
                        NativeTarget.web(NativeTarget.Architecture.WASM32)
                    )
                    component(
                        "sample",
                        "com.example:sample_resources:1.0.0",
                        NativeBridge.WEB
                    )
                }
            }

            val expected = linkedMapOf(
                "jParserBundleGameSampleResources" to
                    "windows-x86_64-jni-wgpu",
                "jParserBundleWindowsFfmSampleResources" to
                    "windows-x86_64-ffm-dawn",
                "jParserBundleLinuxFfmSampleResources" to
                    "linux-x86_64-ffm",
                "jParserBundleAndroidSampleResources" to
                    "android-arm64-v8a-jni-wgpu",
                "jParserBundleIosSampleResources" to
                    "ios-device-arm64-teavm-c",
                "jParserBundleWebSampleResources" to "web"
            )
            val explicitConfigurations = expected.values.mapIndexed { index, classifier ->
                configurations.create("explicit${'$'}index").also { configuration ->
                    dependencies.add(
                        configuration.name,
                        "com.example:sample_resources:1.0.0:${'$'}classifier@jar"
                    )
                }
            }

            tasks.register("resolveResources") {
                doLast {
                    expected.forEach { (configurationName, classifier) ->
                        val selected = configurations
                            .getByName(configurationName)
                            .singleFile
                        println("ATTR_SELECTED=" + selected.name)
                        check(selected.name.endsWith("-${'$'}classifier.jar"))
                    }
                    explicitConfigurations.zip(expected.values).forEach {
                            (configuration, classifier) ->
                        val selected = configuration.singleFile
                        println("CLASSIFIER_SELECTED=" + selected.name)
                        check(selected.name.endsWith("-${'$'}classifier.jar"))
                    }
                }
            }
            """.trimIndent()
        )

        val result = runner(
            consumer,
            "resolveResources",
            "--console=plain",
            "--stacktrace"
        ).build()
        classifiers.forEach { classifier ->
            assertTrue(
                result.output.contains(
                    "ATTR_SELECTED=sample_resources-1.0.0-$classifier.jar"
                )
            )
            assertTrue(
                result.output.contains(
                    "CLASSIFIER_SELECTED=sample_resources-1.0.0-$classifier.jar"
                )
            )
        }
        assertFalse(File(consumer, "src").exists())
    }

    @Test
    fun rejectsReleaseResourcesWithoutNativeTaskProvenance() {
        val producer = Files.createTempDirectory("jparser-resources-provenance").toFile()
        val repository = File(producer, "repository")
        File(producer, "settings.gradle.kts").writeText("")
        archive(File(producer, "implementation.lib"), byteArrayOf(0x4c, 0x01, 1, 2))
        archive(File(producer, "bridge.lib"), byteArrayOf(0x4c, 0x01, 3, 4))
        File(producer, "LICENSE").writeText("test license")
        File(producer, "build.gradle.kts").writeText(
            """
            import com.github.xpenatan.jParser.builder.bundle.NativeBridge
            import com.github.xpenatan.jParser.builder.bundle.NativeTarget

            plugins {
                `maven-publish`
                id("com.github.xpenatan.jParser")
            }

            group = "com.example"
            version = "1.0.0"

            jParser {
                resources {
                    componentId.set("sample")
                    resourcesArtifactId.set("sample_resources")
                    license(file("LICENSE"))
                    declaredClassifier("windows-x86_64-jni")
                    variant("windows") {
                        target.set(
                            NativeTarget.of(
                                NativeTarget.OperatingSystem.WINDOWS,
                                NativeTarget.Architecture.X86_64
                            )
                        )
                        bridge.set(NativeBridge.JNI)
                        toolchainId.set("msvc")
                        implementationArchive(file("implementation.lib"))
                        bridgeArchive(file("bridge.lib"))
                    }
                }
            }

            publishing {
                repositories {
                    maven {
                        name = "test"
                        url = uri("${repository.toURI()}")
                    }
                }
            }
            """.trimIndent()
        )

        val result = runner(
            producer,
            "publishJParserResourcesPublicationToTestRepository",
            "--console=plain",
            "--stacktrace"
        ).buildAndFail()

        assertTrue(
            result.output,
            result.output.contains(
                "Release jParser resource variants must declare native producer tasks"
            )
        )
    }

    private fun runner(projectDir: File, vararg arguments: String): GradleRunner {
        return GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(*arguments)
            .withPluginClasspath()
    }

    private fun archive(file: File, payload: ByteArray) {
        val output = ByteArrayOutputStream()
        output.write("!<arch>\n".toByteArray(StandardCharsets.US_ASCII))
        output.write(field("object.o/", 16))
        output.write(field("0", 12))
        output.write(field("0", 6))
        output.write(field("0", 6))
        output.write(field("100644", 8))
        output.write(field(payload.size.toString(), 10))
        output.write('`'.code)
        output.write('\n'.code)
        output.write(payload)
        file.writeBytes(output.toByteArray())
    }

    private fun field(value: String, width: Int): ByteArray {
        return value.padEnd(width, ' ').toByteArray(StandardCharsets.US_ASCII)
    }
}
