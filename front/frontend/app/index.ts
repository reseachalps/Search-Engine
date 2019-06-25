/// <reference path='../_all.ts' />

/**
 * Importing external libs
 */

// angular
import 'angular';
import 'angular-material';
import 'angular-cookies';
import 'angular-touch';
require('angular-ui-router/release/angular-ui-router'); // AFAIK ui-router doesn't support the import syntax with typescript
require('angular-i18n/angular-locale_en-gb.js');

/**
 * Importing external stylesheets
 */
import 'angular-material/angular-material.css';
import 'font-awesome/css/font-awesome.min.css';

/**
 * Importing internal components
 */
import {config} from './config/core/coreConfig'
import {palette} from './config/material/palette' // angular material custom palette
import {d3Config} from './config/d3/d3' // d3 global config
import {run} from './config/core/coreRun'
import './modules';
import './services/index';
import './filters/index';
import './components/index';

/**
 * Global styling
 */
import './assets/variables.styl';
import './assets/global.styl';
import './assets/global.resp.styl';
/**
 * Importing the app images
 */

require.context('./assets/img', true, /^\.\//);

/**
 * The main app module.
 */

module app {
    angular
        .module('app', [
            "ui.router",
            "ngMaterial",
            "ngTouch",
            "ngCookies",
            "app.filters",
            "app.components",
            "app.services",
            "app.modules",
        ])
        .config(config)
        .config(palette)
        .config(d3Config)
        .run(run);
}