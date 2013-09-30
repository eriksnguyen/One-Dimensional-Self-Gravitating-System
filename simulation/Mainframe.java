package simulation;

import simulation.components.ParticleSystem;
import simulation.listeners.DataWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import simulation.frames.FRAME_Setup;

/**
 * The Mainframe class is the central class of the simulation. It creates data
 * logging and error logging files for reference and prints everything in a
 * console window for reference. The class also initializes specific particle
 * systems for simulation as given from input from the user and the writer that
 * saves system information in data files.
 *
 * @author Erik
 */
public class Mainframe {

	private static final SimpleDateFormat TIMEFORMAT = new SimpleDateFormat("HH:mm:ss");
	private static PrintStream logOut;
	private static PrintStream errOut;
	private ParticleSystem system;
	private DataWriter writer;

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		new Mainframe();
	}
	
	public Mainframe(){
		setupDirectoriesAndStreams();
		new FRAME_Setup(this).setVisible(true);
	}
	
	/**
	 * Records the system created by the setup frame.
	 * @param sys 
	 */
	public void setSystem(ParticleSystem sys){
		system = sys;
		writer = new DataWriter(sys);
		system.addListener(writer);
	}
	
	/**
	 * Simulate the system.
	 */
	public void simulate(){
		logHeader();
		dataLog("Starting Simulation...");
		system.simulate();
		logFooter();
		
		writer.close(system);
	}

	/**
	 * Initializes the data and error output streams and creates any folders
	 * missing from the basic directory archive.
	 */
	private void setupDirectoriesAndStreams(){
		//Check for log directory
		if (!new File("logs").exists()) {
			new File("logs").mkdir();
		}
		
		//Save only the latest 5 logs
		File out4 = new File("logs/out_log4.dat");
		File out3 = new File("logs/out_log3.dat");
		File out2 = new File("logs/out_log2.dat");
		File out = new File("logs/out_log.dat");
		if (new File("logs/out_log5.dat").exists()) {
			new File("logs/out_log5.dat").delete();
		}
		if (out4.exists()) {
			out4.renameTo(new File("logs/out_log5.dat"));
			new File("logs/out_log4.dat").delete();
		}
		if (out3.exists()) {
			out3.renameTo(new File("logs/out_log4.dat"));
			new File("logs/out_log3.dat").delete();
		}
		if (out2.exists()) {
			out2.renameTo(new File("logs/out_log3.dat"));
			new File("logs/out_log2.dat").delete();
		}
		if (out.exists()) {
			out.renameTo(new File("logs/out_log2.dat"));
			new File("logs/out_log.dat").delete();
		}
		
		try {//Set output streams
			PrintStream temp = new PrintStream(System.out);
			System.setOut(new PrintStream(new File("logs/out_log.dat")));
			logOut = temp;
			dataLog("Output Stream Initialized.");
		} catch (FileNotFoundException ex){}
		
		try {//Set error streams
            PrintStream temp = new PrintStream(System.err);
            System.setErr(new PrintStream(new File("logs/error_log.dat")));
            errOut = temp;
            dataLog("Error Stream Initialized");
        } catch (FileNotFoundException ex) {}
		
		//Initialized needed Directories
		dataLog("Checking Necessary Directories...");
        if (!new File("data").exists()) {
            new File("data").mkdir();
            dataLog("Creating directory: data");
        }
		
		DateFormat df = new SimpleDateFormat("M/d/yy");
        String formattedDate = df.format(new Date());
        DateFormat df2 = new SimpleDateFormat("HH:mm:ss");
        String formattedTime = df2.format(new Date());
        dataLog("");
        dataLog("---------------------------------------------------------------");
        dataLog("Log for Graviational Particle Simulation. Version 2. By Erik Nguyen");
        dataLog("---------------------------------------------------------------");
        dataLog("Log started: " + formattedDate + " at " + formattedTime);
        dataLog("---------------------------------------------------------------");
        dataLog("");
        dataLog("========================");
	}
	
	/**
     * Because both the console and an output file are used, rather than having
     * 2 print statements each time something needs to be logged, the dataLog()
     * method receives an Object and prints its toString to both the console and
     * the output stream.
     *
     * @param output - The thing to be printed.
     */
    public static void dataLog(Object output) {
        System.out.println(output);
        logOut.println(output);
    }

    /**
     * Same thing as dataLog() but for the error stream.
     *
     * @param output - The thing to be printed
     */
    public static void errLog(Object output) {
        System.err.println(output);
        errOut.println(output);
    }
	
	/**
     * This prints to the output streams a header that acts as a designation of
     * new processes. It only contains the time the action took place.
     */
    public static void logHeader() {
        dataLog("");
        dataLog("---- " + getTime() + " ----");
    }

    /**
     * This closes the actions that were differentiated by the logHeader();
     */
    public static void logFooter() {
        dataLog("------------------");
    }
	
	/**
	 * Returns the time formatted by hour:minute:second.
	 * @return time as a {@code String}
	 */
	public static String getTime(){
		return TIMEFORMAT.format(new Date());
	}

	/**
	 * Closes all streams and exits the program
	 * @param state 
	 */
	public void exit(int state) {
		dataLog("Closing logs...");
		logOut.flush();
		logOut.close();
		System.out.flush();
		System.out.close();
		errOut.flush();
		errOut.close();
		System.err.flush();
		System.err.close();
		System.exit(state);
	}
}
