/**
 * Structure Module
 */

import 'angular-recaptcha';

/**
 * Internal deps
 */
import {StructureController} from './StructureController'
import {routing as StructureRouting} from './StructureRouting'
import './components';

/**
 * Stylesheet
 */
import "./structure.styl";
import "./structure.resp.styl";

angular.module('app.structure', ['ui.router', 'vcRecaptcha', 'app.structure.components'])
    .config(StructureRouting)
    .controller('StructureController', StructureController);
