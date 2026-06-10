package stage;

import shared.model.Team;
import stage.model.Group;
import stage.model.GroupName;

import java.util.List;

public final class StageGroups {
    public static List<Group> createGroups() {
        return List.of(
                new Group(
                        GroupName.A,
                        List.of(
                                team("Mexico"),
                                team("South Africa"),
                                team("South Korea"),
                                team("Czech Republic")
                        )
                ),
                new Group(
                        GroupName.B,
                        List.of(
                                team("Canada"),
                                team("Bosnia and Herzegovina"),
                                team("Qatar"),
                                team("Switzerland")
                        )
                ),
                new Group(
                        GroupName.C,
                        List.of(
                                team("Brazil"),
                                team("Morocco"),
                                team("Haiti"),
                                team("Scotland")
                        )
                ),
                new Group(
                        GroupName.D,
                        List.of(
                                team("United States"),
                                team("Paraguay"),
                                team("Australia"),
                                team("Turkey")
                        )
                ),
                new Group(
                        GroupName.E,
                        List.of(
                                team("Germany"),
                                team("Curacao"),
                                team("Ivory Coast"),
                                team("Ecuador")
                        )
                ),
                new Group(
                        GroupName.F,
                        List.of(
                                team("Netherlands"),
                                team("Japan"),
                                team("Sweden"),
                                team("Tunisia")
                        )
                ),
                new Group(
                        GroupName.G,
                        List.of(
                                team("Belgium"),
                                team("Egypt"),
                                team("Iran"),
                                team("New Zealand")
                        )
                ),
                new Group(
                        GroupName.H,
                        List.of(
                                team("Spain"),
                                team("Cape Verde"),
                                team("Saudi Arabia"),
                                team("Uruguay")
                        )
                ),
                new Group(
                        GroupName.I,
                        List.of(
                                team("France"),
                                team("Senegal"),
                                team("Iraq"),
                                team("Norway")
                        )
                ),
                new Group(
                        GroupName.J,
                        List.of(
                                team("Argentina"),
                                team("Algeria"),
                                team("Austria"),
                                team("Jordan")
                        )
                ),
                new Group(
                        GroupName.K,
                        List.of(
                                team("Portugal"),
                                team("DR Congo"),
                                team("Uzbekistan"),
                                team("Colombia")
                        )
                ),
                new Group(
                        GroupName.L,
                        List.of(
                                team("England"),
                                team("Croatia"),
                                team("Ghana"),
                                team("Panama")
                        )
                )
        );
    }

    public static void printGroups(List<Group> groups) {
        for (Group group : groups) {
            System.out.println("Group " + group.getName());

            for (Team team : group.getTeams()) {
                System.out.println("- " + team.getName());
            }

            System.out.println();
        }
    }

    private static Team team(String name) {
        return new Team(name);
    }
}
