CREATE TABLE IF NOT EXISTS crawl_info (
    website_id text,
    page_count int,
    start_date timestamp,
    end_date timestamp,
    status text,
    main_lang text,
    histogram blob,
    PRIMARY KEY(website_id)
);