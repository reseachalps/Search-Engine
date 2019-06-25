/**
 Global components for the app
 **/

// import './smarttag.js';
import {TagService} from "./TagService";
import {TagClickDirective} from "./tagClick";
import {TagPageDirective} from "./tagPage";

angular.module('app.components.xiti', [])
    .service('TagService', TagService)
    .directive('tagPage', <any>TagPageDirective)
    .directive('tagClick', <any>TagClickDirective);