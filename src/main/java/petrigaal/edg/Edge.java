package petrigaal.edg;

import java.util.Set;

public interface Edge<
        C extends Configuration<C, E, T>,
        E extends Edge<C, E, T>,
        T extends Target<C, E, T>> extends Set<T> {
    C getSource();
}
