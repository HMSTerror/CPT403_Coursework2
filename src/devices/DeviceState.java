package devices;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple, extensible container for device state values.
 * Keys are strings like "power", "brightness", "temperature", "locked".
 */
public class DeviceState implements Cloneable {
    private final Map<String, Object> values = new HashMap<>();

    public DeviceState() {}

    public DeviceState(Map<String, Object> initial) {
        if (initial != null) {
            values.putAll(initial);
        }
    }

    public Object get(String key) {
        return values.get(key);
    }

    public void set(String key, Object value) {
        values.put(key, value);
    }

    public Map<String, Object> asMap() {
        return Collections.unmodifiableMap(values);
    }

    @Override
    public DeviceState clone() {
        return new DeviceState(new HashMap<>(this.values));
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
