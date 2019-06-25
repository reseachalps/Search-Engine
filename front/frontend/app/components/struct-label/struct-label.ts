/**
 * Display the structure label with its acronym in () if it exists
 */

interface StructLabelComponentScope extends ng.IScope
{
    Struct: any // must match controllerAs
}

export class StructLabelComponent implements ng.IComponentOptions {

    public template:any = '{{Struct.struct.label}} <span ng-if="Struct.struct.acronyms.length && Struct.struct.acronyms[0]">({{Struct.struct.acronyms | join:", "}})</span>';
    public restrict:string = "E";
    public bindings:Object = {
        struct: '='
    };
    public controllerAs:string = 'Struct';

    public controller:Function = ($scope: StructLabelComponentScope) :void => {
        'ngInject';
        var ctrl = $scope.Struct;
        // dirty in order to support old version of the index structure
        if ($scope.Struct.struct.acronym) {
            $scope.Struct.struct.acronyms = $scope.Struct.struct.acronym;
        }
    };
}