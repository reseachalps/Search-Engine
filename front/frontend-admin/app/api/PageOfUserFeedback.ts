/// <reference path="api.d.ts" />

namespace api {
    'use strict';

    export interface PageOfUserFeedback {

        "content"?: Array<UserFeedback>;

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
