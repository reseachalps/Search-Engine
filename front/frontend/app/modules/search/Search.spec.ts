import '../../services'
import {SearchController} from './SearchController';
import {SearchService} from "../../services/SearchService";
import {CustomRootScope} from "../../config/core/coreRun";

describe("Search", () =>{
    var $httpBackend: ng.IHttpBackendService,
        $controller: ng.IControllerService,
        ctrl: any;

    beforeEach(() => {
        angular.mock.module('app.services')
    });

    beforeEach(() => {
        angular.mock.inject(function (_$rootScope_:CustomRootScope, _$httpBackend_:ng.IHttpBackendService, _$controller_:ng.IControllerService, _SearchService_:SearchService, _$location_:ng.ILocationService) {
            $httpBackend = _$httpBackend_;
            $controller = _$controller_;
            ctrl = $controller(SearchController, {$rootScope: _$rootScope_, searchResults:{}, $location:_$location_, SearchService: _SearchService_});
        })
    });

    // it('should bind the search results', () => {
    //     expect(ctrl.searchResults).toEqual({});
    // });
});
