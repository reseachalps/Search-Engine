/**
 * Homepage Component
 */

/**
 * Internal deps
 */
import {ErrorsController} from './errors.ctrl'
import {config as ErrorsRouting} from './errors.routing'

/**
 * Stylesheet
 */
import "./errors.styl";

angular.module('app.errors', ["ui.router", 'app.api'])
    .config(ErrorsRouting)
    .controller('ErrorsController', ErrorsController);
