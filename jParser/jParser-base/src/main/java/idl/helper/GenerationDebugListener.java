package idl.helper;

/**
 * Simple runtime helper used by generated code to notify a global debug listener.
 */
public final class GenerationDebugListener {

    private GenerationDebugListener() {}

    public interface DebugListener {
        void onCall(String className, String methodName, Object[] params);
    }

    /** Public static listener that can be set by application code. */
    public static volatile DebugListener LISTENER = null;

    public static void notify(String className, String methodName, Object... params) {
        DebugListener l = LISTENER;
        if(l != null) {
            try {
                l.onCall(className, methodName, params);
            } catch(Throwable t) {
                // Listener exceptions should not break generated code execution.
            }
        }
    }
}

