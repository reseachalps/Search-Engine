// Component stylesheet
import './search-bar.styl';
import './search-bar.resp.styl';

interface SearchBarComponentScope extends ng.IScope
{
    Bar: any // must match controllerAs
}

export class SearchBarComponent implements ng.IComponentOptions {

    public template:any = require('./search-bar.html');
    public restrict:string = "E";
    public bindings:Object = {
        query: '='
    };
    public controllerAs:string = 'Bar';

    public controller:Function = ($scope: SearchBarComponentScope, $location: ng.ILocationService, $state: ng.ui.IStateService) :void => {
        'ngInject';

        var ctrl = $scope.Bar;

        ctrl.inputValue = $location.search().query;

        ctrl.runSearch = ():void => {
            // If we are already in the search we just manipulate the $state params (because 'view' is not part of the search state url pattern, it is reset if $state.go)
            if($state.is('search')){
                delete $location.search()['page'];
                $location.search('query', ctrl.inputValue);
            } else {
                //otherwise we go to the search state
                $state.go('search', {query: ctrl.inputValue});
            }
        }
    };
}