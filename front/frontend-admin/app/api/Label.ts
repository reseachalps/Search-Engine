/// <reference path="api.d.ts" />

namespace api {
    'use strict';

    export interface Label {

        "label"?: string;

        "method"?: Label.MethodEnum;
    }

    export namespace Label {

        export enum MethodEnum { 
            EXACT = <any> 'EXACT',
            SHORT_LABEL = <any> 'SHORT_LABEL',
            LONG_LABEL = <any> 'LONG_LABEL'
        }
    }
}
