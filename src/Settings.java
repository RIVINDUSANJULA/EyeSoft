import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class Settings {

    private static JFrame frame = null;

    public static void showWindow() {
        if (frame != null && frame.isVisible()) {
            frame.toFront();
            return;
        }

        frame = new JFrame("Settings");

        frame.setType(Window.Type.UTILITY);
        // Hide from Windows
        //Windows Person Fix It Pleaseeeee

        frame.setSize(300, 150);



        // Do Not Kill Background Task
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel timingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));



        JLabel label = new JLabel("Wait");
        // Above is Add Wait Time. Fix UI



        JTextField timeField = new JTextField(String.valueOf(Main.waitSeconds), 5);
        JButton saveButton = new JButton("Save");
        //Save Btn Fix UI

        saveButton.addActionListener(e -> {
            try {
                int newWait = Integer.parseInt(timeField.getText().trim());


                Main.waitSeconds = newWait;


                Preferences prefs = Preferences.userNodeForPackage(Main.class);
                prefs.putInt("savedWaitTime", newWait);
                Main.startT();

                frame.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Valid No");
                //Setting Wrror
            }
        });

        timingPanel.add(label);
        timingPanel.add(timeField);
        timingPanel.add(saveButton);
        timingPanel.setVisible(true);

        JPanel aboutPanel = new JPanel(new BorderLayout());

        JLabel aboutLabel = new JLabel("<html><center><b>EyeSoft Blocker</b><br>Take care of your eyes!<br>Version 1.0</center></html>", SwingConstants.CENTER);
        aboutPanel.add(aboutLabel, BorderLayout.CENTER);


        tabbedPane.addTab("Timing", timingPanel);
        tabbedPane.addTab("About", aboutPanel);


        frame.add(tabbedPane);
        frame.setVisible(true);
    }
}