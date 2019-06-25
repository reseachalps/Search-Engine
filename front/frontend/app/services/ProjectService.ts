import IHttpPromise = angular.IHttpPromise;

export class ProjectService {
    private http:ng.IHttpService;
    private log:ng.ILogService;

    constructor($http, $log){
        "ngInject";
        this.http = $http;
        this.log = $log;
    }

    public getProject(projectId: string) : IHttpPromise<any> {
        return this.http.get('/api/projects/'+projectId);
    }
}