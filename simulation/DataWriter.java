package simulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;

public class DataWriter implements UpdatedListener {

    //Size of the system
    private int size;
    //Will be used to hold the whole system after updates
    private Particle[] temp;
    //will be used to print out information on whole system
    //using floats to compress data.
    private float[] energyData, positionData, velocityData;
    //used to print out information about every particle in the system
    private PrintWriter pEnergy, pPosition, pVelocity;
    //directory where everything is to be stored
    private String rootDir;

    //Prevents calling the default constructor
    private DataWriter() {
    }

    public DataWriter(PSystem p) throws FileNotFoundException {
        //Create the directory where the information will be printed
        rootDir = "C:\\Users\\Erik\\Documents\\School\\Research\\Dr. Miller\\Extended Time Cold IC\\Period Dependency\\"
                + (new Date()).toString().replaceAll(":", "-").replaceAll(" ", "_").substring(4) + "\\";
        (new File(rootDir)).mkdirs();

        size = p.size;

        pEnergy = new PrintWriter(new File(rootDir + "energies.dat"));
        pVelocity = new PrintWriter(new File(rootDir + "velocities.dat"));
        pPosition = new PrintWriter(new File(rootDir + "positions.dat"));

        energyData = new float[size];
        positionData = new float[size];
        velocityData = new float[size];

        //record the initial configuration of the system
        recordInit(p);
    }

    /*
     * Records the initial positions/velocities of every particle in the system.
     */
    private void recordInit(PSystem system) throws FileNotFoundException {
        try (PrintWriter p = new PrintWriter(new File(rootDir + "init.dat"))) {
            String[] init = new String[size];
            for (Particle pt : system.getSys()) {
                init[pt.getId()] = pt.toString();
            }

            for (String s : init) {
                p.println(s);
            }
        }
    }

    /*
     * Receives notification of a mass update to a characteristic time. Prints
     * out information as on whole system.
     */
    @Override
    public void updateReceived(UpdatedEvent event) {

        temp = event.info;

        for (int j = 0; j < size; j++) {
            energyData[temp[j].getId()] = (float)temp[j].getEnergy();
            positionData[temp[j].getId()] = (float)temp[j].x;
            velocityData[temp[j].getId()] = (float)temp[j].v;
        }

        for (int j = 0; j < size; j++) {
            pEnergy.print(energyData[j] + "\t");
            pVelocity.print(velocityData[j] + "\t");
            pPosition.print(positionData[j] + "\t");
        }

        pEnergy.println();
        pVelocity.println();
        pPosition.println();
    }

    public void close(int simLimit, double energy, double virialRatio) throws FileNotFoundException {
        //Prints out final conditions
        try (PrintWriter p = new PrintWriter(new File(rootDir + "info.dat"))) {
            p.println("Size: " + size);
            p.println("Sim time: " + simLimit);
            p.println("Final System Energy: " + energy);
            p.println("System Virial Ratio: " + virialRatio);
            p.println("System Time Resolution: 0.1 Characteristic Times");
        }
        
        //release resources
        pEnergy.close();
        pPosition.close();
        pVelocity.close();
    }
}
