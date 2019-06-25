// Component stylesheet
import './financial.styl';
import './financial.resp.styl';

interface FinancialComponentScope extends ng.IScope
{
    Financial: ctrlScope, // must match controllerAs
}

interface ctrlScope
{
    rightPayroll: any[];
    leftPayroll: any[];
    financial: any;
}

export class FinancialComponent implements ng.IComponentOptions {

    public template:any = require('./financial.html');
    public restrict:string = "E";
    public bindings:Object = {
        financial: '<',
        allowEditing: '=',
        structure: '<'
    };
    public controllerAs:string = 'Financial';

    public controller:Function = ($scope:FinancialComponentScope):void => {
        'ngInject';
        var ctrl = $scope.Financial;

        if(ctrl.financial && ctrl.financial.researchersPayroll && ctrl.financial.researchersPayroll.length > 0){
            let buffer = angular.copy(ctrl.financial.researchersPayroll);
            ctrl.leftPayroll = [];
            ctrl.rightPayroll = [];
            ctrl.leftPayroll = buffer.splice(0,Math.ceil(buffer.length / 2));
            ctrl.rightPayroll = buffer;
        }
    }
}