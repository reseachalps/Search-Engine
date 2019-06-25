/**
 * Search Module
 */

import 'angular-paging';

/**
 * Internal deps
 */
import {SearchController} from './SearchController'
import {config as SearchRouting} from './SearchRouting'
import './components'

/**
 * Stylesheet
 */
import "./search.styl";
import "./search.resp.styl";

angular.module('app.search', ['ui.router', 'bw.paging', 'app.search.components'])
    .config(SearchRouting)
    .controller('SearchController', SearchController);
