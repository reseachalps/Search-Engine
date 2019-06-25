// Component stylesheet
import './financial-private.styl';
import './financial-private.resp.styl';

interface FinancialPrivateComponentScope extends ng.IScope
{
    Financial: ctrlScope, // must match controllerAs
}

interface ctrlScope
{
    financial: any[]
}

export class FinancialPrivateComponent implements ng.IComponentOptions {

    public template:any = require('./financial-private.html');
    public restrict:string = "E";
    public bindings:Object = {
        financial: '<',
        allowEditing: '='
    };
    public controllerAs:string = 'Financial';

    public controller:Function = ($scope:FinancialPrivateComponentScope):void => {
        'ngInject';
        var ctrl = $scope.Financial;
    }
}