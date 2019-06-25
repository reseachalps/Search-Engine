/// <reference path="api.d.ts" />

namespace api {
    'use strict';

    export interface QueueStats {

        "consumerCount"?: number;

        "count"?: number;

        "throughputIn"?: number;

        "throughputOut"?: number;
    }

}
