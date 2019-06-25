import {Utils} from "../../../../../models/Utils";
import {SearchService} from "../../../../../services/SearchService";

interface GeoFilterComponentScope extends ng.IScope
{
    Geo: any // must match controllerAs
}

export class GeoFilterComponent implements ng.IComponentOptions {

    public template = <string>require('./geo-filter.html');
    public restrict:string = "E";
    public bindings:Object = {
        'urbanValues': '=',
        'dptsValues': '=',
        'selected': '=',
        'applyFilter': '&',
        'getLabelsFromNomenclature': '@'
    };
    public controllerAs:string = 'Geo';

    public controller:Function = ($scope: GeoFilterComponentScope, SearchService: SearchService) :void => {
        'ngInject';

        var ctrl = $scope.Geo;

        if(ctrl.selected){
            this.setActiveCheckboxes(ctrl.selected.theme === 'urbanUnit' ? ctrl.urbanValues : ctrl.dptsValues, ctrl.selected);
        } else {
            ctrl.selected = {
                theme: 'urbanUnit',
                urbanValues: []
            }
        }

        ctrl.maxUrbanValue = Utils.getMaxValueFromArrayOfObjects(ctrl.urbanValues, 'count');
        ctrl.maxDptsValue = Utils.getMaxValueFromArrayOfObjects(ctrl.dptsValues, 'count');

        if(ctrl.getLabelsFromNomenclature) {
            ctrl.urbanValues = SearchService.getLabelsForFilter(ctrl.urbanValues, ctrl.filterId);
        }

        ctrl.applySelection = () => {
            let selectedValues = ctrl.selected.theme === 'urbanUnit' ? ctrl.urbanValues.filter((val:any) => val.selected) : ctrl.dptsValues.filter((val:any) => val.selected);
            ctrl.applyFilter({id:ctrl.selected.theme, filters: selectedValues});
        };

        ctrl.clearSelection = () => {
            ctrl.urbanValues.map((value:any) => {
                value.selected = false;
                return value;
            });
            ctrl.dptsValues.map((value:any) => {
                value.selected = false;
                return value;
            });
        };
    };

    private setActiveCheckboxes(values:any[], selected):void {
        values.forEach((val:any)=> {
            if(selected.values.indexOf(val.value) > -1){
                val.selected=true;
            }
        });
    }
}