// Component stylesheet
import './patents.styl';
import {Utils} from "../../../../models/Utils";
import {PublicationService} from "../../../../services/PublicationService";

interface PatentsComponentScope extends ng.IScope
{
    Patents: ctrlScope, // must match controllerAs
}

interface ctrlScope
{
    search:string;
    displayFullDescription:Boolean;
    selectedPatent:any;
    selectPatent:Function;
    patents: any[],
    patentList: any[]
}

export class PatentsComponent implements ng.IComponentOptions {

    public template:any = require('./patents.html');
    public restrict:string = "E";
    public bindings:Object = {
        patents: '<',
        webPatents: '<',
        allowEditing: '=',
        structure: '<'
    };
    public controllerAs:string = 'Patents';

    public controller:Function = ($scope:PatentsComponentScope, PublicationService: PublicationService):void => {
        'ngInject';
        var ctrl = $scope.Patents;
        ctrl.search = '';

        ctrl.patentList = [];
        ctrl.patents = ctrl.patents.sort((a: any,b: any) => { // sort by date first and name second
            if((a.publicationDate || a.lastSourceDate) < (b.publicationDate  || b.lastSourceDate)){
                return 1
            } else if((a.publicationDate || a.lastSourceDate) > (b.publicationDate  || b.lastSourceDate)) {
                return -1
            } else if(a.title.toLowerCase() < b.title.toLowerCase()){
                return -1
            } else if(a.title.toLowerCase() > b.title.toLowerCase()){
                return 1
            }
            return 0
        });
        ctrl.patents.forEach((patent: any, i) =>{
            const year = new Date(patent.publicationDate || patent.lastSourceDate).getFullYear() || 'Without year';
            if(i === 0 || year !==  (new Date(ctrl.patents[i-1].publicationDate || ctrl.patents[i-1].lastSourceDate).getFullYear() || 'Without year')){
                ctrl.patentList.push({
                    header: true,
                    year: year
                });
                ctrl.patentList.push(patent);
            } else {
                ctrl.patentList.push(patent);
            }
        });

        ctrl.selectPatent = function(patent) {
            ctrl.displayFullDescription = false;
            PublicationService.getPublication(patent.id).then(function(data) {
                ctrl.selectedPatent = data.data;
            });
        };

        if (ctrl.patents.length>0)
            ctrl.selectPatent(ctrl.patents[0]);
        window.dispatchEvent(new Event('resize')); // Resize event need to be manually trigger otherwise the virtual repeat doesn't render

    }
}