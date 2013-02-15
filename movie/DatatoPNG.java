package movie;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

/*
 *
 */
public class DatatoPNG extends JFrame {

    //Holds the root directory and the directory for pictures
    private final String rootDir, fileLoc;
    private final ParticlePanel g;

    public DatatoPNG(String root, double energyBoundary, double t) throws IOException {
        rootDir = root;
        fileLoc = rootDir + "Pictures\\Halo\\" + t + " CharTime\\";
        new File(fileLoc).mkdirs();//Setup directories

        //Setup the panel that will make the images
        g = new ParticlePanel(ColorMarker.haloDist(root, energyBoundary, Math.round((float) t)));

        //Setup the default viewable window
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(400, 400);
        setLocation(600, 200);
        setVisible(true);
        add(g);
    }

    public DatatoPNG(String root, int percent) throws IOException {
        rootDir = root;
        fileLoc = rootDir + "Pictures\\" + percent + " percent\\";

        new File(fileLoc).mkdirs();

        g = new ParticlePanel(ColorMarker.initialWaterbag(rootDir, percent / 100.));

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(400, 400);
        setLocation(600, 200);
        setVisible(true);
        add(g);
    }

    /*
     * Sets up all of the images to be made into .png files
     */
    public void makePictures() throws IOException {
        try (Scanner pos = new Scanner(new File(rootDir + "positions.dat"))) {
            try (Scanner vel = new Scanner(new File(rootDir + "velocities.dat"))) {

                for (int i = 0; pos.hasNextLine(); i++) {

                    //update images
                    g.update(pos.nextLine().trim().split("\\s+"),
                            vel.nextLine().trim().split("\\s+"), Double.parseDouble(String.format("%.1f", i/1.)));

                    //make pictures
                    makePNG(String.format("img%04d", i));
                }
            }
        }
    }
    BufferedImage image;//Holds the created image
    /*
     * Creates an .png file out of the current image displayed in the
     * ParticlePanel.
     */

    private void makePNG(String imgName) throws IOException {

        image = (BufferedImage) g.createImage(g.getWidth(), g.getHeight());
        g.paint(image.getGraphics());

        //Try to create the picture
        try {
            ImageIO.write(image, "png", new File(fileLoc + imgName + ".png"));
        } catch (IOException e) {
            System.out.println("Had trouble writing image: " + imgName);
            throw e;
        }

    }

    /*
     * makes itself invisible then releases all used resources
     */
    public void release() {
        setVisible(false);
        dispose();
    }
}
