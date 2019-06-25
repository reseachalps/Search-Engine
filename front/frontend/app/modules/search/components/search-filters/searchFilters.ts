// Component stylesheet
import './search-filters.styl';
import './search-filters.resp.styl';
import {SearchService} from "../../../../services/SearchService";
import {Filter} from "../../../../models/Filter";

interface SearchFiltersComponentScope extends ng.IScope
{
    Filters: any // must match controllerAs
}

export class SearchFiltersComponent implements ng.IComponentOptions {

    public template:any = require('./search-filters.html');
    public restrict:string = "E";
    public bindings:Object = {
        query: '=',
        searchResults: '<',
        applyFilters: '=',
        applyFilter: '='
    };
    public controllerAs:string = 'Filters';

    public controller:Function = ($scope: SearchFiltersComponentScope, $location:ng.ILocationService, SearchService: SearchService) :void => {
        'ngInject';

        let ctrl = $scope.Filters,
            originatorEv,
            query = angular.copy(ctrl.query);

        // Filters config to display only expected filters
        ctrl.filtersConfig = {
            "entityFilterEnable": true,
            "countriesFilterEnable": true,
            "tutelleFilterEnable": false,
            "subjectFilterEnable": false,
            "geographicFilterEnable": true,
            "projectFilterEnable": true,
            "tagsFilterEnable": false,
            "sourcesFilterEnable": true,
        };

        /**********************
         * Filter definitions
         **********************/


        /**
         * Type filter (public / private)
         */

        // Init filter values
        ctrl.typeRadio = this.getActiveValues('publicEntity', query)[0] || 'all';

        const typeDic = {
            'true': 'Public',
            'false': 'Private',
            'undefined': 'Not available'
        };

        // filter values
        ctrl.typeFilterValues = ctrl.searchResults.histograms.publicEntity.bins.map((bin) => {
            return {
                value: bin.key,
                label: typeDic[bin.key] || bin.key,
                count: bin.count
            }
        });
        ctrl.typeFilterValues.unshift({
            value: 'all',
            label: 'All organizations'
        });

        /**
         * Tutelles filter
         */
        ctrl.tutellesCheckboxes = this.getActiveValues('institutions', query) || [];

        // filter values
        ctrl.tutellesFilterValues = [];

        /**
         * Countries filter
         */
        ctrl.countriesCheckboxes = this.getActiveValues('countries', query) || [];

        // filter values
        ctrl.countriesFilterValues = ctrl.searchResults.histograms.countries.bins.map((bin) => {
            return {
                value: bin.key,
                label: bin.label,
                count: bin.count
            }
        });

        /**
         * Countries filter
         */
        ctrl.sourcesCheckboxes = this.getActiveValues('sources', query) || [];

        // filter values
        ctrl.sourcesFilterValues = ctrl.searchResults.histograms.sources.bins.map((bin) => {
            return {
                value: bin.key,
                label: bin.label,
                count: bin.count
            }
        });

        /**
         * Countries filter
         */
        ctrl.nutsCheckboxes = this.getActiveValues('nuts', query) || [];

        // filter values
        ctrl.nutsFilterValues = ctrl.searchResults.histograms.nuts.bins.map((bin) => {
            return {
                value: bin.key,
                label: bin.label,
                count: bin.count
            }
        });

        ctrl.dptsFilterValues = [];

        /**
         * Badges
         */
        ctrl.badgeCheckboxes = this.getBadgeValues(query) || undefined;

        ctrl.badgeFilterValues = [];

        /**
         * Themes (erc, domaine)
         */
        ctrl.selectedTheme =  this.getThemesValue(query) || undefined;

        /**
         * Project / Calls
         */
        ctrl.selectedProject = this.getProjectValue(query) || undefined;

        /**
         * Open the filters menu
         */
        ctrl.openMenu = ($mdOpenMenu, $event) => {
            originatorEv = $event;
            $mdOpenMenu($event);
            window.dispatchEvent(new Event('resize')); // Resize event need to be manually trigger otherwise the virtual repeat doesn't render
        };
    };

    private getActiveValues(type:string, query:any):any {
        if(angular.isUndefined(query[type])){
            return false;
        }
        return query[type][0].values;
    }

    private getThemesValue(query):any {
        if(angular.isDefined(query['erc'])){
            return {theme:'erc', value: query['erc'][0].values[0]};
        }
        if(angular.isDefined(query['domaine'])){
            return {theme:'domaine', value: query['domaine'][0].values[0]};
        }
        return false;
    }

    private getProjectValue(query:any):any {
        if(angular.isDefined(query['projects'])){
            return {theme:'projects', value: query['projects'][0].values[0]};
        }
        if(angular.isDefined(query['calls'])){
            return {theme:'calls', value: query['calls'][0].values[0]};
        }
        return false;
    }

    private getGeoValues(query:any): any {
        if(angular.isDefined(query['urbanUnit'])){
            return {theme:'urbanUnit', values: [].concat(query['urbanUnit'][0].values)};
        }
        if(angular.isDefined(query['departements'])){
            return {theme:'departements', values: [].concat(query['departements'][0].values)};
        }
        return false;
    }

    private getBadgeValues(query:any): any {
        if(angular.isDefined(query['badges'])){
            return {theme:'badges', values: [].concat(query['badges'][0].values)};
        }
        return false;
    }
}