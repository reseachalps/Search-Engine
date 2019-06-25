/// <reference path='../_all.ts' />

/**
 * Importing external libs
 */
import 'angular';
import 'angular-material';
import 'font-awesome/css/font-awesome.min.css';
require('angular-ui-router/release/angular-ui-router'); // AFAIK ui-router doesn't support the import syntax with typescript

/**
 * Importing external stylesheets
 */
import 'angular-material/angular-material.css';
import {palette} from './config/material/palette' // angular material custom palette
/**
 * Importing internal components
 */
import {config} from './config/core/coreConfig'
import {run} from './config/core/coreRun'
import './modules/main/index';
import './modules/home/index';
import './modules/swagger/index';
import './modules/queues/index';
import './modules/errors/index';
import './modules/crowdsourcing/index';
import './modules/correction/index';
import './api/index';

/**
 * The main app module.
 */

module app {
    angular
        .module('app', [
            "ui.router",
            "ngMaterial",
            "app.main",
            "app.swagger",
            "app.queues",
            "app.errors",
            "app.crowdsourcing",
            "app.correction",
            "app.home"
        ])
        .config(config)
        .config(palette)
        .run(run);
}