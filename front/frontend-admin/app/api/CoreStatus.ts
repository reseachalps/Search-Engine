/// <reference path="api.d.ts" />

namespace api {
    'use strict';

    export interface CoreStatus {

        "errors"?: { [key: string]: number; };

        "exchanges"?: { [key: string]: number; };

        "queues"?: { [key: string]: QueueStats; };

        "scheduledMessages"?: { [key: string]: ScheduledMessageDTO; };

        "totalErrors"?: number;
    }

}
