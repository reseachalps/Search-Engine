package eu.researchalps.search.model.request;

import java.util.Objects;

/**
 * @author Jacques Belissent
 */
public class RangeFilter<T extends Comparable<T>> {

    public T min;
    public T max;
    public Boolean missing = Boolean.FALSE;

    public RangeFilter() {}

    public RangeFilter(T min, T max, Boolean missing) {
        this.min = min;
        this.max = max;
        this.missing = missing;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RangeFilter<?> that = (RangeFilter<?>) o;
        return Objects.equals(min, that.min) &&
                Objects.equals(max, that.max) &&
                Objects.equals(missing, that.missing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, missing);
    }
}
