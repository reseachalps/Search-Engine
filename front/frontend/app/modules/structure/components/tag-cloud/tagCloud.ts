// Component stylesheet
import IAugmentedJQuery = angular.IAugmentedJQuery;
import IScope = angular.IScope;
import {directive} from "../../../../decorators/directive";
import {Utils} from "../../../../models/Utils";

// Directive stylesheet
import './tag-cloud.styl';

interface TagCloudComponentScope extends ng.IScope
{
    words: any
}

@directive('$state')
export class TagCloudDirective implements ng.IDirective{

    public scope:any;
    public link:any;
    public template:string;

    constructor($state: ng.ui.IStateService) {
        this.scope = {
            words: '<',
            showTitle: '<'
        };
        this.template = '<div ng-if="showTitle">Keyword cloud</div>';
        this.link = (scope: TagCloudComponentScope, element: any[]) :void => {

            let cloud: any = require("d3-cloud");

            const COLOR_SCALE = ["#1c344c", "#203c57", "#244362", "#284b6d", "#3d5d7b", "#526e8a", "#688198", "#7e93a7", "#93a5b6", "#aab9c6"]; // Color scale
            const BIGGEST_WORD_SIZE = Utils.getMaxValueFromArrayOfObjects(scope.words, 'count'); // biggest word count
            const logScale = d3.scale.linear().range([15, 35]).domain([1, BIGGEST_WORD_SIZE]); //scale log for the font size

            const graphContainer: any = d3.select(element[0])[0][0];
            const containerDimensions = graphContainer.getBoundingClientRect();

            var layout: any = cloud()
                .words(scope.words.slice(0,50).map(function(d:any) {
                    return {text: d.label, size: d.count};
                }).sort((a,b) => {
                    return a.size - b.size;
                }))
                .padding(5)
                .rotate(0)
                .font("Impact")
                .fontSize(function(d: any) { return logScale(d.size) || 20 })
                .size([containerDimensions.width, 500])
                .on("end", draw);

            layout.start();

            function draw(words) {
                const graphContainer: any = d3.select(element[0])[0][0];
                const containerDimensions = graphContainer.getBoundingClientRect();
                d3.select(element[0]).append("svg")
                    .attr("width", containerDimensions.width)
                    .attr("height", layout.size()[1])
                    .append("g")
                    .attr("transform", "translate(" + layout.size()[0] / 2 + "," + layout.size()[1] / 2 + ")")
                    .selectAll("text")
                    .data(words)
                    .enter().append("text")
                    .style("font-size", function (d:any) {
                        return d.size + "px";
                    })
                    .style("font-family", "Impact")
                    .style("fill", function (d, i) {
                        return COLOR_SCALE[i];
                    })
                    .attr("text-anchor", "middle")
                    .attr("transform", function (d: any) {
                        return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";
                    })
                    .text(function (d: any) {
                        return d.text;
                    });

                d3.selectAll('text')
                    .on('click', word =>{
                        $state.go('search',{query:word.text});
                    });
            }
        };
    }
}