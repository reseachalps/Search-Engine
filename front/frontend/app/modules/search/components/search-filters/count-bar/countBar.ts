import './count-bar.styl';

interface CountBarComponentScope extends ng.IScope
{
    Bar: any // must match controllerAs
}

export class CountBarComponent implements ng.IComponentOptions {

    public template = <string>require('./count-bar.html');
    public restrict:string = "E";
    public bindings:Object = {
        'maxValue': '<',
        'count': '<'
    };
    public controllerAs:string = 'Bar';

    public controller:Function = ($scope: CountBarComponentScope) :void => {
        'ngInject';

        var ctrl = $scope.Bar;
    };
}