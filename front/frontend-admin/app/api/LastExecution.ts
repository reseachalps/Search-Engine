/// <reference path="api.d.ts" />

namespace api {
    'use strict';

    export interface LastExecution {

        "lastActualExecutionTime"?: Date;

        "lastCompletionTime"?: Date;

        "lastScheduledExecutionTime"?: Date;

        "status"?: string;
    }

}
