// Component stylesheet
import './thesis.styl';
import {Utils} from "../../../../models/Utils";
import {PublicationService} from "../../../../services/PublicationService";

interface ThesisComponentScope extends ng.IScope
{
    Thesis: ctrlScope, // must match controllerAs
}

interface ctrlScope
{
    search:string;
    selectedThesis:any;
    displayFullDescription:Boolean;
    selectThesis:Function;
    thesisList: any[],
    thesis: any[]
}

export class ThesisComponent implements ng.IComponentOptions {

    public template:any = require('./thesis.html');
    public restrict:string = "E";
    public bindings:Object = {
        thesis: '<',
        webThesis: '<',
        allowEditing: '=',
        structure: '<'
    };
    public controllerAs:string = 'Thesis';

    public controller:Function = ($scope: ThesisComponentScope, PublicationService: PublicationService) :void => {
        'ngInject';
        var ctrl = $scope.Thesis;
        ctrl.search = '';

        ctrl.thesisList = [];
        ctrl.thesis = ctrl.thesis.sort((a: any,b: any) => { // sort by date first and name second
            if(!a.publicationDate && !a.lastSourceDate){
                return 1;
            } else if(!b.publicationDate && !b.lastSourceDate){
                return -1;
            } else if((a.publicationDate || a.lastSourceDate) < (b.publicationDate  || b.lastSourceDate)){
                return 1
            } else if((a.publicationDate || a.lastSourceDate) > (b.publicationDate  || b.lastSourceDate)) {
                return -1
            } else if(a.title === null){
                return -1
            } else if(b.title === null){
                return 1
            } else if(a.title.toLowerCase() < b.title.toLowerCase()){
                return -1
            } else if(a.title.toLowerCase() > b.title.toLowerCase()){
                return 1
            }
            return 0
        });
        ctrl.thesis.forEach((thesis: any, i) =>{
            thesis.title = thesis.title && thesis.title.replace(/(\[i\]|\[\/i\])/g, '') || ''; // removes the weird [i] and [/i] tags
            const year = new Date(thesis.publicationDate || thesis.lastSourceDate).getFullYear() || 'Without year';
            if(i === 0 || year !==  (new Date(ctrl.thesis[i-1].publicationDate || ctrl.thesis[i-1].lastSourceDate).getFullYear() || 'Without year')){
                ctrl.thesisList.push({
                    header: true,
                    year: year
                });
                ctrl.thesisList.push(thesis);
            } else {
                ctrl.thesisList.push(thesis);
            }
        });

        ctrl.selectThesis = function(thesis) {
            ctrl.displayFullDescription = false;
            PublicationService.getPublication(thesis.id).then(function(data) {
                ctrl.selectedThesis = data.data;
            });
        };

        if (ctrl.thesis.length>0)
            ctrl.selectThesis(ctrl.thesis[0]);

        window.dispatchEvent(new Event('resize')); // Resize event need to be manually trigger otherwise the virtual repeat doesn't render


    };
}