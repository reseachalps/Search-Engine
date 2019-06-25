## Plugins

Contains extraction modules

### Install python3

Use the given script in any module `tools/install_python3.sh`

### Dependency install

On each module

```sh
source tools/setup_venv.sh
./tools/deps.sh
./tools/install.sh
```

You have to install those modules in this order

* companies-plugin
* entities-extractor
* fastmatch
* cstore_api
* textmining
* webmining

### Module install

Similarly you can install everything using the following script

```sh
source tools/setup_venv.sh
./tools/deps.sh
```

Then you can run each module using `python {plugin}/main.py --conf {config} --proc {proc-count}`

### Config

To make this run you need a rabbitmq and a cassandra.

```json
{
    "rabbit": {
            "hostname": "localhost",
        "port": "5672",
            "username": "guest",
            "password": "guest"
    },
    "crawl_store": {
            "hostnames": ["localhost"],
            "keyspace": "crawl_store"
    }
}
```

The cassandra is initialized by the backend.

#### Entity extraction

Needs api access to fetch the entities
```json
    "api": {
            "username": "ra",
            "password": "xxx",
            "base_url": "http://localhost:7227"
    }
```

#### Crawler

```json
    "crawler": {
        "depth": 4,
            "page_size_max": 1000000,
            "page_max": 300
    }
```

#### Core extractor

```json
    "coreextractor": {"depth": 4, "page_max": 300},
    "social": {
            "twitter": [{
                    "secret": "...",
                        "key": "..."
                }],
            "facebook": [{
                    "app_id": "...",
                    "secret_id": "..."
                }]
        }
```


### Screenshot

You need node+npm to have phantomjs first.
```{node-dir}/bin/npm -g install phantomjs@1.9.17```

Then use the following config

```json
    "screenshot": {
            "phantom": "{node-dir}/lib/node_modules/phantomjs/lib/phantom/bin/phantomjs",
        "width": 1024,
            "height": 600,
        "load_delay": 500,
        "timeout": 20000
    }
```
