import {directive} from "../../../../decorators/directive";

// Directive stylesheet
import './relations-graph.styl';
import './relations-graph.resp.styl';
import {Utils} from "../../../../models/Utils";

interface RelationsGraphComponentScope extends ng.IScope
{
    selectedView:string;
    changeType:Function;
    NB_NODES:number;
    hasPrivate:boolean;
    hasUnknown:boolean;
    hasPublic:boolean;
    selectedType:string;
    graphData: any,
    structure: any,
    selectedNode: any
}

@directive('$state')
export class RelationsGraphDirective implements ng.IDirective{

    public scope:any;
    public link:any;
    public template:string;

    private force;
    private graphData;
    private graph;
    private center;
    private labels;
    private circles;
    private nodes;
    private links;
    private $scope;
    private width;
    private height;
    private svg;
    private linkWidthScale;
    public controllerAs:string = 'Relations';


    constructor(private $state: ng.ui.IStateService) {
        this.scope = {
            graphData: '<',
            structure: '<'
        };
        this.template = <string> require('./relations-graph.html');
        this.link = ($scope: RelationsGraphComponentScope, element: any[]) :void => {
            this.$scope = $scope;
            this.$scope.nbPrivate = 0;
            this.$scope.nbPublic = 0;

            this.$scope.graphData.forEach((entry) => {
                if(entry.structure.isPublic){
                    this.$scope.nbPublic++;
                } else {
                    this.$scope.nbPrivate++;
                }
            });

            $scope.NB_NODES = 7;
            $scope.selectedView = 'list';
            $scope.selectedType = 'all';
            
            const graphContainer = d3.select(element[0]).select('.graph-container');
            let graphElement: any = graphContainer[0][0];
            const containerDimensions = graphElement.getBoundingClientRect();

            this.width = containerDimensions.width;
            this.height = containerDimensions.height;

            // set fixed positions around the center node
            this.center = {
                x: this.width /2,
                y: this.height /2
            };

            this.svg = graphContainer.append("svg")
                .attr("width", "100%")
                .attr("height", this.height);

            this.force = d3.layout.force()
                .size([this.width, this.height]);

            this.force.on("tick", () => {
                this.links
                    .transition()
                    .duration(500)
                    .attr("d", function(d: any) {
                        //code to bend the links
                        var sourceX = d.source.x,
                            sourceY = d.source.y,
                            targetX = d.target.x,
                            targetY = d.target.y,
                            dx = targetX - sourceX,
                            dy = targetY - sourceY,
                            dr = Math.sqrt(dx * dx + dy * dy);
                        return "M" + sourceX + "," + sourceY + "A" + dr + "," + dr + " 0 0,1 " + targetX + "," + targetY;
                    })
                    .style("stroke-width", (d:any) => {
                        return d.exit ? 0 : this.linkWidthScale(d.value)
                    });

                this.nodes
                    .transition()
                    .duration(500)
                    .attr("transform", function(d: any) { return "translate(" + d.x + "," + d.y + ")"; });
                this.force.stop();
            });
            $scope.changeType = () => {
                this.updateGraph(); // update with new values
            };
            this.updateGraph(true); // first draw
        };
    }

    private updateGraph(firstLoad ?: boolean) {
        this.graphData = angular.copy(this.$scope.graphData);
        console.log(this.$scope.selectedType );
        this.graphData = this.graphData.filter((node:any) => {return node.structure && this.$scope.selectedType !== 'all' ? (node.structure.isPublic === null ? "unknown" : node.structure.isPublic.toString()) === this.$scope.selectedType : true});
        this.$scope.nbNodes = this.graphData.length;
        this.graphData = this.graphData.slice(0,this.$scope.NB_NODES).reverse();
        this.graphData.unshift({structure:{acronyms: (this.$scope.structure.acronyms||[])[0], label: this.$scope.structure.label, value:0, isPublic:"true"}});
        this.graph = {
            nodes: this.graphData
                .map(node => {
                    node.structure.details = node.details;
                    node.structure.weight = node.weight;
                    return node.structure
                }),
            links: this.graphData
                .map((node,i) => {return{
                    source: 0,
                    target: i,
                    value: node.weight
                }})
        };
        this.$scope.selectedNode = this.graphData[this.graphData.length - 1].structure;

        const weakestNode = Utils.getMinValueFromArrayOfObjects(this.graph.nodes, 'weight');
        const LONGEST_LINK = 200;
        const CIRCLE_RADIUS = 20;
        const SHORTEST_LINK = CIRCLE_RADIUS*2;
        this.graph.nodes.forEach((o: any, i: any) => {
            if(i === 0){
                o.x = this.center.x;
                o.y = this.center.y;
                o.fixed = true;
            } else {
                // 2pi circle divided by the number of elements (minus the this.center node) + dynamic length of link depending on the link weight
                let rad = (2 * Math.PI)*(i) / (this.graph.nodes.length-1);
                let dist = weakestNode*LONGEST_LINK/o.weight;
                dist = dist < SHORTEST_LINK ? SHORTEST_LINK : dist;
                o.x = this.center.x + Math.cos(rad)*(dist);
                o.y = this.center.y - Math.sin(rad)*(dist);
                o.fixed = true;
            }
        });

        const widestLink = Utils.getMaxValueFromArrayOfObjects(this.graph.links, 'value');
        this.linkWidthScale = d3.scale.linear().range([10, 30]).domain([1, widestLink]);
        const MAX_LABEL_LENGTH = 27;
        if(firstLoad){ //appending the elements and styling

            // Checking if more than one public / private structure
            this.$scope.graphData.forEach((entry) => {
                if(entry.structure.isPublic) {
                    this.$scope.hasPublic = true;
                } else if(entry.structure.isPublic === false) {
                    this.$scope.hasPrivate = true;
                } else {
                    this.$scope.hasUnknown = true;
                }
            });

            this.links = this.svg.selectAll(".link")
                .data(this.graph.links)
                .enter().append("path")
                .attr("class", "link")
                .style("stroke-width", (d:any) => this.linkWidthScale(d.value));

            this.nodes = this.svg.selectAll(".node")
                .data(this.graph.nodes)
                .enter().append("g")
                .attr("class", "node");

            // circles
            this.circles = this.nodes.append("circle")
                .attr("r", (d:any) => {
                    return d.index === 0 ? '27' : CIRCLE_RADIUS
                })
                .style("fill", function(d,i:number) {
                 if(i === 0){
                 return '#388E4F';
                 } else {
                 return d.isPublic === null ? '#d18130' : (!d.isPublic ? '#d32f2f' : '#3898C3');
                 }
                 })
                .on('click', (node: any) => {
                    this.$scope.selectedNode = node;
                    this.$scope.$apply(); // outside of angular scope.
                })
                .on('dblclick', (node: any) => {
                    this.$state.go('structure',{id: node.id})
                });
            //labels
            this.labels = this.nodes.append("text")
                .on('click', (node: any) => {
                    if(node.index !== 0){
                        this.$scope.selectedNode = node;
                        this.$scope.$apply(); // outside of angular scope.
                    }
                })
                .on('dblclick', (node: any) => {
                    this.$state.go('structure',{id: node.id})
                });
        }
        else {
            //updating with new data and re-styling
            this.nodes.data(this.graph.nodes);

            let links = this.links.data(this.graph.links);
            links
                .exit()
                .style("stroke-width", (d) => {
                    d.exit = true;
                    return 0
                });

            let circles = this.circles.data(this.graph.nodes);

            circles.transition()
                .duration(500)
                .style("fill", function (d, i:number) {
                    if (i === 0) {
                        return '#388E4F';
                    } else {
                        return d.isPublic === null ? '#d18130' : (!d.isPublic ? '#d32f2f' : '#3898C3');
                    }
                })
                .attr('r', CIRCLE_RADIUS);

            circles.exit()
                .transition()
                .duration(500)
                .attr('r', 0);

            let labels = this.labels.data(this.graph.nodes);

            labels
                .style("font-size", '15px');

            labels.exit()
                .style("font-size", 0);
        }
        this.circles
            .classed("relation", (d,i) => i !== 0)
            .classed('private', (d:any) => d.isPublic === false)
            .classed('unknown', (d:any) => d.isPublic === null)
            .classed('public', (d:any) => d.isPublic);
        this.labels
            .text((d:any, i:number) => {
                return i === 0 ? "" : ((d.acronyms||[])[0] || (d.label.length > MAX_LABEL_LENGTH ? d.label.substring(0, MAX_LABEL_LENGTH) + '...' : d.label));
            })
            .attr("dx", function () {
                return -this.getBBox().width / 2;
            })
            .attr("dy", 40);
        this.force.nodes(this.graph.nodes)
            .links(this.graph.links)
            .size([this.width, this.height]);
        this.force.start();
    }
}