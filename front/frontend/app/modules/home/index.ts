/**
 * Homepage Module
 */
import './home-carousel'
import './home-stats'

/**
 * Internal deps
 */
import {config as HomeRouting} from './HomeRouting'
import {HomeController} from "./HomeController";

/**
 * Stylesheet
 */
import "./home.styl";
import "./home.resp.styl";

angular.module('app.home', ['ui.router', 'app.home.homeCarousel', 'app.home.homeStats'])
    .config(HomeRouting)
    .controller('HomeController', HomeController);
