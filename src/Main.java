import devices.*;
import exceptions.ExecutionException;
import hub.*;
import users.*;

public class Main {
    public static void main(String[] args) {
        // Create roles and users
        Role adminRole = RoleFactory.createAdminRole();
        Role parentRole = RoleFactory.createParentRole();
        Role childRole = RoleFactory.createChildRole();

        User Thomas = new User("Thomas", adminRole);
        User Zijian = new User("Zijian", parentRole);
        User charlie = new User("charlie", childRole);

        // Create hub
        SmartHomeHub hub = new SmartHomeHub();

        // Register devices (admin)
        hub.registerDevice(Thomas, new SmartLight("LivingRoomLight1"));
        hub.registerDevice(Thomas, new SmartLight("LivingRoomLight2"));
        hub.registerDevice(Thomas, new SmartThermostat("DownstairsThermostat"));
        hub.registerDevice(Thomas, new SmartLock("FrontDoorLock"));

        // Create a scene (admin)
        Scene movieNight = new Scene("Movie Night");
        DeviceState dimLights = new DeviceState();
        dimLights.set("power", true);
        dimLights.set("brightness", 20);

        DeviceState lockDoor = new DeviceState();
        lockDoor.set("locked", true);

        DeviceState setTemp = new DeviceState();
        setTemp.set("targetTemperature", 19.0);

        movieNight.addAction(new SceneAction("LivingRoomLight1", dimLights));
        movieNight.addAction(new SceneAction("LivingRoomLight2", dimLights));
        movieNight.addAction(new SceneAction("FrontDoorLock", lockDoor));
        movieNight.addAction(new SceneAction("DownstairsThermostat", setTemp));

        hub.createScene(Thomas, movieNight);

        // Parent tries to execute scene - allowed
        try {
            System.out.println("Parent executing scene:");
            hub.executeScene(Zijian, "Movie Night");
        } catch (ExecutionException e) {
            System.err.println("Scene failed: " + e.getMessage());
        }

        // Child tries to execute scene - should fail due to permission or device policy
        try {
            System.out.println("\nChild attempting to execute scene:");
            hub.executeScene(charlie, "Movie Night");
        } catch (Exception e) {
            System.err.println("Child failed to execute scene: " + e.getMessage());
        }

        // Check states
        hub.getAllDevices().forEach(d -> System.out.println(d));
    }
}