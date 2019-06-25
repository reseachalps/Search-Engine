// Component stylesheet
import './leaflet-map.styl';
import {SearchService} from "../../../../../../services/SearchService";

interface LeafletMapComponentScope extends ng.IScope
{
    Map: any // must match controllerAs
}

export class LeafletMapComponent implements ng.IComponentOptions {

    public template:string = <string>require('./leaflet-map.html');
    public restrict:string = "E";
    public bindings:Object = {
    };
    public controllerAs:string = 'Map';

    private FRANCE_LOC = [46,7];

    public controller:Function = ($scope: LeafletMapComponentScope, $timeout:ng.ITimeoutService, SearchService: SearchService, $location: ng.ILocationService, $rootScope: ng.IRootScopeService) :void => {
        'ngInject';

        var ctrl = $scope.Map;
        ctrl.markersArray = []; // just the array of markers, needed for the fitBounds method
        const clusterOptions: L.CustomMarkerClusterGroupOptions = {chunkedLoading: true};
        ctrl.markers = new L.MarkerClusterGroup();
        ctrl.markers.initialize(clusterOptions);
        ctrl.loading = true;

        ctrl.blueIcon = new L.Icon({
            iconUrl: 'app/assets/img/markers/marker-icon-blue.png',
            shadowUrl: 'app/assets/img/markers/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
        });
        ctrl.map = L.map('leafletMap').setView(this.FRANCE_LOC, 4);
        L.Icon.Default.imagePath = 'http://api.tiles.mapbox.com/mapbox.js/v1.0.0beta0.0/images';
        L.tileLayer('https://api.mapbox.com/v4/mapbox.streets/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoiZGVvbmNsZW0iLCJhIjoiY2lpcjh4Z3E5MDA5N3Zra3M0cWdkZWRyZyJ9.LZGaNGGMjhC8Ck8RLHGvXA&country=fr',
            {
                attribution: '© <a href="http://openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> contributors'
            })
            .addTo(ctrl.map);

        window.dispatchEvent(new Event('resize')); // Resize event need to be manually trigger otherwise the map isn't properly centered

        SearchService.getGeoPositions().then(data => {

            const addressPoints = data.data.results.map((point: any) => {
                if(point.address && point.address.gps){
                    return {label: point.label, id:point.id,  point: [point.address.gps.lat,point.address.gps.lon]}
                }
            });
            ctrl.loading = false;

            for (var i = 0; i < addressPoints.length; i++) {
                const addressPoint = addressPoints[i];
                if(addressPoint) {
                    var a:any[] = addressPoint.point;
                    var marker = L.marker(new L.LatLng(a[0], a[1]), {icon: ctrl.blueIcon});
                    marker.bindPopup('<a href="/#/structure/' + addressPoint.id + '"><strong>' + addressPoint.label + '</strong></a>');
                    ctrl.markersArray.push(marker);
                }
            }

            ctrl.markers.addLayers(ctrl.markersArray);

            if($location.search().view === 'firmo'){
                addMapLayer(); // we add the marker map layer on the map if we are already on the firmo tab
            }

        });

        ctrl.unzoom = () => {
            const group = L.featureGroup(ctrl.markersArray);
            ctrl.map.fitBounds(group.getBounds());
        };

        // Otherwise we listen for the tab changed event and display the map when coming from the list tab
        $rootScope.$on('firmoTabDisplayed', () => {
            if(ctrl.mapLayerAdded !== true){
                addMapLayer();
            }
        });

        let addMapLayer:Function = () => {
            ctrl.mapLayerAdded = true;
            ctrl.map.addLayer(ctrl.markers);
        }
    };


}