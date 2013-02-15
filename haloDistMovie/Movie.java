package haloDistMovie;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 *
 * @author Erik
 */
public class Movie {

    /**
     * @param args the command line arguments
     */
    static String fileDir;
    public static void main(String[] args) throws FileNotFoundException, InterruptedException, IOException {
        fileDir = "C:\\Users\\Erik\\Documents\\School\\Research\\Dr. Miller\\New Set of Runs\\0.3013 Virial Ratio\\";
        String fileLoc = fileDir + "Pictures\\HaloGeneral\\";
        (new File(fileLoc+"Initial\\")).mkdirs();
        (new File(fileLoc+"TrueSim\\")).mkdirs();
        Scanner erg = new Scanner(new File(fileDir+"energies.dat"));
        Scanner pos = new Scanner(new File(fileDir + "positions.dat"));
        Scanner vel = new Scanner(new File(fileDir + "velocities.dat"));
        
        String[] xinit = pos.nextLine().trim().split("\\s+");
        String[] vinit = vel.nextLine().trim().split("\\s+");
        ParticlePanel2 sim = new ParticlePanel2(xinit, vinit);
        ParticlePanel3 trueSim = new ParticlePanel3(xinit,vinit);
        
        JFrame frame = new JFrame("Initial Conditions");
        frame.add(sim);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,400);
        frame.setLocation(300,300);
        frame.setVisible(true);
                
        JFrame frame2 = new JFrame("Simulation");
        frame2.add(trueSim);
        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame2.setSize(400,400);
        frame2.setLocation(700,300);
        frame2.setVisible(true);
        

        colors = new Color[xinit.length];
        double time = 0;
        BufferedImage image, image2;
        while(erg.hasNextLine()){
            findColors(erg.nextLine().trim().split("\\s+"),0.0017);
            
            sim.update(colors, ++time);
            trueSim.update(colors, pos.nextLine().trim().split("\\s+"),
                    vel.nextLine().trim().split("\\s+"), time);
            Thread.sleep(500);
            
            /*image = (BufferedImage) trueSim.createImage(trueSim.getWidth(), trueSim.getHeight());
            trueSim.paint(image.getGraphics());
            ImageIO.write(image, "png", new File(fileLoc+"TrueSim\\" + time + ".png"));
            
            image2 = (BufferedImage) sim.createImage(sim.getWidth(), sim.getHeight());
            sim.paint(image2.getGraphics());
            ImageIO.write(image2, "png", new File(fileLoc+"Initial\\" + time + ".png"));*/
        }
    }
    
    static Color[] colors;
    private static void findColors(String[] energies, double energyBoundary){
        for(int i = 0; i < energies.length; i++){
            if(Double.parseDouble(energies[i]) > energyBoundary)
                colors[i] = Color.magenta.darker();
            else
                colors[i] = Color.LIGHT_GRAY;
        }
    }

}
