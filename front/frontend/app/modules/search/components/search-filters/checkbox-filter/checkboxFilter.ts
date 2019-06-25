import {Utils} from "../../../../../models/Utils";
import {SearchService} from "../../../../../services/SearchService";

interface CheckboxFilterComponentScope extends ng.IScope
{
    Checkbox: any // must match controllerAs
}

export class CheckboxFilterComponent implements ng.IComponentOptions {

    public template = <string>require('./checkbox-filter.html');
    public restrict:string = "E";
    public bindings:Object = {
        'values': '<',
        'selected': '<',
        'filterId': '@',
        'applyFilter': '&',
        'getLabelsFromNomenclature': '@'
    };
    public controllerAs:string = 'Checkbox';

    public controller:Function = ($scope: CheckboxFilterComponentScope, SearchService: SearchService) :void => {
        'ngInject';

        var ctrl = $scope.Checkbox;

        this.setActiveCheckboxes(ctrl.values, ctrl.selected);

        ctrl.maxValue = Utils.getMaxValueFromArrayOfObjects(ctrl.values, 'count');

        if(ctrl.getLabelsFromNomenclature) {
            ctrl.values = SearchService.getLabelsForFilter(ctrl.values, ctrl.filterId);
        }

        ctrl.applySelection = () => {
            let selectedValues = ctrl.values.filter((val:any) => val.selected);
            ctrl.applyFilter({id:ctrl.filterId, filters: selectedValues});
        };

        ctrl.clearSelection = () => {
            ctrl.values.map((value:any) => {
                value.selected = false;
                return value;
            });
        };
    };

    private setActiveCheckboxes(values:any[], selected:string[]):void {
        values.forEach((val:any)=> {
            if(selected.indexOf(val.value) > -1){
                val.selected=true;
            }
        });
    }
}