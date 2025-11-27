package hub;

import devices.*;
import exceptions.ExecutionException;
import exceptions.ValidationException;
import users.RoleFactory;
import users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 智能家居集线器（SmartHomeHub）的 JUnit 测试套件。
 * 覆盖了核心功能、权限控制、输入验证和场景执行回滚等边界情况。
 */
public class SmartHomeHubTest {
    private SmartHomeHub hub;
    private User admin;
    private User parent;
    private User child;

    private SmartLight light1;
    private SmartThermostat thermo1;
    private SmartLock lock1;

    /**
     * 在每个测试方法执行前初始化集线器、用户和设备。
     */
    @BeforeEach
    void setUp() {
        hub = new SmartHomeHub();

        // 初始化用户角色
        admin = new User("AdminUser", RoleFactory.createAdminRole());
        parent = new User("ParentUser", RoleFactory.createParentRole());
        child = new User("ChildUser", RoleFactory.createChildRole());

        // 初始化并注册设备
        light1 = new SmartLight("Light1");
        thermo1 = new SmartThermostat("Thermo1");
        lock1 = new SmartLock("Lock1");

        hub.registerDevice(admin, light1);
        hub.registerDevice(admin, thermo1);
        hub.registerDevice(admin, lock1);
    }

    // =========================================================================
    // 1. 输入验证和边界情况测试 (覆盖 ValidationException)
    // =========================================================================

    @Test
    void smartLight_ApplyInvalidState_ThrowsValidationException() {
        // 边界情况 1：亮度超出范围（太高）
        DeviceState invalidState1 = new DeviceState(Map.of("brightness", 150));
        assertThrows(ValidationException.class, () -> {
            light1.applyState(invalidState1);
        }, "亮度值 (150) 必须超出范围 (0-100) 并抛出异常。");

        // 边界情况 2：亮度超出范围（负数）
        DeviceState invalidState2 = new DeviceState(Map.of("brightness", -10));
        assertThrows(ValidationException.class, () -> {
            light1.applyState(invalidState2);
        }, "亮度值 (-10) 必须超出范围并抛出异常。");
    }

    @Test
    void smartLight_ApplyValidState_SucceedsAndImplicitlyTurnsOn() throws ValidationException {
        light1.applyState(new DeviceState(Map.of("power", false))); // 先确保关机

        DeviceState validState = new DeviceState(Map.of("brightness", 50));
        light1.applyState(validState);

        // 验证：亮度更新成功
        assertEquals(50, light1.getState().get("brightness"));
        // 验证：设置亮度时，设备必须自动开启（隐式行为）
        assertTrue((Boolean) light1.getState().get("power"), "设置亮度时必须自动开机。");
    }

    @Test
    void smartThermostat_ApplyInvalidState_ThrowsValidationException() {
        // 边界情况 1：温度超出范围（太高）
        DeviceState invalidState1 = new DeviceState(Map.of("targetTemperature", 31.0));
        assertThrows(ValidationException.class, () -> {
            thermo1.applyState(invalidState1);
        }, "温度值 (31.0) 必须超出范围并抛出异常。");

        // 边界情况 2：温度超出范围（太低）
        DeviceState invalidState2 = new DeviceState(Map.of("targetTemperature", 4.0));
        assertThrows(ValidationException.class, () -> {
            thermo1.applyState(invalidState2);
        }, "温度值 (4.0) 必须超出范围并抛出异常。");
    }

    // =========================================================================
    // 2. 场景执行回滚测试 (覆盖 ExecutionException 和原子性)
    // =========================================================================

    @Test
    void executeScene_RollbackOnValidationFailure_RestoresPreviousState() {
        // 1. 记录初始状态
        DeviceState originalLightState = light1.getState(); // power=false, brightness=100
        DeviceState originalThermoState = thermo1.getState(); // power=false, targetTemperature=20.0

        // 2. 创建场景：第一个动作成功，第二个动作失败
        Scene rollbackScene = new Scene("RollbackTest");

        // 动作 1 (成功): Light1 开启并设置亮度为 50
        DeviceState state1 = new DeviceState(Map.of("power", true, "brightness", 50));
        rollbackScene.addAction(new SceneAction("Light1", state1));

        // 动作 2 (失败): Thermo1 温度超出上限 (35.0 > 30.0)
        DeviceState state2 = new DeviceState(Map.of("targetTemperature", 35.0));
        rollbackScene.addAction(new SceneAction("Thermo1", state2));

        hub.createScene(admin, rollbackScene);

        // 3. 执行并预期抛出 ExecutionException (包含 ValidationException)
        assertThrows(ExecutionException.class, () -> {
            hub.executeScene(admin, "RollbackTest");
        }, "场景执行必须因验证失败而中断。");

        // 4. 验证回滚：Light1 的状态必须恢复到场景执行前的状态
        DeviceState finalLightState = light1.getState();
        assertEquals(originalLightState.get("power"), finalLightState.get("power"), "Light1's power 必须回滚。");
        assertEquals(originalLightState.get("brightness"), finalLightState.get("brightness"), "Light1's brightness 必须回滚。");

        // 5. 验证：Thermo1 的状态也应该保持不变
        DeviceState finalThermoState = thermo1.getState();
        assertEquals(originalThermoState.get("targetTemperature"), finalThermoState.get("targetTemperature"), "Thermo1 的状态不应被改变。");
    }


    // =========================================================================
    // 3. 设备组操作测试
    // =========================================================================

    @Test
    void groupOperations_SuccessAndRollback_Test() throws ExecutionException {
        // 1. 创建群组
        hub.createGroup(admin, "AllDevices", List.of("Light1", "Thermo1", "Lock1"));

        // 2. 成功的群组操作
        DeviceState successState = new DeviceState(Map.of("power", true, "locked", false));
        hub.applyToGroup(admin, "AllDevices", successState);

        // 验证状态更新
        assertTrue((Boolean) light1.getState().get("power"));
        assertTrue((Boolean) thermo1.getState().get("power"));
        assertFalse((Boolean) lock1.getState().get("locked"));

        // 3. 记录群组操作前的状态
        DeviceState preRollbackLightState = light1.getState();
        DeviceState preRollbackLockState = lock1.getState();

        // 4. 失败的群组操作（设置 Light1 的亮度超出范围）
        DeviceState failingState = new DeviceState(Map.of("brightness", 101));

        assertThrows(ExecutionException.class, () -> {
            hub.applyToGroup(admin, "AllDevices", failingState);
        }, "群组操作必须因验证错误而回滚。");

        // 5. 验证回滚：Light1 和 Lock1 的状态必须恢复
        assertEquals(preRollbackLightState.get("power"), light1.getState().get("power"), "回滚后 Light1 的 Power 必须恢复。");
        assertEquals(preRollbackLockState.get("locked"), lock1.getState().get("locked"), "回滚后 Lock1 的状态必须恢复。");
    }

    // =========================================================================
    // 4. 权限检查测试 (覆盖 SecurityException)
    // =========================================================================

    @Test
    void hubManagement_UnauthorizedActions_ThrowSecurityException() {
        // 场景 1：非 Admin 尝试注册新设备 (需要 REGISTER_DEVICE)
        SmartLight newLight = new SmartLight("NewLight");
        assertThrows(SecurityException.class, () -> {
            hub.registerDevice(parent, newLight);
        }, "Parent 缺乏 REGISTER_DEVICE 权限。");

        // 场景 2：Child 尝试创建场景 (需要 EDIT_SCENES)
        Scene newScene = new Scene("ChildScene");
        assertThrows(SecurityException.class, () -> {
            hub.createScene(child, newScene);
        }, "Child 缺乏 EDIT_SCENES 权限。");

        // 场景 3：Parent 尝试删除群组 (需要 EDIT_GROUPS)
        hub.createGroup(admin, "TestGroup", List.of());
        assertDoesNotThrow(() -> hub.getGroup("TestGroup"), "确保群组已创建。");

        assertThrows(SecurityException.class, () -> {
            hub.deleteGroup(child, "TestGroup");
        }, "Child 缺乏 EDIT_GROUPS 权限。");
    }

    @Test
    void executeScene_UnauthorizedUser_ThrowsSecurityException() {
        // 创建一个场景
        Scene testScene = new Scene("AuthTestScene");
        testScene.addAction(new SceneAction("Light1", new DeviceState(Map.of("power", true))));
        hub.createScene(admin, testScene);

        // Child 缺乏 EXECUTE_SCENES 权限 (参见 RoleFactory)
        assertThrows(SecurityException.class, () -> {
            hub.executeScene(child, "AuthTestScene");
        }, "Child 缺乏 EXECUTE_SCENES 权限。");
    }
}