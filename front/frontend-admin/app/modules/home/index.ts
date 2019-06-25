/**
 * Homepage Component
 */

/**
 * Internal deps
 */
import {HomeController} from './HomeController'
import {config as HomeRouting} from './HomeRouting'

/**
 * Stylesheet
 */
import "./home.styl";

angular.module('app.home', ["ui.router", 'app.api'])
    .config(HomeRouting)
    .controller('HomeController', HomeController);
