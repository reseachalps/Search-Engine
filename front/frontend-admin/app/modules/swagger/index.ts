/**
 * Homepage Component
 */

/**
 * Internal deps
 */
import {config as SwaggerRouting} from './SwaggerRouting'

/**
 * Stylesheet
 */
import "./swagger.styl";

angular.module('app.swagger', ["ui.router"])
    .config(SwaggerRouting);
