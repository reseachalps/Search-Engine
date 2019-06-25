import {IdentityComponent} from "./identity";
import './index.ts';
import "babel-polyfill"; // as the IdentityComponent is using Sets
import {FullStructure} from "../../../../models/FullStructure";
import {StructureDetails} from "../../../../models/StructureDetails";
import {SearchService} from "../../../../services/SearchService";

describe("IdentityComponent", () => {

    var scope : ng.IRootScopeService;
    var $componentController : any;
    var structure: FullStructure = new FullStructure();
    var identityComponent: any;
    var $httpBackend: ng.IHttpBackendService;
    var SearchService: SearchService;

    beforeEach(() => {
        angular.mock.module('app.structure.components.identity');
        angular.mock.module('app.services');
    });

    beforeEach(() =>
        angular.mock.inject((_$rootScope_: ng.IRootScopeService, _$componentController_, _$httpBackend_:ng.IHttpBackendService, _SearchService_: SearchService) => {
            scope = _$rootScope_.$new();
            $componentController = _$componentController_;
            SearchService = _SearchService_;
            $httpBackend = _$httpBackend_;
        })
    );

    beforeEach(() => {
        $httpBackend.expect('GET', "api/nomenclatures");
        $httpBackend.whenGET("api/nomenclatures").respond({data:{
            ACTIVITY: {
                DOMAINE: "Domaine scientifique",
                ERC: "Discipline ERC",
                NAF: "NAF",
                THEME: "Theme"
            }
        }});
    });

    afterEach(() => {
        $httpBackend.flush();
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    describe("Public / Private", () => {
        it('should detect if the entity is a public structure or a company', () => {
            structure= new FullStructure();
            structure.structure = new StructureDetails();
            structure.structure.kind = "RNSR";
            identityComponent = $componentController('identity', {$scope: scope, SearchService: SearchService}, {structure: structure, keywords:[]});
            expect(identityComponent.isRNSR).toBe(true);
            structure= new FullStructure();
            structure.structure = new StructureDetails();
            structure.structure.kind = "COMPANY";
            identityComponent = $componentController('identity', {$scope: scope, SearchService: SearchService}, {structure: structure, keywords:[]});
            expect(identityComponent.isRNSR).toBe(false);
        });
    });

    describe("Identification numbers", () => {

        it('should get the idenfication numbers', function () {
            structure= new FullStructure();
            structure.structure = new StructureDetails();
            structure.structure.institutions = [
                {
                    "code": {
                        "normalized": "UMR 7326"
                    }
                },
                {
                    "code": {
                        "normalized": "UMR 7326"
                    }
                },
                {
                    "code": {
                        "normalized": "UMR 7265"
                    }
                },
                {
                    "code": {
                        "normalized": "Dept"
                    }
                },
                {
                    "code": {
                        "normalized": ""
                    }
                },
                {
                    "code": {
                        "normalized": "32"
                    }
                }
            ];
            identityComponent = $componentController('identity', {$scope: scope, SearchService: SearchService}, {structure: structure, keywords:[]});
            expect(identityComponent.idNumbers).toEqual(["UMR 7326", "UMR 7265", "Dept", "32"]);
        });

        it('should return an empty array if institutions is null', function () {
            structure= new FullStructure();
            structure.structure = new StructureDetails();
            structure.structure.institutions = null;
            identityComponent = $componentController('identity', {$scope: scope, SearchService: SearchService}, {structure: structure, keywords:[]});
            expect(identityComponent.idNumbers).toEqual([]);
        });
    });

    describe("Activités", () => {
       it('should group the activites by type', () => {
           structure= new FullStructure();
           structure.structure = new StructureDetails();
           structure.structure.activities = [
                   {
                       "code": null,
                       "activityType": "THEME",
                       "label": "Paléoclimat et magnétisme environnemental\tChamp magnétique et archéologie",
                       "secondary": null
                   },
                   {
                       "code": "PE9",
                       "activityType": "ERC",
                       "label": "Universe sciences : astro-physics/chemistry/biology; solar system; stellar, galactic and extragalactic astronomy, planetary systems, cosmology; space science, instrumentation",
                       "secondary": false
                   },
                   {
                       "code": "PE10",
                       "activityType": "ERC",
                       "label": "Earth system science : physical geography, geology, geophysics, meteorology, oceanography, climatology, ecology, global environmental change, biogeochemical cycles, natural resources management",
                       "secondary": false
                   },
                   {
                       "code": "3",
                       "activityType": "DOMAINE",
                       "label": "Sciences de la terre et de l'univers, espace",
                       "secondary": true
                   },
                   {
                       "code": "AXE",
                       "activityType": null,
                       "label": "Truc",
                       "secondary": true
                   }
               ];
           identityComponent = $componentController('identity', {$scope: scope, SearchService: SearchService}, {structure: structure, keywords:[]});
           /*expect(identityComponent.groupedActivities).toEqual({
               "Theme" : [{
                   "code": null,
                   "activityType": "THEME",
                   "label": "Paléoclimat et magnétisme environnemental\tChamp magnétique et archéologie",
                   "secondary": null
               }],
               'Discipline ERC': [{
                   "code": "PE9",
                   "activityType": "ERC",
                   "label": "Universe sciences : astro-physics/chemistry/biology; solar system; stellar, galactic and extragalactic astronomy, planetary systems, cosmology; space science, instrumentation",
                   "secondary": false
               },
               {
                   "code": "PE10",
                   "activityType": "ERC",
                   "label": "Earth system science : physical geography, geology, geophysics, meteorology, oceanography, climatology, ecology, global environmental change, biogeochemical cycles, natural resources management",
                   "secondary": false
               }],
               'Domaine scientifique': [{
                   "code": "3",
                   "activityType": "DOMAINE",
                   "label": "Sciences de la terre et de l'univers, espace",
                   "secondary": true
               }],
               'Autres': [{
                   "code": "AXE",
                   "activityType": null,
                   "label": "Truc",
                   "secondary": true
               }]
           });*/
       });
    });
});
