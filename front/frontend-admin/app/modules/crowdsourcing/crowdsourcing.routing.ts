export function config($stateProvider:ng.ui.IStateProvider) {
    "ngInject";
    $stateProvider.state('main.crowdsourcing', {
            url: '/crowdsourcing?active',
            views: {
                'content': {
                    template: require('./crowdsourcing.html'),
                    controller: 'CrowdsourcingController',
                    controllerAs: 'Crowd',
                    resolve: {
                        'feedbacks' : (CrowdsourcingApi: api.CrowdsourcingApi, $location:ng.ILocationService) => {
                            "ngInject";
                            return CrowdsourcingApi.getFeedbacks($location.search()['active'] || false);
                        }
                    }
                }
            }
        })
}
