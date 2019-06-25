import {FirmographyComponent} from "./firmography";
import {LeafletMapComponent} from "./leaflet-map/leafletMap";



//d3
import 'd3';
import 'nvd3';
import 'nvd3/build/nv.d3.css';
import 'angular-nvd3';

angular.module('app.search.components.results.firmo', ['nvd3'])
    .component('firmography', new FirmographyComponent())
    .component('leafletMap', new LeafletMapComponent());