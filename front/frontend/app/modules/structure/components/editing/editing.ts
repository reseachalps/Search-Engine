// Component stylesheet
import './editing.styl';
import './editing.resp.styl';
import {directive} from "../../../../decorators/directive";
import IDialogService = angular.material.IDialogService;
import IMedia = angular.material.IMedia;
import {StructureService} from "../../../../services/StructureService";

interface EditingComponentScope extends ng.IScope
{
    displayEdit: boolean,
    status: string,
    editingId: string,
    editingContent: any,
    editingLabel: any,
    editingRow: boolean,
    showAlert: Function,
    structure: any
}

@directive('$mdDialog', '$mdMedia', 'StructureService', '$timeout')
export class EditingDirective implements ng.IDirective{

    public scope:any;
    public link:any;
    public transclude:boolean;
    public restrict:string;
    public template:string;

    constructor(private $mdDialog:IDialogService, private $mdMedia:IMedia, private StructureService:StructureService, private $timeout:ng.ITimeoutService) {
        this.restrict = 'A';
        this.transclude = true;
        this.scope = {
            editing: '=',
            editingId: '@',
            editingContent: '<',
            editingRow: '@',
            editingLabel: '@',
            structure: '<'
        };
        this.template = `
        <div ng-show="displayEdit" class="edit-panel {{editingRow}}" ng-click="showAlert($event)"></div>
        <div ng-transclude></div>
        `;
        this.link = (scope:EditingComponentScope):void => {
            scope.$watch('editing', (n:boolean, o:boolean) => {
                if(angular.isDefined(n)){
                    scope.displayEdit = n;
                }
            });

            scope.showAlert = (ev) => {
                $mdDialog.show({
                        controller: ($scope, id, content, label) => {
                            'ngInject';
                            
                            $scope.id = id;
                            $scope.content = content;
                            $scope.structure = scope.structure;
                            $scope.label = label;
                            $scope.loading = false;
                            $scope.hide = function() {
                                $mdDialog.cancel();
                            };
                            $scope.cancel = function() {
                                $mdDialog.cancel();
                            };
                            $scope.sendSuggestion = function() {
                                $scope.loading = true;
                                $scope.suggestionSent = true;
                                StructureService.newFeedback(grecaptcha.getResponse($scope.recaptchaId),scope.structure.id,$scope.userName, $scope.email, $scope.id, $scope.demand).then(() => {
                                    $scope.status = {code:'ok', message:'Your suggestion as been transmitted'};
                                    $scope.loading = false;
                                    $timeout(() => {
                                        $mdDialog.hide();
                                    }, 4000);
                                }, (error) => {
                                    $scope.suggestionSent = false;
                                    $scope.loading = false;
                                    $scope.status = {code:'error', message:'An error occured, please retry or contact the technical staff'};
                                    console.error(error);    
                                });
                            };
                            
                            // Fix recaptacha bug when submitting crowd requests multiple times https://gedgei.wordpress.com/2015/08/07/using-recaptcha-in-single-page-applications/
                            $scope.setRecaptchaId = function(widgetId) {
                                $scope.recaptchaId = widgetId;
                            };
                        },
                        template: <string> require('./templates/simple-text.html'),
                        parent: angular.element(document.body),
                        targetEvent: ev,
                        clickOutsideToClose:true,
                        locals: {
                            id: scope.editingId,
                            content: scope.editingContent,
                            label: scope.editingLabel
                        },
                    })
                    .then(function() {
                    }, function() {
                        // user canceled
                    });
            }
        }
    }
}