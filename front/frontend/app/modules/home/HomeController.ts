import {CustomRootScope} from "../../config/core/coreRun";
import {DatabaseStats} from "../../models/DatabaseStats";
/**
 * The homepage controller for the app.
 */
export class HomeController {

    public stats: DatabaseStats;
    constructor($rootScope: CustomRootScope, $scope: any, stats: ng.IHttpPromiseCallbackArg<DatabaseStats>){
        "ngInject";
        var ctrl = this;
        $rootScope.pageTitle = 'Re-search Alps';
        $rootScope.og.title = "Discover Re-Search Alps, the engine of research and innovation (beta)";
        ctrl.stats = stats.data;
    }
}