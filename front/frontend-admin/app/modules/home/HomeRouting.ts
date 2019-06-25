/**
 * ui-router homepage state
 * @param $stateProvider
 */

export function config($stateProvider: ng.ui.IStateProvider): void {

    'ngInject'; //needed when directly exporting a class or function

    $stateProvider.state('main.home', {
        url: '/home',
        views: {
            "content": {
                template: require('./home.html'),
                controller: 'HomeController',
                controllerAs: 'Home'
            }
        },
        resolve: {
            "stats": (CountApi: api.CountApi) => {
                "ngInject";
                return CountApi.get().then(d => d.data);
            }
        }
    });
}