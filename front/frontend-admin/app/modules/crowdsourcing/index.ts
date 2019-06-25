/**
 * Homepage Component
 */

/**
 * Internal deps
 */
import {CrowdsourcingController} from './crowdsourcing.ctrl'
import {config as CrowdsourcingRouting} from './crowdsourcing.routing'

/**
 * Stylesheet
 */
import "./crowdsourcing.styl";

angular.module('app.crowdsourcing', ["ui.router", 'app.api'])
    .config(CrowdsourcingRouting)
    .controller('CrowdsourcingController', CrowdsourcingController);
