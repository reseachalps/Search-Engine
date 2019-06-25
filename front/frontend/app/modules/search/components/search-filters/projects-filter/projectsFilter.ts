import './projects-filter.styl';
import {SearchService} from "../../../../../services/SearchService";
import {FiltersService} from "../../../../../services/FiltersService";

interface ProjectsFilterComponentScope extends ng.IScope
{
    Projects: any // must match controllerAs
}

export class ProjectsFilterComponent implements ng.IComponentOptions {

    public template:any = require('./projects-filter.html');
    public restrict:string = "E";
    public bindings:Object = {
        'applyFilter': '&',
        'selected': '<'
    };
    public controllerAs:string = 'Projects';

    public controller:Function = ($scope: ProjectsFilterComponentScope, FiltersService: FiltersService, SearchService: SearchService) :void => {
        'ngInject';

        var ctrl = $scope.Projects;
        if(ctrl.selected){
            if(ctrl.selected.theme === 'projects') {
                FiltersService.getProjectName(ctrl.selected.value).then(data => {
                    ctrl.selectedItem = {
                        id: ctrl.selected.value,
                        label: (data.data.acronym||[])[0] || data.data.label
                    }
                });
            } else {
                ctrl.selectedItem = {
                    id: ctrl.selected.value,
                    label: ctrl.selected.value
                }
            }
        }

        ctrl.apply = () => {
            ctrl.applyFilter({
                id: ctrl.selected.theme,
                filters: ctrl.selected.theme === 'projects' ? [{value:ctrl.selectedItem.id}] : [{value:ctrl.selectedItem.label}]
            })
        };

        ctrl.querySearch = (query:string):any => {
            if(ctrl.selected.theme !== 'calls'){
                return SearchService.searchProject(query);
            } else {
                return SearchService.searchCall(query);
            }
        };
    };
}