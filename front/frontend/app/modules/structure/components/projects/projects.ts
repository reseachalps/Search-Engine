// Component stylesheet
import './projects.styl';
import {Utils} from "../../../../models/Utils";
import {ProjectService} from "../../../../services/ProjectService";

interface ProjectsComponentScope extends ng.IScope
{
    Projects: ctrlScope, // must match controllerAs
}

interface ctrlScope
{
    search:string;
    selectedProject:any;
    selectProject:Function;
    displayFullDescription:Boolean;
    projectsList: any[]
    projects: any[],
    projectTypeChart: any
}

export class ProjectsComponent implements ng.IComponentOptions {

    public template:any = require('./projects.html');
    public restrict:string = "E";
    public bindings:Object = {
        projects: '<',
        webProjects: '<',
        allowEditing: '=',
        structure: '<'
    };
    public controllerAs:string = 'Projects';

    public controller:Function = ($scope: ProjectsComponentScope, ProjectService: ProjectService) :void => {
        'ngInject';
        var ctrl = $scope.Projects;
        ctrl.search = '';
        const CHART_HEIGHT = 300;

        /**
         * Project type chart (pcrdt / anr)
         */
        ctrl.projectTypeChart = {
            options: {
                chart: {
                    type: 'pie'
                },
                plotOptions: {
                    pie: {
                        dataLabels: {
                            enabled: true,
                            style: {
                                color: 'black'
                            }
                        }
                    }
                }
            },
            title: {text: 'Project type'},
            series: [],
            "size": {height: CHART_HEIGHT}
        };

        ctrl.projectsList = [];
        ctrl.projects = ctrl.projects.sort((a,b) => { // sort by date first and name second
            if((a.year || -Infinity) < (b.year || -Infinity)){
                return 1
            } else if((a.year || -Infinity) > (b.year || -Infinity)){
                return -1
            } else if(angular.isUndefined(a.acronym)){
                return -1
            } else if(angular.isUndefined(b.acronym)){
                return 1
            } else if(a.acronym.toLowerCase() < b.acronym.toLowerCase()){
                return -1
            } else if(a.acronym.toLowerCase() > b.acronym.toLowerCase()){
                return 1
            }
            return 0
        });
        ctrl.selectProject = function(project) {
            ctrl.displayFullDescription = false;
            ProjectService.getProject(project.id).then(function(data) {
                ctrl.selectedProject = data.data;
            });
        };
        ctrl.projects.forEach((project, i) =>{
            if(i === 0 || project.year !== ctrl.projects[i-1].year){
                ctrl.projectsList.push({
                    header: true,
                    year: project.year
                });
                ctrl.projectsList.push(project);
            } else {
                ctrl.projectsList.push(project);
            }
        });

        // auto select project
        if ( ctrl.projects.length>0){
            ctrl.selectProject(ctrl.projects[0]);
        }
        window.dispatchEvent(new Event('resize')); // Resize event need to be manually trigger otherwise the virtual repeat doesn't render

    };
}