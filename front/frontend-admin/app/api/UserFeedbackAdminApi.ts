/// <reference path="api.d.ts" />

/* tslint:disable:no-unused-variable member-ordering */

namespace api {
    'use strict';

    export class UserFeedbackAdminApi {
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
         * Get list of user feedbacks
         * Retrive the user feedbacks (active=true to retrieve only unprocessed feedbacks)
         * @param page page
         * @param size size
         * @param active active
         */
        public getUserFeedbacks (page?: number, size?: number, active?: boolean, extraHttpRequestParams?: any ) : ng.IHttpPromise<PageOfUserFeedback> {
            const localVarPath = this.basePath + '/api/admin/feedback';

            let queryParameters: any = {};
            let headerParams: any = this.extendObj({}, this.defaultHeaders);
            if (page !== undefined) {
                queryParameters['page'] = page;
            }

            if (size !== undefined) {
                queryParameters['size'] = size;
            }

            if (active !== undefined) {
                queryParameters['active'] = active;
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
         * Create a userFeedback
         * Don&#39;t provide any id to create one
         * @param feedback feedback
         */
        public addUserFeedback (feedback: UserFeedback, extraHttpRequestParams?: any ) : ng.IHttpPromise<UserFeedback> {
            const localVarPath = this.basePath + '/api/admin/feedback';

            let queryParameters: any = {};
            let headerParams: any = this.extendObj({}, this.defaultHeaders);
            // verify required parameter 'feedback' is set
            if (!feedback) {
                throw new Error('Missing required parameter feedback when calling addUserFeedback');
            }
            let httpRequestParams: any = {
                method: 'PUT',
                url: localVarPath,
                json: true,
                data: feedback,
                
                
                params: queryParameters,
                headers: headerParams
            };

            if (extraHttpRequestParams) {
                httpRequestParams = this.extendObj(httpRequestParams, extraHttpRequestParams);
            }

            return this.$http(httpRequestParams);
        }
        /**
         * getFeedback
         * 
         * @param id id
         */
        public getFeedback (id: string, extraHttpRequestParams?: any ) : ng.IHttpPromise<UserFeedback> {
            const localVarPath = this.basePath + '/api/admin/feedback/{id}'
                .replace('{' + 'id' + '}', String(id));

            let queryParameters: any = {};
            let headerParams: any = this.extendObj({}, this.defaultHeaders);
            // verify required parameter 'id' is set
            if (!id) {
                throw new Error('Missing required parameter id when calling getFeedback');
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
         * Mark a userFeedback as processed
         * 
         * @param id id
         */
        public markFeedbackAsProcessed (id: string, extraHttpRequestParams?: any ) : ng.IHttpPromise<UserFeedback> {
            const localVarPath = this.basePath + '/api/admin/feedback/{id}/markProcessed'
                .replace('{' + 'id' + '}', String(id));

            let queryParameters: any = {};
            let headerParams: any = this.extendObj({}, this.defaultHeaders);
            // verify required parameter 'id' is set
            if (!id) {
                throw new Error('Missing required parameter id when calling markFeedbackAsProcessed');
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
    }
}
