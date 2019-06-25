export class CorrectionController {
    private correction: api.FullStructureCorrection;
    private structure: any;
    private twitterCorrection: boolean;
    private facebookCorrection: boolean;
    private detectedPublications: Array<any>;
    private selectedPublication: any;
    private addPublication: Function;
    private searchPublication: string;
    private addProject: Function;
    private selectedProject: any;
    private searchProject: string;
    private save: Function;
    private querying: boolean;

    constructor(CorrectionAdminApi: api.CorrectionAdminApi, correction: api.FullStructureCorrection, structure:any) {
        "ngInject";
        this.correction = correction;
        this.querying = false;
        if (correction.twitterAccounts) {
            this.twitterCorrection = true;
        } else {
            correction.twitterAccounts = []
        }
        if (correction.facebookAccounts) {
            this.facebookCorrection = true;
        } else {
            correction.facebookAccounts = []
        }
        this.searchProject = "";
        this.searchPublication = "";
        correction.removedPublications = correction.removedPublications || [];
        correction.removedProjects = correction.removedProjects || [];
        this.detectedPublications = (structure.detectedPublicationList || []).concat(structure.detectedPatentList || [])
        this.structure = structure;

        var self = this;
        this.addPublication = function() {
            if (self.selectedPublication == null) {
                return;
            }
            self.correction.removedPublications.push(self.selectedPublication.id);
            self.selectedPublication = undefined;
            self.searchPublication = '';
        };
        this.addProject = function() {
            if (self.selectedProject == null) {
                return;
            }
            self.correction.removedProjects.push(self.selectedProject.id);
            self.selectedProject = undefined;
            self.searchProject = '';
        };
        this.save = function() {
            var correction = angular.copy(self.correction);
            if (!self.twitterCorrection) {
                correction.twitterAccounts = null;
            }
            if (!self.facebookCorrection) {
                correction.facebookAccounts = null;
            }
            self.querying = true;
            CorrectionAdminApi.saveCorrection(correction).then(function (res) {
                self.querying = false;
            });
        }
    }
}
