/// <reference path="api.d.ts" />

namespace api {
    'use strict';

    export interface PageOfErrorMessage {

        "content"?: Array<ErrorMessage>;

        "first"?: boolean;

        "last"?: boolean;

        "number"?: number;

        "numberOfElements"?: number;

        "size"?: number;

        "sort"?: any;

        "totalElements"?: number;

        "totalPages"?: number;
    }

}
