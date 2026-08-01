package com.github.xpenatan.jParser.builder.bundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class NativeTargetTest {
    @Test
    public void resolvesCurrentDesktopNames() {
        assertEquals(
                NativeTarget.of(
                        NativeTarget.OperatingSystem.WINDOWS,
                        NativeTarget.Architecture.X86_64),
                NativeTarget.currentDesktop("Windows 11", "amd64"));
        assertEquals(
                NativeTarget.of(
                        NativeTarget.OperatingSystem.LINUX,
                        NativeTarget.Architecture.X86),
                NativeTarget.currentDesktop("Linux", "i686"));
        assertEquals(
                NativeTarget.of(
                        NativeTarget.OperatingSystem.MACOS,
                        NativeTarget.Architecture.ARM64),
                NativeTarget.currentDesktop("Darwin", "aarch64"));
    }

    @Test
    public void rejectsUnsupportedDesktopNames() {
        assertThrows(
                IllegalStateException.class,
                () -> NativeTarget.currentDesktop("Plan 9", "amd64"));
        assertThrows(
                IllegalStateException.class,
                () -> NativeTarget.currentDesktop("Windows 11", "mips64"));
    }
}
