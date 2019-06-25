/**
 * Display the structure label with its acronym in () if it exists
 */

import './footer.styl';
import "./footer.resp.styl";
import {CustomRootScope} from "../../config/core/coreRun";

interface FooterComponentScope extends ng.IScope
{
    Footer: any // must match controllerAs
}

export class FooterComponent implements ng.IComponentOptions {

    public template:string = <string> require('./footer.html');
    public restrict:string = "E";
    public bindings:Object = {
        struct: '<'
    };
    public controllerAs:string = 'Footer';

    public controller:Function = ($scope: FooterComponentScope, $rootScope: CustomRootScope) :void => {
        'ngInject';
        var ctrl = $scope.Footer;
        ctrl.sc  = $rootScope.sc;
    };
}