/// <reference path="./Map.ts" />
/// <reference path='./api.d.ts' />

/**
 * App Services
 */
angular.module('app.api', [])
    .service('CountApi', require('exports?api!./CountApi').CountApi)
    .service('CoreStatusApi', require('exports?api!./CoreStatusApi').CoreStatusApi)
    .service('CrowdsourcingApi', require('exports?api!./CrowdsourcingApi').CrowdsourcingApi)
    .service('SchedulingApi', require('exports?api!./SchedulingApi').SchedulingApi)
    .service('ErrorServiceApi', require('exports?api!./ErrorServiceApi').ErrorServiceApi)
    .service('CorrectionAdminApi', require('exports?api!./CorrectionAdminApi').CorrectionAdminApi);