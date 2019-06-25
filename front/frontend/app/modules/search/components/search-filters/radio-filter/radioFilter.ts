import {Utils} from "../../../../../models/Utils";
import './radio-filter.styl';

interface RadioFilterComponentScope extends ng.IScope
{
    Radio: any // must match controllerAs
}

export class RadioFilterComponent implements ng.IComponentOptions {

    public template:any = require('./radio-filter.html');
    public restrict:string = "E";
    public bindings:Object = {
        'values': '<',
        'selected': '<',
        'filterId': '@',
        'applyFilter': '&'
    };
    public controllerAs:string = 'Radio';

    public controller:Function = ($scope: RadioFilterComponentScope) :void => {
        'ngInject';

        var ctrl = $scope.Radio;

        ctrl.maxValue = Utils.getMaxValueFromArrayOfObjects(ctrl.values, 'count');
    };
}