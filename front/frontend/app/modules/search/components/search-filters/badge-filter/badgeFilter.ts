import {Utils} from "../../../../../models/Utils";
import {SearchService} from "../../../../../services/SearchService";
import './badge-filter.styl';

interface BadgeFilterComponentScope extends ng.IScope {
    Badge:any // must match controllerAs
}

export class BadgeFilterComponent implements ng.IComponentOptions {

    public template = <string>require('./badge-filter.html');
    public restrict:string = "E";
    public bindings:Object = {
        'values': '=',
        'selected': '=',
        'applyFilter': '&',
        'filterId': '@',
    };
    public controllerAs:string = 'Badge';

    public controller:Function = ($scope:BadgeFilterComponentScope, SearchService:SearchService):void => {
        'ngInject';

        var ctrl = $scope.Badge;

        ctrl.maxValue = Utils.getMaxValueFromArrayOfObjects(ctrl.values, 'count');

        // Add labels to code
        ctrl.values = SearchService.getLabelsForFilter(ctrl.values, "badge");

        // select them
        if(ctrl.selected) {
            ctrl.values.forEach((val:any)=> {
                if (ctrl.selected.values.indexOf(val.value) > -1) {
                    val.selected = true;
                }
            });
        }

        ctrl.applySelection = () => {
            let selectedValues = ctrl.values.filter((val:any) => val.selected);
            ctrl.applyFilter({id: ctrl.filterId, filters: selectedValues, operation:'all'});
        };

        ctrl.clearSelection = () => {
            ctrl.values.map((value:any) => {
                value.selected = false;
                return value;
            });
        };
    };

}