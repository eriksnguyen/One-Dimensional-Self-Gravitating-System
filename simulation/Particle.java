package simulation;

public class Particle implements Comparable<Particle> {

    //Position, velocity and acceleration values
    protected double x, v;
    private int id;//used to distinguish particles
    private double potential, kinetic;//Only instantiated and defined by the particles in scrutiny.

    public Particle(int num, double i1, double i2) {
        id = num;
        x = i1;
        v = i2;
    }

    public void setEnergy(double d, double e) {
        potential = d;
        kinetic = e;
    }

    public double getPotential() {
        return potential;
    }

    public double getKinetic() {
        return kinetic;
    }

    public double getEnergy() {
        return potential + kinetic;
    }

    //Updates the particle by the amount of time specified using kinematics equations
    public void update(double t, double a) {
        x = x + v * t + .5 * a * t * t;
        v = v + a * t;
    }

    /*
     * Shifts the position by the parameter specified (primarily used for
     * shifting the whole system such that the original center of mass is the
     * origin)
     */
    public void shift(double p, double v) {
        x -= p;
        this.v -= v;
    }

    /*
     * Scales the Particle with the Rybicki scaling method, both x and v are
     * scaled. M_total =1 and that 2piG = 1 for scaling
     */
    public void scale(double energy) {
        v = (v / 2) * Math.sqrt(3 / energy);
        x = 3 * x / (4 * energy);
    }

    @Override
    public boolean equals(Object o) {
        return ((Particle) o).id == id;
    }

    @Override
    public int compareTo(Particle oP) {
        //Return -1 if this is strictly to the left
        if (Math.abs(oP.x - x) <= 4.9E-324) {//If positions are identical
            return v < oP.v ? -1 : 1;//compare by velocity
        }
        return (x < oP.x) ? -1 : 1;//Compare by positions
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return id + "\t" + x + "\t" + v;
    }
}
