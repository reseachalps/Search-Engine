// Component stylesheet
import './active-filter.styl';
import './active-filter.resp.styl';
import {FiltersService} from "../../../../../../services/FiltersService";
import {Filter} from "../../../../../../models/Filter";

interface ActiveFilterComponentScope extends ng.IScope
{
    Filter: any // must match controllerAs
}

interface ctrlScope
{
    filter: Filter,
    filterLabel: string,
    removeFilter: Function
}

export class ActiveFilterComponent implements ng.IComponentOptions {

    public template:any = require('./active-filter.html');
    public restrict:string = "E";
    public bindings:Object = {
        filter: '<'
    };
    public controllerAs:string = 'Filter';

    public controller:Function = ($scope: ActiveFilterComponentScope, $location:ng.ILocationService, FiltersService: FiltersService) :void => {
        'ngInject';

        var ctrl: ctrlScope = $scope.Filter;

        ctrl.filterLabel = FiltersService.getFilterLabel(ctrl.filter);

        if(ctrl.filter.type === 'projects'){
            // We need to GET the project name from the project ID
            FiltersService.getProjectName(ctrl.filter.values[0]).then((data) => {
               ctrl.filterLabel += data.data.label ? ('(' + data.data.acronym + ') ' + data.data.label) : data.data.acronym;
            });
        }

        ctrl.removeFilter = () => {
            $location.search(ctrl.filter.type, undefined);
        }
    };
}