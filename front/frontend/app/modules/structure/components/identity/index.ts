import {IdentityComponent} from './identity'
import {MapComponent} from "./map/map";
import {RelativeComponent} from "./relative/relative";

angular.module('app.structure.components.identity', [])
    .component('map', new MapComponent())
    .component('relative', new RelativeComponent())
    .component('identity', new IdentityComponent());