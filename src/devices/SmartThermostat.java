package devices;

import exceptions.ValidationException;

/**
 * SmartThermostat supports:
 *  - power: Boolean
 *  - targetTemperature: Double (e.g., degrees Celsius)
 */
public class SmartThermostat extends Device {

    private final double MIN_TEMP = 5.0;
    private final double MAX_TEMP = 30.0;

    public SmartThermostat(String name) {
        super(name, "THERMOSTAT");
        state.set("power", false);
        state.set("targetTemperature", 20.0);
    }

    @Override
    public synchronized void applyState(DeviceState targetState) throws ValidationException {
        if (targetState == null) return;

        Object p = targetState.get("power");
        if (p != null) {
            if (!(p instanceof Boolean)) throw new ValidationException("power must be boolean");
            state.set("power", p);
        }

        Object t = targetState.get("targetTemperature");
        if (t != null) {
            if (!(t instanceof Number)) throw new ValidationException("temperature must be number");
            double val = ((Number) t).doubleValue();
            if (val < MIN_TEMP || val > MAX_TEMP) {
                throw new ValidationException("temperature out of supported range: " + MIN_TEMP + " - " + MAX_TEMP);
            }
            state.set("targetTemperature", val);
            // if setting temperature, also ensure power on
            state.set("power", true);
        }
    }
}
