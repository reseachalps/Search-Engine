import IHttpPromise = angular.IHttpPromise;
import {FullStructure} from "../models/FullStructure";

export class StructureService {
    private http:ng.IHttpService;
    private log:ng.ILogService;

    constructor($http, $log) {
        "ngInject";
        this.http = $http;
        this.log = $log;
    }

    public getStructure(structureId:string):IHttpPromise<FullStructure> {
        return this.http.get('/api/structures/' + structureId);
    }

    public getSurroundings(structureId:string):IHttpPromise<FullStructure> {
        const DIST = 30; // 30km around;
        return this.http.get('/api/structures/near/' + structureId + '?distance=' + DIST);
    }

    public getKeywords(structureId:string):IHttpPromise<FullStructure> {
        return this.http.get('/api/structures/' + structureId + '/keywords');
    }

    public newFeedback(verificationCode:string, structure:String ,userName:string, email:string, field:string, comment?:string, value?:string):IHttpPromise<any> {
        let body = {
            feedback: {
                action: 'MODIFY',
                structure,
                userName,
                email,
                field,
                comment,
                value,
            },
            verificationCode
        };
        return this.http.put('/api/feedback', body);
    }
}