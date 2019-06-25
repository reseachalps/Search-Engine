export function config($stateProvider: ng.ui.IStateProvider): void {

    'ngInject'; //needed when directly exporting a class or function

    $stateProvider.state('main.swagger', {
        url: '/swagger',
        views: {
            "content": {
                template: require('./swagger.html')
            }
        }
    });
}