import './share-button.styl';
import './share-button.resp.styl';
import './modal-template/share-modal.styl';
import {CustomRootScope} from "../../config/core/coreRun";

interface ShareButtonComponentScope extends ng.IScope
{
    Share: any // must match controllerAs
}

export class ShareButtonComponent implements ng.IComponentOptions {

    public template:string = <string> require('./share-button.html');
    public restrict:string = "E";
    public bindings:Object = {
    };
    public controllerAs:string = 'Share';

    public controller:Function = ($scope: ShareButtonComponentScope, $rootScope: CustomRootScope, $mdDialog: angular.material.IDialogService) :void => {
        'ngInject';
        var ctrl = $scope.Share;
        ctrl.og = $rootScope.og;
        
        ctrl.displayShareModal = (ev: MouseEvent) => {
            console.log(ctrl.og);
            let dialogOptions : angular.material.IDialogOptions = {
                controller: ($scope) => {
                    'ngInject';
                    $scope.og = ctrl.og;
                },
                template: <string>require('./modal-template/share-modal.html'),
                parent: angular.element(document.body),
                targetEvent: ev,
                clickOutsideToClose:true
            };
            $mdDialog.show(dialogOptions);
        }
    };
}