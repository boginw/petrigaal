package petrigaal.petri;

import java.util.*;
import java.util.stream.Collectors;

public class PetriGame {
    private Map<String, Integer> markings;
    private HashSet<String> places;
    private HashSet<Transition> controllerTransitions;
    private HashSet<Transition> environmentTransitions;

    public PetriGame() {
        markings = new HashMap<>();
        places = new HashSet<>();
        controllerTransitions = new HashSet<>();
        environmentTransitions = new HashSet<>();
    }

    public PetriGame addTransition(Player player, Transition t) {
        places.addAll(placesFromArcs(t.getInputsArcs()));
        places.addAll(placesFromArcs(t.getOutputArcs()));

        getSetForPlayer(player).add(t);
        return this;
    }

    public List<Transition> getTransitions(Player player) {
        return new ArrayList<>(getSetForPlayer(player));
    }

    public Set<String> getPlaces() {
        return places;
    }

    public void setMarking(String place, int marking) {
        if (marking < 0) {
            throw new IllegalArgumentException("Negative marking is not allowed");
        }
        markings.put(place, marking);
    }

    public void addMarkings(String place, int markingsToAdd) {
        setMarking(place, getMarking(place) + markingsToAdd);
    }

    public void subtractMarkings(String place, int markingsToSubtract) {
        addMarkings(place, -markingsToSubtract);
    }


    public int getMarking(String place) {
        return markings.getOrDefault(place, 0);
    }

    private Set<String> placesFromArcs(Set<Transition.Arc> t) {
        return t.stream().map(Transition.Arc::getPlace).collect(Collectors.toSet());
    }

    private HashSet<Transition> getSetForPlayer(Player player) {
        if (player.equals(Player.Controller)) {
            return controllerTransitions;
        } else if (player.equals(Player.Environment)) {
            return environmentTransitions;
        } else {
            throw new RuntimeException("No such player");
        }
    }

    public PetriGame fire(Transition t) {
        if (!isEnabled(t)) {
            return this;
        }

        PetriGame newPG = cloneGame();
        newPG.performTransition(t);

        return newPG;
    }

    public boolean isEnabled(Transition t) {
        return t.getInputsArcs().stream().allMatch(
                a -> getMarking(a.getPlace()) >= a.getWeight()
        );
    }

    private PetriGame cloneGame() {
        PetriGame newPG = new PetriGame();
        newPG.controllerTransitions = this.controllerTransitions;
        newPG.environmentTransitions = this.environmentTransitions;
        newPG.markings = new HashMap<>(this.markings);
        return newPG;
    }

    private void performTransition(Transition t) {
        t.getInputsArcs().forEach(a -> subtractMarkings(a.getPlace(), a.getWeight()));
        t.getOutputArcs().forEach(a -> addMarkings(a.getPlace(), a.getWeight()));
    }
}
