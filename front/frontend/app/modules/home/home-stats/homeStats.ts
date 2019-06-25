

// stylesheet
import './home-stats.styl';

interface HomeStatsComponentScope extends ng.IScope
{
    Stats: any // must match controllerAs
}

export class HomeStatsComponent  implements ng.IComponentOptions{

    public scope:any;
    public link:any;
    public template:any = require('./home-stats.html');
    public bindings:Object = {
        stats: '='
    };
    public controllerAs:string = 'Stats';

    public controller:Function = ($scope: HomeStatsComponentScope) :void => {
        'ngInject';
    };
}