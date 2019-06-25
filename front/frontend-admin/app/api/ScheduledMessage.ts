/// <reference path="api.d.ts" />

namespace api {
    'use strict';

    export interface ScheduledMessage {

        "id"?: string;

        "lastExecution"?: LastExecution;

        "message"?: string;

        "nextExecution"?: Date;

        "priority"?: number;

        "provider"?: string;

        "queue"?: string;

        "replyTo"?: string;

        "status"?: ScheduledMessage.StatusEnum;

        "triggerInfo"?: TriggerInfo;
    }

    export namespace ScheduledMessage {

        export enum StatusEnum { 
            PLANNED = <any> 'PLANNED',
            CANCELLED = <any> 'CANCELLED',
            ERROR = <any> 'ERROR',
            SUBMITTED = <any> 'SUBMITTED'
        }
    }
}
