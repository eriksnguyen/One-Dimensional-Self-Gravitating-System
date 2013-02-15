package movie;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/*
 * Creates a movie out of the images in a file
 */
public final class ImagesToMovie extends JPanel implements ActionListener {

    private Dimension size = new Dimension();
    private BufferedImage image;//Image to display
    private File[] all;//All image files
    private long waitLength;//Pause time

    public ImagesToMovie(String root) {

        all = new File(root).listFiles();
        waitLength = 100;
        update(new BufferedImage(200, 200, 1));
    }

    /*
     * Display method
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Center image in this component.
        int x = (getWidth() - size.width) / 2;
        int y = (getHeight() - size.height) / 2;
        g.drawImage(image, x, y, this);
    }

    /*
     * Updates the shown image
     */
    public void update(BufferedImage i) {
        image = i;
        size.setSize(image.getWidth(), image.getHeight());
        repaint();
    }
    
    //Variables that affect movie play
    private boolean pause = false, bForward = true, running = true;
    private int i = 0;

    /*
     * Begins the movie
     */
    public void begin() throws IOException {
        for (; i < all.length;) {
            if (i < 0) {
                i = 0;
            }
            update(ImageIO.read(all[i]));
            if (pause) {
                continue;
            } else if (bForward) {
                i++;
            } else {
                i--;
            }
            autoPause();
        }
        running = false;
    }

    @Override
    public Dimension getPreferredSize() {
        return size;
    }

    /*
     * Basically the time between frames
     */
    private void autoPause() {
        try {
            Thread.sleep(waitLength);
        } catch (InterruptedException e) {
        }
    }

    /*
     * Receives events and changes variables as needed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "pause":
                pause = true;
                break;
            case "forward":
                bForward = true;
                break;
            case "backward":
                bForward = false;
                break;
            case "play":
                pause = false;
                break;
            case "restart":
                i = 0;
                if (running == false) {
                    running = true;
                    try {
                        begin();
                    } catch (IOException e1) {
                    }
                }
                break;
        }
    }
}
