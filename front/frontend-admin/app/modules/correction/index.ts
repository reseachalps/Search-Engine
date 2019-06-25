/**
 * Homepage Component
 */

/**
 * Internal deps
 */
import {CorrectionController} from './correction.ctrl'
import {config as CorrectionRouting} from './correction.routing'

/**
 * Stylesheet
 */
import "./correction.styl";

angular.module('app.correction', ["ui.router", 'app.api'])
    .config(CorrectionRouting)
    .controller('CorrectionController', CorrectionController);
