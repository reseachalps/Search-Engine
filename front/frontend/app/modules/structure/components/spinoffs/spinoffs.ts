// Component stylesheet
import './spinoffs.styl';

interface SpinoffsComponentScope extends ng.IScope
{
    Spinoff: any, // must match controllerAs
}

export class SpinoffsComponent implements ng.IComponentOptions {

    public template:any = require('./spinoffs.html');
    public restrict:string = "E";
    public bindings:Object = {
        spinoffs: '<',
        spinoffFrom: '<',
        structure: '<'
    };
    public controllerAs:string = 'Spinoff';

    public controller:Function = ($scope: SpinoffsComponentScope) :void => {
        'ngInject';
        var ctrl = $scope.Spinoff;
    };
}