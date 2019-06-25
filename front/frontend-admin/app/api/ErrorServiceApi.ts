/// <reference path="api.d.ts" />

/* tslint:disable:no-unused-variable member-ordering */

namespace api {
    'use strict';

    export class ErrorServiceApi {
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
         * Get the list of errors sorted by date desc
         * 
         * @param select select
         * @param page page
         * @param size size
         */
        public getErrors (select: Object, page?: number, size?: number, extraHttpRequestParams?: any ) : ng.IHttpPromise<PageOfErrorMessage> {
            const localVarPath = this.basePath + '/api/services/error';

            let queryParameters: any = {};
            let headerParams: any = this.extendObj({}, this.defaultHeaders);
            // verify required parameter 'select' is set
            if (!select) {
                throw new Error('Missing required parameter select when calling getErrors');
            }
            if (select !== undefined) {
                queryParameters['select'] = select;
            }

            if (page !== undefined) {
                queryParameters['page'] = page;
            }

            if (size !== undefined) {
                queryParameters['size'] = size;
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
         * Ignoring a selection of errors
         * Warning! This could be harmful for your workflow
         * @param select select
         */
        public dismiss (select: ErrorSelector, extraHttpRequestParams?: any ) : ng.IHttpPromise<number> {
            const localVarPath = this.basePath + '/api/services/error/ignore';

            let queryParameters: any = {};
            let headerParams: any = this.extendObj({}, this.defaultHeaders);
            // verify required parameter 'select' is set
            if (!select) {
                throw new Error('Missing required parameter select when calling dismiss');
            }
            let httpRequestParams: any = {
                method: 'POST',
                url: localVarPath,
                json: true,
                data: select,
                
                
                params: queryParameters,
                headers: headerParams
            };

            if (extraHttpRequestParams) {
                httpRequestParams = this.extendObj(httpRequestParams, extraHttpRequestParams);
            }

            return this.$http(httpRequestParams);
        }
        /**
         * Recover a selection of errors
         * 
         * @param select select
         */
        public recover (select: ErrorSelector, extraHttpRequestParams?: any ) : ng.IHttpPromise<number> {
            const localVarPath = this.basePath + '/api/services/error/recover';

            let queryParameters: any = {};
            let headerParams: any = this.extendObj({}, this.defaultHeaders);
            // verify required parameter 'select' is set
            if (!select) {
                throw new Error('Missing required parameter select when calling recover');
            }
            let httpRequestParams: any = {
                method: 'POST',
                url: localVarPath,
                json: true,
                data: select,
                
                
                params: queryParameters,
                headers: headerParams
            };

            if (extraHttpRequestParams) {
                httpRequestParams = this.extendObj(httpRequestParams, extraHttpRequestParams);
            }

            return this.$http(httpRequestParams);
        }
        /**
         * Get a specific error
         * 
         * @param id id
         */
        public getError (id: string, extraHttpRequestParams?: any ) : ng.IHttpPromise<ErrorMessage> {
            const localVarPath = this.basePath + '/api/services/error/{id}'
                .replace('{' + 'id' + '}', String(id));

            let queryParameters: any = {};
            let headerParams: any = this.extendObj({}, this.defaultHeaders);
            // verify required parameter 'id' is set
            if (!id) {
                throw new Error('Missing required parameter id when calling getError');
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
