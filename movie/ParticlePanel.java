package movie;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import javax.swing.JPanel;

/*
 * Given a pre-defined set of colors, this class will be used to display the
 * particles in the simulation.  The information on the particles is sent down
 * each update.
 */
public class ParticlePanel extends JPanel {

    //Holds the color coding of the particles
    private final Color[] colors;

    public ParticlePanel(Color[] c){
        colors = c;
        setSize(400,400);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        int w = getWidth();
        int h = getHeight();
        double midX = w / 2., midY = h / 2.;

        //Draw ordinate
        g2.draw(new Line2D.Double(midX, 0, midX, h));
        //Draw abscissa
        g2.draw(new Line2D.Double(0, midY, w, midY));


        double scaleX = midX / 4.;
        double scaleY = midY / 4.;

        //Draw ordinate and abscissa scale marks
        for (double i = 0.5; (midY - scaleY * i >= 0.0) || (midX - scaleX * i >= 0.0); i += 0.5) {
            if (midY - scaleY * i >= 0.0) {
                //Ordinate scale marks
                g2.draw(new Line2D.Double(midX - 5, midY - i * scaleY, 
                                            midX + 5, midY - i * scaleY));
                g2.draw(new Line2D.Double(midX - 5, midY + i * scaleY, 
                                            midX + 5, midY + i * scaleY));
            }
            if (midX - scaleX * i >= 0.0) {
                //Abscissa scale marks
                g2.draw(new Line2D.Double(midX - i * scaleX, midY - 5, 
                                            midX - i * scaleX, midY + 5));
                g2.draw(new Line2D.Double(midX + i * scaleX, midY - 5, 
                                            midX + i * scaleX, midY + 5));
            }
        }

        //Paint all particles
        for (int i = 0; i < p.length; i++) {
            g2.setPaint(colors[i]);
            double x = midX + scaleX * Double.parseDouble(p[i]);
            double y = midY - scaleY * Double.parseDouble(v[i]);
            g2.fill(new Ellipse2D.Double(x - 1, y - 1, 2, 2));
        }       

        //Draw time and numerical scale reference marks
        g2.setPaint(Color.BLUE);
        g2.drawString("Characteristic Time: " + t, 5, h - 15);
        g2.drawString("1", (int) (midX + scaleX - 3), (int) (midY + 16));
        g2.drawString("1", (int) (midX - 10), (int) (midY - scaleY + 5));
    }
    
    //Temporarily holds the position and velocities of particles
    private String[] p, v;
    private double t;//Holds the time

    public void update(String[] pos, String[] vel, double time) {
        p = pos;
        v = vel;
        t = time;
        repaint();
    }
}
