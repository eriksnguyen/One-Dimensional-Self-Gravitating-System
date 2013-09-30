package simulation.components;

/**
 * The Particle class represents a particle in the simulation. It stores many
 * values that are pertinent to a particle, position, velocity, potential and
 * kinetic energy, and time spent in the simulation. However it is dependent on
 * the ParticleSystem class to send it updated values like new potential and
 * kinetic energy values as dependent on the particle's location in the system,
 * along with acceleration and time to update as dependent on events that occur
 * in the entire system of particles.
 *
 * @author Erik Nguyen
 */
public class Particle implements Comparable<Particle> {

	/**
	 * The number used to identify this particle within its particle system.
	 */
	public final int ID_Number;
	protected double x;//The current position of the particle
	protected double v;//The current velocity of the particle
	private double time = 0; //The current time that the particle stands in
	private double kinetic_energy;
	private double potential_energy;

	/**
	 * Constructs a particle from the specified parameters.
	 *
	 * @param id_number the identification number of a particle in the system
	 * @param position the initial position of the particle in the system
	 * @param velocity the initial velocity of the particle in the system
	 */
	public Particle(int id_number, double position, double velocity) {
		ID_Number = id_number;
		x = position;
		v = velocity;
	}

	/**
	 * Compares two particles using their positions and momentums.
	 *
	 * <p>The mass of the two particles is the same by definition and so
	 * velocity is used to represent momentum. This is equivalent to:
	 * <br>{@code this.x - oP.x}</br>
	 * <br>unless {@code this.x - op.x = 0}, in which case it is equivalent
	 * to:</br>
	 * <br>{@code this.v - oP.v}</br></p>
	 *
	 * @param oP argument particle to be compared with this particle
	 *
	 * @return the value -1 if this particle is to the left of the argument
	 * particle in position or if this particle coincides with the position of
	 * the argument particle and has a smaller velocity; a value 1 of otherwise
	 */
	@Override
	public int compareTo(Particle oP) {
		//Return -1 if this is strictly to the left
		if (Math.abs(oP.x - x) <= 4.9E-324) {//If positions are identical
			return v < oP.v ? -1 : 1;//compare by velocity
		}
		return (x < oP.x) ? -1 : 1;//Compare by positions
	}

	/**
	 * Compares this particle to an object and returns true only if the argument
	 * isn't null and is a Particle with the same ID number as this particle.
	 *
	 * @param o the object to compare this particle to
	 * @return true if the argument represents this particle. false otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Particle)) {
			return false;
		}
		return ((Particle) o).ID_Number == ID_Number;
	}

	/**
	 * Returns a hash code for this particle.
	 *
	 * <p>The hash code is computed as {@code this.ID + 5 * 17}</p>
	 *
	 * @return a hash code value for this particle
	 */
	@Override
	public int hashCode() {
		int hash = 5;
		hash = 17 * hash + this.ID_Number;
		return hash;
	}

	/**
	 * Returns the potential energy of the particle at a specific instance in
	 * time.
	 *
	 * @return the potential energy of the particle.
	 */
	public double getPotentialEnergy() {
		return potential_energy;
	}

	/**
	 * Returns the kinetic energy of the particle at a specific instance in
	 * time.
	 *
	 * @return the kinetic energy of the particle.
	 */
	public double getKineticEnergy() {
		return kinetic_energy;
	}

	/**
	 * Returns the total energy of the particle at a specific instance in time
	 * as defined by the sum of its potential and kinetic energies.
	 *
	 * @return the total energy of the particle.
	 */
	public double getEnergy() {
		return potential_energy + kinetic_energy;
	}

	/**
	 * Returns the ID number of the particle in the system.
	 *
	 * @return identification number of the particle
	 */
	public int getID() {
		return ID_Number;
	}
	
	public double getX(){
		return x;
	}
	
	public double getV(){
		return v;
	}

	/**
	 * Scales this particle's position and momentum according to the total
	 * energy of the system using the Rybicki scaling method.
	 *
	 * <p>For convenience in scaling, let:
	 * <br>M = 1</br>
	 * <br>2 * PI * G = 1</br>
	 * <br>where M is the mass of the system, PI is the mathematical constant,
	 * and G is the gravitational constant.</br></p>
	 *
	 * @param systemEnergy the energy of the system that the particle is in
	 */
	public void scale(double systemEnergy) {
		v = (v / 2) * Math.sqrt(3 / systemEnergy);
		x = 3 * x / (4 * systemEnergy);
	}

	/**
	 * Sets the energy of the particle as calculated from the system it lies in.
	 *
	 * @param potential the potential energy of this particle
	 * @param kinetic the kinetic energy of this particle
	 */
	public void setEnergy(double potential, double kinetic) {
		potential_energy = potential;
		kinetic_energy = kinetic;
	}

	/**
	 * Shifts the particle in the system such that the entire system lies in the
	 * simulation's center of mass and momentum.
	 *
	 * <p>The shift should occur to the entire system by the amounts specified.
	 * Note that values sent down are subtracted, and hence a positive value
	 * represents a shift to the left while a negative value represents a shift
	 * to the right.<p>
	 *
	 * @param position the amount to shift this particle's position by
	 * @param velocity the amount to shift this particle's velocity by
	 */
	public void shift(double position, double velocity) {
		x -= position;
		v -= velocity;
	}

	/**
	 * Updates the particle by a specified amount of time at a given
	 * acceleration according to basic kinematic equations.
	 *
	 * @param t the amount of time the particle spends moving at a given
	 * acceleration
	 * @param a the given acceleration of this particle for a specific time
	 * period
	 */
	public void update(double t, double a) {
		x = x + v * t + .5 * a * t * t;
		v = v + a * t;
		time += t;
	}

	/**
	 * Returns a string representation of the particle with its ID number first,
	 * followed by it's position, followed by it's velocity, followed by the
	 * current timestamp that it is in.
	 *
	 * @return A string representation of this particle.
	 */
	@Override
	public String toString() {
		return String.format("%6d\t%f\t%f\t%f", ID_Number, x, v, time);
	}
	
	/**
	 * Returns a string representation of the position and velocity of the
	 * particle.
	 * @return A simplified data representation of the particle.
	 */
	public String dataString(){
		return String.format("%f\t%f", x, v);
	}
}