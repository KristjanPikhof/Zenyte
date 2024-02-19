package MyHelpers.Threads;

public class eThreads {

    // how to use:
    //logger.info("Number of threads running: " + eThreads.getActiveThreadCount());
    public static int getActiveThreadCount() {
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        int activeCount = rootGroup.activeCount();
        int threadCount = activeCount;
        Thread[] threads = new Thread[activeCount];
        rootGroup.enumerate(threads, false);
        for (Thread thread : threads) {
            if (thread == null)
                continue;
            ThreadGroup group = thread.getThreadGroup();
            while (group != null && group.getParent() != null) {
                group = group.getParent();
            }
            if (group == rootGroup) {
                threadCount++;
            }
        }
        return threadCount;
    }
}
