package net.jasonly027.steamsalebot.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Scheduler {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private Scheduler() {}

    public static ScheduledExecutorService getScheduler() {
        return scheduler;
    }
}
