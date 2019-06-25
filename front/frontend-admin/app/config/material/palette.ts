import IThemingProvider = angular.material.IThemingProvider;

export function palette($mdThemingProvider: IThemingProvider) {
    "ngInject"; //needed when directly exporting a class or function
    $mdThemingProvider.definePalette('scanrPrimary', {
        '50': '93a5b6',
        '100': '7e93a7',
        '200': '688198',
        '300': '526e8a',
        '400': '3d5d7b',
        '500': '284b6d',
        '600': '244362',
        '700': '203c57',
        '800': '1c344c',
        '900': '182d41',
        'A100': '142536',
        'A200': '101e2b',
        'A400': '0c1620',
        'A700': '080f15',
        'contrastDefaultColor': 'light',    // whether, by default, text (contrast)
                                            // on this palette should be dark or light
        'contrastDarkColors': ['50', '100', //hues which contrast should be 'dark' by default
            '200', '300', '400', 'A100'],
        'contrastLightColors': undefined    // could also specify this if default was 'dark'
    });
    $mdThemingProvider.definePalette('scanrAccent', {
        '50': '3898C3',
        '100': '3898C3',
        '200': '3898C3',
        '300': '3898C3',
        '400': '3898C3',
        '500': '3898C3',
        '600': '3898C3',
        '700': '3898C3',
        '800': '3898C3',
        '900': '3898C3',
        'A100': '3898C3',
        'A200': '3898C3',
        'A400': '3898C3',
        'A700': '3898C3',
        'contrastDefaultColor': 'light',    // whether, by default, text (contrast)
                                            // on this palette should be dark or light
        'contrastDarkColors': ['50', '100', //hues which contrast should be 'dark' by default
            '200', '300', '400', 'A100'],
        'contrastLightColors': undefined    // could also specify this if default was 'dark'
    });
    //$mdThemingProvider.definePalette('scanrAccent', {
    //    '50': 'eeeeee',
    //    '100': 'eaeaea',
    //    '200': 'e7e7e7',
    //    '300': 'e3e3e3',
    //    '400': 'e0e0e0',
    //    '500': 'DDDDDD',
    //    '600': 'c6c6c6',
    //    '700': 'b0b0b0',
    //    '800': '9a9a9a',
    //    '900': '848484',
    //    'A100': '6e6e6e',
    //    'A200': '585858',
    //    'A400': '424242',
    //    'A700': '2c2c2c',
    //    'contrastDefaultColor': 'light',    // whether, by default, text (contrast)
    //                                        // on this palette should be dark or light
    //    'contrastDarkColors': ['50', '100', //hues which contrast should be 'dark' by default
    //        '200', '300', '400', 'A100'],
    //    'contrastLightColors': undefined    // could also specify this if default was 'dark'
    //
    //});

    $mdThemingProvider
        .theme('default')
        .primaryPalette('scanrPrimary')
        .accentPalette('scanrAccent');
}