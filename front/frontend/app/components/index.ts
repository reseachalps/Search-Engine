import {StructLabelComponent} from "./struct-label/struct-label";
import {FooterComponent} from "./footer/footer";
import {ShareButtonComponent} from "./share-button/shareButton";
import './xiti';
/**
 Global components for the app
 **/

angular.module('app.components', ['app.components.xiti'])
    .component('structLabel', new StructLabelComponent())
    .component('shareButton', new ShareButtonComponent())
    .component('footer', new FooterComponent());