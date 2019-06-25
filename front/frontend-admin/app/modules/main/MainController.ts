/**
 * The homepage controller for the app. The controller:
 * - display a <hello world> message
 */
export class MainController {
    public $state: ng.ui.IState;

    constructor($state: ng.ui.IState){
        "ngInject";
        this.$state = $state;
    }
}