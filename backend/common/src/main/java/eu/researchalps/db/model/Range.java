package eu.researchalps.db.model;

/**
 * Abstract class to store ranges
 *
 * @param <T>
 */
public abstract class Range<T> {
    protected T min;
    protected T max;

    public Range(T min, T max) {
        this.min = min;
        this.max = max;
    }

    public T getMin() {
        return min;
    }

    public T getMax() {
        return max;
    }
}
