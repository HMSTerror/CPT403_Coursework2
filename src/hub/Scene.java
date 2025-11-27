package hub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Scene contains a list of SceneAction. It's essentially a named macro.
 */
public class Scene {
    private final String name;
    private final List<SceneAction> actions = new ArrayList<>();

    public Scene(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void addAction(SceneAction action) {
        actions.add(action);
    }

    public List<SceneAction> getActions() {
        return Collections.unmodifiableList(actions);
    }

    @Override
    public String toString() {
        return "Scene{" + name + ", actions=" + actions.size() + "}";
    }
}
