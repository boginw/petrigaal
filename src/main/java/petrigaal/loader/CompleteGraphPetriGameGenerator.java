package petrigaal.loader;

import petrigaal.petri.PetriGame;
import petrigaal.petri.Player;
import petrigaal.petri.Transition;

public class CompleteGraphPetriGameGenerator {
    public PetriGame generate(int nodes) {
        PetriGame pg = new PetriGame();

        pg.addMarkings("P0", 1);

        for (int i = 1; i < nodes; i++) {
            pg.addMarkings("P" + i, 0);
        }

        for (int i = 0; i < nodes; i++) {
            for (int j = 0; j < nodes; j++) {
                Transition t = new Transition("T" + i + "-" + j)
                        .addInput("P" + i)
                        .addOutput("P" + j);
                pg.addTransition(Player.Controller, t);
            }
        }

        System.out.println("Generated PG");
        return pg;
    }
}
