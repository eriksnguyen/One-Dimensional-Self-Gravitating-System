package movie;

import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.BoxLayout;
import javax.swing.JFrame;

/*
 * Recreates the simulation using the data stored in the data files
 */
public class Recreate {

    private static String fileDir;

    public static void main(String[] args) throws IOException {
        fileDir = "C:\\Users\\Erik\\Documents\\School\\Research\\Dr. Miller\\New Set of Runs\\0.4957 Virial Ratio\\";

        //Creates the title for the "movie"
        String title = "";
        try (Scanner info = new Scanner(new File(fileDir + "info.dat"))) {
            title = info.nextLine();
            info.nextLine();
            info.nextLine();
            title += " " + info.nextLine();
        }

        //If the pictures aren't already stored, make them
        checkPictures("HaloDist", 200);

        System.out.println(fileDir);
        //Setup Movie and run it
        movie(new ImagesToMovie(fileDir), new ButtonLayout(), title);
    }

    public static void checkPictures(String picType, int i) throws IOException {
        DatatoPNG d = null;
        switch (picType) {
            case "InitialDist":
                if (!(new File(fileDir + "Pictures\\" + i + " percent\\")).exists()) {
                    d = new DatatoPNG(fileDir, i);
                }
                fileDir += "Pictures\\" + i + " percent\\";
                break;
            case "HaloDist":
                if (!(new File(fileDir + "Pictures\\Halo\\" + i + ".0 CharTime\\").exists())) {
                    d = new DatatoPNG(fileDir, 0.0015, i);
                }
                fileDir += "Pictures\\Halo\\" + i + ".0 CharTime\\";
                break;
        }

        if (d != null) {
            d.makePictures();
            d.release();
        }

    }
    /*
     * Sets up the movie and then plays it
     */

    private static void movie(ImagesToMovie i, ButtonLayout b, String title) {
        //Creates the frame in which the movie will be viewed.
        JFrame f = new JFrame(title);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(400, 500);
        f.setLocation(600, 200);
        f.setVisible(true);

        //Formats the viewable frame
        Container pane = f.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(i);
        pane.add(b);

        //Add the movie viewer to the listeners of the buttons
        b.addListener(i);

        try {
            //run movie
            i.begin();
        } catch (IOException ex) {
            System.out.println("Could not run movie");
        }
    }
}
