package eu.researchalps.search.model.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MultiValueSearchFilter {

    private Operator op;

    private List<String> values = new ArrayList<>();

    public enum Operator {
        all, any, none, not_all, exists
    }

    public MultiValueSearchFilter() {
    }

    public MultiValueSearchFilter(Operator op) {
        this.op = op;
    }

    public Operator getOp() {
        return op;
    }

    public List<String> getValues() {
        return values;
    }

    public MultiValueSearchFilter setValues(List<String> values) {
        this.values = values;
        return this;
    }

    public MultiValueSearchFilter addValue(String v) {
        if (v != null) {
            this.values.add(v);
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiValueSearchFilter that = (MultiValueSearchFilter) o;
        return op == that.op &&
                Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, values);
    }
}
