/// <reference path="api.d.ts" />

namespace api {
    'use strict';

    export interface TriggerInfo {

        "expression"?: string;

        "initialDelay"?: number;

        "period"?: number;

        "type"?: TriggerInfo.TypeEnum;
    }

    export namespace TriggerInfo {

        export enum TypeEnum { 
            CRON = <any> 'CRON',
            RATE = <any> 'RATE',
            FIXED_RATE = <any> 'FIXED_RATE',
            CUSTOM = <any> 'CUSTOM'
        }
    }
}
