package simulation;

/**
 * This class is simply a wrapper for a general exception that could occur
 * within the simulation.
 *
 * @author Erik
 */
public class EXCEPTION_Simulation extends Exception {

	//Remove these constructors from use
	private EXCEPTION_Simulation() {}

	private EXCEPTION_Simulation(String msg, Throwable cause) {}

	private EXCEPTION_Simulation(Throwable cause) {}

	public EXCEPTION_Simulation(String msg) {
		super(msg);
	}
}
