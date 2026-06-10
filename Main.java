import shared.ProbabilityLoader;
import shared.model.MatchProbabilityTable;
import stage.LoadGroups;
import stage.model.Group;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Group> groups = LoadGroups.createGroups();
        LoadGroups.printGroups(groups);

        MatchProbabilityTable probabilities = ProbabilityLoader.load();
        probabilities.printAll();
    }
}
