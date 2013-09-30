package simulation.components;

import simulation.listeners.EVENT_Updated;
import simulation.listeners.LISTENER_Updated;
import static simulation.Mainframe.*;
import java.util.ArrayDeque;
import java.util.Arrays;
import simulation.EXCEPTION_Simulation;
import simulation.Mainframe;

/**
 *	The {@code ParticleSystem} class holds the data for a system of particles.
 * It keeps track of time, collision events, etc and handles the evolution of
 * the system accordingly. Past events are sent to a separate data writer that
 * stores the raw data in files. 
 * 
 * <p>Unfortunately this class is currently designed assuming that no more than
 * 2 particles can ever collide at the same location at the same time. This is 
 * a HUGE error and a completely false assumption and breaks down in highly 
 * symmetric systems.</p>
 * @author Erik Nguyen
 */
public final class ParticleSystem {

	/**
	 * Pointer to the simulator.
	 */
	private final Mainframe simulator;
	
	/**
	 * Useful constant that represents the characteristic time of all systems.
	 */
	public static final double CHARACTERISTIC_TIME = Math.PI * 2;
	/**
	 * The number of particles in the ParticleSystem.
	 */
	public final int size;
	/**
	 * Useful constant that holds the value {@code size * 2}.
	 */
	private final int sizeDoubled;
	/**
	 * Useful constant that holds the value {@code size * size}.
	 */
	private final int sizeSquared;
	/**
	 * The maximum amount of characteristic time units in which the simulation
	 * will run.
	 */
	public final int maxSimulationTime;
	/**
	 * The virial ratio of the system of particles.
	 */
	private double virialRatio;
	/**
	 * The interval length at which the system's data is recorded for viewing in
	 * system time units.
	 */
	public final double systemInterval;
	/**
	 * Stores the interval length at which the system's data is recorded for
	 * viewing in characteristic time units.
	 */
	public final double characteristicInterval;
	/**
	 * The kinetic energy within the system.
	 */
	private double kineticEnergy;
	/**
	 * The potential energy within the system.
	 */
	private double potentialEnergy;
	/**
	 * The initial accumulation of velocities of each particle in the system.
	 */
	private double velocityAccum;
	/**
	 * The initial accumulation of positions of each particle in the system.
	 */
	private double positionAccum;
	/**
	 * Holds all of the listeners for this simulated system.
	 */
	private final ArrayDeque<LISTENER_Updated> _listeners = new ArrayDeque<LISTENER_Updated>();
	/**
	 * Holds the amount of time the system has been evolving in characteristic
	 * time units.
	 */
	private double simulationTime = 0.0;
	/**
	 * The accelerations of each particle in the system by index.
	 *
	 * <p>The values within this array are constant and apply to the particle in
	 * the respective index of {@code system}. As such this requires the
	 * particles in the corresponding array to change indeces as they move
	 * around in the system.</p>
	 */
	private final double[] accel;
	/**
	 * The array of particles within this specific system.
	 */
	private final Particle[] system;
	
	/**
	 * The type of system (configuration wise) that the particles are initialized
	 * in.
	 */
	public final ENUM_SystemType systemType;

	/**
	 * Constructs a system of particles based upon the desired parameters.
	 *
	 * The interval length is set to a default 1/20th of a characteristic time.
	 *
	 * @param numParticles the number of particles to be put in the system
	 * @param simLength the length of time to allow the system to evolve in
	 * characteristic time units
	 * @param virial the desired virial ratio for the system
	 * @param type the initial configuration of the system
	 */
	public ParticleSystem(int numParticles, int simLength, double virial, ENUM_SystemType type, Mainframe simulator) {
		this(numParticles, simLength, 0.05, virial, type, simulator);
	}

	/**
	 * Constructs a system of particles based upon the desired parameters.
	 *
	 * @param numParticles the number of particles to be put in the system
	 * @param simLength the length of time to allow the system to evolve in
	 * characteristic time units
	 * @param intervalSize the size of the interval at which the system will be
	 * recorded in characteristic time units
	 * @param virial the desired virial ratio for the system
	 * @param type the initial configuration of the system
	 */
	public ParticleSystem(int numParticles, int simLength, double intervalSize, double virial, ENUM_SystemType type, Mainframe simulator) {
		this.simulator = simulator;
		size = numParticles;
		sizeSquared = size * size;
		sizeDoubled = size * 2;
		maxSimulationTime = simLength;
		system = new Particle[size];
		accel = new double[size];
		systemType = type;

		setAccelerations();//initialize the acceleration values

		switch (type) {//Generate the system
			case WATERBAG_RECTANGULAR:
				generateRectangular(virial);
				break;
		}

		characteristicInterval = intervalSize;
		systemInterval = characteristicInterval * CHARACTERISTIC_TIME;
	}

	/**
	 * Generates a rectangular waterbag system with particles randomly, though
	 * generally evenly spaced out.
	 *
	 * <p>From previous simulations, the Virial ratio Vr can be shown as follows:
	 *		Vr = velBound^2 / 2 * posBound
	 * for large size values. For simplicity, assume velBound = 1.
	 * Therefore, posBound = 1 / 2 * Vr.</p>
	 * <p>The system is then shifted and scaled.<\p>
	 *
	 * @param virial the desired virial ratio for the waterbag
	 */
	private void generateRectangular(double virial) {
		double xRange = 1.0 / (2 * virial);
		double vRange = 1;
		for (int i = 0; i < size; i++) {
			double tempP = Math.random() * xRange;//position before correction
			double tempV = Math.random() * vRange;//velocity before correction
			positionAccum += tempP;//accumulate positions
			velocityAccum += tempV;//accumulate velocities
			system[i] = new Particle(i, tempP, tempV);
		}

		Arrays.sort(system);//Sort the system by position
		shiftAndScale();//Shift the system and then scale it
	}

	/**
	 * Initializes the accelerations that each particle will have due to the
	 * gravitational forces of the other particles in the system.
	 *
	 * <p>Acceleration of the ith particle is modeled by:
	 * <br>(2 * PI * G / N) * (N - 2 * (i + 1) + 1)</br>
	 * <br>where PI represents the mathematical constant pi, G represents the
	 * gravitational constant, N represents the size of the system, and i
	 * represents the index of the ith particle of the system (therefore
	 * requiring (i + 1) because the array is 0 index bound).</br></p>
	 *
	 * <p>Allow 2 * PI * G = 1 for scaling purposes.</p>
	 */
	private void setAccelerations() {
		for (int i = 0; i < size; i++) {
			accel[i] = (size - 2 * (i + 1) + 1.0) / size;
		}
	}

	/**
	 * Shifts the entire system such that the center of mass and center of
	 * momentum of the system is set to 0, and then scales the system using
	 * Rybicki scaling.
	 *
	 * <p>Because the mass of each particle is identical and equivalent to 1/N,
	 * where 1 represents the total mass of the system and N represents the
	 * number of particles in the system, it can be ignored in correcting the
	 * center of mass and momentum as a simple scale factor. Due to this, only
	 * position and velocity values are used in the correction.</p>
	 *
	 * <p>The system is shifted to (0,0), while the scaling forces the energy of
	 * the system to 0.75 regardless of system time (thereby standardizing the
	 * system).</p>
	 */
	private void shiftAndScale() {
		positionAccum /= size;//Find the current center of mass
		velocityAccum /= size;//Find the current center of momentum

		//shift the particles
		for (Particle p : system) {
			p.shift(positionAccum, velocityAccum);
		}

		calculateEnergy();
		double initialEnergy = kineticEnergy + potentialEnergy;

		for (Particle p : system) {//Scale the system by the energy
			p.scale(initialEnergy);
		}

		calculateEnergy();//Recalculate energy values
	}

	/**
	 * Calculates the total energy of the system at the current frame in time.
	 */
	private void calculateEnergy() {
		//reset the energy;
		potentialEnergy = 0.0;
		kineticEnergy = 0.0;

		for (Particle p1 : system) {
			double sum = 0.0;
			for (Particle p2 : system) {
				sum += Math.abs(p1.x - p2.x);//accumulate potentials
			}
			p1.setEnergy(sum / sizeSquared, p1.v * p1.v / sizeDoubled);
			//Accumulate the energies
			potentialEnergy += sum;
			kineticEnergy += p1.v * p1.v;
		}

		//Correct the energy values by the missing scale factor
		potentialEnergy /= 2 * sizeSquared;
		kineticEnergy /= sizeDoubled;
		virialRatio = 2 * kineticEnergy / potentialEnergy;
	}

	/**
	 * Returns the total energy of the system as calculated by summing the
	 * system's potential and kinetic energies.
	 *
	 * @return the total energy of the system
	 */
	public double getEnergy() {
		return kineticEnergy + potentialEnergy;
	}

	/**
	 * Returns the virial ratio of this system of particles.
	 *
	 * @return the virial ratio of the system
	 */
	public double getVirialRatio() {
		return virialRatio;
	}

	/**
	 * Returns the system as represented by an array of constituent particles.
	 *
	 * @return the array of particles in this system
	 */
	public Particle[] getSystem() {
		return system;
	}

	/**
	 * Adds a listener to the this system.
	 *
	 * @param listener the listener to be added
	 * @return true if the listener could be added as specified by
	 * {@code Collections.add}
	 */
	public synchronized boolean addListener(LISTENER_Updated listener) {
		return _listeners.add(listener);
	}

	/**
	 * Removes a listener from this system.
	 *
	 * @param listener the {@code LISTENER_Updated} object to be removed
	 * @return true if it was removed
	 */
	public synchronized boolean removeListener(LISTENER_Updated listener) {
		return _listeners.remove(listener);
	}

	/**
	 * Notifies all listeners that the entire system has been updated after
	 * passing another interval of time within the system's simulation.
	 * <p> The reference to time has no relation to the amount of time passed in
	 * the real world while the simulation is running. </p>
	 */
	private synchronized void _fireUpdatedEvent() {
		EVENT_Updated evt = new EVENT_Updated(this, system, simulationTime);
		for (LISTENER_Updated l : _listeners) {
			l.receiveUpdate(evt);
		}
	}
	/**
	 * This array holds the time of the next collision in system time units
	 * assuming that both particles are in the same time frame.
	 * <p>To ensure this, more necessary data is held in two other arrays:
	 * {@code baseCollisionTime} and {@code particleTimeFrame}</p>
	 */
	private double[] nextCollisionTime;
	/**
	 * This array holds the "standard" time from which a collision is projected
	 * to occur.
	 * <p>This "standard" time can be unique for each collision between two
	 * particles and can be seen as the (t = 0) for the collision. It is defined
	 * as the same as the particle in the time frame furthest along for
	 * convenience in calculating the collision. </p>
	 * <p>E.g. if one particle is at (t = .1), whereas the second is at (t =
	 * .2), the array will store (t = .2) as the "standard" time of the
	 * collision, and the simulation will automatically update the first
	 * particle to (t = .2) before updating both to the actual moment of
	 * collision.</p>
	 */
	private double[] baseCollisionTime;
	/**
	 * This array stores the actual time frame that each individual particle is
	 * located at.
	 * <p> This refers to the fact that particles are only ever updated to a
	 * specific time during a collision involving the particle or when the
	 * entire system is updated to a standard time to allow the data to be
	 * written out. This allows the system to drastically reduce the number of
	 * needed computations for speed purposes.</p>
	 */
	private double[] particleTime;
	/**
	 * Holds the index of the left particle in the pair involved in the current
	 * collision of interest.
	 */
	private int collisionLeft;

	/**
	 * Begins the simulation of this system of particles.
	 * <p>The simulation will only end once the system evolves past the maximum
	 * alloted time frame given, or if the program terminates.</p>
	 * <p>The simulation is run in a very special way to reduce computational
	 * time. When a collision between two particles occurs, rather than updating
	 * the entire system by a small amount of time, only those 2 particles are
	 * updated. However, this means that every particle could be in a different
	 * time frame from every other particle. In order to do this, collision
	 * times are stored in a slightly awkward way. "Time" is split up into 3
	 * components. The time of every particle is stored in one array. In
	 * another array, baseCollisionTime, the time from which the collision
	 * should be projected is stored. i.e. if p1 and p2 are at times t1 = 0, and
	 * t2 = t, respectively baseCollisionTime will store max(t1, t2) = t because
	 * p1 needs to be updated to t before the collision between the two
	 * particles can be projected. Lastly, the array nextCollisionTime stores
	 * the time of collision from the baseCollisionTime for a pair of particles.
	 * Therefore, when one collision is handled, up to 3 new collision need to
	 * be formed. The first between the pair the just collided, the second
	 * concerning the collision time between the left particle in the pair 
	 * and its left neighbor, and the last between the right particle in the
	 * pair and its right neighbor (e.g. [i-1, i], [i, i + 1], [i + 1, i + 2]
	 * represent the collision pairs that need to be updated).</p>
	 */
	public void simulate() {
		//For N particles, there are N - 1 collisions to look at
		nextCollisionTime = new double[size - 1];

		for (int i = 0; i < nextCollisionTime.length; i++) {
			//Generate the first set of possible collisions from the initial configurations
			nextCollisionTime[i] = calculateCollision(i, i + 1, 0.0, 0.0);
		}
		//Essentially initializes the time frames and tells any listener that the initial configurations have been set
		resetTimeFrames();

		while (simulationTime < maxSimulationTime) {//The brunt of the simulation
			double nextTime = findNextCollision();

			//If the next collision will occur after the time interval that marks exporting of data
			//This also helps prevent loss of precision as numbers get larger
			if (nextTime > systemInterval) {
				massUpdate();
				calculateEnergy();
				simulationTime += characteristicInterval;//increment the "clock"
				resetTimeFrames();
				nextTime -= systemInterval;//Adjust the time of the collision
			}

			//Update the two collided particles to the collision moment
			system[collisionLeft].update(nextTime - particleTime[collisionLeft], accel[collisionLeft]);
			system[collisionLeft + 1].update(nextTime - particleTime[collisionLeft + 1], accel[collisionLeft + 1]);
			swap(collisionLeft, collisionLeft + 1);//swap their locations (let them "pass through each other")

			//Update the stored times of the collision and particles to their updated status
			particleTime[collisionLeft] = nextTime;
			particleTime[collisionLeft + 1] = nextTime;
			baseCollisionTime[collisionLeft] = nextTime;

			//Calculate the next collision between these two particles
			nextCollisionTime[collisionLeft] = calculateCollision(collisionLeft, collisionLeft + 1, 0.0, 0.0);

			//Adjust the collision time of the particle neighboring the collided pair to the left
			if (collisionLeft > 0) {//If there is a particle to the left
				baseCollisionTime[collisionLeft - 1] = 
						Math.max(particleTime[collisionLeft - 1], particleTime[collisionLeft]);
				nextCollisionTime[collisionLeft - 1] = calculateCollision(collisionLeft - 1, collisionLeft,
						Math.abs(baseCollisionTime[collisionLeft - 1] - particleTime[collisionLeft - 1]),
						Math.abs(baseCollisionTime[collisionLeft - 1] - particleTime[collisionLeft]));
			}

			//Adjust the collision time of the particle neighboring the collided pair to the right
			if (collisionLeft + 1 < size - 1) {//If there is a particle neighboring to the right
				baseCollisionTime[collisionLeft + 1] = 
						Math.max(particleTime[collisionLeft + 1], particleTime[collisionLeft + 2]);
				nextCollisionTime[collisionLeft + 1] = calculateCollision(collisionLeft + 1, collisionLeft + 2,
						Math.abs(baseCollisionTime[collisionLeft + 1] - particleTime[collisionLeft + 1]),
						Math.abs(baseCollisionTime[collisionLeft + 1] - particleTime[collisionLeft + 2]));
			}
		}
	}

	/**
	 * Returns the time until the very next collision and sets
	 * {@code collisionLeft} to the index of the left particle in the next
	 * collision.
	 *
	 * @return the next closest collision time
	 */
	private double findNextCollision() {
		double ret = Double.MAX_VALUE;
		for (int i = 0; i < nextCollisionTime.length; i++) {
			double time = nextCollisionTime[i] + baseCollisionTime[i];
			if (ret > time) {
				ret = time;
				collisionLeft = i;
			}
		}
		return ret;
	}

	/**
	 * Updates the entire system to the next interval and adjusts all time
	 * values as needed.
	 */
	private void massUpdate() {
		for (int i = 0; i < size; i++) {//Update all of the particles to the proper time
			system[i].update(systemInterval - particleTime[i], accel[i]);
		}
		for (int i = 0; i < nextCollisionTime.length; i++) {//Adjust the time for each possible collision
			nextCollisionTime[i] = nextCollisionTime[i] + baseCollisionTime[i] - systemInterval;
		}
	}

	/**
	 * Calculates the amount of time until a collision occurs between the two
	 * particles in question, while taking into account the fact that they may
	 * be in different time frames, and returns the amount of time needed.
	 * <p>The way the difference in time frames is taken into account is via the
	 * two offset parameters sent down that denote how far forward the particles
	 * should move in time before calculating the collision. The result returned
	 * is the amount of time needed for a collision after the offsets have been
	 * taken into account.</p>
	 *
	 * @param lIndex the index of the particle on the left in a possible
	 * collision in the {@code system}
	 * @param rIndex the index of the particle on the right in a possible
	 * collision in the {@code system}
	 * @param lTimeOffset the amount of time the left particle needs to move
	 * forward in time before a collision can be accurately calculated
	 * @param rTimeOffset the amount of time the right particle needs to move
	 * forward in time before a collision can be accurately calculated
	 * @return the amount of time it will take for the two particles to collide
	 */
	private double calculateCollision(int lIndex, int rIndex, double lTimeOffset, double rTimeOffset) {
		Particle l = system[lIndex], r = system[rIndex];
		double x1 = l.x + lTimeOffset * l.v + 0.5 * lTimeOffset * lTimeOffset * accel[lIndex];
		double v1 = l.v + lTimeOffset * accel[lIndex];
		double x2 = r.x + rTimeOffset * r.v + 0.5 * rTimeOffset * rTimeOffset * accel[rIndex];
		double v2 = r.v + rTimeOffset * accel[rIndex];

		double dx = x1 - x2;
		double dv = v1 - v2;
		double da = accel[lIndex] - accel[rIndex];
		
		//This can be derived from solving for the collision time from the two
		//kinematic equations: 
		//x1 + v1 * t + .5 * a1 * t^2 = x2 + v2 * t + .5 * a2 * t^2
		double val = (-dv + Math.sqrt(dv * dv - 2 * da * dx)) / da;
		
		try {
			if (val < 0) {
				throw new EXCEPTION_Simulation(
						"Collision time for particles at indeces "
						+ "(" + lIndex + ", " + rIndex + ")"
						+ " was negative after time " + simulationTime);
			}
		} catch (EXCEPTION_Simulation ex) {
			errLog(ex.getMessage());
			errLog("Particle Data: x v a t_offset");
			errLog(String.format("Left : %f %f %f %f", l.x, l.v, accel[lIndex], lTimeOffset));
			errLog(String.format("Right: %f %f %f %f", r.x, r.v, accel[rIndex], rTimeOffset));
			errLog("Simulation terminated...");
			simulator.exit(1);
		}

		
		return val;
	}

	/**
	 * Resets the base values of the collision time frames and the time frames
	 * that the particles are in after the entire system has been updated to a
	 * standard time, and then fires an update to all listeners that the system
	 * was updated.
	 */
	private void resetTimeFrames() {
		baseCollisionTime = new double[size - 1];
		particleTime = new double[size];
		_fireUpdatedEvent();
	}

	/**
	 * Swaps two particles in the system itself.
	 *
	 * <p>Swapping requires the particles to do two things. Firstly they must
	 * have their indeces in the array {@code system} swapped. Secondly, the
	 * swapped particles MUST have their positions such that the particle on the
	 * left must now also physically lie to the right in the pair. This ensures
	 * that the array is accurate and lets the particles be affected by the
	 * proper acceleration values.</p>
	 *
	 * <p>Note that due to floating point error, a swap in physical positions is
	 * not always needed. However, it normally is needed.</p>
	 *
	 * @param left index of the left particle in the pair to be swapped
	 * @param right index of the right particle in the pair to be swapped
	 */
	private void swap(int left, int right) {
		//Ensures that the particles pass through each other by swapping
		//positions.  Normally, position differences are on the order of E-15
		//if they need to be swapped.
		if (system[left].x < system[right].x) {
			double l = system[left].x;
			system[left].x = system[right].x;
			system[right].x = l;
		}

		//swap index locations
		Particle temp = system[left];
		system[left] = system[right];
		system[right] = temp;
	}
}
