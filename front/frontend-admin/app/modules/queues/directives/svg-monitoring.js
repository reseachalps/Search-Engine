(function () {
    "use strict";
    var uid = 0;
    angular.module("app.queues.svg", []).directive("svgMonitoring", function () {
        return {
            restrict: 'E',
            scope: {
                nodes: '=',
                description: '=',
                status: '=',
                selected: '='
            },
            template: require("./svg-tooltip.html"),
            transclude: true,
            link: function (scope, element) {
                scope.element = element;
                scope.init();
                scope.$watch("status", function (nv) {
                    if (!nv) {
                        return;
                    }
                    scope.updateSVG();
                });
            },
            controller: ["$scope", "$http", "$location", "$filter", "$sce", "SchedulingApi",function ($scope, $http, $location, $filter, $sce, SchedulingApi) {
                $scope.tooltip = {show: false, left: "110 px", top: "110 px", text: null, enable:{schedule:false,unschedule:false,force:false}};

                $scope.tooltipClicked = function (e, action) {
                    if ($scope.tooltip.selectedScheduling){
                        switch(action){
                            case "schedule":
                                SchedulingApi.schedule($scope.tooltip.selectedScheduling);
                                break;
                            case "force":
                                SchedulingApi.scheduleNow($scope.tooltip.selectedScheduling);
                                break;
                            case "unschedule":
                                SchedulingApi.unschedule($scope.tooltip.selectedScheduling);
                                break;
                        }
                        $scope.tooltip.show=false;
                    }
                    e.stopPropagation();
                };

                function buildMaskForClick(nodeToClone, container) {
                    var elt = angular.element(nodeToClone).clone();
                    container.appendChild(elt[0]);
                    elt.attr("fill", "rgba(0,0,0,0)")
                        .attr("stroke", "none")
                        .css("cursor", "pointer");
                    return elt;
                }
                function buildSVGElement(parent,type) {
                    var elt = document.createElementNS('http://www.w3.org/2000/svg', type);
                    parent.appendChild(elt);
                    return angular.element(elt);
                }
                function buildTextNode(parent, clazz, x, y, width, height) {
                    var node = buildSVGElement(parent,'text')
                        .attr("class", clazz)
                        .attr("x", x)
                        .attr("y", y);
                    if (width && height)
                        node.attr("width", width)
                            .attr("height", height);
                    return node;
                }

                function buildQueueNode(e, select, desc, name) {
                    var errNode;
                    var outNode;
                    var inputNode;
                    var outputCount;
                    var rect = e.previousElementSibling;
                    var parent = e.parentNode;
                    var bbox = rect.getBBox();
                    select.remove();


                    var x = bbox.x;
                    var y = bbox.y;
                    var height = bbox.height;
                    var width = bbox.width;

                    var countNode = buildTextNode(parent, "centered-text monitoring message-count", x + 5, y + height - 12, width, height);
                    if (!desc.label) {
                        countNode.attr("style", "font-size: 12pt");
                    }
                    if (desc.label) {
                        var titleElt = buildTextNode(parent, "monitoring title", x + 5, y + 15, width, height);

                        if (desc.input) {
                            inputNode = buildTextNode(parent, "monitoring throughput", x + width - 30, y + 12, width, height)
                                .text("In: " + 12 + "/m");
                        }
                        if (desc.output) {
                            outNode= buildTextNode(parent, "monitoring throughput", x + width - 30, y + 22, width, height)
                                .text("Out: " + 12 + "/m");
                            outputCount= buildTextNode(parent, "monitoring throughput", x + width - 30, y + 33, width, height)
                                .text("");
                        }
                    }

                    errNode=buildTextNode(parent, "monitoring throughput error", x + width - 30, y + height - 6, width, height)
                        .text("e: " + 12);

                    var barHeight = height - 10;
                    buildSVGElement(parent,'rect').attr("fill", "white")
                        .attr("stroke", "black")
                        .attr("width", 20)
                        .attr("height", barHeight)
                        .attr("x", x + width - 25)
                        .attr("y", y + 5)
                        .attr("rx", 4)
                        .attr("ry", 4)
                        .text(desc.label);

                    var bar = buildSVGElement(parent,'rect').attr("fill", "#0E90D2")
                        .attr("width", 20)
                        .attr("height", barHeight)
                        .attr("x", x + width - 25)
                        .attr("y", y + 5)
                        .attr("rx", 4)
                        .attr("ry", 4)
                        .text(desc.label);
                    $scope.handlers[name] = function (maxCount) {
                        var q = $scope.status.queues[name];
                        var q_out = $scope.status.queues[name + "_OUT"];
                        if (titleElt) {
                            titleElt.text(desc.label + " [" + q.consumerCount + "]");
                        }
                        if (q_out) {
                            q.throughputOut = q_out.throughputOut;
                            if (outputCount) {
                                outputCount.text("Out [" + q_out.consumerCount + "]: " + q_out.count);
                                if (q_out.count > 0) {
                                    outputCount.attr("fill", "black");
                                    outputCount.attr("font-weight", "bold");
                                } else {
                                    outputCount.attr("fill", "gray");
                                    outputCount.attr("font-weight", "normal");
                                }
                            }
                        }
                        countNode.text(q.count);
                        var ratio = maxCount ? (q.count) / maxCount : 0;
                        var h = barHeight * ratio;
                        if (h < 2) {// refuse 1px height
                            h = 2;
                        }
                        var t = barHeight - h;

                        bar.attr("height", h).attr("y", y + 5 + t);
                        if (desc.input && desc.label) {
                            inputNode.text("In: " + q.throughputIn + "/m");
                        }
                        if (desc.output && desc.label) {
                            outNode.text("Out: " + q.throughputOut + "/m");
                        }
                        var errCount = $scope.status.errors[name];
                        if (q_out) {
                            errCount += $scope.status.errors[name + "_OUT"];
                        }
                        errNode.text("E: " + errCount);
                        if (errCount === 0) {
                            errNode.attr("fill", "gray");
                        } else {
                            errNode.attr("fill", "red");
                        }
                    };
                    // Transparent element to make the queue clickable
                    return buildMaskForClick(rect, parent);
                }

                function buildButtonNode(e, select) {
                    var parent = e.parentNode;
                    select.remove();
                    return buildMaskForClick(parent.childNodes[1], parent);
                }

                function buildExchangeNode(e, select, name) {
                    // dirty dom lookup
                    var rect = e.parentNode.previousElementSibling.getBBox();
                    var rectX = rect.x;
                    var rectY = rect.y;
                    var y = parseFloat(select.attr("y"));
                    var x = parseFloat(select.attr("x"));
                    if (rectY + 20 < y) { // dirty check to see if the node is under
                        var width = rect.width;
                        select.attr("x", rectX + width / 2);
                        select.attr("text-anchor", "middle");
                    } else if (rectX > x) { // text is a lefty
                        select = select.attr("fill", "black")
                            .attr("stroke", "none")
                            .attr("width", e.getBBox().width)
                            .attr("height", e.getBBox().height)
                            .attr("x", x + e.getBBox().width)
                            .attr("text-anchor", "end")
                            .attr("y", y);
                    }
                    $scope.handlers[name] = function () {
                        select.text($scope.status.exchanges[name] + "/m");
                    };
                    return null; // cant click!
                }

                function buildErrorCountNode(e, select, name) {
                    var parent = e.parentNode;
                    var mask = parent.childNodes[0];
                    /* jshint ignore:start */
                    if (mask instanceof Text) {
                        mask = parent.childNodes[1];
                    }
                    /* jshint ignore:end */
                    var bbox = parent.getBBox();
                    select.remove();

                    var countNode =  buildTextNode(parent, "monitoring error-count", bbox.x + bbox.width / 2,bbox.y + bbox.height / 2 + 15, bbox.width,  bbox.height)
                    $scope.handlers[name] = function () {
                        countNode.text($scope.status.totalErrors);
                    };
                    return buildMaskForClick(mask, parent);
                }

                function buildScheduleNode(e, select, desc, name) {
                    var rect = e.previousElementSibling;
                    var parent = e.parentNode;
                    var bbox = rect.getBBox();
                    select.remove();

                    var x = bbox.x;
                    var y = bbox.y;
                    var height = bbox.height;
                    var width = bbox.width;
                    var labelNode = buildTextNode(parent, "monitoring scheduling", x + width / 2, y + 15).text("");
                    var triggerNode = buildTextNode(parent, "monitoring trigger", x + width / 2, y + 25).text("");
                    var nextNode = buildTextNode(parent, "monitoring trigger", x + width / 2, y + 35).text("");

                    var circleStatus = buildSVGElement(parent,'circle')
                        .attr("class", "")
                        .attr("cx",x+width-10)
                        .attr("cy",y+height/2)
                        .attr("r",6);
                    $scope.handlers[name] = function () {
                        var scheduledMessage = $scope.status.scheduledMessages[name];
                        if (scheduledMessage) {
                            var next = $filter('date')(scheduledMessage.scheduledMessage.nextExecution, "dd MMM hh:mm")
                            nextNode.text("next: " + next);
                            triggerNode.text(scheduledMessage.triggerHuman);
                            circleStatus.attr("class", scheduledMessage.scheduledMessage.status.toLowerCase());
                        } else {
                            triggerNode.text("unscheduled");
                            nextNode.text("");
                            circleStatus.attr("class", "")
                        }
                        labelNode.text(desc.label);
                    };
                    var maskForClick = buildMaskForClick(rect, parent);
                    maskForClick.bind("click", function (e) {
                        $scope.tooltip.selectedScheduling = name;
                        $scope.tooltip.show = true;
                        $scope.tooltip.left = e.layerX + 'px';
                        if (desc.placement == "bottom") {
                            $scope.tooltip.top =  e.layerY + 'px';
                            $scope.tooltip.bottom = '';
                        } else {
                            $scope.tooltip.top = '';
                            $scope.tooltip.bottom = ($scope.element[0].getBoundingClientRect().height - e.layerY)+"px";
                        }

                        if ($scope.status.scheduledMessages[name]) {
                            var st = $scope.status.scheduledMessages[name].scheduledMessage;
                            var next = $filter('date')(st.nextExecution, "dd MMM HH:mm");
                            var lastExecution = $filter('date')(st.lastExecution.lastActualExecutionTime, "dd MMM HH:mm");
                            var lastCompletion = $filter('date')(st.lastExecution.lastCompletionTime, "dd MMM HH:mm");
                            $scope.tooltip.text = $sce.trustAsHtml("<div class='title'>" + desc.label + "</div>"
                                + "<div class='status'>Status: " + st.status + "</div>"
                                + "<div>When: " + $scope.status.scheduledMessages[name].triggerHuman + "</div>"
                                + "<div>Next execution: " + next + "</div>"
                                + "<div>Last execution: " + lastExecution + "</div>"
                                + "<div>Last completion: " + lastCompletion + "</div>"
                                + "<div>Last execution status: " + st.lastExecution.status + "</div>"
                            );
                            $scope.tooltip.enable={schedule:(st.status=="CANCELLED"),unschedule:true,force:(st.status=="PLANNED")}
                        } else {
                            $scope.tooltip.enable={schedule:true,unschedule:false,force:false}
                            $scope.tooltip.text = $sce.trustAsHtml("<div class='title'>" + desc.label + "</div>");
                        }
                        $scope.$apply();
                    });
                    return maskForClick;
                }

                $scope.handlers = {};
                $scope.updateSVG = function () {
                    if (!$scope.queues || !$scope.status) {
                        return;
                    }
                    var maxCount = null;
                    var totalMessages = 0;
                    var totalErrors = 0;
                    for (var q in $scope.status.queues) {
                        if (!$scope.queues[q]) {
                            continue;
                        }
                        var count = $scope.status.queues[q].count;
                        var errors = $scope.status.errors[q];
                        totalMessages += count;
                        totalErrors += errors;
                        if (maxCount === null || count > maxCount) {
                            maxCount = count;
                        }
                    }
                    $scope.description.count = {
                        messages: totalMessages,
                        errors: totalErrors
                    };
                    for (var h in $scope.handlers) {
                        if (!$scope.handlers.hasOwnProperty(h)) {
                            continue;
                        }
                        $scope.handlers[h](maxCount);
                    }
                };
                $scope.init = function () {
                    $scope.src = $scope.description.svg;
                    var data = require("../assets/svg/core.svg");
                    data = data.replace(new RegExp("id=\"", "g"), "id=\"svg" + $scope.src);
                    data = data.replace(new RegExp("url\\(#", "g"), "url("+(ON_PROD?"/queues":"")+"#svg" + $scope.src);
                    data = ( new window.DOMParser() ).parseFromString(data, "text/xml");

                    // references all used queues
                    $scope.queues = {};
                    // dirty again
                    var container = angular.element($scope.element);
                    var svg = angular.element(data).find("svg");

                    container.append(svg);
                    $scope.svg = svg;
                    angular.forEach(svg.find("text"), function (e, i) {
                        var select = angular.element(e);
                        var name = select.text();
                        var desc = $scope.nodes[name];
                        if (!desc) {
                            return;
                        }
                        var elt;
                        if (desc.type === "exchange") {
                            elt = buildExchangeNode(e, select, name);
                        } else if (desc.type === "queue") {
                            $scope.queues[name] = 1;
                            $scope.queues[name + "_OUT"] = 1;
                            elt = buildQueueNode(e, select, desc, name);
                        } else if (desc.type === "button") {
                            elt = buildButtonNode(e, select);
                        } else if (desc.type === "error_count") {
                            elt = buildErrorCountNode(e, select);
                        } else if (desc.type === "schedule") {
                            elt = buildScheduleNode(e, select, desc, name);
                        }
                        if (elt && (desc.placement || desc.url)) {
                            elt.bind("click", function (e) {
                                if (desc.url) {
                                    $scope.$apply(function () {
                                        $location.path(desc.url);
                                    });
                                } else {
                                    e.stopPropagation();
                                }
                            });
                            if (desc.url) {
                                return;
                            }
                        }
                    });

                    $scope.updateSVG();
                    container.bind("click", function (e) {
                        if ($scope.tooltip.show) {
                            $scope.tooltip.show = false;
                        }
                        $scope.$apply();
                    });
                };
            }]
        };
    });
})();