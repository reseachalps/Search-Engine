/// <reference path="api.d.ts" />

/* tslint:disable:no-unused-variable member-ordering */

namespace api {
    'use strict';

    export class CrowdsourcingApi {
        protected basePath = '.';

        static $inject: string[] = ['$http', '$httpParamSerializer'];

        constructor(protected $http: ng.IHttpService, protected $httpParamSerializer?: (d: any) => any, basePath?: string) {
            if (basePath) {
                this.basePath = basePath;
            }
        }

        /**
         * get feedbacks
         *
         */
        public getFeedbacks (active?: boolean ) : ng.IHttpPromise<any> {
            return this.$http.get('/api/admin/feedback?active='+active);
        }

        /**
         * Validate a feedback
         *
         */
        public validateFeedback (feedbackId: any ) : ng.IHttpPromise<any> {
            return this.$http.post('/api/admin/feedback/'+feedbackId+'/markProcessed', {});
        }
    }
}
