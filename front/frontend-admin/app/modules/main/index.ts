/**
 * Homepage Component
 */

/**
 * Internal deps
 */
import {config as MainRouting} from './MainRouting'
import {MainController} from './MainController'

/**
 * Stylesheet
 */
import "./main.styl";

angular.module('app.main', ["ui.router"])
    .config(MainRouting)
    .controller("MainController", MainController);
