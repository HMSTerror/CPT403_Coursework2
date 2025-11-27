package devices;

import exceptions.ValidationException;

/**
 * SmartLock supports:
 *  - locked: Boolean
 */
public class SmartLock extends Device {

    public SmartLock(String name) {
        super(name, "LOCK");
        state.set("locked", true);
    }

    @Override
    public synchronized void applyState(DeviceState targetState) throws ValidationException {
        if (targetState == null) return;

        Object l = targetState.get("locked");
        if (l != null) {
            if (!(l instanceof Boolean)) throw new ValidationException("locked must be boolean");
            state.set("locked", l);
        }
    }
}
