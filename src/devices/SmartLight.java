package devices;

import exceptions.ValidationException;

/**
 * SmartLight supports:
 *  - power: Boolean (true=ON, false=OFF)
 *  - brightness: Integer (0-100)
 */
public class SmartLight extends Device {

    public SmartLight(String name) {
        super(name, "LIGHT");
        // default off
        state.set("power", false);
        state.set("brightness", 100);
    }

    @Override
    public synchronized void applyState(DeviceState targetState) throws ValidationException {
        if (targetState == null) return;

        Object p = targetState.get("power");
        if (p != null) {
            if (!(p instanceof Boolean)) throw new ValidationException("power must be boolean");
            state.set("power", p);
        }

        Object b = targetState.get("brightness");
        if (b != null) {
            if (!(b instanceof Number)) throw new ValidationException("brightness must be number");
            int val = ((Number) b).intValue();
            if (val < 0 || val > 100) throw new ValidationException("brightness out of range 0-100");
            state.set("brightness", val);
            // if brightness > 0, ensure power true
            if (val > 0) state.set("power", true);
        }
    }
}