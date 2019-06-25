// Component stylesheet
import './publications.styl';
import {PublicationService} from "../../../../services/PublicationService";
import {SearchService} from "../../../../services/SearchService";

interface PublicationsComponentScope extends ng.IScope
{
    Publications: ctrlScope, // must match controllerAs
}

interface ctrlScope
{
    webPublicationsLength:Number;
    revuesLimit:Number;
    search:string;
    countFiltredPublications:Number;
    filtredPublications:any[];
    filterPublications:Function;
    authorLimit:Number;
    displayFullDescription: Boolean;
    selectedPublication: any;
    selectPublication: Function;
    publicationsList: any[],
    publications: any[],
    webPublications: any[],
    revues: any[]
}

export class PublicationsComponent implements ng.IComponentOptions {

    public template:any = require('./publications.html');
    public restrict:string = "E";
    public bindings:Object = {
        publications: '<',
        webPublications: '<',
        allowEditing: '=',
        structure: '<'
    };
    public controllerAs:string = 'Publications';

    public controller:Function = ($scope: PublicationsComponentScope, $filter:ng.IFilterService, PublicationService: PublicationService, SearchService: SearchService) :void => {
        'ngInject';
        var ctrl = $scope.Publications;
        ctrl.search = '';
        ctrl.publicationsList = [];
        ctrl.revuesLimit = 5;
        ctrl.webPublications = ctrl.webPublications || []; // safety
        ctrl.webPublicationsLength = ctrl.webPublications.length;
        var sortPublication = (a: any, b: any) => { // sort by date first and name second
            if(!a.publicationDate && !b.publicationDate){
                return -1;
            }
            if((a.publicationDate || a.lastSourceDate) < (b.publicationDate  || b.lastSourceDate)){
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
        };
        ctrl.webPublications = ctrl.webPublications.sort(sortPublication);
        ctrl.publications = ctrl.publications.sort(sortPublication);

        let publicationsTypes = {};
        ctrl.publications.forEach((publication: any, i) =>{
            publication.title = publication.title && publication.title.replace(/(\[i\]|\[\/i\])/g, '') || ''; // removes the weird [i] and [/i] tags
            const year = new Date(publication.publicationDate || publication.lastSourceDate).getFullYear() || 'Without year';
            if(publication.source.type === 'COLLECTION' && publication.source.collection !== null && publication.source.collection.title !== null) {
                if (!publicationsTypes[publication.source.collection.title]) { // populate charts data
                    publicationsTypes[publication.source.collection.title] = 1;
                } else {
                    publicationsTypes[publication.source.collection.title]++;
                }
            }
            if(i === 0 || year !==  (new Date(ctrl.publications[i-1].publicationDate || ctrl.publications[i-1].lastSourceDate).getFullYear() || 'Without year')){
                ctrl.publicationsList.push({
                    header: true,
                    year: year
                });
                ctrl.publicationsList.push(publication);
            } else {
                ctrl.publicationsList.push(publication);
            }
            publication.type = SearchService.getLabelForFilter('PUBLICATION_TYPE', publication.type)
        });

        ctrl.selectPublication = function(publication) {
            ctrl.displayFullDescription = false;
            ctrl.authorLimit=3;
            PublicationService.getPublication(publication.id).then(function(data) {
                ctrl.selectedPublication = data.data;
            });
        };

        /**
         * Publication source type series
         */
        ctrl.revues = [];
        for(var i in publicationsTypes){
            ctrl.revues.push({name:i, y:publicationsTypes[i]});
        }

        if (ctrl.publications.length>0)
            ctrl.selectPublication(ctrl.publications[0]);

        window.dispatchEvent(new Event('resize')); // Resize event need to be manually trigger otherwise the virtual repeat doesn't render

    };
}