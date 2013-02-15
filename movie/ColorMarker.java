package movie;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/*
 * This class will define a set of colors to color specific regions of the
 * system as needed by the user. Data is currently read from a dat file instead
 * of an xls spreadsheet.
 */
public class ColorMarker {

    private static Color[] colors;
    private static double maxX, maxV;
    /*
     * Returns an array of colors for a rectangular waterbag in as follows
     *
     * G*****R Where G stands for green, B for blue, R for red ******* and * for
     * grey particles. The size of the regions ***B*** is dependent upon the
     * area specified by percent ******* [percent of area for core and for
     * corners] R*****G
     */

    public static Color[] initialWaterbag(String root, double percent) throws FileNotFoundException {

        //Record the initial configuration of the system
        String[] s1, s2;
        try (Scanner pos = new Scanner(new File(root + "positions.dat"))) {
            try (Scanner vel = new Scanner(new File(root + "velocities.dat"))) {
                s1 = pos.nextLine().trim().split("\\s+");
                s2 = vel.nextLine().trim().split("\\s+");
            }
        }

        colors = new Color[s1.length];//initialize the color array

        //Convert initial record to numerical record
        double[] x = new double[s1.length], v = new double[s1.length];
        for (int i = 0; i < x.length; i++) {
            x[i] = Double.parseDouble(s1[i]);
            v[i] = Double.parseDouble(s2[i]);
        }

        maxX = 0.0;
        maxV = 0.0;
        rectDim(x, v);//Find waterbag dimensions

        //Sets the boundaries that will be used to classify particles
        double alpha = maxX * Math.sqrt(percent),
                beta = maxV * Math.sqrt(percent),
                _alpha = maxX * (1 - Math.sqrt(percent)),
                _beta = maxV * (1 - Math.sqrt(percent));

        //Searches through the particles
        for (int i = 0; i < x.length; i++) {

            //Checks the corners
            if (!(inRange(x[i], _alpha) || inRange(v[i], _beta))) {
                if (x[i] * v[i] > 0) {//Quadrants 1, 3 corners
                    colors[i] = Color.RED;
                } else {//Quadrants 2,4 corners
                    colors[i] = Color.GREEN;
                }
            } else if (inRange(x[i], alpha) && inRange(v[i], beta)) {
                colors[i] = Color.BLUE;//Core
            } else {//everything else
                colors[i] = Color.LIGHT_GRAY;
            }
        }

        return colors;
    }

    /*
     * @Pre-requisite: range > 0; Returns true if the double falls within
     * [-range,range].
     */
    private static boolean inRange(double d, double range) {
        return (-range < d) && (d < range);
    }

    /*
     * Assuming that the particles form a rectangle in mu space, this finds the
     * dimensions. Remember that the waterbag should be centered at (0,0) in mu
     * space and so finding the largest x and v value will give the proper
     * dimensions for a heavily populated system.
     */
    private static void rectDim(double[] x, double[] v) {

        for (int i = 0; i < x.length; i++) {
            maxX = Math.max(maxX, Math.abs(x[i]));
            maxV = Math.max(maxV, Math.abs(v[i]));
        }

    }

    /*
     * Configures the colors of the particles depending upon the energies of the
     * particles at the end of the simulation.
     */
    public static Color[] haloDist(String root, double boundary, int time) throws FileNotFoundException {
        //Retrieve the proper time
        Color darkMagenta = Color.MAGENTA.darker();//holder for specific color

        String[] e;
        int t = time;
        try (Scanner sc = new Scanner(new File(root + "energies.dat"))) {
            while (t-- > 0) {
                sc.nextLine();
            }
            e = sc.nextLine().trim().split("\\s+");
        }
        colors = new Color[e.length];
        for (int i = 0; i < e.length; i++) {//Store colors
            if (Double.parseDouble(e[i]) < boundary)//core or low halo
            {
                colors[i] = Color.LIGHT_GRAY;
            } else//Presumably halo particles
            {
                colors[i] = darkMagenta;
            }
        }


        return colors;
    }

    private static double radius(String s1, String s2) {
        return Math.sqrt(Math.pow(Double.parseDouble(s1), 2) + Math.pow(Double.parseDouble(s2), 2));
    }
}
