import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.io.IOException;

void main() {

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    Runnable blockingTask = () -> {

        Process process = null;

        try {


            //Below Is for Frontend People
            //Basically it just import code from screen blocker (UI)
            // Please Contact Me Before Edit UI Pleaseeeeeee
            String screenblockpath = System.getProperty("java.class.path");
            ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", screenblockpath, "ScreenBlocker");
            processBuilder.inheritIO();


            process = processBuilder.start();

            Thread.sleep(5000);
            // Above Code --> Who Much Time Should It have the Screen Blocker
            // 1 Second = 1000 ms
            // We need to make it adjusted from settings
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (process != null || process.isAlive()) {
                process.destroy();
            }
        }
    };

    scheduler.scheduleWithFixedDelay(blockingTask, 10, 30, TimeUnit.SECONDS);
    //In Brackets
    //2) After how much time the application run
    //(Basically after User Open App after how many seconds will Screen Blocker appear first)

    //3) After 1st Block Done - Above One Thread.sleep( ms ) in above part
    //The delay for next block screen
}