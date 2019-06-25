/// <reference path="api.d.ts" />

/* tslint:disable:no-unused-variable member-ordering */

namespace api {
    'use strict';

    export class QueueServiceApi {
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
         * Get the list of all internal subscribers
         * 
         */
        public getListeners (extraHttpRequestParams?: any ) : ng.IHttpPromise<Array<string>> {
            const localVarPath = this.basePath + '/api/services/queue/subscribers';

            let queryParameters: any = {};
            let headerParams: any = this.extendObj({}, this.defaultHeaders);
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
         * Resume an internal subscriber based on a name
         * 
         * @param subscriber subscriber
         */
        public resumeListener (subscriber: string, extraHttpRequestParams?: any ) : ng.IHttpPromise<OK> {
            const localVarPath = this.basePath + '/api/services/queue/subscribers/resume';

            let queryParameters: any = {};
            let headerParams: any = this.extendObj({}, this.defaultHeaders);
            // verify required parameter 'subscriber' is set
            if (!subscriber) {
                throw new Error('Missing required parameter subscriber when calling resumeListener');
            }
            if (subscriber !== undefined) {
                queryParameters['subscriber'] = subscriber;
            }

            let httpRequestParams: any = {
                method: 'POST',
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
         * Stop an internal subscriber based on a name
         * 
         * @param subscriber subscriber
         */
        public stopListener (subscriber: string, extraHttpRequestParams?: any ) : ng.IHttpPromise<OK> {
            const localVarPath = this.basePath + '/api/services/queue/subscribers/stop';

            let queryParameters: any = {};
            let headerParams: any = this.extendObj({}, this.defaultHeaders);
            // verify required parameter 'subscriber' is set
            if (!subscriber) {
                throw new Error('Missing required parameter subscriber when calling stopListener');
            }
            if (subscriber !== undefined) {
                queryParameters['subscriber'] = subscriber;
            }

            let httpRequestParams: any = {
                method: 'POST',
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
         * Update the throttle limit
         * 
         * @param messagesPerSecond messagesPerSecond
         */
        public setThrottle (messagesPerSecond: number, extraHttpRequestParams?: any ) : ng.IHttpPromise<OK> {
            const localVarPath = this.basePath + '/api/services/queue/throttle';

            let queryParameters: any = {};
            let headerParams: any = this.extendObj({}, this.defaultHeaders);
            // verify required parameter 'messagesPerSecond' is set
            if (!messagesPerSecond) {
                throw new Error('Missing required parameter messagesPerSecond when calling setThrottle');
            }
            if (messagesPerSecond !== undefined) {
                queryParameters['messagesPerSecond'] = messagesPerSecond;
            }

            let httpRequestParams: any = {
                method: 'PATCH',
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
