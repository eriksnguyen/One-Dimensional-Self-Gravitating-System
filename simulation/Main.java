package simulation;

import java.io.FileNotFoundException;

public class Main {

    /*
     * For the Rectangular Waterbag system, the virial ratio, Vr = v_lim^2/ (2 *
     * x_lim) where x_lim and v_lim are the parameters used to define a waterbag
     * before it is shifted. Use:
     *
     * double vR = v_lim^2/ (2 * x_lim); do{ sys = new PSystem(numParticles,
     * simLength, xlim, vlim, "RectWaterbag); }while(Math.abs(sys.virialRatio -
     * vR)/vR > 0.01);
     *
     * That will create a system w/ desired virial ratio within 1% error.
     */
    public static void main(String[] args) throws FileNotFoundException {

        int[] num = {1};
        for (int i : num) {
            //Setup system;
            PSystem sys = new PSystem(1000, 6000, i, 0.001, "Tremaine_Homo_Density");

            System.out.println("System Parameters: " + sys.virialRatio + " Vr "
                    + sys.getEnergy() + " Total Energy");

            //create the data writer instance
            DataWriter writer = new DataWriter(sys);

            //allows the writer to receive events
            sys.addUpdatedListener(writer);

            sys.simulate();//begin simulation
            writer.close(sys.simLimit, sys.getEnergy(), sys.virialRatio);

        }

    }
}
