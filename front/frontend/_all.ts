/// <reference path="typings/main.d.ts" />
/// <reference path="app/config/core/coreConfig.ts" />
/// <reference path="app/config/core/coreRun.ts" />
/// <reference path="app/modules/search/index.ts" />
/// <reference path="app/modules/home/index.ts" />
/// <reference path="app/modules/structure/index.ts" />

declare var ON_PROD: boolean;
declare var ga: Function; // google analytics global variable
declare var ATInternet: any; // xiti global variable
declare var grecaptcha: any; // google captcha global variable
declare var OverlappingMarkerSpiderfier: any; // oms global variable

// Override of the MarkerClusterGroupOptions as the chunkedLoading options aren't declared in the d.ts file
declare module L {
    export interface CustomMarkerClusterGroupOptions {

        /*
         * When you mouse over a cluster it shows the bounds of its markers.
         */
        showCoverageOnHover?:boolean;

        /*
         * When you click a cluster we zoom to its bounds.
         */
        zoomToBoundsOnClick?:boolean;

        /*
         * When you click a cluster at the bottom zoom level we spiderfy it
         * so you can see all of its markers.
         */
        spiderfyOnMaxZoom?:boolean;

        /*
         * Clusters and markers too far from the viewport are removed from the map
         * for performance.
         */
        removeOutsideVisibleBounds?:boolean;

        /*
         * Smoothly split / merge cluster children when zooming and spiderfying.
         * If L.DomUtil.TRANSITION is false, this option has no effect (no animation is possible).
         */
        animate?:boolean;

        /*
         * If set to true (and animate option is also true) then adding individual markers to the
         * MarkerClusterGroup after it has been added to the map will add the marker and animate it
         * into the cluster. Defaults to false as this gives better performance when bulk adding markers.
         * addLayers does not support this, only addLayer with individual Markers.
         */
        animateAddingMarkers?:boolean;

        /*
         * If set, at this zoom level and below markers will not be clustered. This defaults to disabled.
         */
        disableClusteringAtZoom?:number;

        /*
         * The maximum radius that a cluster will cover from the central marker (in pixels). Default 80.
         * Decreasing will make more, smaller clusters.
         */
        maxClusterRadius?:number;

        /*
         * Options to pass when creating the L.Polygon(points, options) to show the bounds of a cluster.
         * Defaults to empty
         */
        polygonOptions?:PolylineOptions;

        /*
         * If set to true, overrides the icon for all added markers to make them appear as a 1 size cluster.
         */
        singleMarkerMode?:boolean;

        /*
         * Allows you to specify PolylineOptions to style spider legs.
         * By default, they are { weight: 1.5, color: '#222', opacity: 0.5 }.
         */
        spiderLegPolylineOptions?:PolylineOptions;

        /*
         * Increase from 1 to increase the distance away from the center that spiderfied markers are placed.
         * Use if you are using big marker icons (Default: 1).
         */
        spiderfyDistanceMultiplier?:number;

        /*
         * Function used to create the cluster icon
         */
        iconCreateFunction?:any;

        /*
         * Boolean to split the addLayers processing in to small intervals so that the page does not freeze.
         */
        chunkedLoading?:boolean;

        /*
         * Time interval (in ms) during which addLayers works before pausing to let the rest of the page process.
         * In particular, this prevents the page from freezing while adding a lot of markers. Defaults to 200ms.
         */
        chunkInterval?:number;

        /*
         * Time delay (in ms) between consecutive periods of processing for addLayers. Default to 50ms.
         */
        chunkDelay?:number;
    }
}
