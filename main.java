import stage.CreateGroups;
import stage.model.Group;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Group> groups = CreateGroups.createGroups();

        CreateGroups.printGroups(groups);
    }
}
