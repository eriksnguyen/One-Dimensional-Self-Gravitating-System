package simulation;

import java.util.EventObject;

public class UpdatedEvent extends EventObject {

    protected final Particle[] info;
    protected final double time;

    public UpdatedEvent(Object source, Particle[] data, double t) {
        super(source);
        info = data;
        time = t;
    }
}
