package eu.researchalps.workflow.website.extractor.dto;

import java.util.List;

/**
 * Created by glebourg on 25/11/14.
 */
public class ECommerceMeta {
    public Integer pages_with_prices;
    public Integer pages_with_basket;
    public float perc_pages_with_prices;
    public float avg_prices_per_page;
    public float perc_pages_with_basket;
    public float avg_price;
    public float variance;
    public float median_price;
    public float first_quart_price;
    public float third_quart_price;
    public List<String> delivery_options;
    public List<String> payment_options;
}
