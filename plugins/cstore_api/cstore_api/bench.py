import crawl_store_api
from crawl_store import CrawlPage
from multiprocessing import (
    Process,
    Queue
)
import random
import datetime
import os

conf = {
    "hostnames": ["127.0.0.1", "127.0.0.2", "127.0.0.3"],
    "keyspace": "crawl_store",
    "hostname": "localhost",
    "port": "9200",
    "index": "crawl-store",
}


def rand_site():
    return ''.join(random.sample("abcdefghijklmnopqrstuvwxyz-", 20)) + ".fr"


def rand_text(size):
    return ' '.join([''.join(random.sample("abcdefghijklmnopqrstuvwxyz-", 6))
                     for _ in range(size // 6)])


class Event:
    STOP = 0
    READ = 1
    WRITE = 2


def process(q, res):
    crawl_store = crawl_store_api.CrawlStoreAPI(conf)

    while True:
        (event, args) = q.get()
        if event == Event.STOP:
            return
        if event == Event.WRITE:
            url, nb_pages = args
            crawl_id = crawl_store.begin_crawl(url, 2, 80)
            for i in range(nb_pages):
                crawl_store.push_page(CrawlPage(crawl_id, url=url + "/" + str(i),
                                                http_status=200, depth=1,
                                                crawl_date=datetime.datetime.now(),
                                                headers={"Guillaume": "Lebourgeois"},
                                                charset="thomas-dudouet-8859-15",
                                                title=rand_text(100),
                                                content=rand_text(2000)))
            crawl_store.end_crawl(crawl_id, nb_pages)
            res.put((Event.WRITE, True))
        if event == Event.READ:
            url, nb_pages = args
            crawl_id = crawl_store.get_crawl_by_url(url)
            pages = crawl_store.get_crawl(crawl_id)

            if len(pages) == nb_pages:
                res.put((Event.READ, True))
            else:
                res.put((Event.READ, False))


def bench(N=4):
    q = Queue()
    res = Queue()
    processes = [Process(target=process, args=(q, res))
                 for i in range(N)]

    NB = 300
    sites = [rand_site() for _ in range(300)]

    for i in range(NB):
        q.put((Event.WRITE, (sites[i], 20)))
    for i in range(NB):
        q.put((Event.READ, (sites[i], 20)))
    for i in range(NB):
        q.put((Event.WRITE, (sites[i], 40)))
    for i in range(NB):
        q.put((Event.READ, (sites[i], 40)))

    for p in processes:
        q.put((Event.STOP, ()))

    start = datetime.datetime.now()

    for p in processes:
        p.start()
    for p in processes:
        p.join()

    end = datetime.datetime.now()

    writes = 0
    reads_ok = 0
    reads_ko = 0
    try:
        while True:
            e = res.get(False)
            if e[0] == Event.WRITE:
                writes += 1
            if e[0] == Event.READ:
                if e[1]:
                    reads_ok += 1
                else:
                    reads_ko += 1
    except:
        pass

    print("TIME %s" % (end - start))
    print("WRITE %s" % writes)
    print("READ OK %s" % reads_ok)
    print("READ KO %s" % reads_ko)

bench()
