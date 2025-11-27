package devices;

import exceptions.ValidationException;

/**
 * Basic device abstraction.
 */
public abstract class Device {
    protected final String name;
    protected final String type;
    protected DeviceState state;

    public Device(String name, String type) {
        this.name = name;
        this.type = type;
        this.state = new DeviceState();
    }

    public String getName() { return name; }
    public String getType() { return type; }

    /**
     * Apply a target state fragment to this device (may validate and transform).
     * Implementations should validate and modify this.state accordingly.
     */
    public abstract void applyState(DeviceState targetState) throws ValidationException;

    /**
     * Restore state during rollback.
     */
    public void restoreState(DeviceState original) {
        this.state = original.clone();
    }

    public DeviceState getState() { return state.clone(); }

    @Override
    public String toString() {
        return String.format("Device{name='%s', type=%s, state=%s}", name, type, state);
    }
}