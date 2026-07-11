package emu.c.com.github.xpenatan.jParser.loader;

import com.github.xpenatan.jParser.loader.JParserLibraryLoaderListener;
import com.github.xpenatan.jParser.loader.JParserLibraryLoaderOptions;
import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import org.teavm.interop.Address;
import org.teavm.interop.Import;

/** TeaVM C implementation of the shared native-library loader API. */
public final class JParserLibraryLoader {
    private static final int RESULT_SUCCESS = 0;

    private JParserLibraryLoader() {
    }

    public static void load(String libraryName, JParserLibraryLoaderListener listener) {
        loadInternal(libraryName, null, listener);
    }

    public static void load(String libraryName, JParserLibraryLoaderOptions options,
                            JParserLibraryLoaderListener listener) {
        loadInternal(libraryName, options, listener);
    }

    private static void loadInternal(String libraryName, JParserLibraryLoaderOptions options,
                                     JParserLibraryLoaderListener listener) {
        if(listener == null) {
            throw new RuntimeException("Should implement listener");
        }

        Throwable failure = null;
        try {
            byte[] logicalName = toCString(libraryName);
            byte[] path = toCString(options != null ? options.path : null);
            byte[] fileName = toCString(options != null ? options.fileName : null);
            int autoAddPrefix = options == null || options.autoAddPrefix ? 1 : 0;
            int autoAddSuffix = options == null || options.autoAddSuffix ? 1 : 0;

            int result = nativeLoad(addressOf(logicalName), addressOf(path), addressOf(fileName),
                    autoAddPrefix, autoAddSuffix);
            if(result != RESULT_SUCCESS) {
                failure = nativeFailure(result, libraryName);
            }
        }
        catch(Throwable t) {
            failure = t;
        }

        notifyListener(listener, failure);
    }

    static void notifyListener(JParserLibraryLoaderListener listener, Throwable failure) {
        listener.onLoad(failure == null, failure);
    }

    public static void closeQuietly(Closeable closeable) {
        if(closeable != null) {
            try {
                closeable.close();
            }
            catch(Throwable ignored) {
            }
        }
    }

    static byte[] toCString(String value) {
        if(value == null) {
            return null;
        }
        byte[] utf8 = value.getBytes(StandardCharsets.UTF_8);
        byte[] terminated = new byte[utf8.length + 1];
        System.arraycopy(utf8, 0, terminated, 0, utf8.length);
        return terminated;
    }

    private static Address addressOf(byte[] value) {
        return value == null ? Address.fromLong(0) : Address.ofData(value);
    }

    private static Throwable nativeFailure(int result, String libraryName) {
        String detail = null;
        int size = nativeErrorSize();
        if(size > 0) {
            byte[] message = new byte[size + 1];
            int copied = nativeErrorCopy(Address.ofData(message), message.length);
            if(copied > 0) {
                detail = new String(message, 0, copied, StandardCharsets.UTF_8);
            }
        }
        if(detail == null || detail.isEmpty()) {
            detail = "TeaVM C native-library load failed for '" + libraryName + "' (error " + result + ")";
        }
        return new JParserSharedLibraryLoadRuntimeException(detail);
    }

    @Import(name = "jparser_teavmc_loader_load")
    private static native int nativeLoad(Address logicalName, Address path, Address exactFileName,
                                         int autoAddPrefix, int autoAddSuffix);

    @Import(name = "jparser_teavmc_loader_error_size")
    private static native int nativeErrorSize();

    @Import(name = "jparser_teavmc_loader_error_copy")
    private static native int nativeErrorCopy(Address destination, int capacity);

    public static class JParserSharedLibraryLoadRuntimeException extends RuntimeException {
        private static final long serialVersionUID = 8263101105331379889L;

        public JParserSharedLibraryLoadRuntimeException(String message) {
            super(message);
        }

        public JParserSharedLibraryLoadRuntimeException(Throwable cause) {
            super(cause);
        }

        public JParserSharedLibraryLoadRuntimeException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
