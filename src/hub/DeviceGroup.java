package hub;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple device group which stores device names.
 */
public class DeviceGroup {
    private final String name;
    private final Set<String> deviceNames = new HashSet<>();

    public DeviceGroup(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void addDevice(String deviceName) { deviceNames.add(deviceName); }
    public void removeDevice(String deviceName) { deviceNames.remove(deviceName); }
    public Set<String> getDeviceNames() { return Collections.unmodifiableSet(deviceNames); }

    @Override
    public String toString() {
        return "DeviceGroup{" + name + ", size=" + deviceNames.size() + "}";
    }
}
