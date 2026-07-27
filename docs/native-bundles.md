# Fat Native Bundles

Fat native bundles let an application combine several jParser binding libraries
into one final native binary. The Java bindings remain normal Maven
dependencies; a separate set of resource-only Maven artifacts supplies the
static native inputs to the application-owned final link.

This is an opt-in packaging mode. Existing standalone JNI, FFM, TeaVM C, web,
DLL/SO/DYLIB, AAR, and loader behavior remains unchanged.

## Architecture Boundary

Each binding producer publishes two independent products:

1. Its existing Java binding artifacts.
2. One `<libName>_resources` Maven coordinate containing classified,
   resource-only JARs.

The application declares Java bindings on its normal classpath. It declares
`_resources` coordinates only in a jParser bundle specification. The Gradle
plugin resolves the requested variants and passes the resulting JAR paths to
`NativeBundleBuilder`; it does not inspect manifests, extract archives,
construct linker flags, compile web bridges, merge archives, or invoke a
linker itself.

The manual builders are the source of truth:

- `NativeComponentBuilder.build(NativeComponentRequest)` creates one
  classifier JAR.
- `NativeBundleBuilder.build(NativeBundleRequest)` consumes resolved
  classifier JAR paths.
- `NativeBundleResult` exposes only raw native output paths.

The application remains responsible for staging or packaging those raw files.
jParser does not create an application JAR, AAR, XCFramework, or generated
platform module.

## Maven Contract

Default coordinates use the binding group and version:

```text
<group>:jWebGPU_resources:<jWebGPU-version>
<group>:box3d_resources:<box3d-version>
com.github.xpenatan.jParser:runtime_resources:<jParser-version>
```

`resourcesArtifactId` can override the default artifact ID. The resources
version must equal the corresponding binding version; the plugin rejects a
mismatch.

The coordinate has POM packaging `pom`, classified JARs, and no unclassified
Java JAR. This keeps resource artifacts off the application classpath and
avoids artificial source/Javadoc JARs for the resource-only module. See the
[Maven classifier documentation](https://maven.apache.org/plugins/maven-deploy-plugin/examples/deploying-with-classifiers.html)
and [Maven Central requirements](https://central.sonatype.org/publish/requirements/).

Typical classifiers are:

```text
windows-x86_64-jni-wgpu
windows-x86_64-ffm-dawn
linux-x86_64-ffm
android-arm64-v8a-jni-wgpu
ios-device-arm64-teavm-c
web
```

Gradle Module Metadata describes each classifier as a variant. The important
attributes are:

| Attribute | Example |
|---|---|
| `org.gradle.usage` | `jparser-native` |
| `org.gradle.category` | `jparser-native-resources` |
| `com.github.xpenatan.jparser.operating-system` | `windows` |
| `com.github.xpenatan.jparser.architecture` | `x86_64` |
| `com.github.xpenatan.jparser.abi` | `arm64-v8a` |
| `com.github.xpenatan.jparser.environment` | `device` or `simulator` |
| `com.github.xpenatan.jparser.bridge` | `jni`, `ffm`, `teavm-c`, or `web` |
| `com.github.xpenatan.jparser.backend` | `wgpu`, `dawn`, or empty |
| `com.github.xpenatan.jparser.component-format` | `1` |
| `com.github.xpenatan.jparser.build-type` | `release` or `debug` |
| `com.github.xpenatan.jparser.toolchain` | `msvc`, `gcc`, `apple-clang`, etc. |

The plugin publishes through an adhoc software component so the classified
artifacts are represented in both the Maven publication and Gradle Module
Metadata. Attaching arbitrary classifier files directly to a
`MavenPublication` is not sufficient for variant-aware Gradle resolution. See
[Gradle publication customization](https://docs.gradle.org/current/userguide/publishing_customization.html).

A Maven-only consumer selects a classifier explicitly:

```xml
<dependency>
  <groupId>com.example</groupId>
  <artifactId>jWebGPU_resources</artifactId>
  <version>${jwebgpu.version}</version>
  <type>jar</type>
  <classifier>windows-x86_64-jni-wgpu</classifier>
</dependency>
```

## Classifier JAR Layout

Classifier JARs contain no Java classes:

```text
META-INF/jparser/native-component.properties
META-INF/licenses/...
native/implementation/<archive>
native/bridge/<archive>
native/dependencies/<ordered-static-archive>
web/idl/...                         # web only
web/include/...                     # web only
```

The versioned manifest records the logical component and backend, target,
bridge, build type, runtime ABI, minimum Java/platform versions, toolchain,
CRT/STL, system libraries/frameworks, linker options, exported symbols, file
roles, link modes, and SHA-256 checksums.

Runtime ABI and toolchain IDs must match exactly. Optional toolchain version,
CRT, and STL values must agree whenever more than one component specifies
them; an omitted optional value does not conflict with a concrete value.

Desktop, Android, and iOS classifiers contain precompiled implementation and
bridge archives, not headers. Every required third-party static archive must
be embedded under `native/dependencies`; manifests cannot contain
machine-local paths or unresolved Maven coordinates.

Web is different. Its classifier contains the component-owned WebIDL and the
complete required header closure. The final bundle merges the WebIDL and
generates one bridge. Existing side-module glue is not concatenated because
doing so would duplicate shared wrapper state.

## Producer Configuration

Apply the existing jParser plugin and `maven-publish`. Declare the complete
release classifier matrix even when a local host builds only a subset:

```kotlin
import com.github.xpenatan.jParser.builder.bundle.NativeArchiveLinkMode
import com.github.xpenatan.jParser.builder.bundle.NativeBridge
import com.github.xpenatan.jParser.builder.bundle.NativeTarget

plugins {
    id("com.github.xpenatan.jParser")
    `maven-publish`
}

jParser {
    libName.set("jWebGPU")

    resources {
        // Defaults are jWebGPU, project.version, and jWebGPU_resources.
        resourcesArtifactId.set("jWebGPU_resources")
        license(layout.projectDirectory.file("LICENSE"))
        declaredClassifier("windows-x86_64-jni-wgpu")
        declaredClassifier("windows-x86_64-ffm-dawn")

        variant("windowsJniWgpu") {
            target.set(
                NativeTarget.of(
                    NativeTarget.OperatingSystem.WINDOWS,
                    NativeTarget.Architecture.X86_64
                )
            )
            bridge.set(NativeBridge.JNI)
            backend.set("wgpu")
            implementationArchive.set(
                layout.buildDirectory.file("native/wgpu.lib")
            )
            bridgeArchive.set(
                layout.buildDirectory.file("native/jWebGPU_bridge.lib")
            )
            dependencyArchive(
                "wgpu-native",
                layout.buildDirectory.file("native/wgpu_native.lib"),
                NativeArchiveLinkMode.WHOLE_ARCHIVE
            )
            toolchainId.set("msvc")
            toolchainVersion.set("19.40")
            cRuntime.set("msvc-md")
            cppRuntime.set("msvc")
            runtimeAbi.set("jparser-runtime-1")
            exportedSymbol("Java_com_example_jwebgpu_Native_entry")
            builtBy(tasks.named("buildWindowsJniWgpu"))
        }
    }
}
```

For web, omit `bridgeArchive`, set `bridge` to `NativeBridge.WEB`, set
`webModuleName`, and add `webIDL(...)` plus `webHeaders(...)`.

The plugin registers:

- `jParserBuildResource<Variant>` for each classifier.
- `verifyJParserResourcesPublication`.
- The `jParserResources` adhoc component and Maven publication when
  `maven-publish` is applied.
- Standard `publishToMavenLocal`, snapshot, and release task dependencies.

Release/snapshot publication requires every declared classifier and requires
each native input to be an output of a task named with `builtBy(...)`. Local
Maven publication may contain only the configured host variants.

## Bundle Consumer Configuration

A bundle-only project can apply the plugin without configuring IDL, packages,
or binding generation:

```kotlin
import com.github.xpenatan.jParser.builder.bundle.NativeBridge
import com.github.xpenatan.jParser.builder.bundle.NativeTarget

plugins {
    id("com.github.xpenatan.jParser")
}

val bindingVersion = "1.0.0"
val jParserVersion = "<jParser-version>"

jParser {
    bundle("game") {
        bundleName.set("game")
        target.set(
            NativeTarget.of(
                NativeTarget.OperatingSystem.WINDOWS,
                NativeTarget.Architecture.X86_64
            )
        )
        toolchainId.set("msvc")

        // A mixed bundle selects the FFM runtime exactly once.
        component(
            "runtime",
            "com.github.xpenatan.jParser:runtime_resources:$jParserVersion",
            NativeBridge.FFM
        )
        component(
            "graphics",
            "com.example:jWebGPU_resources:$bindingVersion",
            NativeBridge.JNI
        ) {
            backend.set("wgpu")
        }
        component(
            "physics",
            "com.example:box3d_resources:$bindingVersion",
            NativeBridge.FFM
        )
    }
}
```

This creates one resolvable configuration per component and a
`jParserBundleGame` task. Gradle attributes select each classifier
automatically. `jParserBundleGameElements` exposes the resulting raw native
files to another Gradle project.

For a JNI-only desktop bundle, select `NativeBridge.JNI` for the runtime.
Exactly one compatible runtime component is mandatory.

## Manual Builder API

Manual producers and non-Gradle integrations use `gen-build-tool` directly:

```java
NativeComponentRequest component = new NativeComponentRequest();
component.outputJar = Path.of("jWebGPU_resources-windows-x86_64-jni-wgpu.jar");
component.componentId = "jWebGPU";
component.componentVersion = "1.0.0";
component.target = NativeTarget.of(
        NativeTarget.OperatingSystem.WINDOWS,
        NativeTarget.Architecture.X86_64);
component.bridge = NativeBridge.JNI;
component.variantName = "wgpu";
component.runtimeAbi = "jparser-runtime-1";
component.toolchainId = "msvc";
component.cRuntime = "msvc-md";
component.cppRuntime = "msvc";
component.implementationArchive = Path.of("jWebGPU.lib");
component.bridgeArchive = Path.of("jWebGPU_bridge.lib");
component.dependencyArchives.add(new NativeArchiveInput(
        "wgpu-native",
        Path.of("wgpu_native.lib"),
        NativeArchiveLinkMode.WHOLE_ARCHIVE));
component.licenseFiles.add(Path.of("LICENSE"));

Path resourceJar = NativeComponentBuilder.build(component).getPath();

NativeBundleRequest bundle = new NativeBundleRequest();
bundle.bundleName = "game";
bundle.outputDirectory = Path.of("build/native");
bundle.target = component.target;
bundle.componentJars.add(Path.of("runtime_resources-windows-x86_64-jni.jar"));
bundle.componentJars.add(resourceJar);
bundle.componentJars.add(Path.of("box3d_resources-windows-x86_64-jni.jar"));

NativeBundleResult result = NativeBundleBuilder.build(bundle);
List<Path> rawOutputs = result.getOutputPaths();
```

Inputs to `NativeBundleBuilder` are already-resolved local JAR paths. Maven
resolution is intentionally outside the manual builder.

## Loading and Java Dependencies

Fat-mode applications load once before accessing generated bindings:

```java
JParserNativeBundleLoader.load("game", (success, failure) -> {
    if(failure != null) {
        throw new RuntimeException(failure);
    }
    // Generated bindings are now usable.
});
```

Do not invoke the generated runtime or per-library loaders for components
already linked into that bundle. Standalone mode continues to use those
loaders normally; fat mode does not intercept them.

The Java dependency path must not package the old standalone native files.
Library producers that historically put DLL/SO/DYLIB files in their binding
JAR must expose a class-only artifact or configuration. In this repository,
JNI uses the shared class-only `runtime-jni` and `<Lib>-jni` modules; FFM
publishes a classes-only main artifact and exposes `fatModeClasses` for local
project consumption. `_resources` dependencies belong only to the bundle
configuration, never `implementation` or `runtimeOnly`.

Desktop raw outputs can be put at the root of a runtime classpath directory so
the loader can extract them. Android packaging should place `lib<bundle>.so`
in the selected ABI's normal native-library location. iOS links the returned
archive into the application. Web stages both returned files together.

## Platform Results

| Target | Raw result |
|---|---|
| Windows x64 | `<bundle>64.dll` |
| Linux x64 | `lib<bundle>64.so` |
| macOS | `lib<bundle><arch-suffix>.dylib` |
| Android | `lib<bundle>.so` for the selected ABI |
| iOS | `lib<bundle>.a` for the selected device/simulator slice |
| Web | `<bundle>.js`, optionally plus `<bundle>.wasm` |

Desktop may mix JNI and FFM archives. Any FFM component makes Java 25 and the
FFM runtime mandatory; JNI-only bundles remain Java 8 compatible. On web, all
component module names alias the combined module so existing JSBody bindings
continue to resolve.

## Validation and Diagnostics

Component creation and final bundling reject:

- import libraries or thin archives presented as self-contained static
  archives;
- path traversal, duplicate JAR entries, and checksum failures;
- missing implementation/bridge inputs or licenses;
- duplicate component IDs, exported symbols, or WebIDL declarations;
- incompatible targets, build types, runtime ABIs, toolchains, CRTs, or STLs;
- a missing, duplicate, or incorrectly selected runtime component;
- FFM metadata below Java 25;
- non-system dynamic dependencies and unsafe linker options.

Use `keepTemporaryFiles` only for linker diagnostics. It preserves the
validated extraction/merge directory but does not weaken validation.

## Release Workflow

Never publish classifiers for one release coordinate from independent jobs.
Maven POM and Gradle Module Metadata must describe the complete coordinate in
one atomic upload.

The intended CI flow is:

1. Compatible Windows, Linux, macOS, Android, iOS, and web jobs build the
   implementation archive, bridge archive, packaged dependencies, and their
   component metadata.
2. Those immutable inputs are uploaded as CI artifacts.
3. One final job downloads the complete declared matrix.
4. It runs `verifyJParserResourcesPublication`, creates every classifier with
   `NativeComponentBuilder`, and generates the POM and `.module` file once.
5. The existing `easyPublishing`, `prepareRelease`, `publishRelease`,
   `prepareSnapshot`, and `publishSnapshot` flow signs and uploads the whole
   coordinate.

The repository's `runtime_resources` publication follows this model. Local
development selects host classifiers by default or accepts:

```text
"-Pjparser.runtimeResources.variants=windows-x86_64-jni,windows-x86_64-ffm"
```

Complete verification uses:

```text
./gradlew :jParser:runtime:resources:verifyJParserResourcesPublication \
  "-Pjparser.runtimeResources.complete=true"
```

The complete command expects every declared platform input to have already
been collected by CI.
