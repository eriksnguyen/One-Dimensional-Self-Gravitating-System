package simulation;

import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PSystem {

    public final int size, size2, size_2;
    public final int simLimit;
    public final double virialRatio;
    private final double[] a;
    
    private final double INTERVAL = Math.PI/10;
    private Particle[] sys;//the system of particles
    private double positionCenter, momentumCenter;
    private double simCharTime;

    /*
     * length must be in system times
     */
    public PSystem(int num, int length, double xlim, double vlim, String type) {
        sys = new Particle[size = num];
        a = new double[num];

        size2 = size * size;
        size_2 = 2 * size;

        simLimit = length;

        switch (type) {
            case "RectWaterbag":
                random(xlim, vlim);
                break;
            case "Tremaine_Homo_Density":
                homoD(xlim, vlim);
                break;
            case "Tremaine_Hetero_Density":
                heteroD(xlim, vlim);
                break;
        }


        setAccelerations();
        shiftAndScale();

        virialRatio = 2 * kinetic / potential;
    }

    /*
     * Creates a constant density system
     */
    private void random(double x, double v) {
        for (int i = 0; i < size; i++) {
            double tempP = Math.random() * x;
            double tempV = Math.random() * v;
            positionCenter += tempP;//accumulate positions
            momentumCenter += tempV;//accumulate velocities
            sys[i] = new Particle(i, tempP, tempV);
        }

        Arrays.sort(sys);//Sort the system by position
    }

    /*
     * Homogeneous density Tremaine system. Setup for 4 periods within -pi/2 <=
     * x <= pi/2
     */
    private void homoD(double period, double vlim) {
        double xlim = Math.PI / 2;
        double step = 2 * xlim / size;//ensures even density
        double x = -xlim + step / 2;//Start at the negative and allow for symmetry

        for (int i = 0; i < size; i++) {
            double tempV = vlim * (-Math.sin(x) /*+ Math.sin(2 * period * x)*/);
            positionCenter += x;
            momentumCenter += tempV;
            sys[i] = new Particle(i, x, tempV);

            x += step;
        }

        //Note this will already by sorted by position
    }

    private void heteroD(double xlim, double vlim) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    /*
     * Shifts the entire system such that the center of mass and momentum is
     * (0,0). Scaling also forces the energy of the system to become 0.75 for
     * any system. This standardizes energy for every system.
     */

    private void shiftAndScale() {
        positionCenter /= size;//find true center of mass
        momentumCenter /= size;//find true center of momentum

        for (Particle p : sys)//shift each particle to the center of the system
        {
            p.shift(positionCenter, momentumCenter);
        }

        setEnergy();
        double initialEnergy = getEnergy();

        for (Particle p : sys)//scale the system by energy
        {
            p.scale(initialEnergy);
        }

        //Ensures all variables are correct
        setEnergy();
    }
    /*
     * Returns the energy of the system. Pre-requisite: The system is already
     * shifted to the center of mass and momentum
     *
     * Note: Mass of each particle is 1/N because the total mass of system is 1.
     * M (total)=1 and that 2piG = 1 for scaling
     */
    private double kinetic, potential;

    public double getEnergy() {
        return kinetic + potential;
    }

    public void setEnergy() {
        kinetic = 0;
        potential = 0;

        for (Particle p1 : sys) {
            double sum = 0.0;
            for (Particle p2 : sys) {
                sum += Math.abs(p1.x - p2.x);
            }
            p1.setEnergy(sum / size2, p1.v * p1.v / size_2);
            potential += sum;
            kinetic += p1.v * p1.v;
        }

        potential /= 2 * size2;
        kinetic /= size_2;
    }

    /*
     * Sets the accelerations Acceleration of the ith particle is modeled by
     * (2piG/N)(N-2i+1) (use (i+1) because java starts with i=0 We assume that
     * the right is the positive direction.
     *
     * Also allow 2piG = 1 for scaling purposes
     */
    private void setAccelerations() {
        for (int i = 0; i < size; i++) {
            a[i] = (size - 2 * (i + 1) + 1.0) / size;
        }
    }

    public Particle[] getTargets() {
        Particle[] ret = new Particle[4];

        double tol = 15. / size;//tolerance level
        for (Particle p : sys) {//iterate through the system
            if (ret[0] == null || dist(ret[0]) > dist(p)) {
                ret[0] = p;//center particle
            }
            if (inTolerance(p.x, tol) && (ret[1] == null || ret[1].v < p.v)) {
                ret[1] = p;//outer + velocity particle w/ minimal position
            }
            if (inTolerance(p.v, tol) && (ret[2] == null || ret[2].x < p.x)) {
                ret[2] = p;//outer + position particle w/ minimal velocity
            }
            if ((ret[3] == null || dist(ret[3]) < dist(p)) && (p.x > 0 && p.v > 0)) {
                ret[3] = p;//Top Right Corner particle
            }
        }

        return ret;
    }

    /*
     * Returns true if the variables lies within a tolerance level of +- "d"
     * from 0.0
     */
    private boolean inTolerance(double var, double d) {
        return (-d < var) && (var < d);
    }

    /*
     * Returns the distance a particle is from the phase-space origin
     */
    private double dist(Particle p) {
        return Math.sqrt(p.x * p.x + p.v * p.v);
    }

    public synchronized Particle[] getSys() {
        return sys;
    }
    private List<UpdatedListener> _listeners = new ArrayList<>();

    public synchronized void addUpdatedListener(UpdatedListener l) {
        _listeners.add(l);
    }

    public synchronized void removeUpdateListener(UpdatedListener l) {
        _listeners.remove(l);
    }

    private synchronized void _fireUpdatedEvent() {
        UpdatedEvent update = new UpdatedEvent(this, sys.clone(), simCharTime);
        for (UpdatedListener l : _listeners) {
            l.updateReceived(update);
        }
        //   	System.out.println(getEnergy());
    }
    private double[] nextSet;//next collisions
    private double[] timeFrame;//particle time frame
    private double[] collisionFrame;//collision time frame
    private int nextLoc;//left particle location

    public void simulate() {
        //holds all of the next possible collisions
        nextSet = new double[size - 1];
        //generate the initial possible collisions
        for (int i = 0; i < size - 1; i++) {
            nextSet[i] = calCollision(i, i + 1, 0.0, 0.0);
        }

        reset();//initialize

        while (simCharTime < simLimit) {
            /*
             * for(Particle p: sys) System.out.println(p);
             * System.out.println(Arrays.toString(timeFrame));
             * System.out.println(Arrays.toString(nextSet));
             * System.out.println(Arrays.toString(collisionFrame));
             */
            findNext();

            //Go by characteristic Times
            if (nextTime > INTERVAL) {
                massUpdate();
                setEnergy();
                reset();
                simCharTime += 0.1;
            }

            sys[nextLoc].update(nextTime - timeFrame[nextLoc], a[nextLoc]);
            sys[nextLoc + 1].update(nextTime - timeFrame[nextLoc + 1], a[nextLoc + 1]);
            swap(nextLoc, nextLoc + 1);

            //update the time frames of that collision
            timeFrame[nextLoc] = nextTime;
            timeFrame[nextLoc + 1] = nextTime;
            collisionFrame[nextLoc] = nextTime;
            nextSet[nextLoc] = calCollision(nextLoc, nextLoc + 1, 0.0, 0.0);

            if (nextLoc > 0) {//Left modify
                collisionFrame[nextLoc - 1] = Math.max(timeFrame[nextLoc - 1], timeFrame[nextLoc]);
                nextSet[nextLoc - 1] = calCollision(nextLoc - 1, nextLoc,
                        Math.abs(collisionFrame[nextLoc - 1] - timeFrame[nextLoc - 1]),
                        Math.abs(collisionFrame[nextLoc - 1] - timeFrame[nextLoc]));
            }
            if (nextLoc < (size - 2)) {//Right modify
                collisionFrame[nextLoc + 1] = Math.max(timeFrame[nextLoc + 2], timeFrame[nextLoc + 1]);
                nextSet[nextLoc + 1] = calCollision(nextLoc + 1, nextLoc + 2,
                        Math.abs(collisionFrame[nextLoc + 1] - timeFrame[nextLoc + 1]),
                        Math.abs(collisionFrame[nextLoc + 1] - timeFrame[nextLoc + 2]));
            }

        }

    }

    /*
     * Calculates the collision between two particles that are not necessarily
     * in the same time frame. This is mitigated by the addition of offsets in
     * time to ensure that calculation takes differing time frames into account.
     */
    private double calCollision(int l, int r, double offsetLeft, double offsetRight) {
        //make sure both particles are in their proper time frames
        double x1 = sys[l].x + offsetLeft * sys[l].v + .5 * a[l] * offsetLeft * offsetLeft,
                v1 = sys[l].v + a[l] * offsetLeft,
                x2 = sys[r].x + offsetRight * sys[r].v + .5 * a[r] * offsetRight * offsetRight,
                v2 = sys[r].v + a[r] * offsetRight;
        //find the differences
        double dX = x1 - x2,
                dV = v1 - v2,
                dA = a[l] - a[r];

        double ret = 0;

        try {
            ret = (-dV + Math.sqrt(dV * dV - 2 * dA * dX)) / dA;
            if (ret < 0.0) {
                throw new UnexpectedException("Collision Time < 0");
            }
        } catch (UnexpectedException e) {
            System.out.println(e.getMessage());
            System.out.println(sys[l] + "\t" + a[l] + "\t:\t" + sys[r] + "\t" + a[r]);
            System.out.println(offsetLeft + "\t:\t" + offsetRight);
        }


        return ret;
    }
    private double nextTime, tempTime;//next time
    
    /*
     * Finds the next collision time
     */
    private void findNext() {
        nextTime = Double.MAX_VALUE;
        for (int i = 0; i < nextSet.length; i++) {
            tempTime = collisionFrame[i] + nextSet[i];
            if (nextTime > tempTime) {
                nextTime = tempTime;
                nextLoc = i;
            }
        }
    }

    /*
     * updates all particles and collision times
     */
    private void massUpdate() {
        for (int i = 0; i < size - 1; i++) {
            sys[i].update(INTERVAL - timeFrame[i], a[i]);
            nextSet[i] = nextSet[i] + collisionFrame[i] - INTERVAL;
        }
        sys[size - 1].update(INTERVAL - timeFrame[size - 1], a[size - 1]);
        nextTime -= INTERVAL;
    }

    /*
     * Resets the time frames so that each particle and collision is in the same
     * frame. Then fires the event to all listeners
     */
    private void reset() {
        timeFrame = new double[size];
        collisionFrame = new double[size - 1];
        _fireUpdatedEvent();
    }

    /*
     * Swaps the indexes of objects in the array, keeping the array sorted by
     * position. 
     */
    private void swap(int left, int right) {
        //Ensures that the particles pass through each other by swapping
        //positions.  Normally, position differences are on the order of E-15
        //if they need to be swapped.
        if (sys[left].x < sys[right].x) {
            double l = sys[left].x;
            sys[left].x = sys[right].x;
            sys[right].x = l;
        }

        //swap index locations
        Particle temp = sys[left];
        sys[left] = sys[right];
        sys[right] = temp;
    }
}