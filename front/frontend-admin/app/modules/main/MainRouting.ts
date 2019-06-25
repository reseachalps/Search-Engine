/**
 * ui-router homepage state
 * @param $stateProvider
 */

export function config($stateProvider: ng.ui.IStateProvider): void {

    'ngInject'; //needed when directly exporting a class or function

    $stateProvider.state('main', {
        abstract: true,
        views: {
            "@": {
                template: require('./main.html'),
                controller: "MainController",
                controllerAs: "Main"
            }
        }
    });
}