export function config($urlRouterProvider: ng.ui.IUrlRouterProvider, $locationProvider: ng.ILocationProvider, $touchProvider:any) {
    "ngInject"; //needed when directly exporting a class or function

    // Not using the html5prod on dev env
    if (ON_PROD) {
        $locationProvider.html5Mode(true);
    }
    $urlRouterProvider.otherwise('/');

    // Call this method to enable/disable ngTouch's ngClick directive.
    // If enabled, the default ngClick directive will be replaced by a version that eliminates the 300ms delay for click events on browser for touch-devices.
    $touchProvider.ngClickOverrideEnabled(true);
}