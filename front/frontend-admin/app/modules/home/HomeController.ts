import CountReport = api.CountReport;

export class HomeController {
    public stats: CountReport;

    constructor(stats: CountReport){
        "ngInject";
        this.stats = stats;
    }
}