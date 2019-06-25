/**
 * ui-router home state
 * @param $stateProvider
 */
import {StatsService} from "../../services/StatsService";


export function config($stateProvider: ng.ui.IStateProvider): void {

    'ngInject'; //needed when directly exporting a class or function

    $stateProvider.state('home', {
        url: '/',
        views: {
            "@": {
                template: <string>require('./home.html'), // IState only accepts an explicit string for the template (require return type isn't specified)
                controller: 'HomeController',
                controllerAs: 'Home'
            }
        },
        resolve: {
            stats : (StatsService: StatsService) => {
                "ngInject";
                // return {fullStructures: 96153};
                return StatsService.getStats();
            }
        }
    });
}