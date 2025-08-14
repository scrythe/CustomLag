package dev.scrythe.customlag.DelayHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PacketScheduler {
    private static final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    public static void delayPacket(Runnable runnable, int delay) {
        service.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }
}
