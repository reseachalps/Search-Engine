import {ActiveFilterComponent} from "./active-filter/activeFilter";
import {ActiveFiltersComponent} from "./activeFilters";

angular.module('app.search.components.filters.active', [])
    .component('activeFilters', new ActiveFiltersComponent())
    .component('activeFilter', new ActiveFilterComponent());
