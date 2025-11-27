package hub;

import devices.DeviceState;

/**
 * A SceneAction ties a device (by name) to a target DeviceState.
 */
public class SceneAction {
    private final String deviceName;
    private final DeviceState targetState;

    public SceneAction(String deviceName, DeviceState targetState) {
        this.deviceName = deviceName;
        this.targetState = targetState;
    }

    public String getDeviceName() { return deviceName; }
    public DeviceState getTargetState() { return targetState; }
}
