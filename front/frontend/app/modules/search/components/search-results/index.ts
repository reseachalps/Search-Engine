/**
 * Search Components
 */

/**
 * External ngSanitize dep
 */

import 'angular-sanitize';
/**
 * Internal deps
 */
import {SearchResultsComponent} from './searchResults'
import {ResultComponent} from "./result/result";

import './firmography';

angular.module('app.search.components.results', ['ngSanitize', 'app.search.components.results.firmo'])
    .component('searchResults', new SearchResultsComponent())
    .component('result', new ResultComponent());
