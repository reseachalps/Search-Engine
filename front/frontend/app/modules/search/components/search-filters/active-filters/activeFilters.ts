// Component stylesheet
import './active-filters.styl';
import {Filter} from "../../../../../models/Filter";

interface ActiveFiltersComponentScope extends ng.IScope
{
    Active: any // must match controllerAs
}

export class ActiveFiltersComponent implements ng.IComponentOptions {

    public template:any = require('./active-filters.html');
    public restrict:string = "E";
    public bindings:Object = {
        query: '<'
    };
    public controllerAs:string = 'Active';

    public controller:Function = ($scope: ActiveFiltersComponentScope, $location:ng.ILocationService) :void => {
        'ngInject';

        const validFilters = ['publicEntity', 'countries', 'institutions', 'urbanUnit', 'departements', 'erc', 'domaine', 'projects', 'calls', 'badges', 'sources', 'nuts', 'ids']; // list of the query params that we want to display as "active" (page number and query for ex. are none of them)
        var ctrl = $scope.Active;
        ctrl.filters = [];

        angular.forEach(ctrl.query, (value: Array<Filter>, key: string) => {
            if(validFilters.indexOf(key) > -1){
                ctrl.filters.push(new Filter(value[0].op, value[0].values, key));
            }
        });

        ctrl.reset = () => {
            let params = $location.search();
            for(var param in params){
                if(validFilters.indexOf(param) > -1){
                    delete params[param];
                }
            }
            delete params.page;
            $location.search(params);
        }

    };
}