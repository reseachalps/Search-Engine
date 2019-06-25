// Component stylesheet
import './result.styl';
import './result.resp.styl';
import ISanitizeService = angular.sanitize.ISanitizeService;

interface ResultComponentScope extends ng.IScope
{
    Result: any // must match controllerAs
}

export class ResultComponent implements ng.IComponentOptions {

    public template:any = require('./result.html');
    public restrict:string = "E";
    public bindings:Object = {
        result: '='
    };
    public controllerAs:string = 'Result';

    public controller:Function = ($scope: ResultComponentScope, $sanitize: ISanitizeService) :void => {
        'ngInject';

        var ctrl = $scope.Result;
        if(angular.isDefined(ctrl.result.highlights)){
            ctrl.result.highlights.forEach((highlight) => {
                highlight.value = $sanitize(highlight.value);
            });
        }

        /**
         * Will get the structure id numbers from its institutions
         */
        ctrl.idNumbers = [...new Set(
            ctrl.result.institutions ? ctrl.result.institutions
                .map(inst => inst.code || "")
                .filter(x => x.length > 0) : []
        )];
    };
}