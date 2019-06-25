import {ActiveFilterComponent} from "./activeFilter";
import {Filter} from "../../../../../../models/Filter";
import '../../../../../../services'
import {FiltersService} from "../../../../../../services/FiltersService";
import './../index.ts';

describe("ActiveFilter", () => {

    var scope : ng.IRootScopeService;
    var $location : ng.ILocationService;
    var $httpBackend : ng.IHttpBackendService;
    var $componentController : any;
    var FiltersService : FiltersService;
    var activeFilterComponent: any;
    var filter: Filter;

    beforeEach(() => {
        angular.mock.module('app.services');
        angular.mock.module('app.search.components.filters.active');
    });

    beforeEach(() => {
        angular.mock.inject(function (_$rootScope_:ng.IRootScopeService, _$componentController_, _$httpBackend_:ng.IHttpBackendService, _$location_:ng.ILocationService, _FiltersService_:FiltersService) {
            scope = _$rootScope_.$new();
            $componentController = _$componentController_;
            FiltersService = _FiltersService_;
            $location = _$location_;
            $httpBackend = _$httpBackend_;
        })
    });

    beforeEach(() => {
        $httpBackend.expect('GET', "api/nomenclatures");
        $httpBackend.whenGET("api/nomenclatures").respond({data:{
            INSTITUTIONS: {
                '0753639Y': "CNRS",
                '0780491K': "INRIA",
                '0383493R': "Université Grenoble Alpes"
            },
            DOMAINE: {
                2: "Physique"
            },
            ERC: {
                SH5: "Cultures and cultural production : literature, visual and performing arts, music, cultural and comparative studies"
            }
        }});
    });

    afterEach(() => {
        $httpBackend.flush();
        $httpBackend.verifyNoOutstandingExpectation();
        $httpBackend.verifyNoOutstandingRequest();
    });

    it('should get the right label for the public structures', () => {
        filter = new Filter('all', ['true'], "publicEntity");
        activeFilterComponent = $componentController('activeFilter', {$scope: scope, $location:$location, FiltersService: FiltersService}, {filter: filter});
        expect(activeFilterComponent.filterLabel).toBe('public organizations');
    });

    //it('should get the right label for the institutions', () => {
    //    filter = new Filter('any', ["0753639Y", "0780491K", "0383493R"], "institutions");
    //    activeFilterComponent = $componentController('activeFilter', {$scope: scope, $location:$location, FiltersService: FiltersService}, {filter: filter});
    //    expect(activeFilterComponent.filterLabel).toBe('Tutelles : CNRS, INRIA ou Université Grenoble Alpes');
    //});
    //
    //it('should get the right label for the domain', () => {
    //    filter = new Filter('any', ["2"], "domaine");
    //    activeFilterComponent = $componentController('activeFilter', {$scope: scope, $location:$location, FiltersService: FiltersService}, {filter: filter});
    //    expect(activeFilterComponent.filterLabel).toBe('Domaine : Physique');
    //});
    //
    //it('should get the right label for the erc', () => {
    //    filter = new Filter('any', ["SH5"], "erc");
    //    activeFilterComponent = $componentController('activeFilter', {$scope: scope, $location:$location, FiltersService: FiltersService}, {filter: filter});
    //    expect(activeFilterComponent.filterLabel).toBe('ERC : Cultures and cultural production : literature, visual and performing arts, music, cultural and comparative studies');
    //});
    //
    //it('should get the right label for the urban unit', () => {
    //    filter = new Filter('any', ["Lyon", "Albi", "Alès", "Amiens", "Angers", "Annecy"], "urbanUnit");
    //    activeFilterComponent = $componentController('activeFilter', {$scope: scope, $location:$location, FiltersService: FiltersService}, {filter: filter});
    //    expect(activeFilterComponent.filterLabel).toBe('Unités urbaines : Lyon, Albi, Alès, Amiens, Angers... (ou 2 autres)');
    //});
});
