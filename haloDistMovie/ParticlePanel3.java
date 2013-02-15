package haloDistMovie;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import javax.swing.JPanel;

/**
 *
 * @author Erik
 */
public class ParticlePanel3 extends JPanel{

    private String[] p, v;
    public ParticlePanel3(String[] xinit, String[] vinit){
        p = xinit;
        v = vinit;
        setSize(400,400);
        
        colors = new Color[p.length];
        for(int i = 0; i < p.length; i++)
            colors[i] = Color.BLACK;
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
    
    //Holds the color coding of the particles
    private Color[] colors;
    private double t;//Holds the time

    public void update(Color[] c, String[] pos, String[] vel, double time) {
        colors = c;
        p = pos;
        v = vel;
        t = time;
        repaint();
    }
}
