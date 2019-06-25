export function config($stateProvider:ng.ui.IStateProvider) {
    "ngInject";
    $stateProvider.state('main.correction', {
        url: '/correction/:id',
        views: {
            'content': {
                template: require('./correction.html'),
                controller: 'CorrectionController',
                controllerAs: 'Correction',
                resolve: {
                    'correction': (CorrectionAdminApi:api.CorrectionAdminApi, $stateParams:any) => {
                        "ngInject";
                        return CorrectionAdminApi.getCorrection($stateParams["id"]).then(f => f.data);
                    },
                    'structure': (CorrectionAdminApi:api.CorrectionAdminApi, $stateParams:any) => {
                        "ngInject";
                        return CorrectionAdminApi.getStructure($stateParams["id"]).then(f => f.data);
                    }
                }
            }
        }
    })
}
