// Component stylesheet
import './sources.styl';

interface SourcesComponentScope extends ng.IScope
{
    Sources: ctrlScope, // must match controllerAs
}

interface ctrlScope
{
    sources: any[];
    identifiers: any[];
}

export class SourcesComponent implements ng.IComponentOptions {

    public template:any = require('./sources.html');
    public restrict:string = "E";
    public bindings:Object = {
        sources: '<',
        identifiers: '<'
    };
    public controllerAs:string = 'Sources';

    public controller:Function = ($scope: SourcesComponentScope) :void => {
        'ngInject';
        var ctrl = $scope.Sources;
    };
}