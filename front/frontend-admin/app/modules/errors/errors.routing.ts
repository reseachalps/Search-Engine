export function config($stateProvider:ng.ui.IStateProvider) {
    "ngInject";
    $stateProvider.state('main.errors', {
            url: '/errors',
            views: {
                'content': {
                    template: require('./errors.html'),
                    controller: 'ErrorsController',
                    controllerAs: 'Errors'
                }
            }
        })
        .state("main.errors.queue", {
            url: "/:queue",
            views: {
                "content@main": {
                    template: require('./errors.html'),
                    controller: 'ErrorsController',
                    controllerAs: 'Errors'
                }
            }
        });
}
