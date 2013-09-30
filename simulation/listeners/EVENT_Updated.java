package simulation.listeners;

import simulation.components.Particle;

/**
 *
 * @author Erik Nguyen
 */
public class EVENT_Updated extends java.util.EventObject {

	protected final Particle[] data;
	protected final double time;

	public EVENT_Updated(Object source, Particle[] system, double time) {
		super(source);
		data = system;
		this.time = time;
	}
}
