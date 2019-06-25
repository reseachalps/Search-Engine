/// <reference path="api.d.ts" />

namespace api {
    'use strict';

    export interface ErrorMessage {

        "id"?: string;

        "message"?: string;

        "priority"?: number;

        "queue"?: string;

        "recoverable"?: boolean;

        "replyTo"?: string;

        "stackTrace"?: string;

        "timestamp"?: Date;
    }

}
