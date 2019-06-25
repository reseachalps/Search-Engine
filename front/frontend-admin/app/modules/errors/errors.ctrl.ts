export class ErrorsController {
    queue;
    loading;
    messages;
    refresh;
    errorsCount;
    errors;
    queues;
    setQueue;
    dismiss;
    recover;
    dismissQueue;
    recoverQueue;
    execute;

    constructor($timeout, $state, $stateParams, CoreStatusApi:api.CoreStatusApi, ErrorServiceApi:api.ErrorServiceApi) {
        "ngInject";
        var ctrl = this;

        ctrl.queue = $stateParams.queue || "All";
        ctrl.messages = {};
        ctrl.loading = true;
        ctrl.refresh = function () {
            $timeout(function () {
                ctrl.messages = {};
            }, 3000);

            CoreStatusApi.status().then(function (data) {
                let errors = data.data.errors;
                ctrl.errorsCount = {
                    'All': 0
                };
                for (var queue in errors) {
                    if (errors[queue] > 0) {
                        ctrl.errorsCount[queue] = errors[queue];
                        ctrl.errorsCount["All"] += errors[queue];
                    }
                }
            });
            ErrorServiceApi.getErrors({
                queues: ctrl.queue !== 'All' ? [ctrl.queue] : null
            }).then(function (data) {
                ctrl.errors = data.data.content;
                ctrl.queues = {};
                for (var i = 0; i < ctrl.errors.length; i++) {
                    ctrl.queues[ctrl.errors[i].queue] = 1;
                    try {
                        ctrl.errors[i].message = JSON.parse(ctrl.errors[i].message);
                    } catch(err) {
                        //ctrl.errors[i].message = ctrl.errors[i].message
                    }
                }
                ctrl.loading = false;
            });
        };
        var error = function (data, status) {
            $timeout(function () {
                ctrl.messages = {};
            }, 3000);
            ctrl.messages.error = "Could not call API (status: " + status + ")";
        };
        ctrl.refresh();
        ctrl.setQueue = function (q) {
            $state.go("main.errors.queue", {queue: q === 'All' ? "" : q});
        };

        ctrl.dismiss = function (id) {
            ctrl.execute("ignore", {id: id});
        };

        ctrl.recover = function (id) {
            ctrl.execute("recover", {id: id});
        };

        ctrl.dismissQueue = function () {
            var select = ctrl.queue ? {queues: [ctrl.queue]} : {};
            ctrl.execute("ignore", select);
        };

        ctrl.recoverQueue = function () {
            var select = ctrl.queue ? {queues: [ctrl.queue]} : {};
            ctrl.execute("recover", select);
        };

        ctrl.execute = function (operation, selector) {
            var future;
            if (operation == "ignore") {
                future = ErrorServiceApi.dismiss(selector);
            } else {
                future = ErrorServiceApi.recover(selector);
            }
            future.then(function (data) {
                ctrl.messages.error = (operation === "ignore" ? "Error deletion done" : "Recovering done");
                ctrl.refresh();
            }, error);
        };
    };
}
