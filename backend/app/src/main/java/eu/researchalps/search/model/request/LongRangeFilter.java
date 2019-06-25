package eu.researchalps.search.model.request;


public class LongRangeFilter extends RangeFilter<Long> {

    public LongRangeFilter() {
    }

    public LongRangeFilter(Long min, Long max, Boolean missing) {
        super(min, max, missing);
    }

}
