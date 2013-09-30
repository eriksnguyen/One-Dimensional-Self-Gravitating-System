package simulation.listeners;

import java.io.File;
import java.io.FileNotFoundException;
import static simulation.Mainframe.*;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import simulation.components.Particle;
import simulation.components.ParticleSystem;

/**
 * This class handles writing the particle system's data into data files
 * for storage. Note that some data is truncated to floats in order to reduce
 * file storage sizes, and hence some resolution will be lost.
 * @author Erik
 */
public class DataWriter implements LISTENER_Updated{
	private static final SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
	private int size;
	private Particle[] temp;
	private float[] particle_e, particle_v, particle_x;
	private PrintWriter write_e, write_v, write_x;
	private double initialVirial;
	private final String rootDir;
	
	//Prevent default constructor calls
	private DataWriter(){
		rootDir = null;
	}
	
	public DataWriter(ParticleSystem sys){		
		logHeader();
		dataLog("Initialized data streams...");
		rootDir = "data/" + sys.systemType + "/" + DATEFORMAT.format(new Date())+"/";
		File dir = new File(rootDir);
		if(!dir.exists())
			dir.mkdirs();
		
		size = sys.size;
		initialVirial = sys.getVirialRatio();
		
		try{//Initialize the data writers
			write_e = new PrintWriter(new File(rootDir + "energies.dat"));
			write_v = new PrintWriter(new File(rootDir + "velocity.dat"));
			write_x = new PrintWriter(new File(rootDir + "position.dat"));
		} catch (FileNotFoundException ex){}
		
		//Initialize the storage arrays
		particle_e = new float[size];
		particle_v = new float[size];
		particle_x = new float[size];
		
		dataLog("Recording initial settings");
		recordInitialSettings(sys);
		
		dataLog("Completed.");
		logFooter();
	}
	
	/**
	 * Records the initial positions and velocities of the particles.
	 * <p>Each particle is given its own line.</p>
	 * @param sys the system to be recorded
	 */
	private void recordInitialSettings(ParticleSystem sys){
		try {
			PrintWriter temp = new PrintWriter(new File(rootDir + "init.dat"));
			String[] init = new String[size];
			for(Particle p : sys.getSystem()){
				init[p.ID_Number] = p.dataString();
			}
			for(String s : init){
				temp.println(s);
			}
			temp.flush();
			temp.close();
		} catch (FileNotFoundException ex) {}
	}

	/**
	 * After receiving an update, the data writer dumps the particle 
	 * data into the proper files. Each particle is put on the same line
	 * and different lines represent different times in the simulation.
	 * However, the particles are ordered within the line by their ID
	 * numbers. Hence while the rows of data represent time evolution,
	 * the columns of data represent 1 distinct particle each.
	 * @param evt the trigger event for an update of the system 
	 */
	@Override
	public void receiveUpdate(EVENT_Updated evt) {
		temp = evt.data;
		
		//Saves the data into the properly ordered spots and
		//truncates to data to save space.
		for(int j = 0; j < size; j++){
			particle_e[temp[j].ID_Number] = (float)temp[j].getEnergy();
			particle_v[temp[j].ID_Number] = (float)temp[j].getV();
			particle_x[temp[j].ID_Number] = (float)temp[j].getX();
		}
		
		//Print the data
		for(int j = 0; j < size; j++){
			write_e.print(particle_e[j] + "\t");
			write_v.print(particle_v[j] + "\t");
			write_x.print(particle_x[j] + "\t");
		}
		
		//End the lines
		write_e.println();
		write_v.println();
		write_x.println();
	}
	
	/**
	 * Closes the data writer.
	 * <p>The end data for the particle system is first written out into a
	 * separate file {@code info.dat}. Then every data stream is flushed
	 * and closed.</p>
	 * @param sys 
	 */
	public void close(ParticleSystem sys){
		logHeader();
		dataLog("Closing the Data Writer...");
		dataLog("Writing final configurations to file 'info.dat'...");
		try {
			PrintWriter end = new PrintWriter(new File(rootDir + "info.dat"));
			end.println("Size: " + size);
			end.println("Simulation Time: " + sys.maxSimulationTime + " characteristic time units");
			end.println("Final System Energy: " + sys.getEnergy());
			end.println("Initial Virial Ratio: " + initialVirial);
			end.println("Final Virial Ratio: " + sys.getVirialRatio());
			end.println("System Time Resolution: " + sys.characteristicInterval + " characteristic time units");
			end.flush();
			end.close();
		} catch (FileNotFoundException ex) {}
		
		dataLog("Flushing data streams...");
		write_e.flush();
		write_v.flush();
		write_x.flush();
		
		dataLog("Closing data streams...");
		write_e.close();
		write_v.close();
		write_x.close();
		
		dataLog("Completed.");
		logFooter();
	}
}
