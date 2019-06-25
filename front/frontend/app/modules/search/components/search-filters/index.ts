import './active-filters';
import {SearchFiltersComponent} from './searchFilters';
import {RadioFilterComponent} from "./radio-filter/radioFilter";
import {CheckboxFilterComponent} from "./checkbox-filter/checkboxFilter";
import {CountBarComponent} from "./count-bar/countBar";
import {ThemesFilterComponent} from "./themes-filter/themesFilter";
import {ProjectsFilterComponent} from "./projects-filter/projectsFilter";
import {GeoFilterComponent} from "./geo-filter/geoFilter";
import {BadgeFilterComponent} from "./badge-filter/badgeFilter";

angular.module('app.search.components.filters', ['app.search.components.filters.active'])
    .component('searchFilters', new SearchFiltersComponent())
    .component('countBar', new CountBarComponent())
    .component('radioFilter', new RadioFilterComponent())
    .component('themesFilter', new ThemesFilterComponent())
    .component('projectsFilter', new ProjectsFilterComponent())
    .component('geoFilter', new GeoFilterComponent())
    .component('badgeFilter', new BadgeFilterComponent())
    .component('checkboxFilter', new CheckboxFilterComponent());
