import {SearchService} from "../../services/SearchService";
import {CustomRootScope} from "../../config/core/coreRun";
import {Filter} from "../../models/Filter";
/**
 * The search controller for the app.
 */
export class SearchController {

    private searchResults: any;
    private query: any;
    private exportLink: any;
    private applyFilter: any;
    private applyFilters: any;

    constructor($rootScope: CustomRootScope, $location: ng.ILocationService, $window:ng.IWindowService, SearchService: SearchService, searchResults: any){
        "ngInject";

        $window.scrollTo(0,0); // scroll to top, useful when coming from a structure

        var ctrl = this;
        ctrl.query = SearchService.deserializeQuery($location.search());
        ctrl.searchResults = searchResults;
        $rootScope.pageTitle = (ctrl.query.query && ctrl.query.query.length > 0)? ctrl.query.query + " - Search Re-Search Alps" : "Search - Re-Search Alps";
        $rootScope.og.title = (ctrl.query.query && ctrl.query.query.length > 0)? "Re-Search Alps : results for research '" + ctrl.query.query + "'" : "Discover Re-Search Alps, the engine of research and innovation (beta)";

        ctrl.exportLink = SearchService.generateExportLink($location.url(), ctrl.searchResults.request);

        /**
         * Apply a single value filter (like radio)
         * @param type
         * @param value
         */
        ctrl.applyFilter = (type:string, value:any) => {
            if(value === 'all'){
                delete ctrl.query[type];
            } else {
                ctrl.query[type] = new Filter('all',[value]);
            }
            SearchService.runSearch(ctrl.query);
        };

        /**
         * Apply a multiple values filter (like checkboxes)
         * @param type
         * @param values
         */
        ctrl.applyFilters = (type: string, values: any[], operation: string) => {
            if (angular.isUndefined(operation))
                operation='any';
            ctrl.query = $location.search();
            if(values.length === 0){
                delete ctrl.query[type];
            } else {

                /**
                 * Theme filter special case
                 */
                // if applying a new theme filter, we need to remove the other one as it is 2 different search params
                if(type === 'domaine') {
                    delete ctrl.query['erc'];
                }
                if(type === 'erc') {
                    delete ctrl.query['domaine'];
                }

                /**
                 * Projects / Calls filter special case
                 */
                // if applying a new theme filter, we need to remove the other one as it is 2 different search params
                if(type === 'projects') {
                    delete ctrl.query['calls'];
                }
                if(type === 'calls') {
                    delete ctrl.query['projects'];
                }

                /**
                 * Urban units / Deparetments filter special case
                 */
                // if applying a new theme filter, we need to remove the other one as it is 2 different search params
                if(type === 'departements') {
                    delete ctrl.query['urbanUnit'];
                }
                if(type === 'urbanUnit') {
                    delete ctrl.query['departements'];
                }
                /**
                 * End of special case
                 */

                ctrl.query[type] = new Filter(operation,
                    values.map(val => val.value)
                )
            }
            SearchService.runSearch(ctrl.query);
        }

    }
}