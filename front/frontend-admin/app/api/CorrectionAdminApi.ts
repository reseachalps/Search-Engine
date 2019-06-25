/// <reference path="api.d.ts" />

/* tslint:disable:no-unused-variable member-ordering */

namespace api {
    'use strict';

    export class CorrectionAdminApi {
        protected basePath = '.';
        public defaultHeaders : any = {};

        static $inject: string[] = ['$http', '$httpParamSerializer'];

        constructor(protected $http: ng.IHttpService, protected $httpParamSerializer?: (d: any) => any, basePath?: string) {
            if (basePath) {
                this.basePath = basePath;
            }
        }

        private extendObj<T1,T2>(objA: T1, objB: T2) {
            for(let key in objB){
                if(objB.hasOwnProperty(key)){
                    objA[key] = objB[key];
                }
            }
            return <T1&T2>objA;
        }

        /**
         * Mark a userFeedback as§ processed
         * 
         * @param correction correction
         */
        public saveCorrection (correction: FullStructureCorrection, extraHttpRequestParams?: any ) : ng.IHttpPromise<OK> {
            const localVarPath = this.basePath + '/api/admin/corrections';

            let queryParameters: any = {};
            let headerParams: any = this.extendObj({}, this.defaultHeaders);
            // verify required parameter 'correction' is set
            if (!correction) {
                throw new Error('Missing required parameter correction when calling saveCorrection');
            }
            let httpRequestParams: any = {
                method: 'PUT',
                url: localVarPath,
                json: true,
                data: correction,
                
                
                params: queryParameters,
                headers: headerParams
            };

            if (extraHttpRequestParams) {
                httpRequestParams = this.extendObj(httpRequestParams, extraHttpRequestParams);
            }

            return this.$http(httpRequestParams);
        }
        /**
         * getCorrection
         * 
         * @param id id
         */
        public getCorrection (id: string, extraHttpRequestParams?: any ) : ng.IHttpPromise<FullStructureCorrection> {
            const localVarPath = this.basePath + '/api/admin/corrections/{id}'
                .replace('{' + 'id' + '}', String(id));

            let queryParameters: any = {};
            let headerParams: any = this.extendObj({}, this.defaultHeaders);
            // verify required parameter 'id' is set
            if (!id) {
                throw new Error('Missing required parameter id when calling getCorrection');
            }
            let httpRequestParams: any = {
                method: 'GET',
                url: localVarPath,
                json: true,
                
                
                params: queryParameters,
                headers: headerParams
            };

            if (extraHttpRequestParams) {
                httpRequestParams = this.extendObj(httpRequestParams, extraHttpRequestParams);
            }

            return this.$http(httpRequestParams);
        }
        /**
         * getStructure
         * 
         * @param id id
         */
        public getStructure (id: string, extraHttpRequestParams?: any ) : ng.IHttpPromise<any> {
            const localVarPath = this.basePath + '/api/admin/corrections/{id}/structure'
                .replace('{' + 'id' + '}', String(id));

            let queryParameters: any = {};
            let headerParams: any = this.extendObj({}, this.defaultHeaders);
            // verify required parameter 'id' is set
            if (!id) {
                throw new Error('Missing required parameter id when calling getStructure');
            }
            let httpRequestParams: any = {
                method: 'GET',
                url: localVarPath,
                json: true,
                
                
                params: queryParameters,
                headers: headerParams
            };

            if (extraHttpRequestParams) {
                httpRequestParams = this.extendObj(httpRequestParams, extraHttpRequestParams);
            }

            return this.$http(httpRequestParams);
        }
    }
}
