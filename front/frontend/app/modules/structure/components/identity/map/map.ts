import './map.styl';

interface MapComponentScope extends ng.IScope
{
    Map: any // must match controllerAs
}

export class MapComponent implements ng.IComponentOptions {

    public template:string = `
            <div class="leaflet-map" id="leafletMap"></div>
            <small data-layout="row" data-layout-align="start" ng-click="Map.displaySurroundings()"><u><b>See the nearby organizations (20 closest in a 30km neighborhood)</b></u></small>`;
    public restrict:string = "E";
    public bindings:Object = {
        address: '<',
        struct: '<',
        surroundings: '<'
    };
    public controllerAs:string = 'Map';

    public controller:Function = ($scope: MapComponentScope) :void => {
        'ngInject';

        var ctrl = $scope.Map;

        if(ctrl.address.gps){
            ctrl.map = L.map('leafletMap').setView([ctrl.address.gps.y,ctrl.address.gps.x], 4);
            L.Icon.Default.imagePath = 'http://api.tiles.mapbox.com/mapbox.js/v1.0.0beta0.0/images';
            L.tileLayer('https://api.mapbox.com/v4/mapbox.streets/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoiZGVvbmNsZW0iLCJhIjoiY2lpcjh4Z3E5MDA5N3Zra3M0cWdkZWRyZyJ9.LZGaNGGMjhC8Ck8RLHGvXA',
                {
                    attribution: '© <a href="http://openstreetmap.org/copyright" target="_blank">OpenStreetMap</a> contributors'
                })
                .addTo(ctrl.map);
            window.dispatchEvent(new Event('resize')); // Resize event need to be manually trigger otherwise the map isn't properly centered

            ctrl.greenIcon = new L.Icon({
                iconUrl: 'app/assets/img/markers/marker-icon-green.png',
                shadowUrl: 'app/assets/img/markers/marker-shadow.png',
                iconSize: [25, 41],
                iconAnchor: [12, 41],
                popupAnchor: [1, -34],
                shadowSize: [41, 41]
            });

            ctrl.blueIcon = new L.Icon({
                iconUrl: 'app/assets/img/markers/marker-icon-blue.png',
                shadowUrl: 'app/assets/img/markers/marker-shadow.png',
                iconSize: [25, 41],
                iconAnchor: [12, 41],
                popupAnchor: [1, -34],
                shadowSize: [41, 41]
            });

            ctrl.redIcon = new L.Icon({
                iconUrl: 'app/assets/img/markers/marker-icon-red.png',
                shadowUrl: 'app/assets/img/markers/marker-shadow.png',
                iconSize: [25, 41],
                iconAnchor: [12, 41],
                popupAnchor: [1, -34],
                shadowSize: [41, 41]
            });

            ctrl.markers = [];
            ctrl.markers.push(L.marker(new L.LatLng(ctrl.address.gps.y, ctrl.address.gps.x), {icon: ctrl.greenIcon}).addTo(ctrl.map));
        }

        ctrl.displaySurroundings = () => {

            if(angular.isDefined(ctrl.markersGroup) && angular.isDefined(ctrl.group)){
                ctrl.map.fitBounds(ctrl.group.getBounds());
                return;
            }

            for (var i = 0; i < ctrl.markers.length; i++) {
                ctrl.map.removeLayer(ctrl.markers[i]);
            }

            ctrl.markers = [];
            const oms = new OverlappingMarkerSpiderfier(ctrl.map, {keepSpiderfied: true, nearbyDistance: 10});
            for (var i = 0; i < ctrl.surroundings.length; i++) {
                const struct = ctrl.surroundings[i];
                const addressPoint = struct.address.gps;
                const marker = L.marker(new L.LatLng(addressPoint.y, addressPoint.x), {icon: ctrl.surroundings[i].kind === 'PUBLIC' ? ctrl.blueIcon : ctrl.redIcon})
                    .bindPopup('<a href="/#/structure/' + struct.id + '"><strong>' + ((struct.acronym||[])[0] || struct.label) + '</strong></a>')
                    .addTo(ctrl.map);
                ctrl.markers.push(marker);
                oms.addMarker(marker);
            }
            const structMarker = L.marker(new L.LatLng(ctrl.address.gps.y, ctrl.address.gps.x), {icon: ctrl.greenIcon})
                .bindPopup('<strong>' + ((ctrl.struct.acronym||[])[0] || ctrl.struct.label) + '</strong>')
                .addTo(ctrl.map);
            ctrl.markers.push(structMarker);
            oms.addMarker(structMarker);

            ctrl.group = L.featureGroup(ctrl.markers);
            ctrl.map.fitBounds(ctrl.group.getBounds());
        }
    };


}