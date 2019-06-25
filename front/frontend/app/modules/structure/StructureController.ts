import {FullStructure} from "../../models/FullStructure";
import {StructureService} from "../../services/StructureService";
import {CustomRootScope} from "../../config/core/coreRun";
/**
 * The structure controller
 */
export class StructureController {

    public structure: FullStructure;
    public keywords: any[];
    public surroundings: any[];

    constructor($rootScope: CustomRootScope, $window:ng.IWindowService, structure:ng.IHttpPromiseCallbackArg<FullStructure>, surroundings:ng.IHttpPromiseCallbackArg<any>, StructureService:StructureService, $stateParams:ng.ui.IStateParamsService) {
        "ngInject";

        $window.scrollTo(0,0); // scroll to top, useful when coming from the search

        var ctrl = this;
        ctrl.structure = structure.data;
        var fullStructure = new FullStructure(structure.data.id,structure.data.structure,structure.data.children,structure.data.parents,structure.data.websites,structure.data.projects);
        ctrl.surroundings = surroundings.data;

        // Put "struct name - struct id / idnumbers - Re-Search Alps" in the page title
        $rootScope.pageTitle = `${((ctrl.structure.structure.acronyms || [])[0] || ctrl.structure.structure.label)}
        - ${fullStructure.isRNSR() ? fullStructure.getIdNumbers().join(', ') : ctrl.structure.id}
        - Re-Search Alps`;

        $rootScope.og.title = "Re-Search Alps : details for " + ((ctrl.structure.structure.acronyms||[])[0] || ctrl.structure.structure.label);
        StructureService.getKeywords($stateParams['id']).then((keywords: any) => {
            ctrl.keywords = keywords.data;
        });
    }
}