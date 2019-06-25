package eu.researchalps.search.model.request;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class DateRangeFilter extends RangeFilter<Date> {
    public DateRangeFilter() {}

    public DateRangeFilter(Date min, Date max, Boolean missing) {
        super(min, max, missing);
    }


    @DateTimeFormat(pattern = "yyyy-MM-dd")
    public Date getMin() {
        return min;
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    public void setMin(Date min) {
        this.min = min;
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    public Date getMax() {
        return max;
    }

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    public void setMax(Date max) {
        this.max = max;
    }
}
