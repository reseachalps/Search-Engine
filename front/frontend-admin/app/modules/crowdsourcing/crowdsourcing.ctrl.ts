export class CrowdsourcingController {
    public feedbacks: any[];
    public selectedStatus: string;
    public changeStatus: Function;
    public validate: Function;

    constructor($location: ng.ILocationService, CrowdsourcingApi: api.CrowdsourcingApi,feedbacks) {
        "ngInject";
        var ctrl = this;
        ctrl.feedbacks = feedbacks.data.content;
        console.log($location.search()['active']);
        ctrl.selectedStatus = $location.search()['active'] === true ? 'SUBMITTED' : 'ALL';

        ctrl.changeStatus = () => {
            $location.search('active',(ctrl.selectedStatus === 'ALL' ? false : true));
        };

        ctrl.validate = (feedback) => {
            CrowdsourcingApi.validateFeedback(feedback.id).then(() => {
                feedback.status = 'VALIDATED';
            });
        };
    }
}
