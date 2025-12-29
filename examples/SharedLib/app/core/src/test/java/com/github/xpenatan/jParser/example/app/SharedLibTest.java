package com.github.xpenatan.jParser.example.app;

import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;
import com.github.xpenatan.jparser.idl.IDLLoader;
import libA.LibALoader;
import libB.LibBLoader;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SharedLibTest {

    private static boolean libLoaded = false;

    @BeforeClass
    public static void setUp() {
        IDLLoader.init(new JParserLibraryLoaderListener() {
            @Override
            public void onLoad(boolean idl_isSuccess, Throwable idl_t) {
                if(idl_t != null) {
                    idl_t.printStackTrace();
                    return;
                }
                LibALoader.init((libA_isSuccess, libA_t) -> {
                    if(libA_t != null) {
                        libA_t.printStackTrace();
                        return;
                    }
                    LibBLoader.init((libB_isSuccess, libB_t) -> {
                        if(libB_t != null) {
                            libB_t.printStackTrace();
                            return;
                        }
                        libLoaded = true;
                    });
                });
            }
        });
    }

    @Test
    public void test_shared_lib() {
        if(!libLoaded) {
            Assert.fail("Libraries not loaded");
        }
        boolean test = SharedLib.test();
        Assert.assertTrue("SharedLib must return true", test);
    }
}