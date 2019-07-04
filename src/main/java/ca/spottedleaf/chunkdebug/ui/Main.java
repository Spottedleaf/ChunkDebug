package ca.spottedleaf.chunkdebug.ui;

import ca.spottedleaf.chunkdebug.data.ServerInformation;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {

    static final JFrame THE_FRAME = new JFrame("Chunk debugging tool");

    private static void setupMain() throws Throwable {
        if (GraphicsEnvironment.isHeadless()) {
            throw new UnsupportedOperationException("Environment is headless");
        }

        /* Search through each graphics device to find a monitor, and find the main monitor's resolution */
        final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();

        final GraphicsDevice[] devices =  env.getScreenDevices();

        final List<GraphicsDevice> screens = new ArrayList<>(devices.length);

        for (final GraphicsDevice device : devices) {
            if (device.getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
                screens.add(device);
            }
        }

        if (screens.isEmpty()) {
            throw new UnsupportedOperationException("No raster screen");
        }

        //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        /* Assume first screen is the main screen */

        final GraphicsDevice mainScreen = screens.get(0);
        final DisplayMode mode = mainScreen.getDisplayMode();


        Util.setMonitorSize(mode.getWidth(), mode.getHeight());
        THE_FRAME.setBounds(mode.getWidth() / 2 - 320, mode.getHeight() / 2 - 240, Util.scaleX(640), Util.scaleY(480));
        THE_FRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static File selectFile() {
        final JFileChooser pickFile = new JFileChooser();
        pickFile.setApproveButtonText("Open Log");
        pickFile.setDialogTitle("Select chunk dump");

        final int result = pickFile.showOpenDialog(THE_FRAME);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return pickFile.getSelectedFile();
    }

    private static ServerInformation setupWaitScreen(final File file) {
        final JTextArea text = new JTextArea("Parsing " + file.getName());
        Font font = text.getFont();
        font = font.deriveFont(font.getSize2D() * 1.3f);
        text.setFont(font);
        text.setBackground(THE_FRAME.getBackground());

        final JPanel panel = new JPanel();
        panel.add(Box.createVerticalStrut(Util.scaleY(5)));
        panel.add(text);
        panel.add(Box.createVerticalStrut(Util.scaleY(5)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        final JPanel canvas = new JPanel();
        canvas.add(Box.createHorizontalStrut(Util.scaleX(5)));
        canvas.add(panel);
        canvas.add(Box.createHorizontalStrut(Util.scaleX(5)));

        THE_FRAME.add(canvas);
        THE_FRAME.pack();
        THE_FRAME.setVisible(true);

        try {
            return ServerInformation.fromFile(file);
        } catch (final Throwable ex) {
            ex.printStackTrace();
            System.exit(-1);
            return null;
        }
    }

    public static void main(final String[] args) throws Throwable {
        setupMain();

        // get file
        final File file = selectFile();
        if (file == null) {
            System.exit(0);
            return;
        }

        final ServerInformation info = setupWaitScreen(file);

        System.out.println();
    }
}
