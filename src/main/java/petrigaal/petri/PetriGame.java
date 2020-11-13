package petrigaal.petri;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PetriGame {
    private Map<String, Integer> markings;
    private HashSet<Transition> controllerTransitions;
    private HashSet<Transition> environmentTransitions;

    public PetriGame() {
        markings = new HashMap<>();
        controllerTransitions = new HashSet<>();
        environmentTransitions = new HashSet<>();
    }

    public PetriGame addTransition(Path path, Transition t) {
        getSetForPlayer(path).add(t);
        return this;
    }

    public List<Transition> getTransitions(Path path) {
        return new ArrayList<>(getSetForPlayer(path));
    }

    public List<Transition> getEnabledTransitions(Path path) {
        return Stream.concat(controllerTransitions.stream(), environmentTransitions.stream())
                .filter(this::isEnabled)
                .collect(Collectors.toList());
    }

    public void setMarking(String place, int marking) {
        if (marking == 0) {
            markings.remove(place);
        } else if (marking < 0) {
            throw new IllegalArgumentException("Negative marking is not allowed");
        } else {
            markings.put(place, marking);
        }
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

    private HashSet<Transition> getSetForPlayer(Path path) {
        if (path.equals(Path.E)) {
            return controllerTransitions;
        } else if (path.equals(Path.A)) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PetriGame petriGame = (PetriGame) o;
        return Objects.equals(markings, petriGame.markings) &&
                Objects.equals(controllerTransitions, petriGame.controllerTransitions) &&
                Objects.equals(environmentTransitions, petriGame.environmentTransitions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(markings, controllerTransitions, environmentTransitions);
    }

    @Override
    public String toString() {
        return markings.toString();
    }
}
