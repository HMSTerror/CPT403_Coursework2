package users;

/**
 * All permissions used in the system.
 */
public enum Permission {
    CONTROL_ALL_DEVICES,
    CONTROL_LIGHTS,
    CONTROL_THERMOSTATS,
    CONTROL_LOCKS,
    CONTROL_OWN_DEVICES, // reserved for future expansion
    EDIT_GROUPS,
    EDIT_SCENES,
    EXECUTE_SCENES,
    VIEW_STATUS,
    REGISTER_DEVICE,
    DEREGISTER_DEVICE
}