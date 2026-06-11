import stage.model.Group;
import stage.LoadGroups;
import shared.model.MatchProbabilityTable;
import shared.ProbabilityLoader;
import stage.GroupOutcomeCalculator;
import stage.GroupPlacementCalculator;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Group> groups = LoadGroups.createGroups();
        // LoadGroups.printGroups(groups);

        MatchProbabilityTable probabilities_group = ProbabilityLoader.load('g');
        // probabilities_group.printAll();

        for (Group group : groups) {
            GroupOutcomeCalculator.calculate(group, probabilities_group);
            // GroupOutcomeCalculator.printGroupOutcomes(group);
            GroupPlacementCalculator.calculate(group);
            GroupPlacementCalculator.printPlacementProbabilities(group);
        }

        // MatchProbabilityTable probabilities_round = ProbabilityLoader.load('r');
        // probabilities_group.printAll();
    }
}
