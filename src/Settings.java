import javax.swing.*;
import java.awt.*;

public class Settings {

    private static JFrame frame = null;

    public static void showWindow() {
        if (frame != null && frame.isVisible()) {
            frame.toFront();
            return;
        }

        frame = new JFrame("Settings");
        frame.setSize(300, 150);



        // Do Not Kill Background Task
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 20));
        frame.setLocationRelativeTo(null);
        frame.setAlwaysOnTop(true);

        JLabel label = new JLabel("Wait");
        // Above is Add Wait Time. Fix UI



        JTextField timeField = new JTextField(String.valueOf(Main.waitSeconds), 5);
        JButton saveButton = new JButton("Save");
        //Save Btn Fix UI

        saveButton.addActionListener(e -> {
            try {
                int newTime = Integer.parseInt(timeField.getText().trim());


                Main.waitSeconds = newTime;
                Main.startT();

                frame.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Valid No");
                //Setting Wrror
            }
        });

        frame.add(label);
        frame.add(timeField);
        frame.add(saveButton);
        frame.setVisible(true);
    }
}