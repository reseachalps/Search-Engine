import IHttpPromise = angular.IHttpPromise;
import {DatabaseStats} from "../models/DatabaseStats";

export class StatsService {
    private http:ng.IHttpService;
    private log:ng.ILogService;

    constructor($http, $log){
        "ngInject";
        this.http = $http;
        this.log = $log;
    }

    public getStats() : IHttpPromise<DatabaseStats> {
        return this.http.get('/api/stats');
    }
}