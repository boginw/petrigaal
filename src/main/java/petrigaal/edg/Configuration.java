package petrigaal.edg;

import java.util.Set;

public interface Configuration<
        C extends Configuration<C, E, T>,
        E extends Edge<C, E, T>,
        T extends Target<C, E, T>> {
    Set<E> getSuccessors();
}
