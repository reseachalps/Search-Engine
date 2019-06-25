import IHttpPromise = angular.IHttpPromise;

export class PublicationService {
    private http:ng.IHttpService;
    private log:ng.ILogService;

    constructor($http, $log){
        "ngInject";
        this.http = $http;
        this.log = $log;
    }

    public getPublication(publicationId: string) : IHttpPromise<any> {
        return this.http.get('/api/publications/'+publicationId);
    }
}