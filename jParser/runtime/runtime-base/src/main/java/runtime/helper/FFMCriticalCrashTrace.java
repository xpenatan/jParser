package runtime.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lightweight crash breadcrumb for FFM critical downcalls.
 * Stores the latest critical method/symbol before invokeExact so post-crash analysis can recover it.
 */
public final class FFMCriticalCrashTrace {

    private static final boolean ENABLED = Boolean.getBoolean("jparser.ffm.crashTrace");
    private static final boolean STRICT_SYNC = Boolean.getBoolean("jparser.ffm.crashTrace.strictSync");
    private static final int FLUSH_EVERY = getPositiveInt("jparser.ffm.crashTrace.flushEvery", 512);
    private static final long FLUSH_INTERVAL_MS = getPositiveLong("jparser.ffm.crashTrace.flushIntervalMs", 50L);
    private static final File TRACE_FILE = new File(System.getProperty("jparser.ffm.crashTrace.file", "jparser_ffm_last_critical.log")).getAbsoluteFile();

    private static final AtomicLong SEQUENCE = new AtomicLong();
    private static final Object FLUSH_LOCK = new Object();
    private static final ThreadLocal<Integer> LOCAL_COUNT = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };

    private static volatile State lastState = State.EMPTY;
    private static volatile long lastFlushedSeq = -1L;

    static {
        if(ENABLED && !STRICT_SYNC) {
            startFlusherThread();
        }
    }

    private FFMCriticalCrashTrace() {
    }

    public static void mark(String symbolName, String javaMethodName) {
        if(!ENABLED) {
            return;
        }
        Thread thread = Thread.currentThread();
        State state = new State(
                SEQUENCE.incrementAndGet(),
                System.currentTimeMillis(),
                thread.getId(),
                sanitize(thread.getName()),
                sanitize(symbolName),
                sanitize(javaMethodName));
        lastState = state;

        if(STRICT_SYNC) {
            flush(state.sequence, true);
            return;
        }

        int localCount = LOCAL_COUNT.get() + 1;
        if(localCount >= FLUSH_EVERY) {
            LOCAL_COUNT.set(0);
            flush(state.sequence, false);
        }
        else {
            LOCAL_COUNT.set(localCount);
        }
    }

    private static void startFlusherThread() {
        Thread flusher = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(FLUSH_INTERVAL_MS);
                    }
                    catch(InterruptedException ignored) {
                        break;
                    }
                    flush(0L, false);
                }
            }
        }, "jparser-ffm-crash-trace");
        flusher.setDaemon(true);
        flusher.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                flush(0L, true);
            }
        }, "jparser-ffm-crash-trace-shutdown"));
    }

    private static void flush(long minSequence, boolean syncDisk) {
        State state = lastState;
        if(state.sequence < minSequence || state.sequence <= lastFlushedSeq) {
            return;
        }
        synchronized(FLUSH_LOCK) {
            state = lastState;
            if(state.sequence < minSequence || state.sequence <= lastFlushedSeq) {
                return;
            }
            writeState(state, syncDisk);
            lastFlushedSeq = state.sequence;
        }
    }

    private static void writeState(State state, boolean syncDisk) {
        try {
            File parent = TRACE_FILE.getParentFile();
            if(parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            String line = "seq=" + state.sequence +
                    " tsMillis=" + state.timeMillis +
                    " threadId=" + state.threadId +
                    " threadName=" + state.threadName +
                    " symbol=" + state.symbolName +
                    " method=" + state.javaMethodName +
                    "\n";
            byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
            try(FileOutputStream out = new FileOutputStream(TRACE_FILE, false)) {
                out.write(bytes);
                out.flush();
                if(syncDisk) {
                    out.getFD().sync();
                }
            }
        }
        catch(Throwable ignored) {
        }
    }

    private static int getPositiveInt(String key, int defaultValue) {
        Integer value = Integer.getInteger(key);
        if(value == null || value <= 0) {
            return defaultValue;
        }
        return value;
    }

    private static long getPositiveLong(String key, long defaultValue) {
        Long value = Long.getLong(key);
        if(value == null || value <= 0L) {
            return defaultValue;
        }
        return value;
    }

    private static String sanitize(String value) {
        if(value == null) {
            return "";
        }
        return value.replace('\n', ' ').replace('\r', ' ');
    }

    private static final class State {
        private static final State EMPTY = new State(-1L, 0L, -1L, "", "", "");

        private final long sequence;
        private final long timeMillis;
        private final long threadId;
        private final String threadName;
        private final String symbolName;
        private final String javaMethodName;

        private State(long sequence, long timeMillis, long threadId, String threadName, String symbolName, String javaMethodName) {
            this.sequence = sequence;
            this.timeMillis = timeMillis;
            this.threadId = threadId;
            this.threadName = threadName;
            this.symbolName = symbolName;
            this.javaMethodName = javaMethodName;
        }
    }
}
