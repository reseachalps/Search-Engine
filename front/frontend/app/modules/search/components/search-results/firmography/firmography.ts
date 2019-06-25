import {SearchService} from "../../../../../services/SearchService";

// Component stylesheet
import './firmography.styl';
const NUTS_LABELS = JSON.parse(require<string>('../../../../../assets/nuts-labels.json'));

interface FirmographyComponentScope extends ng.IScope
{
    Firmography: any // must match controllerAs
}

export class FirmographyComponent implements ng.IComponentOptions {

    public template:any = require('./firmography.html');
    public restrict:string = "E";
    public bindings:Object = {
        results: '=',
        applyFilters: '=',
        applyFilter: '=',
        keywords: '<'
    };
    public controllerAs:string = 'Firmography';

    public controller:Function = ($scope: FirmographyComponentScope, $timeout: ng.ITimeoutService, $filter:ng.IFilterService, $log: ng.ILogService, SearchService: SearchService) :void => {
        'ngInject';

        var ctrl = $scope.Firmography;

        const histograms = ctrl.results.histograms;
        const total = ctrl.results.total;
        const BAR_COLORS = ['#243c55','#264361','#284b6d','#3d5d7c','#57738d','#758ba1','#94a5b6','#b1becd','#ccd4dc','#e2e7eb'];
        const PIE_COLORS = ['#2a4d69', '#4b86b4', '#adcbe3', '#e7eff6', '#63ace5','#526e8a','#688198','#7e93a7','#93a5b6'];

        ctrl.tagCloudWords = [];
        ctrl.keywordsLoading = true;
        $scope.$watch('Firmography.keywords', (n: any) => {
            if(n) {
                n.forEach((keyword:any) => {
                    ctrl.tagCloudWords.push({key: keyword.keyword, count: keyword.score, label: keyword.keyword});
                });
                ctrl.keywordsLoading = false;
            }
        });

        /**
         * Base nvd3 config objects for a bar chart
         */
        ctrl.baseBarChartOptions = {
            chart: {
                type: 'discreteBarChart',
                height: 250,
                noData: 'No data available for this graphic',
                margin : {
                    top: 10,
                    right: 30,
                    bottom: 100,
                    left: 20
                },
                x: function(d){return d.label;},
                y: function(d){return d.value;},
                showValues: true,
                valueFormat: function(d){
                    return d3.format(',.0f')(d);
                },
                duration: 500,
                xAxis: {
                    tickFormat: function(d){
                        const MAX_LENGTH = 20;
                        if(!d){
                            $log.warn("Undefined label for bar chart");
                            return;
                        }
                        return d.length > MAX_LENGTH ? (d.substr(0,MAX_LENGTH) + '...') : d;
                    },
                    rotateLabels: -45
                },
                yAxis: {
                    tickValues: 0,
                    showMaxMin: false,
                    axisLabel: "Organizations",
                    axisLabelDistance: -45
                },
                color: function(d, i){
                    return BAR_COLORS[i];
                },
                tooltip: {
                    contentGenerator: function (e) {
                        var series = e.series[0];
                        if (series.value === null) return;

                        var header =
                            `<thead><tr><td class='legend-color-guide'><div style='background-color: ${series.color};'></div></td><td class='key'><strong>${series.key}</strong></td></tr></thead>`;

                        var rows =
                            `<tr><td></td><td class='x-value'>${(series.value ? $filter('number')(series.value, 0) : 0)}</tr>`;

                        return `<table>${header}<tbody>${rows}</tbody></table>`;
                    }
                }
            }
        };

        /**
         * Base nvd3 config objects for a bar chart
         */
        ctrl.baseHorBarChartOptions = {
            chart: {
                type: 'multiBarHorizontalChart',
                height: 200,
                noData: 'No data available for this graphic',
                margin : {
                    top: 10,
                    right: 5,
                    bottom: 20,
                    left: 200
                },
                showControls: false,
                x: function(d){return d.label;},
                y: function(d){return d.value;},
                showValues: true,
                valueFormat: function(d){
                    return d3.format(',.0f')(d);
                },
                duration: 500,
                xAxis: {
                    tickFormat: function(d){
                        const MAX_LENGTH = 30;
                        if(!d){
                            $log.warn("Undefined label for horizontal bar chart");
                            return;
                        }
                        return d.length > MAX_LENGTH ? (d.substr(0,MAX_LENGTH) + '...') : d;
                    }
                },
                yAxis: {
                    tickValues: 0,
                    showMaxMin: false,
                    axisLabel: "Organizations",
                    axisLabelDistance: -30
                },
                barColor: function(d, i){
                    return BAR_COLORS[i];
                },
                showLegend: false,
                tooltip: {
                    contentGenerator: function (e) {
                        var series = e.series[0];
                        if (series.value === null){return;}

                        var header =
                            `<thead><tr><td class='legend-color-guide'><div style='background-color: ${series.color};'></div></td><td class='key'><strong>${e.value}</strong></td></tr></thead>`;

                        var rows =
                            `<tr><td></td><td class='x-value'>${(series.value ? $filter('number')(series.value, 0) : 0)}</tr>`;

                        return `<table>${header}<tbody>${rows}</tbody></table>`;
                    }
                }
            }
        };
        /**
         * Base nvd3 config objects for a pie chart
         */
        ctrl.basePieChartOptions = {
            chart: {
                type: 'pieChart',
                height: 200,
                noData: 'No data available for this graphic',
                x: function(d){return d.key;},
                y: function(d){return d.y;},
                showLabels: true,
                duration: 500,
                labelThreshold: 0.01,
                showLegend: false,
                labelsOutside: true,
                color: function(d, i){
                    return PIE_COLORS[d.color];
                },
                tooltip: {
                    contentGenerator: function (e) {
                        var series = e.series[0];
                        if (series.value === null) return;

                        var header =
                            `<thead><tr><td class='legend-color-guide'><div style='background-color: ${series.color};'></div></td><td class='key'><strong>${(series.value ? $filter('number')(series.value, 0) : 0)}</strong>&nbsp;${series.key.toLowerCase()} entities</td></tr></thead>`;
                        return `<table>${header}</table>`;
                    }
                }
            }
        };
    
        const publicEntities = histograms.publicEntity.bins.find((bin) => bin.key === "true");
        const privateEntities = histograms.publicEntity.bins.find((bin) => bin.key === "false");
        const unknownEntities = histograms.publicEntity.bins.find((bin) => bin.key === "undefined");
        /**
         * Private / Public chart
         */
        ctrl.kindChart = [
            {
                key: 'Public',
                y: publicEntities && publicEntities.count || 0,
                color: 0,
                id: "true"
            },
            {
                key: 'Private',
                y: privateEntities && privateEntities.count || 0,
                color: 1,
                id: "false"
            },
            {
                key: 'Not available',
                y: unknownEntities && unknownEntities.count || 0,
                color: 2,
                id: "undefined"
            }
        ].filter(a => a.y !== 0);

        ctrl.typeChartsOptions = angular.copy(ctrl.basePieChartOptions);
        ctrl.typeChartsOptions.chart.height = 250;
        ctrl.typeChartsOptions.chart.pie = {
          dispatch: {
            elementClick: function(e) {
                console.log("Type", e.data);
                $scope.$apply(() => ctrl.applyFilter("publicEntity", e.data.id));
            }
          }
        };


        /**
         * Top 10 urban units chart
         */
        ctrl.nutsChartsOptions = angular.copy(ctrl.baseBarChartOptions);
        ctrl.nutsChartsOptions.chart.height = 250;
        ctrl.nutsChartsOptions.chart.discretebar = {
          dispatch: {
            elementClick: function(e) {
                console.log("NUTS", e.data);
                $scope.$apply(() => ctrl.applyFilter("nuts", e.data.id));
            }
          }
        };
        ctrl.nutsChart =
            [{
                key: 'NUTS2 Areas',
                values: histograms.nuts.bins.map((nature) => {return {label: "["+nature.label.substr(0, 2) + "] "+ NUTS_LABELS[nature.label], value:nature.count, id: nature.key}}).slice(0,10)
            }];


        /**
         * Types chart
         */
        ctrl.countriesChartsOptions = angular.copy(ctrl.baseBarChartOptions);
        ctrl.countriesChartsOptions.chart.height = 250;
        ctrl.countriesChartsOptions.chart.discretebar = {
          dispatch: {
            elementClick: function(e) {
                console.log("Countries", e.data);
                $scope.$apply(() => ctrl.applyFilter("countries", e.data.id));
            }
          }
        };
        ctrl.countriesChart =
            [{
                key: 'Countries',
                values: histograms.countries.bins.map((nature) => {return {label: nature.label, value:nature.count, id: nature.key}})
            }];

        /**
         * Types chart
         */
        ctrl.publicationsChartsOptions = angular.copy(ctrl.baseBarChartOptions);
        ctrl.publicationsChartsOptions.chart.height = 250;
        ctrl.publicationsPercent = 100 * (1 - histograms.publications.bins[0].count / total);
        ctrl.publicationsChart =
            [{
                key: 'Publications',
                values: histograms.publications.bins.slice(1).map((nature) => {return {label: nature.label, value:nature.count}})
            }];

        /**
         * Types chart
         */
        ctrl.projectsChartsOptions = angular.copy(ctrl.baseBarChartOptions);
        ctrl.projectsChartsOptions.chart.height = 250;
        ctrl.projectsPercent = 100 * (1 - histograms.projects.bins[0].count / total);
        ctrl.projectsChart =
            [{
                key: 'Projects',
                values: histograms.projects.bins.slice(1).map((nature) => {return {label: nature.label, value:nature.count}})
            }];

        /**
         * Types chart
         */
        ctrl.connectionsChartsOptions = angular.copy(ctrl.baseBarChartOptions);
        ctrl.connectionsChartsOptions.chart.height = 250;
        ctrl.connectionsPercent = 100 * (1 - histograms.connections.bins[0].count / total);
        ctrl.connectionsChart =
            [{
                key: 'Network Connections',
                values: histograms.connections.bins.slice(1).map((nature) => {return {label: nature.label, value:nature.count}})
            }];

        /**
         * Types chart
         */
        ctrl.leadersChartsOptions = angular.copy(ctrl.baseBarChartOptions);
        ctrl.leadersPercent = 100 * (1 - histograms.leaders.bins[0].count / total);
        ctrl.leadersChartsOptions.chart.height = 250;
        ctrl.leadersChart =
            [{
                key: 'Leaders',
                values: histograms.leaders.bins.slice(1).map((nature) => {return {label: nature.label, value:nature.count}})
            }];

    };
}