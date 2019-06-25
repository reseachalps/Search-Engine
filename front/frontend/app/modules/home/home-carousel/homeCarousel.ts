

// stylesheet
import './home-carousel.styl';

interface HomeCarouselComponentScope extends ng.IScope
{
    Carousel: any // must match controllerAs
}

export class HomeCarouselComponent  implements ng.IComponentOptions{

    public scope:any;
    public link:any;
    public template:any = require('./home-carousel.html');
    public selectedImages:string[];
    public controllerAs:string = 'Carousel';

    public controller:Function = ($scope: HomeCarouselComponentScope, $interval: ng.IIntervalService) :void => {
        'ngInject';
        var ctrl = $scope.Carousel;

        ctrl.imagePool = [
            {
                image: 'app/assets/img/contributor/Logo_Universita_degli_Studi_di_Modena_e_Reggio_Emilia.svg.png',
                link: 'https://www.unimore.it/'
            },
            {
                image: 'app/assets/img/contributor/MIUR.png',
                link: 'http://www.miur.gov.it/'
            },
            {
                image: 'app/assets/img/contributor/unimont.png',
                link: 'https://www.unimontagna.it/'
            },
            {
                image: 'app/assets/img/contributor/logo_sidetrade.png',
                link: 'http://www.sidetrade.com/'
            },
            {
                image: 'app/assets/img/contributor/mesri.png',
                link: 'http://www.enseignementsup-recherche.gouv.fr/'
            }
        ];

        var selectRandomImage = function() {
            var choices = [];
            var maxElements = Math.floor(window.innerWidth / 300);
            if (maxElements > ctrl.imagePool.length) {
                maxElements = ctrl.imagePool.length;
            }
            while(choices.length < maxElements) {
                var randomChoice = Math.floor(Math.random()*ctrl.imagePool.length);
                if(choices.indexOf(ctrl.imagePool[randomChoice]) === -1) {
                    choices.push(ctrl.imagePool[randomChoice]);
                }
            }
            ctrl.selectedImages = choices;
        };

        var carouselAnimation = $interval(
            selectRandomImage,
            3000
        );

        selectRandomImage();

        $scope.$on('$destroy', function(){
            if(carouselAnimation) {
                $interval.cancel(carouselAnimation);
            }
        });
    };
}