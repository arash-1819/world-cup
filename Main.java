import shared.ProbabilityLoader;
import shared.model.MatchProbabilityTable;
import stage.LoadGroups;
import stage.model.Group;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("\n--------------------------------");
        System.out.println("Groups:");
        List<Group> groups = LoadGroups.createGroups();
        LoadGroups.printGroups(groups);

        System.out.println("\n--------------------------------");
        System.out.println("Match Probabilities:");
        MatchProbabilityTable probabilities = ProbabilityLoader.load();
        probabilities.printAll();
    }
}
