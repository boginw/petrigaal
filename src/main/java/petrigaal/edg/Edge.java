package petrigaal.edg;

import java.util.List;

public interface Edge<
        C extends Configuration<C, E, T>,
        E extends Edge<C, E, T>,
        T extends Target<C, E, T>> extends List<T> {
    C getSource();
}
