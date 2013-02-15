/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataAnalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 *
 * @author Erik
 */
public class SumDist {

    public static void main(String[] args) throws FileNotFoundException {
        String s = "C:\\Users\\Erik\\Documents\\School\\Research\\Dr. Miller\\"
                + "Extended Time Cold IC\\Particle Number Dependency\\"
                + "1000 Particles 2 Periods\\";

        Scanner sc1 = new Scanner(new File(s + "positions.dat"));
        Scanner sc2 = new Scanner(new File(s + "velocities.dat"));
        try (PrintWriter p = new PrintWriter(new File(s + "deltaDist.dat"))) {
            while (sc1.hasNextLine()) {
                double dist = 0.0;
                String[] pos = sc1.nextLine().trim().split("\\s+");
                String[] vel = sc2.nextLine().trim().split("\\s+");

                for (int i = 0; i < pos.length - 1; i++) {
                    dist += dist(delta(pos[i], pos[i + 1]), delta(vel[i], vel[i + 1]));
                }

                p.println(dist);
            }
        }
    }

    public static double delta(String left, String right) {
        return Double.parseDouble(right) - Double.parseDouble(left);
    }

    public static double dist(double x, double v) {
        return Math.sqrt(x * x + v * v);
    }
}
