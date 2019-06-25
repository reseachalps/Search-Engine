import {StructureService} from "../../services/StructureService";
import {SearchService} from "../../services/SearchService";
/**
 * ui-router search state
 * @param $stateProvider
 */

export function routing($stateProvider: ng.ui.IStateProvider): void {

    'ngInject'; //needed when directly exporting a class or function

    $stateProvider.state('structure', {
        url: '/structure/:id',
        views: {
            "@": {
                template: <string> require('./structure.html'), // IState only accepts an explicit string for the template (require return type isn't specified)
                controller: 'StructureController',
                controllerAs: 'Structure'
            }
        },
        resolve: {
            nomenclature : (SearchService:SearchService) => {
                "ngInject"; // we need to resolve this so it's accessible in the Identity component
                return SearchService.getNomenclature();
            },
            structure: ($stateParams: ng.ui.IStateParamsService, StructureService: StructureService) => {
                return StructureService.getStructure($stateParams['id']);
            },
            surroundings : ($stateParams: ng.ui.IStateParamsService, StructureService: StructureService) => {
                return StructureService.getSurroundings($stateParams['id']);
            }
        }
    });
}