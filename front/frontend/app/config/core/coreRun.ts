export interface CustomRootScope extends ng.IRootScopeService {
    acceptCookies:Function;
    showCookieDisclaimer:boolean;
    appLoaded:boolean;
    sc: any;
    og: any;
    changingState: boolean,
    pageTitle: string,
    $state: ng.ui.IState
}

export function run($rootScope: CustomRootScope, $log: ng.ILogService, $location: ng.ILocationService, $state: ng.ui.IState, $cookies:angular.cookies.ICookiesService, $window: ng.IWindowService) {
    "ngInject"; //needed when directly exporting a class or function
    
    $rootScope.$state = $state; // so $state is accessible in the view
    $rootScope.og = {};
    $rootScope.appLoaded = false;


    $rootScope.showCookieDisclaimer = !$cookies.get('scanr_cookies');
    $rootScope.acceptCookies = function () {
        const date = new Date();
        date.setFullYear(date.getFullYear() + 1); // cookie expires in one year;
        $cookies.put('scanr_cookies', 'true', {expires:date});
        $rootScope.showCookieDisclaimer = false;
    };

    $rootScope.$on("$stateChangeStart", () => {
        $rootScope.changingState = true;
        if(!$cookies.get('scanr_cookies') && $rootScope.appLoaded){
            $rootScope.acceptCookies();
        }
    });

    $rootScope.$on("$stateChangeSuccess", () => {
        $rootScope.changingState = false;
        $rootScope.appLoaded = true;
        $rootScope.og.url = $location.absUrl();
        $window['gtag']('config', 'UA-101606917-2', {'page_path': $location.path()});
    });

    $rootScope.$on("$stateChangeError", (event, toState, toParams, fromState, fromParams, error) => {
        $rootScope.changingState = false;
        $log.error("State change error: ", error);
    })
}

export default run;