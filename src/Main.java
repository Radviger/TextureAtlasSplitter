import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Texture Atlas Splitter");
        frame.setResizable(false);
        frame.setMinimumSize(new Dimension(350, 200));
        frame.setMaximumSize(new Dimension(350, 200));
        frame.setPreferredSize(new Dimension(350, 200));
        JTextField dnd = new JTextField();
        dnd.setEditable(false);
        dnd.setHorizontalAlignment(SwingConstants.CENTER);
        dnd.setText("Drop atlas file here");
        dnd.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (droppedFiles.size() == 1) {
                        File file = droppedFiles.get(0);
                        processFile(frame, file);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        frame.add(dnd);
        frame.setVisible(true);
    }

    private static void error(JFrame frame, String text) {
        JOptionPane.showMessageDialog(frame, text, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static void success(JFrame frame, String text) {
        JOptionPane.showMessageDialog(frame, text, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void processFile(JFrame frame, File file) throws IOException {
        System.out.println("Got file dropped: " + file);
        String fileName = file.getName();
        if (!fileName.endsWith(".png")) {
            error(frame, "Expected png image");
            return;
        }
        String name = fileName.substring(0, fileName.length() - 4);
        File outputDir = new File(file.getParentFile(), name);
        if (!outputDir.exists()) {
            if (!outputDir.mkdir()) {
                error(frame, "Unable to create output directory: " + name);
                return;
            }
        }
        if (!outputDir.isDirectory()) {
            error(frame, "Output directory is a file: " + name);
            return;
        }
        BufferedImage image = ImageIO.read(file);
        int width = image.getWidth();
        int height = image.getHeight();
        if (width != height) {
            error(frame, "Expected square atlas (" + width + " != " + height + ")");
            return;
        }
        if (width % 16 != 0) {
            error(frame, "Atlas size must be multiple fo 16 (was " + width + ")");
            return;
        }
        int slots = width / 16;
        System.out.println("Slots: " + slots);
        for (int x = 0; x < slots; x++) {
            for (int y = 0; y < slots; y++) {
                BufferedImage slot = image.getSubimage(x * 16, y * 16, 16, 16);
                int id = x + y * 16;
                ImageIO.write(slot, "png", new File(outputDir, id + ".png"));
            }
        }
        success(frame, "Done!");
    }
}
