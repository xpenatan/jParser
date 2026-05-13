# FFM Performance Review (Generator + Runtime + TestLib)

## Scope

This review covers:

- `jParser/gen/gen-ffm`
- `jParser/runtime/runtime-ffm`
- `examples/TestLib/lib/lib-ffm`

Goal: identify what is currently good, what is problematic, and what can be improved for runtime performance and scalability.

## What Is Already Good

- MethodHandle lookup/binding is cached in generated static `FFMHandles` fields (good for hot-call throughput).
- `downcallCritical(...)` fallback to default is done at handle init time, not per call.
- Runtime helpers already include optional native build optimization switches (`jparser.ffm.nativeOptimize`, `jparser.ffm.nativeLto`, `jparser.ffm.nativePgo*`).
- String input conversion has a thread-local cache in `NativeUtils.toCString(...)` to reduce repeated UTF-8 allocations for repeated literals.
- Callback upcall stubs are grouped into one arena and released on delete path (better than per-stub arenas).

## Main Problems (Priority Ordered)

### P0 - Correctness + Performance Risks

1. **ABI mismatch for `boolean` in FFM mapping**
   - Files:
     - `jParser/gen/gen-ffm/src/main/java/com/github/xpenatan/jParser/ffm/FFMTypeMapper.java`
     - generated classes in `runtime-ffm` / `lib-ffm`
   - Current mapping uses:
     - Java descriptor layout: `ValueLayout.JAVA_BOOLEAN` (1 byte)
     - C glue type: `int32_t` (4 bytes)
   - This mismatch is risky for ABI correctness and can force unnecessary conversions/marshalling.
   - Recommendation:
     - Align both sides to `bool` + `JAVA_BOOLEAN`, or align both to 32-bit integer consistently (`JAVA_INT` + `int32_t`).

2. **Generated callback enum return calls user method twice**
   - File: `examples/TestLib/lib/lib-ffm/.../CallbackClass.java`
   - In `internal_onEnumReturnCallback(...)`, `onEnumReturnCallback(...)` is invoked twice inside ternary expression.
   - Impact:
     - Doubled callback cost.
     - Side-effect hazard (behavioral bug).
   - Recommendation:
     - Store callback result in local variable once, then convert.

3. **C++ static temporary objects for value returns are thread-unsafe**
   - Generator source: `FFMCodeParser` templates `METHOD_GET_OBJ_VALUE_TEMPLATE`, `METHOD_GET_OBJ_VALUE_STATIC_TEMPLATE`.
   - Pattern uses `static [COPY_TYPE] copy_addr;` and returns pointer.
   - Impact:
     - Data race under concurrent calls.
     - False sharing and serialization effects under load.
   - Recommendation:
     - Replace with thread-local storage or caller-owned output buffer strategy.

### P1 - High Throughput Loss in Hot Paths

4. **Array copy APIs still perform per-element native crossings in generated wrappers**
   - File example: `jParser/runtime/runtime-ffm/.../NativeByteArray.java`
   - `arraycopy(...)` loops call `setValue/getValue` for each element.
   - This is expensive vs. bulk copy (`memcpy`/single downcall).
   - Recommendation:
     - Route generated `arraycopy` through `NativeUtils.copyByteArrayToNative/copyByteArrayFromNative` when size threshold is met.
     - Keep tiny sizes on scalar loops if benchmark proves faster.

5. **`NativeUtils.address(ByteBuffer)` mutates position/limit each call**
   - File: `jParser/runtime/runtime-ffm/.../NativeUtils.java`
   - Saves/restores buffer state for every call; adds overhead in frequent paths.
   - Recommendation:
     - Add fast path for `position()==0 && limit()==capacity()` to avoid mutation.
     - Consider API variants that require normalized buffers from callers.

6. **String return path uses `reinterpret(Long.MAX_VALUE).getString(0)`**
   - Generator source: `FFMCodeParser.convertToFFMBridgeMethod(...)`.
   - Very broad reinterpret and unbounded C-string scan per call.
   - Recommendation:
     - Prefer bounded/string-length-aware return conventions for hot APIs.
     - Optional: generate specialized return path using explicit length-return pair for performance-critical methods.

### P2 - Start-up and Footprint Overheads

7. **Every generated class emits duplicate `LOOKUP/LINKER/options/rethrow/downcall*` boilerplate**
   - Widespread in generated `FFMHandles` inner classes.
   - Impact:
     - Larger bytecode and class init footprint.
   - Recommendation:
     - Centralize shared linker/symbol lookup helpers into runtime utility class.
     - Keep per-class only handle fields + descriptors.

8. **Critical mode policy underused in TestLib**
   - File: `examples/TestLib/lib/lib-build/src/main/java/BuildLib.java`
   - `FFMClassData(false)` default disables critical, with narrow getter-only override.
   - In current generated `TestMethodClass`, all methods are `downcallDefault` (including many primitive-safe calls).
   - Recommendation:
     - Expand policy to enable critical for all primitive-safe, non-callback methods.
     - Keep callback-related and string/object/address-heavy methods on default.

9. **Native optimization flags are available but opt-in and undocumented in commands docs**
   - Files:
     - `BuildLib.java`
     - `BuildRuntimeHelper.java`
   - Recommendation:
     - Document recommended profiles (dev/release/pgo) in `docs/commands.md`.

## Recommended Improvement Plan

## Phase 1 (Low risk, high value)

1. Fix callback double-invocation bug in generator output path.
2. Fix boolean ABI mapping consistency in `FFMTypeMapper` + descriptor generation.
3. Extend critical-mode policy for TestLib generated classes.
4. Add fast path in `NativeUtils.address(ByteBuffer)`.

Expected: immediate call-site throughput gain and removal of correctness landmines.

## Phase 2 (Hot-path optimization)

1. Change array wrappers to bulk-copy strategy by default above threshold.
2. Keep scalar loops only for tiny copies (benchmark-driven cutoff).
3. Add generator option for bounded string return handling in performance-sensitive methods.

Expected: major reduction in FFM call count for data movement workloads.

## Phase 3 (Structural cleanup)

1. Consolidate shared downcall helper code to runtime utility.
2. Replace static C++ temporary return objects with thread-safe strategy.
3. Add official benchmark module and CI perf smoke checks.

Expected: better scalability and lower maintenance risk.

## Benchmark/Validation Checklist

Measure before/after for:

- Primitive setter/getter loop throughput (`int`, `float`, `long`).
- Boolean-heavy method throughput and correctness.
- String input/output throughput (`toCString`, C-string return decoding).
- Byte/native array transfer (`arraycopy` vs bulk copy).
- Callback latency (upcall roundtrip).

Suggested minimal metrics:

- ns/op (JMH) for hot methods.
- CPU samples to verify reduced JNI/FFM transition overhead.
- Allocation rate and retained memory for string-heavy workloads.

## Concrete Observations from Current Generated Output

- `TestMethodClass` currently emits only `downcallDefault` handles.
- Runtime helper classes (`runtime-ffm`) often use `downcallCritical` for primitive-safe methods.
- `CallbackClass` has heavy setup in constructor (`setupCallback()` builds many stubs immediately), which may hurt startup if callback object creation is frequent.

## Final Notes

- The single most important technical fix is **boolean ABI consistency**.
- The single biggest throughput win is typically **reducing per-element native crossings** (bulk array operations).
- The easiest near-term win in this repo is **expanding critical-mode usage for safe primitive-only methods** in generated TestLib bindings.

