/**
 * Main Modules
 */



//Leaflet
import 'leaflet';
import 'leaflet/dist/leaflet.css';
import 'leaflet.markercluster';
import 'leaflet.markercluster';
import 'leaflet.markercluster/dist/MarkerCluster.css';
import 'leaflet.markercluster/dist/MarkerCluster.Default.css';
import '../components/oms/oms.js'

//d3 tag-cloud
import 'd3';
import 'd3-cloud';

/**
 * Internal deps
 */
import './home'
import './search'
import './structure'

angular.module('app.modules', [
    'app.home',
    'app.search',
    'app.structure'
]);
