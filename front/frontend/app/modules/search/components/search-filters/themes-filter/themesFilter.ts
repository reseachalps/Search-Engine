import './themes-filter.styl';
import {SearchService} from "../../../../../services/SearchService";

interface ThemesFilterComponentScope extends ng.IScope
{
    Themes: any // must match controllerAs
}

export class ThemesFilterComponent implements ng.IComponentOptions {

    public template:any = require('./themes-filter.html');
    public restrict:string = "E";
    public bindings:Object = {
        'applyFilter': '&',
        'selected': '<'
    };
    public controllerAs:string = 'Themes';

    public controller:Function = ($scope: ThemesFilterComponentScope, SearchService: SearchService) :void => {
        'ngInject';

        var ctrl = $scope.Themes;
        if(ctrl.selected){
            ctrl.selectedItem = {
                key: ctrl.selected.theme,
                label:SearchService.getLabelForFilter(ctrl.selected.theme,ctrl.selected.value)
            }
        }
        ctrl.querySearch = (query) => {
           return SearchService.searchTheme(ctrl.selected.theme, query);
        };
    };
}