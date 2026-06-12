import shared.model.MatchProbabilityTable;
import shared.ProbabilityLoader;
import stage.model.Group;
import stage.LoadGroups;
import stage.GroupOutcomeCalculator;
import stage.GroupPlacementCalculator;
import round32.model.*;
import round32.Round32Calculator;
import round16.Round16Calculator;
import round16.model.Round16MatchResult;
import round8.Round8Calculator;
import round8.model.Round8MatchResult;
import round4.Round4Calculator;
import round4.model.Round4MatchResult;
import round2.Round2Calculator;
import round2.model.Round2MatchResult;

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
            // GroupPlacementCalculator.printPlacementProbabilities(group);
        }

        MatchProbabilityTable probabilities_round = ProbabilityLoader.load('t');
        // probabilities_round.printAll();

        System.out.println("________________________________________________________________");
        List<Round32MatchResult> round32Results = Round32Calculator.calculate(groups, probabilities_round);
        Round32Calculator.printRound32Results(round32Results);

        System.out.println("________________________________________________________________");
        List<Round16MatchResult> round16Results = Round16Calculator.calculate(round32Results, probabilities_round);
        Round16Calculator.printRound16Results(round16Results);

        System.out.println("________________________________________________________________");
        List<Round8MatchResult> round8Results = Round8Calculator.calculate(round16Results, probabilities_round);
        Round8Calculator.printRound8Results(round8Results);
        // System.out.println("----------------------------------------------------------------");
        // Round8Calculator.printTop4Probabilities(round8Results);

        System.out.println("________________________________________________________________");
        List<Round4MatchResult> round4Results = Round4Calculator.calculate(round8Results, probabilities_round);
        Round4Calculator.printRound4Results(round4Results);
        // System.out.println("----------------------------------------------------------------");
        // Round4Calculator.printTop2Probabilities(round4Results);

        System.out.println("________________________________________________________________");
        List<Round2MatchResult> round2Results = Round2Calculator.calculate(round4Results, probabilities_round);
        Round2Calculator.printRound2Results(round2Results);

        System.out.println();
    }
}
