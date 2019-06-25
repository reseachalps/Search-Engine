// Component stylesheet
import './relative.styl';

interface RelativeComponentScope extends ng.IScope
{
    Relative: any // must match controllerAs
}

export class RelativeComponent implements ng.IComponentOptions {

    public template:any = require('./relative.html');
    public restrict:string = "E";
    public bindings:Object = {
        data: '<'
    };
    public controllerAs:string = 'Relative';

    public controller:Function = ($scope: RelativeComponentScope) :void => {
        'ngInject';
        var ctrl = $scope.Relative;
    };
}