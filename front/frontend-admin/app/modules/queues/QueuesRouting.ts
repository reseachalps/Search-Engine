export function config($stateProvider:ng.ui.IStateProvider):void {

    'ngInject'; //needed when directly exporting a class or function

    $stateProvider.state("main.queues", {
        url: "/queues",
        views: {
            "content": {
                template: require("./queues.html"),
                controller: "QueuesController",
                controllerAs: "Queues"
            }
        }
    });
}