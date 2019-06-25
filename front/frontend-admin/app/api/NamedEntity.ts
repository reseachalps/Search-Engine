/// <reference path="api.d.ts" />

namespace api {
    'use strict';

    export interface NamedEntity {

        "id"?: string;

        "labels"?: Array<Label>;

        "type"?: NamedEntity.TypeEnum;
    }

    export namespace NamedEntity {

        export enum TypeEnum { 
            PROJECT = <any> 'PROJECT',
            PUBLICATION = <any> 'PUBLICATION'
        }
    }
}
