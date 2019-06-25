import {SearchService} from "../../services/SearchService";
/**
 * ui-router search state
 * @param $stateProvider
 */


export function config($stateProvider: ng.ui.IStateProvider): void {

    'ngInject'; //needed when directly exporting a class or function

    $stateProvider.state('search', {
        url: '/recherche?' +
        'page&' + // page number
        'query&' + // text query
        'publicEntity&' + // public/private
        'urbanUnit&' + // unités urbaines
        'departements&' + // departements
        'erc&' + // erc themes
        'domaine&' + // domaine themes
        'calls&' + // project call
        'projects&' + // projects
        'badges&' + // projects
        'countries&' + // countries
        'nuts&' + // nuts
        'sources&' + // sources
        'ids&' + // ids
        'institutions', // tutelles
        views: {
            "@": {
                template: <string>require('./search.html'), // IState only accepts an explicit string for the template (require return type isn't specified)
                controller: 'SearchController',
                controllerAs: 'Search'
            }
        },
        resolve: {
            nomenclature : (SearchService:SearchService) => {
                "ngInject";
                return SearchService.getNomenclature();
            },
            searchResults: (nomenclature, $stateParams: ng.ui.IStateParamsService, SearchService:SearchService) => { //injecting nomenclature so we're sure it's resolved
                "ngInject";
                var params = SearchService.buildSearchParams($stateParams);
                return SearchService.search(params).then((data) => {
                    return data.data;
                });
            }
        }
    });
}