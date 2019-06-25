/**
 * Homepage Component
 */

/**
 * Internal deps
 */
import {QueuesController} from './QueuesController'
import {config as QueuesRouting} from './QueuesRouting'

/**
 * Stylesheet
 */
import "./queues.styl";
import "./directives/svg-monitoring.js";
import "./directives/svg-monitoring.styl";

angular.module('app.queues', ["ui.router", 'app.api', 'app.queues.svg'])
    .config(QueuesRouting)
    .controller('QueuesController', QueuesController);
