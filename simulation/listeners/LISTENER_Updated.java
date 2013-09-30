package simulation.listeners;

/**
 * This class defines a listener interface is used to let the data writer know 
 * when to print out more data from the system.
 * 
 * @author Erik Nguyen
 */
public interface LISTENER_Updated {
    public void receiveUpdate(EVENT_Updated evt);
}
