/// <reference path="api.d.ts" />

namespace api {
    'use strict';

    export interface ErrorSelector {

        "id"?: string;

        "queues"?: Array<string>;

        "recoverable"?: boolean;
    }

}
