//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
import java.util.concurrent.*;

import java.io.IOException;

import java.util.prefs.Preferences;


public class Main {

//    public static int waitSeconds = ;

    static Preferences prefs = Preferences.userNodeForPackage(Main.class);
    public static int waitSeconds = prefs.getInt("savedWaitTime", 30);
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static ScheduledFuture<?> scheduledTask;

    void main() {
        AppTray.setupTray();
        startT();
    }

    public static void startT() {

        if (scheduledTask != null && !scheduledTask.isCancelled()) {
            scheduledTask.cancel(false);
        }

        Runnable blockingTask = () -> {

            Process process = null;

            try {


                //Below Is for Frontend People
                //Basically it just import code from screen blocker (UI)
                // Please Contact Me Before Edit UI Pleaseeeeeee
                String screenblockpath = System.getProperty("java.class.path");
                ProcessBuilder processBuilder = new ProcessBuilder("java","-Dapple.awt.UIElement=true", "-cp", screenblockpath, "ScreenBlocker");
                //-Dapple.awt.UIElement --> Docker Icon Remove
                processBuilder.inheritIO();


                process = processBuilder.start();

                Thread.sleep(5000);
                // Above Code --> Who Much Time Should It have the Screen Blocker
                // 1 Second = 1000 ms
                // We need to make it adjusted from settings
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            } finally {
                if (process != null && process.isAlive()) {
                    process.destroy();
                }
            }
        };

        

        scheduler.scheduleWithFixedDelay(blockingTask, waitSeconds, waitSeconds, TimeUnit.SECONDS);
        //In Brackets
        //2) After how much time the application run
        //(Basically after User Open App after how many seconds will Screen Blocker appear first)

        //3) After 1st Block Done - Above One Thread.sleep( ms ) in above part
        //The delay for next block screen

    }


        public static void shutdown () {
            scheduler.shutdownNow();
            System.exit(0);
        }
}