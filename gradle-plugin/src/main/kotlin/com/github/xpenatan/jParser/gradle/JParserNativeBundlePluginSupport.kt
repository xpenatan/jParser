package com.github.xpenatan.jParser.gradle

import com.github.xpenatan.jParser.builder.bundle.NativeBridge
import com.github.xpenatan.jParser.builder.bundle.NativeBundleOutputPaths
import com.github.xpenatan.jParser.builder.bundle.NativeComponentManifest
import com.github.xpenatan.jParser.builder.bundle.NativeResourceClassifier
import com.github.xpenatan.jParser.builder.bundle.NativeTarget
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskProvider
import java.util.Locale

internal object JParserNativeBundlePluginSupport {
    fun configure(
        project: Project,
        extension: JParserExtension,
        softwareComponentFactory: SoftwareComponentFactory
    ) {
        if(extension.resources.variants.isNotEmpty()) {
            configureResources(project, extension.resources, softwareComponentFactory)
        }
        extension.bundles.forEach { bundle ->
            configureBundle(project, bundle)
        }
    }

    private fun configureResources(
        project: Project,
        resources: JParserResourcesExtension,
        softwareComponentFactory: SoftwareComponentFactory
    ) {
        val componentId = required(resources.componentId.orNull, "jParser.resources.componentId")
        val componentVersion = required(
            resources.componentVersion.orNull,
            "jParser.resources.componentVersion"
        )
        val projectVersion = project.version.toString()
        if(componentVersion != projectVersion) {
            throw GradleException(
                "jParser resources version '$componentVersion' must match binding project version " +
                    "'$projectVersion'"
            )
        }
        val artifactId = required(
            resources.resourcesArtifactId.orNull,
            "jParser.resources.resourcesArtifactId"
        )
        val component = softwareComponentFactory.adhoc("jParserResources")
        project.components.add(component)
        val classifiers = linkedSetOf<String>()
        val componentTasks = mutableListOf<TaskProvider<JParserNativeComponentTask>>()

        resources.variants.forEach { variant ->
            val target = variant.target.orNull
                ?: throw GradleException("jParser resource variant '${variant.name}' is missing target")
            val bridge = variant.bridge.orNull
                ?: throw GradleException("jParser resource variant '${variant.name}' is missing bridge")
            if(!variant.toolchainId.isPresent) {
                throw GradleException("jParser resource variant '${variant.name}' is missing toolchainId")
            }
            if(!variant.implementationArchive.isPresent) {
                throw GradleException(
                    "jParser resource variant '${variant.name}' is missing implementationArchive"
                )
            }
            val backend = NativeResourceClassifier.normalizeBackend(variant.backend.get())
            val classifier = NativeResourceClassifier.of(target, bridge, backend)
            if(!classifiers.add(classifier)) {
                throw GradleException("Duplicate jParser resources classifier: $classifier")
            }
            val taskName = "jParserBuildResource${taskSegment(variant.name)}"
            val outputFile = project.layout.buildDirectory.file(
                "jparser/resources/$classifier/$artifactId-$componentVersion-$classifier.jar"
            )
            val producerTasks = resolveProducerTasks(project, variant.taskDependencies)
            val provenance = project.tasks.register(
                "verifyJParserResource${taskSegment(variant.name)}Inputs",
                JParserVerifyResourceInputsTask::class.java
            ) {
                group = "verification"
                description = "Verify native-task provenance for jParser resources '$classifier'."
                nativeInputs.from(
                    variant.implementationArchive,
                    variant.dependencyArchives.map { archive -> archive.file }
                )
                if(variant.bridgeArchive.isPresent) {
                    nativeInputs.from(variant.bridgeArchive)
                }
                producerOutputs.from(producerTasks)
                requireProvenance.set(requiresCompletePublication(project))
                dependsOn(producerTasks)
            }
            val task = project.tasks.register(taskName, JParserNativeComponentTask::class.java) {
                group = JParserGradlePlugin.TASK_GROUP
                description = "Build jParser native resources classifier '$classifier'."
                this.resources = resources
                this.variant = variant
                outputJar.set(outputFile)
                dependsOn(provenance)
                registerComponentInputs(this, resources, variant)
            }
            componentTasks.add(task)

            val outgoing = project.configurations.create(
                "jParserResources${taskSegment(variant.name)}Elements"
            ) {
                isCanBeConsumed = true
                isCanBeResolved = false
                description = "jParser native resources variant $classifier"
            }
            configureResourceAttributes(
                project,
                outgoing.attributes,
                target,
                bridge,
                backend,
                variant.buildType.get().name.lowercase(Locale.ROOT),
                variant.toolchainId.get()
            )
            outgoing.outgoing.artifact(task.flatMap { it.outputJar }) {
                this.classifier = classifier
                extension = "jar"
                type = "jar"
                builtBy(task)
            }
            component.addVariantsFromConfiguration(outgoing) {
                mapToMavenScope("runtime")
                mapToOptional()
            }
        }

        val verify = project.tasks.register(
            "verifyJParserResourcesPublication",
            JParserVerifyResourcesPublicationTask::class.java
        ) {
            group = "verification"
            description = "Verify all configured jParser _resources classifier artifacts."
            this.componentId.set(componentId)
            this.componentVersion.set(componentVersion)
            declaredClassifiers.set(resources.declaredClassifiers)
            requireCompleteMatrix.set(requiresCompletePublication(project))
            resourceJars.from(componentTasks.map { task -> task.flatMap { it.outputJar } })
            dependsOn(componentTasks)
        }

        if(project.pluginManager.hasPlugin("maven-publish")) {
            configureMavenPublication(project, component, artifactId, verify)
        }
    }

    private fun registerComponentInputs(
        task: JParserNativeComponentTask,
        resources: JParserResourcesExtension,
        variant: JParserResourceVariant
    ) {
        task.inputs.property("componentId", resources.componentId)
        task.inputs.property("componentVersion", resources.componentVersion)
        task.inputs.property("componentRole", resources.role.map { it.name })
        task.inputs.property("target", variant.target.map { it.toString() })
        task.inputs.property("bridge", variant.bridge.map { it.id })
        task.inputs.property("backend", variant.backend)
        task.inputs.property("buildType", variant.buildType.map { it.name })
        task.inputs.property("minimumJavaVersion", variant.minimumJavaVersion)
        task.inputs.property("minimumPlatformVersion", variant.minimumPlatformVersion)
        task.inputs.property("runtimeAbi", variant.runtimeAbi)
        task.inputs.property("toolchainId", variant.toolchainId)
        task.inputs.property("toolchainVersion", variant.toolchainVersion)
        task.inputs.property("cRuntime", variant.cRuntime)
        task.inputs.property("cppRuntime", variant.cppRuntime)
        task.inputs.property("webModuleName", variant.webModuleName.orElse(""))
        task.inputs.property("dependencyNames", variant.dependencyArchives.map { it.name })
        task.inputs.property(
            "dependencyLinkModes",
            variant.dependencyArchives.map { it.linkMode.name }
        )
        task.inputs.property("systemLibraries", variant.systemLibraries)
        task.inputs.property("frameworks", variant.frameworks)
        task.inputs.property("dynamicDependencies", variant.dynamicDependencies)
        task.inputs.property("linkerOptions", variant.linkerOptions)
        task.inputs.property("exportedSymbols", variant.exportedSymbols)
        task.inputs.file(variant.implementationArchive)
            .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.NONE)
        task.inputs.file(variant.bridgeArchive)
            .optional()
            .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.NONE)
        task.inputs.files(variant.dependencyArchives.map { it.file })
            .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.NONE)
        task.inputs.files(variant.webIDLFiles)
            .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.RELATIVE)
        task.inputs.files(variant.webHeaderDirectories)
            .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.RELATIVE)
        task.inputs.files(resources.licenses, variant.licenses)
            .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.NONE)
    }

    private fun configureMavenPublication(
        project: Project,
        component: AdhocComponentWithVariants,
        artifactId: String,
        verify: TaskProvider<JParserVerifyResourcesPublicationTask>
    ) {
        val publishing = project.extensions.getByType(PublishingExtension::class.java)
        if(publishing.publications.findByName("jParserResources") != null) {
            throw GradleException("Maven publication 'jParserResources' already exists")
        }
        publishing.publications.create(
            "jParserResources",
            MavenPublication::class.java
        ) {
            this.artifactId = artifactId
            from(component)
            pom.packaging = "pom"
            suppressAllPomMetadataWarnings()
        }
        project.tasks.matching { task ->
            task.name.startsWith("publishJParserResourcesPublication") ||
                task.name == "generateMetadataFileForJParserResourcesPublication" ||
                task.name == "generatePomFileForJParserResourcesPublication"
        }.configureEach {
            dependsOn(verify)
        }
    }

    private fun configureBundle(project: Project, bundle: JParserNativeBundleSpec) {
        val target = bundle.target.orNull
            ?: throw GradleException("jParser bundle '${bundle.name}' is missing target")
        if(bundle.components.isEmpty()) {
            throw GradleException("jParser bundle '${bundle.name}' has no resource components")
        }
        val componentConfigurations = bundle.components.map { component ->
            val notation = component.dependencyNotation
                ?: throw GradleException(
                    "jParser bundle '${bundle.name}' component '${component.name}' has no dependency"
                )
            val bridge = component.bridge.orNull
                ?: throw GradleException(
                    "jParser bundle '${bundle.name}' component '${component.name}' is missing bridge"
                )
            val backend = NativeResourceClassifier.normalizeBackend(component.backend.get())
            val configuration = project.configurations.create(
                "jParserBundle${taskSegment(bundle.name)}${taskSegment(component.name)}Resources"
            ) {
                isCanBeResolved = true
                isCanBeConsumed = false
                isTransitive = false
                description = "Resolved _resources JAR for ${bundle.name}/${component.name}"
            }
            configureResourceAttributes(
                project,
                configuration.attributes,
                target,
                bridge,
                backend,
                bundle.buildType.get().name.lowercase(Locale.ROOT),
                component.toolchainId.orNull ?: bundle.toolchainId.orNull
            )
            project.dependencies.add(configuration.name, notation)
            configuration
        }
        val taskName = "jParserBundle${taskSegment(bundle.name)}"
        val bundleTask = project.tasks.register(taskName, JParserNativeBundleTask::class.java) {
            group = JParserGradlePlugin.TASK_GROUP
            description = "Build the '${bundle.bundleName.get()}' fat native bundle."
            this.bundle = bundle
            this.componentConfigurations = componentConfigurations
            componentJars.from(componentConfigurations)
            outputDirectory.set(bundle.outputDirectory)
            registerBundleInputs(this, bundle)
        }
        val outgoing = project.configurations.create(
            "jParserBundle${taskSegment(bundle.name)}Elements"
        ) {
            isCanBeConsumed = true
            isCanBeResolved = false
            description = "Raw native outputs for jParser bundle '${bundle.name}'"
        }
        configureBundleOutputAttributes(project, outgoing, bundle, target)
        val outputs = NativeBundleOutputPaths.forTarget(
            bundle.bundleName.get(),
            target,
            bundle.webOutput.get(),
            bundle.outputDirectory.get().asFile.toPath()
        )
        outputs.forEach { output ->
            outgoing.outgoing.artifact(output.toFile()) {
                name = output.fileName.toString().substringBeforeLast('.')
                extension = output.fileName.toString().substringAfterLast('.', "")
                type = extension
                builtBy(bundleTask)
            }
        }
    }

    private fun registerBundleInputs(
        task: JParserNativeBundleTask,
        bundle: JParserNativeBundleSpec
    ) {
        task.inputs.property("bundleName", bundle.bundleName)
        task.inputs.property("target", bundle.target.map { it.toString() })
        task.inputs.property("buildType", bundle.buildType.map { it.name })
        task.inputs.property("webOutput", bundle.webOutput.map { it.name })
        task.inputs.property("androidApiLevel", bundle.androidApiLevel)
        task.inputs.property("minimumMacOSVersion", bundle.minimumMacOSVersion)
        task.inputs.property("minimumIOSVersion", bundle.minimumIOSVersion)
        task.inputs.property("linkerExecutable", bundle.linkerExecutable)
        task.inputs.property("visualCppEnvironment", bundle.visualCppEnvironment)
        task.inputs.property("androidNdkHome", bundle.androidNdkHome)
        task.inputs.property("emscriptenRoot", bundle.emscriptenRoot)
        task.inputs.property("pythonExecutable", bundle.pythonExecutable)
        task.inputs.property("environment", bundle.environment)
        task.inputs.property("keepTemporaryFiles", bundle.keepTemporaryFiles)
    }

    private fun configureResourceAttributes(
        project: Project,
        attributes: AttributeContainer,
        target: NativeTarget,
        bridge: NativeBridge,
        backend: String,
        buildType: String,
        toolchain: String?
    ) {
        attributes.attribute(
            Usage.USAGE_ATTRIBUTE,
            project.objects.named(Usage::class.java, JParserNativeResourceAttributes.USAGE)
        )
        attributes.attribute(
            Category.CATEGORY_ATTRIBUTE,
            project.objects.named(Category::class.java, JParserNativeResourceAttributes.CATEGORY)
        )
        attributes.attribute(
            Bundling.BUNDLING_ATTRIBUTE,
            project.objects.named(Bundling::class.java, Bundling.EXTERNAL)
        )
        attributes.attribute(
            LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
            project.objects.named(LibraryElements::class.java, LibraryElements.JAR)
        )
        attributes.attribute(
            JParserNativeResourceAttributes.OPERATING_SYSTEM,
            target.operatingSystem.id
        )
        attributes.attribute(
            JParserNativeResourceAttributes.ARCHITECTURE,
            target.architecture.id
        )
        attributes.attribute(JParserNativeResourceAttributes.ABI, target.abi)
        attributes.attribute(JParserNativeResourceAttributes.ENVIRONMENT, target.environment)
        attributes.attribute(JParserNativeResourceAttributes.BRIDGE, bridge.id)
        attributes.attribute(JParserNativeResourceAttributes.BACKEND, backend)
        attributes.attribute(
            JParserNativeResourceAttributes.COMPONENT_FORMAT,
            NativeComponentManifest.FORMAT_VERSION
        )
        attributes.attribute(JParserNativeResourceAttributes.BUILD_TYPE, buildType)
        if(!toolchain.isNullOrBlank()) {
            attributes.attribute(JParserNativeResourceAttributes.TOOLCHAIN, toolchain)
        }
    }

    private fun configureBundleOutputAttributes(
        project: Project,
        outgoing: Configuration,
        bundle: JParserNativeBundleSpec,
        target: NativeTarget
    ) {
        outgoing.attributes.attribute(
            Usage.USAGE_ATTRIBUTE,
            project.objects.named(Usage::class.java, "jparser-native-bundle")
        )
        outgoing.attributes.attribute(
            Category.CATEGORY_ATTRIBUTE,
            project.objects.named(Category::class.java, "jparser-native-bundle")
        )
        outgoing.attributes.attribute(
            JParserNativeResourceAttributes.OPERATING_SYSTEM,
            target.operatingSystem.id
        )
        outgoing.attributes.attribute(
            JParserNativeResourceAttributes.ARCHITECTURE,
            target.architecture.id
        )
        outgoing.attributes.attribute(JParserNativeResourceAttributes.ABI, target.abi)
        outgoing.attributes.attribute(JParserNativeResourceAttributes.ENVIRONMENT, target.environment)
        outgoing.attributes.attribute(
            JParserNativeResourceAttributes.BUILD_TYPE,
            bundle.buildType.get().name.lowercase(Locale.ROOT)
        )
        val bridges = bundle.components.map { it.bridge.get() }.toSet()
        outgoing.attributes.attribute(
            JParserNativeResourceAttributes.BRIDGE,
            if(bridges.size == 1) bridges.single().id else "mixed"
        )
    }

    private fun requiresCompletePublication(project: Project): Boolean {
        return project.gradle.startParameter.taskNames.any { taskName ->
            val normalized = taskName.lowercase(Locale.ROOT)
            normalized.contains("preparerelease") ||
                normalized.contains("prepareSnapshot".lowercase(Locale.ROOT)) ||
                normalized.contains("publishrelease") ||
                normalized.contains("publishsnapshot") ||
                (normalized.contains("publish") && !normalized.contains("mavenlocal"))
        }
    }

    private fun required(value: String?, name: String): String {
        if(value.isNullOrBlank()) {
            throw GradleException("$name must be configured")
        }
        return value.trim()
    }

    private fun resolveProducerTasks(project: Project, dependencies: List<Any>): List<Any> {
        return dependencies.map { dependency ->
            if(dependency is String) {
                try {
                    project.tasks.getByPath(dependency)
                }
                catch(exception: Exception) {
                    throw GradleException(
                        "jParser resource builtBy task '$dependency' does not exist in this build",
                        exception
                    )
                }
            }
            else {
                dependency
            }
        }
    }

    private fun taskSegment(value: String): String {
        val words = value.split(Regex("[^A-Za-z0-9]+"))
            .filter { it.isNotEmpty() }
        val result = words.joinToString("") { word ->
            word.replaceFirstChar { character -> character.uppercase(Locale.ROOT) }
        }
        if(result.isEmpty()) {
            throw GradleException("jParser name contains no task-safe characters: '$value'")
        }
        return result
    }
}
