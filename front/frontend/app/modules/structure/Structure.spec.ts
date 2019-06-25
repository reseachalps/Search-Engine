import 'angular-mocks';
import {StructureController} from './StructureController';

describe("Structure", () =>{
    var $httpBackend: ng.IHttpBackendService,
        $controller: ng.IControllerService,
        ctrl: StructureController;

    beforeEach(() => angular.mock.inject(function (_$httpBackend_: ng.IHttpBackendService, _$controller_: ng.IControllerService) {
        $httpBackend = _$httpBackend_;
        $controller = _$controller_;
        ctrl = $controller(StructureController, {structure:{}, keywords:[]});
    }));

    // it('should pass', () => {
    //     expect(true).toBe(true);
    // });
});
