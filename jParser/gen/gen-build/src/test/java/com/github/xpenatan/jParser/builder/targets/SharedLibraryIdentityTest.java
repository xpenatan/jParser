package com.github.xpenatan.jParser.builder.targets;

import static org.junit.Assert.assertTrue;

import com.github.xpenatan.jParser.core.util.CustomFileDescriptor;
import java.util.ArrayList;
import org.junit.Test;

public class SharedLibraryIdentityTest {

    @Test
    public void linuxSharedLibrariesUseTheirFileNameAsSoname() {
        TestLinuxTarget target = new TestLinuxTarget();

        target.prepareLink("/tmp/libExample64.so");

        assertTrue(target.linkerFlags.contains("-Wl,-soname,libExample64.so"));
    }

    @Test
    public void macSharedLibrariesUseLoaderRelativeInstallName() {
        TestMacTarget target = new TestMacTarget();

        target.prepareLink("/tmp/libExample64.dylib");

        assertTrue(target.linkerFlags.contains("-Wl,-install_name,@rpath/libExample64.dylib"));
    }

    private static final class TestLinuxTarget extends LinuxTarget {
        void prepareLink(String outputPath) {
            isStatic = false;
            onLink(new ArrayList<CustomFileDescriptor>(), "objects.txt", outputPath);
        }
    }

    private static final class TestMacTarget extends MacTarget {
        TestMacTarget() {
            super(false);
        }

        void prepareLink(String outputPath) {
            isStatic = false;
            onLink(new ArrayList<CustomFileDescriptor>(), "objects.txt", outputPath);
        }
    }
}
