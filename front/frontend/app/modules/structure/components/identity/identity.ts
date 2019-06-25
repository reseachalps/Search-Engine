// Component stylesheet
import './identity.styl';
import './identity.resp.styl';
import {FullStructure} from "../../../../models/FullStructure";
import {SearchService} from "../../../../services/SearchService";

interface IdentityComponentScope extends ng.IScope
{
    Identity: ctrlScope // must match controllerAs
}

interface ctrlScope
{
    children:any[];
    rightParents:any[];
    leftParents:any[];
    keywordsLoading:boolean;
    getMonitoringLabel:Function;
    keywords:any[];
    structureLogo:string;
    mainWebsite:any;
    tagCloudWords:any[];
    structure: FullStructure,
    idNumbers: any,
    isRNSR: boolean,
    getEmailIcon: Function,
    groupedActivities: Object
}

export class IdentityComponent implements ng.IComponentOptions {

    public bindings:Object;
    public controller: Function;
    public template:string;
    public controllerAs:string;
    private SearchService: SearchService;

    constructor() {
        this.bindings = {
            structure: '<',
            keywords: '<',
            allowEditing: '=',
            surroundings: '='
        };
        this.controllerAs = 'Identity';
        this.template = <string> require('./identity.html');
        this.controller = ($scope: IdentityComponentScope, SearchService: SearchService, $filter) :void => {
            'ngInject';
            let ctrl: ctrlScope = $scope.Identity;
            this.SearchService = SearchService;

            let str = new FullStructure(ctrl.structure.id,ctrl.structure.structure,ctrl.structure.children,ctrl.structure.parents,ctrl.structure.websites,ctrl.structure.projects);
            ctrl.tagCloudWords = [];
            ctrl.keywordsLoading = true;
            $scope.$watch('Identity.keywords', (n: any) => {
                if(n) {
                    n.forEach((keyword:any) => {
                        ctrl.tagCloudWords.push({key: keyword.keyword, count: keyword.score, label: keyword.keyword});
                    });
                    ctrl.keywordsLoading = false;
                }
            });

            ctrl.idNumbers = str.getIdNumbers();
            ctrl.isRNSR = str.isRNSR();

            ctrl.groupedActivities = this.groupActivities(str.structure.activities);

            ctrl.structureLogo = str.structure.logo;
            if(str.structure.links && str.websites){
                let mainWebsite = str.structure.links.filter((link:any) => link.type === "main");
                if (mainWebsite.length === 0) {
                    mainWebsite = str.structure.links;
                }
                if(mainWebsite.length > 0) {
                    ctrl.mainWebsite = str.websites.filter((website:any) => website.id === mainWebsite[0].id)[0];

                    if(angular.isDefined(ctrl.mainWebsite)) {
                        if(!ctrl.structureLogo) {
                            if (ctrl.mainWebsite.twitter.length > 0) {
                                ctrl.structureLogo = ctrl.mainWebsite.twitter[0].profilePictureUrl;
                            }
                        }
                    }
                }
            }

            if(str.structure.relations) {
                str.structure.relations.forEach((relation:any) => {
                    relation.typeLabel = SearchService.getLabelForFilter("RELATION",relation.type);
                });
            }

            if(str.parents && str.parents.length > 0){
                let buffer = angular.copy(str.parents);
                ctrl.leftParents = [];
                ctrl.rightParents = [];
                ctrl.leftParents = buffer.splice(0,Math.ceil(buffer.length / 2));
                ctrl.rightParents = buffer;
            }

            if(str.children && str.children.length > 0){
                ctrl.children = [];
                let children = $filter("localeCompareString")(angular.copy(str.children), "label");
                const chunk = 2;
                for (let i=0, j=children.length; i<j; i+=chunk) {
                    ctrl.children.push(children.slice(i,i+chunk));
                }
            }

            let emailIconsDic = {
                'main': 'home',
                'repository': 'book',
                'team': 'group',
                'personal': 'user',
                'wikipedia': 'wikipedia-w',
                'hceres': 'external-link'
            };

            ctrl.getEmailIcon = (type) => {
                return emailIconsDic[type] || 'external-link';
            };

            let monitDisc = {
                'ganalytics': 'Google Analytics',
                'xiti': 'Xiti'
            };

            ctrl.getMonitoringLabel = (monit) => {
                return monitDisc[monit] || monit;
            };
        };
    }

    private groupActivities(activities:Array<any>):Object {
        let res = {};
        angular.forEach(activities, (activity: any) => {
            let group = activity.activityType ? this.SearchService.getLabelForFilter('ACTIVITY', activity.activityType) : 'Others';
            if(angular.isUndefined(res[group])){
                res[group] = [activity];
            } else {
                res[group].push(activity);
            }
        });
        if (Object.keys({}).length == 0)
            res = null;
        return res;
    }
}