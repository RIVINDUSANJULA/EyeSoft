import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

void main() {
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    Runnable blockingTask = () -> {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    };

    scheduler.scheduleWithFixedDelay(blockingTask, 0, 30, TimeUnit.SECONDS);
}