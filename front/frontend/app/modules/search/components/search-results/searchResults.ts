// Component stylesheet
import './search-results.styl';
import './search-results.resp.styl';
import {SearchService} from "../../../../services/SearchService";

interface SearchResultsComponentScope extends ng.IScope
{
    Results: any // must match controllerAs
}

export class SearchResultsComponent implements ng.IComponentOptions {

    public template:any = require('./search-results.html');
    public restrict:string = "E";
    public bindings:Object = {
        searchResults: '=',
        applyFilters: '=',
        applyFilter: '='
    };
    public controllerAs:string = 'Results';

    public controller:Function = ($scope: SearchResultsComponentScope, $window:ng.IWindowService, $location: ng.ILocationService, $rootScope: ng.IRootScopeService, SearchService: SearchService, $sce: ng.ISCEService) :void => {
        'ngInject';

        var ctrl = $scope.Results;
        ctrl.searchRequest = $sce.trustAsResourceUrl("./map/?search="+encodeURIComponent(JSON.stringify(SearchService.lastSearch)));

        let viewSelected = ($location.search().view && $location.search().view.toString());
        if (viewSelected === 'firmo') {
            ctrl.selectedTab = 1;
        } else if (viewSelected === 'map') {
            ctrl.selectedTab = 2;
        } else {
            ctrl.selectedTab = 0;
        }

        ctrl.tabSelected = (tab) => {
            if(tab === 'firmo') {
                $rootScope.$broadcast('firmoTabDisplayed');
                $location.search('view', 'firmo');
            } else if(tab === 'map'){
                $rootScope.$broadcast('mapTabDisplayed');
                $location.search('view','map');
            } else {
                $location.search('view', undefined);
            }
        };
        ctrl.currentPage = $location.search()['page'] || 1;
        ctrl.PAGE_SIZE = 20;
        ctrl.total = ctrl.searchResults.total;
        ctrl.maxPage = Math.ceil(ctrl.total / ctrl.PAGE_SIZE); 
        ctrl.pageChanged  = (page) => {
            $window.scrollTo(0,0); // scroll to top before changing the page
            $location.search('page', page);
        };
        $rootScope.$on("firmoTabDisplayed", () => {
            SearchService.getTagCloud(SearchService.lastSearch).then((keywords: any) => {
                ctrl.keywords = keywords.data;
            });
        });
    };
}