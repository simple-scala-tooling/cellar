package cellar.fixture.java;

import java.util.List;

/**
 * A generic Java interface for testing class-file symbol extraction.
 */
public interface CellarJavaInterface<T extends Comparable<T>> {
    T identity(T value);
    List<T> repeat(T value, int times);
    default String describe() {
        return getClass().getSimpleName();
    }
}
