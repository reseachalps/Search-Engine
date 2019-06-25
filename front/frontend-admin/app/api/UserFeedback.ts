/// <reference path="api.d.ts" />

namespace api {
    'use strict';

    export interface UserFeedback {

        "action"?: UserFeedback.ActionEnum;

        "comment"?: string;

        "createdDate"?: Date;

        "email"?: string;

        "field"?: string;

        "id"?: string;

        "lastUpdated"?: Date;

        "status"?: UserFeedback.StatusEnum;

        "structure"?: string;

        "userName"?: string;

        "value"?: string;
    }

    export namespace UserFeedback {

        export enum ActionEnum { 
            ADD = <any> 'ADD',
            DELETE = <any> 'DELETE',
            MODIFY = <any> 'MODIFY'
        }

        export enum StatusEnum { 
            SUBMITTED = <any> 'SUBMITTED',
            PROCESSED = <any> 'PROCESSED'
        }
    }
}
