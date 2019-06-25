CREATE TABLE IF NOT EXISTS crawl_data (
    website_id text,
    url text,
    content text,
    title text,
    relevant_txt text,
    lang text,
    PRIMARY KEY(website_id, url)
);