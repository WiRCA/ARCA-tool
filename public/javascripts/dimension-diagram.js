/*
 * Copyright (C) 2011 - 2013 by Eero Laukkanen, Risto Virtanen, Jussi Patana,
 * Juha Viljanen, Joona Koistinen, Pekka Rihtniemi, Mika Kekäle, Roope Hovi,
 * Mikko Valjus, Timo Lehtinen, Jaakko Harjuhahto, Jonne Viitanen, Jari Jaanto,
 * Toni Sevenius, Anssi Matti Helin, Jerome Saarinen, Markus Kere
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

// Current zoom level
var zoomLevel = 1;
// Minimum zoom level
var zoomMin = 1 / 32;
// Maximum zoom level
var zoomMax = 2;
// Steps in zoom slider
var zoomSteps = 64;
// Placeholder for the #radial_menu jQuery object
var $radial_menu;
// Placeholder for the ForceDirected object
var fd;
// Selected edge data
var selectedEdge;
// Current weighting value
var weightingValue = 0;
// Current simplicity value
var simplicityValue = 0;
// Timer for updating the graph after using a slider
var sliderTimer = null;

var WHAT = 1;
var WHERE = 2;
var WHAT_STEP = 22;

// Add the function $.disableSelection() to jQuery
(function ($) {
    $.fn.disableSelection = function () {
        return this.each(function () {
            $(this)
                .attr('unselectable',   'on')
                .css({'-moz-user-select': 'none',
                      '-o-user-select': 'none',
                      '-khtml-user-select': 'none',
                      '-webkit-user-select': 'none',
                      '-ms-user-select': 'none',
                      'user-select': 'none'})
                .each(function () {
                          $(this)
                              .attr('unselectable', 'on')
                              .bind('selectstart', function () {
                                  return false;
                              });
                      });
        });
    };
})(jQuery);

/**
 * Returns the cause name list if the relation is connected to "rootcause"
 * @param causeArray array of causes that are related to chosen classification
 * @return {Array} the cause names that are related to chosen relation
 */
function causeNamesForRootRelation(causeArray) {
    var nameArray = new Array();
    var rcaCaseName = window.arca.rootNode.name;
    for (var causeId in causeArray) {
        for (var neighbourId in causeArray[causeId].neighbourCauseIds) {
            if (causeArray[causeId].neighbourCauseIds[neighbourId].name === rcaCaseName) {
                nameArray.push(causeArray[causeId].name);
            }
        }
    }
    return nameArray;
}

/**
 * Populates the related cause modal window with the names of the related causes
 */
function populateRelatedCauses() {
    // Empty the list
    //var container = $('#causeNameList');  //This doesn't work
    var container = document.getElementById('causeNameList');
    container.innerHTML = '';
    var nameArray = new Array();

    var causeNames = selectedEdge.nodeFrom.data.causeNames;
    var toCauseNames = selectedEdge.nodeTo.data.causeNames;

    if (causeNames === undefined) {
        nameArray = causeNamesForRootRelation(toCauseNames);
    } else if (toCauseNames === undefined) {
        nameArray = causeNamesForRootRelation(causeNames);
    } else {
        var causeId, toCauseId, neighbourId;
        for (causeId in causeNames) {
            if (!causeNames.hasOwnProperty(causeId)) { continue; }
            for (neighbourId in causeNames[causeId].neighbourCauseIds) {
                for (toCauseId in toCauseNames) {
                    if (toCauseId === neighbourId) {
                        if ($.inArray(causeNames[causeId].name, nameArray) == -1) {
                            nameArray.push(causeNames[causeId].name);
                        }
                        if ($.inArray(toCauseNames[toCauseId].name, nameArray) == -1) {
                            nameArray.push(toCauseNames[toCauseId].name);
                        }
                    }
                }
            }
        }
    }

    var name;
    for (name in nameArray) {
        var new_element = document.createElement('li');
        new_element.innerHTML = nameArray[name];
        container.insertBefore(new_element, container.firstChild);
    }
}

/**
 * Initializes the graph for the canvas
 */
function initGraph(graph_id, radial_menu_id, width, height, respondToResize) {
       /*
    sessionStorage.clear();
    sessionStorage.setItem("rcaCaseId", window.arca.rcaCaseId);
    sessionStorage.setItem("openedEdges", JSON.stringify(new Array()));
     */

    if(sessionStorage.getItem("rcaCaseId") && sessionStorage.getItem("rcaCaseId") != window.arca.rcaCaseId) {
        sessionStorage.clear();
        sessionStorage.setItem("rcaCaseId", window.arca.rcaCaseId);
        sessionStorage.setItem("openedEdges", JSON.stringify(new Array()));
    } else if(!sessionStorage.getItem("rcaCaseId")) {
        sessionStorage.clear();
        sessionStorage.setItem("rcaCaseId", window.arca.rcaCaseId);
        sessionStorage.setItem("openedEdges", JSON.stringify(new Array()));
    }

    $radial_menu = $("#" + radial_menu_id);
    $radial_menu.radmenu({
        // The list class inside which to look for menu items
        listClass: 'list',
        // The items - NOTE: the HTML inside the item is copied into the menu item
        itemClass: 'item',
        // The menu radius in pixels
        radius: 25,
        // The animation speed in milliseconds
        animSpeed: 2000,
        // The X axis offset of the center
        centerX: -20,
        // The Y axis offset of the center
        centerY: -40,
        // The name of the selection event
        selectEvent: "click",

       // The actual functionality for the menu
       onSelect: function ($selected) {
           $('.twipsy').remove();

           // Opening of the edge
           if ($selected[0].id == "radmenu-event-openEdge") {
               openEdge($selected);
               jQuery("#radial_menu").radmenu("hide");
           }

           // naming the edge
           else if ($selected[0].id == "radmenu-event-nameEdge") {
               alert("not implemented yet");
               $radial_menu.radmenu("hide");
           }
           // closing the edge
           else if ($selected[0].id == "radmenu-event-closeEdge") {
               closeEdge($selected);
               jQuery("#radial_menu").radmenu("hide");
           }
           // show related causes
           else if ($selected[0].id == "radmenu-event-showRelatedCauseNames") {
               $('#showCauses-modal').modal('show');
               populateRelatedCauses();
               $radial_menu.radmenu("hide");
           }
       },

       // The base angle offset in degrees
       angleOffset: 90
   });

    $("#" + graph_id).css("width", width);
    $("#" + graph_id).css("height", height);

    if (respondToResize) {
        var resizeTimer;
        $(window).resize(function () {
            clearTimeout(resizeTimer);
            resizeTimer = setTimeout(doResize, 100);
        });
    }

    fd = new $jit.ForceDirected({
        // ID of the visualization container
        injectInto: graph_id,

        // Dimensions
        width: width,
        height: height,

        // Enable zooming and panning by scrolling and drag/drop
        Navigation: {
            enable: true,
            panning: 'avoid nodes',
            zooming: 0
        },

        // Change node and edge styles.
        // These properties are also set per node with dollar prefixed data-properties in the JSON structure.
        Node: {
            overridable: true,
            color: "#ddffdd",
            type: "circle",
            dim: 1
        },

        Edge: {
            overridable: true,
            type: 'line',
            dim: 75,
            color: '#23A4FF',
            lineWidth: 2
        },

        // Native canvas text styling
        Label: {
            type: 'HTML', // Native or HTML
            size: 16,
            style: 'bold'
        },

        // Tooltips
        Tips: {
            enable: false
        },

        // Add node events
        Events: {
            enableForEdges: true,
            enable: true,

            // Change cursor style when the mouse cursor is on a non-root node
            onMouseEnter: function (node, eventInfo, e) {
                if(node.data.dimension == WHERE) {
                    fd.canvas.getElement().style.cursor = 'move';
                }
            },

            // Restore mouse cursor when moving outside a node
            onMouseLeave: function (node, eventInfo, e) {
                fd.canvas.getElement().style.cursor = '';
            },

            // Update node positions when dragged
            onDragMove: function (node, eventInfo, e) {
                if (node.data.dimension == WHAT) {
                    return;
                }

                var pos = eventInfo.getPos();
                var dx = node.pos.x - pos.x;
                var dy = node.pos.y - pos.y;
                node.pos.setc(pos.x, pos.y);
                node.setPos(new $jit.Complex(pos.x, pos.y), 'end');

                node.eachAdjacency(function(adj) {
                    var nodeTo = adj.nodeTo;
                    if(nodeTo.data.dimension == WHAT) {
                        var upDown = checkUpDown(nodeTo, node);

                        nodeTo.setPos(new $jit.Complex(nodeTo.pos.x - dx,
                                                           nodeTo.pos.y - dy), 'current');

                        var upDown = checkUpDown(nodeTo, node);
                        if (nodeTo.pos.y < node.pos.y && upDown == 1) {
                            nodeTo.setPos(new $jit.Complex(nodeTo.pos.x, nodeTo.pos.y + 48), 'current');
                        }
                        if (nodeTo.pos.y > node.pos.y && upDown == -1) {
                            nodeTo.setPos(new $jit.Complex(nodeTo.pos.x, nodeTo.pos.y - 48), 'current');
                        }


                        // Dig other side what
                        nodeTo.eachAdjacency(function(n2) {
                            var n2to = n2.nodeTo;
                            if (n2to.data.dimension == WHAT) {
                                n2to.eachAdjacency(function(n3) {
                                    var n3to = n3.nodeTo;
                                    if (n3to.data.dimension == WHERE) {
                                        var upDown = checkUpDown(n3to, node);
                                        if (n3to.pos.y > n2to.pos.y && upDown == -1) {
                                            n2to.setPos(new $jit.Complex(n2to.pos.x, n2to.pos.y + 48), 'current');
                                        }
                                        if (n3to.pos.y < n2to.pos.y && upDown == 1) {
                                            n2to.setPos(new $jit.Complex(n2to.pos.x, n2to.pos.y - 48), 'current');
                                        }
                                    }
                                });
                            }
                        });
                    }
                });

                fd.plot();
            },

            onDragEnd: function (node, eventInfo, e) {
                var nodeParent = fd.graph.getNode(node.data.parent);
                var xPos, yPos;
                if (nodeParent != undefined) {
                    xPos = node.getPos('end').x - nodeParent.getPos('end').x;
                    yPos = node.getPos('end').y - nodeParent.getPos('end').y;
                } else {
                    xPos = node.getPos('end').x;
                    yPos = node.getPos('end').y;
                }
            },

            // Implement the same handler for touchscreens
            onTouchMove: function (node, eventInfo, e) {
                $jit.util.event.stop(e); // Stop the default touchmove event
                this.onDragMove(node, eventInfo, e);
            },

            // Implement zooming for nodes
            onMouseWheel: function (delta, e) {
                $jit.util.event.stop(e);
                var zoom = (50 / 1000 * delta) + 1;
                applyZoom(zoom, true);
            },

            onClick: function(node, eventInfo, e) {
                if (eventInfo.getEdge()) {
                    show_edge_radial_menu(eventInfo, node);
                    fd.plot();
                } else {
                    $radial_menu.radmenu("hide");
                    fd.plot();
                }
            }
        },

        // Number of iterations for the FD algorithm
        iterations: 400,

        // Edge length
        levelDistance: 200,

        // Change node styles when DOM labels are placed or moved.
        onPlaceLabel: function (domElement, node) {
            var style = domElement.style;
            var left = parseInt(style.left);
            var top = parseInt(style.top);
            var w = parseInt(domElement.offsetWidth);
            var h = parseInt(domElement.offsetHeight);
            style.left = (left - (w / 2)) + 'px';
            style.top = (top - (h / 2)) + 'px';
            node.data.$height = h;
            node.data.$width = w;
        },

        // Add the nodeBox class to the label, add text
        onCreateLabel: function (domElement, node) {
            if (node.id == 0) {
                $(domElement).addClass('rootNodeBox');
            } else {
                if (node.data.dimension == WHAT) {
                    $(domElement).addClass('whatNodeBox');
                } else {
                    $(domElement).addClass('nodeBox');
                }
            }
            $(domElement).html(node.name);
        }
    });
    implementEdgeTypes();

    // Initialize Twipsy
    $("a[rel=twipsy]").twipsy({live: true, placement: 'left'});
    $('a[rel=twipsy]').twipsy('show');

    $('#' + graph_id).live("mousedown", function(event) {
        $radial_menu.radmenu("hide");
        $('.popover').remove();
    });

    fd.plot();
}


/**
 * Resizes the ForceDirected canvas according to the window size
 */
function doResize() {
    //fd.canvas.resize(window.innerWidth, window.innerHeight);
    $("#infovis").css("width", window.innerWidth);
    $("#infovis").css("height", window.innerHeight);
    //applyZoom(1, false);
}


/**
 * Returns a new dummy node (makes showSimpleGraph() easier)
 * @param data the classification data from window.arca.classifications
 * @return object
 */
function newNode(data, id, type) {
    var name = data.title;

    if (data.dimension == WHAT) {
        name = data.abbreviation;
        if (!name) {
            name = data.title.substring(0,2);
        }
        name = '<a href="#" rel="twipsy" title="'+data.title+'">'+name+'</a>';
    }
    return {
        id: id,
        name: name,
        data: data,
        adjacencies: []
    };
}

function radmenu_fadeIn(selectedEdge) {
    jQuery("#radial_menu").radmenu(
        "show",
        function (items) {
            items.fadeIn(400);
        });
}

function radmenu_fadeOut () {
    jQuery("#radial_menu").radmenu(
        "hide",
        function (items) {
            items.fadeOut(4000);
        }
    );
}

function getEdgeParentId(ed) {
    return ed.substring(0,ed.indexOf(":"));
}
function getEdgeChildId(ed) {
    return ed.substring(ed.indexOf(":")+1);
}


function closeEdge(selected) {
    var openedEdges = JSON.parse(sessionStorage.getItem("openedEdges"));
    for (var i = 0; i < openedEdges.length; i++) {
        if ((openedEdges[i].firstId == getEdgeParentId(selectedEdge.nodeFrom.id) &&
             openedEdges[i].secondId == getEdgeParentId(selectedEdge.nodeTo.id)) ||
            (openedEdges[i].firstId == getEdgeParentId(selectedEdge.nodeTo.id) &&
             openedEdges[i].secondId == getEdgeParentId(selectedEdge.nodeFrom.id))) {
            openedEdges.splice(i,1);
        }
    }
    sessionStorage.setItem("openedEdges", JSON.stringify(openedEdges));
    showSimpleGraph(0, 0, []);
}

function openEdge(selected) {
    var openedEdges = JSON.parse(sessionStorage.getItem("openedEdges"));

    openedEdges.push({
        open: true,
        firstId: selectedEdge.nodeFrom.id,
        secondId: selectedEdge.nodeTo.id
    });

    sessionStorage.setItem("openedEdges", JSON.stringify(openedEdges));

    showSimpleGraph(0, 0, []);
}

function configurationView() {
    $('#configurationArea').slideToggle();
}

function show_edge_radial_menu(eventInfo, mouseEvent) {
    selectedEdge = eventInfo.getEdge();
    var nodeFrom = eventInfo.getEdge().nodeFrom;
    var nodeTo = eventInfo.getEdge().nodeTo;

    if (nodeFrom.data.dimension == WHAT) {
        var fromPos = $("#" + getEdgeParentId(nodeFrom.id)).offset();
    } else {
        var fromPos = $("#" + nodeFrom.id).offset();
    }
    if (nodeTo.data.dimension == WHAT) {
        var toPos = $("#" + getEdgeParentId(nodeTo.id)).offset();
    } else {
        var toPos = $("#" + nodeTo.id).offset();
    }
    var widthMid = Math.abs((fromPos.left - toPos.left)/2);
    if (fromPos.left < toPos.left) {
        widthMid = fromPos.left + widthMid;
    }
    else {
        widthMid = toPos.left + widthMid;
    }
    var heightMid = Math.abs((fromPos.top - toPos.top)/2);
    if (fromPos.top < toPos.top) {
        heightMid = fromPos.top + heightMid;
    }
    else {
        heightMid = toPos.top + heightMid;
    }
    jQuery("#radial_menu").radmenu("opts").radius = 30;

    // show the menu directly over the placeholder
    $radial_menu.css({
                         "left": widthMid + 50 + "px",
                         "top": heightMid + 16 + "px"
                              }).show();

     // jQuery("#radial_menu").radmenu("show");
    radmenu_fadeIn(selectedEdge);
    $("#radial_menu").disableSelection();
    if (selectedEdge.nodeTo.data.dimension == WHERE && selectedEdge.nodeFrom.data.dimension == WHERE) {
        jQuery("#radial_menu").radmenu("items")[0].style.visibility = "visible";
        jQuery("#radial_menu").radmenu("items")[1].style.visibility = "visible";
        jQuery("#radial_menu").radmenu("items")[2].style.visibility = "hidden";
    } else if (selectedEdge.nodeTo.data.dimension == WHAT && selectedEdge.nodeFrom.data.dimension == WHAT) {
        jQuery("#radial_menu").radmenu("items")[0].style.visibility = "hidden";
        jQuery("#radial_menu").radmenu("items")[1].style.visibility = "hidden";
        jQuery("#radial_menu").radmenu("items")[2].style.visibility = "visible";

    }
}


/**
 * Returns a suitable color for an edge with the given amount of total node likes.
 * @param likes the amount of likes the causes of a relation have
 * @return color string
 */
function getColor(likes) {
    if (likes > 15) {
        return "#FF0000";
    }
    if (likes > 9) {
        return "#FF3399";
    }
    if (likes > 5) {
        return "#FF66FF";
    }
    if (likes > 2) {
        return "#CCCCFF";
    }
    if (likes > 0) {
        return "#6666FF";
    }
    return "#000099";
}


/**
 * Returns a suitable glow amount for an edge with the given amount of corrections.
 * @param corrections the amount of corrections the causes of a relation have
 * @return a suitable glow strength number
 */
function getGlow(corrections) {
    if (corrections > 15) {
        return 25;
    }
    if (corrections > 9) {
        return 20;
    }
    if (corrections > 5) {
        return 15;
    }
    if (corrections > 2) {
        return 10;
    }
    if (corrections > 0) {
        return 5;
    }
    return 0;
}


/**
 * Returns a suitable line width for a relation with the given strength
 * @param strength the strength of a relation
 * @return a suitable line width in pixels
 */
function getWeight(strength) {
    return 1 + (Math.log(strength) / Math.log(2));
}


/**
 * Shows the graph, filtering nodes and edges by their relevance
 * @param minNodeRelevance minimum visible node relevance
 * @param minEdgeRelevance minimum visible edge relevance
 * @param keepNodes list of node IDs to keep regardless of relevance
 */
function showSimpleGraph(minNodeRelevance, minEdgeRelevance, keepNodes,
    forceColorRelations, forceGlowRelations, forceWeightRelations) {

    var openedEdges = JSON.parse(sessionStorage.getItem("openedEdges"));
    var data = window.arca.relationMap.simpleRelations;
    var graphData = [window.arca.rootNode];

    // Settings
    var colorRelations = forceColorRelations || ($('input:radio[name=groupProposed]:checked').val() == "1");
    var glowRelations = forceGlowRelations || ($('input:radio[name=groupCorrections]:checked').val() == "1");
    var weightRelations = forceWeightRelations || ($('input:radio[name=groupCauses]:checked').val() == "1");

    // This stores the index of the classification node in graphData, keyed by the classification ID
    var created = [];

    // Placeholders
    var firstNodeData, secondNodeData, relationData, color, glow;

    // Filter the relation data to the actual graph data
    for (var first in data) {
        if (!data.hasOwnProperty(first)) { continue; }

        // Filter irrelevant nodes
        firstNodeData = arca.classifications[first];
        if (firstNodeData.relevance < minNodeRelevance && keepNodes.indexOf(first) == -1) { continue; }

        // Create the node if necessary
        if (!created.hasOwnProperty(first)) {
            graphData.push(newNode(firstNodeData, firstNodeData.id));
            created[first] = graphData.length - 1;
        }

        for (var second in data[first]) {
            if (!data[first].hasOwnProperty(second)) { continue; }

            relationData = data[first][second];

            // Filter irrelevant target nodes
            secondNodeData = window.arca.classifications[second];
            var secondNodeDataWithRelevance = data[second];
            if (secondNodeData.relevance < minNodeRelevance) { continue; }

            // Filter irrelevant edges
            if (relationData.strength < minEdgeRelevance) { continue; }

            // Create the node if necessary
            if (!created.hasOwnProperty(second)) {
                graphData.push(newNode(secondNodeData, secondNodeData.id, type));
                created[second] = graphData.length - 1;
            }

            var lineWidth;
            // Check if relation lines should be weighted
            if (weightRelations) {
                lineWidth = getWeight(relationData.strength);
            } else {
                lineWidth = 1;
            }

            var color;
            // Check if relation should be colored because they are liked
            if (colorRelations) {
                color = getColor(relationData.likes);
            } else {
                color = "#0000aa";
            }

            var glow;
            // Check if relation should be glowed because there are corrections
            if (glowRelations) {
                glow = getGlow(relationData.corrections);
            } else {
                glow = 0;
            }
            var type = "relationLine";
            if (first == second) {
                type = "circleline";
            }
            if (first.id == 0 || second.id == 0) {
                //type = "line";
            }
            if (openedEdges.length == 0) {
                graphData[created[first]].adjacencies.push({
                   nodeTo: second,
                   "data": {
                       "$dim": 15,
                       "$color": color,
                       "$weight": 2,
                       "$type": type,
                       "$lineWidth": lineWidth,
                       "$glow": glow
                   }
                });
            }
            var found = false;
            for (var i = 0; i < openedEdges.length; i++) {
                var openedEdge = openedEdges[i];

                // Add the adjacency
                if ((openedEdge.firstId == first && openedEdge.secondId == second) ||
                    (openedEdge.firstId == second && openedEdge.secondId == first)) {
                    found = true;

                    // Open edge found
                    var pairRelations = window.arca.relationMap.pairRelations;
                    for(var index in pairRelations) {

                        // to get the only key there is. Looks stupid but works
                        for (key in pairRelations[index]) openFirst = key;
                        for (var openSecond in pairRelations[index][openFirst]) {

                            var firstParentId = getEdgeParentId(openFirst);
                            for (var openSecond in pairRelations[index][openFirst]) {
                                var secondParentId = getEdgeParentId(openSecond);

                                if ((openedEdge.firstId == firstParentId && openedEdge.secondId == secondParentId) ||
                                    (openedEdge.firstId == secondParentId && openedEdge.secondId == firstParentId)) {

                                    var firstChildId = openFirst;
                                    var secondChildId = openSecond;

                                    var nNode = newNode(window.arca.classifications[getEdgeChildId(openFirst)], firstChildId, type);
                                    nNode.adjacencies = [];
                                    graphData.push(nNode);
                                    created[firstChildId] = graphData.length - 1;

                                    nNode = newNode(window.arca.classifications[getEdgeChildId(openSecond)], secondChildId, type);
                                    nNode.adjacencies = [];
                                    graphData.push(nNode);
                                    created[secondChildId] = graphData.length - 1;

                                    // Between parent and child
                                    graphData[created[firstParentId]].adjacencies.push({
                                        nodeTo: firstChildId,
                                        "data": {
                                            "$dim": 15,
                                            "$color": "#ffffff",
                                            "$type": type,
                                            "$weight": 2,
                                            "$lineWidth": 2,
                                            "$glow": glow
                                        }
                                    });
                                    graphData[created[secondParentId]].adjacencies.push({
                                        nodeTo: secondChildId,
                                        "data": {
                                            "$dim": 15,
                                            "$color": "#ffffff",
                                            "$type": type,
                                            "$weight": 2,
                                            "$lineWidth": 2,
                                            "$glow": glow
                                        }
                                    });
                                    // Between childs
                                    graphData[created[firstChildId]].adjacencies.push({
                                       nodeTo: secondChildId,
                                       "data": {
                                           "$dim": 15,
                                           "$color": color,
                                           "$weight": 2,
                                           "$type": type,
                                           "$lineWidth": lineWidth,
                                           "$glow": glow
                                       }
                                    });
                                }
                            }
                        }
                    }
                }
            }
            // If no opened node is found
            if (first.id == 0 || second.id == 0) {
                type = "relationLine";
            }
            if(!found) {
                graphData[created[first]].adjacencies.push({
                    nodeTo: second,
                    "data": {
                       "$dim": 15,
                       "$color": color,
                       "weight": 2,
                       "$type": type,
                       "$lineWidth": lineWidth,
                       "$glow": glow
                    }
                });
            }
        }
    }

    // Add the root node connections
    for (first in arca.relationMap.rootRelations) {
        if (!arca.relationMap.rootRelations.hasOwnProperty(first)) { continue; }
        relationData = arca.relationMap.rootRelations[first];
        color = colorRelations ? getColor(relationData.likes) : '#0000aa';
        glow = glowRelations ? getGlow(relationData.corrections) : 0;
        lineWidth = weightRelations ? getWeight(relationData.strength) : 1;

        // Create the relation if necessary
        if (relationData.strength < minEdgeRelevance) { continue; }

        // Create the node if necessary
        if (!created.hasOwnProperty(first)) {
            firstNodeData = arca.classifications[first];
            if (firstNodeData.relevance < minNodeRelevance && keepNodes.indexOf(first) == -1) { continue; }
            graphData.push(newNode(firstNodeData));
            created[first] = graphData.length - 1;
        }

        // Add the adjacency
        graphData[created[first]].adjacencies.push({
            nodeTo: 0,
            "data": {
                "$dim": 15,
                "$color": color,
                "$lineWidth": lineWidth,
                "$glow": glow,
                "weight": 2
            }
        });
    }

    // If there are no nodes, stop here and go grab a drink or something
    if (graphData.length == 0) { return; }

    // Load the data, disable selections from each node
    fd.loadJSON(graphData);
    $("#graph-label div.node").disableSelection();


    // Set the initial positions for the nodes, radially around the root node
    var i = 0, x, y;

    fd.graph.eachNode(function (node) {
        if (node.id == 0) {
            x = 0;
            y = 0;
        } else {
            x = fd.config.levelDistance * Math.cos(i * 2 * Math.PI / (graphData.length - 1));
            y = fd.config.levelDistance * Math.sin(i * 2 * Math.PI / (graphData.length - 1));
        }
        node.setPos(new $jit.Complex(x, y), 'current');
        ++i;
    });


    fd.graph.eachNode(function (node) {
        if(node.data.dimension == WHERE) {
            var nodeArr = new Array();
            node.eachAdjacency(function(adj) {
                if (adj.nodeTo.data.dimension == WHAT) {
                    nodeArr.push(adj.nodeTo);
                }
            });

            if (nodeArr.length > 0) {
                firstPos = ((nodeArr.length - 1) * (WHAT_STEP / 2));
                for (i = 0; i < nodeArr.length; i++) {
                    var x = node.getPos("current").x - firstPos + (i * WHAT_STEP);
                    var upDown = checkUpDown(nodeArr[i], node);
                    var y = node.getPos("current").y + (24*upDown);
                    nodeArr[i].setPos(new $jit.Complex(x, y), "current");
                }
            }
        }
    });
    fd.plot();

    //fd.animate({'duration': 1000})
    // Let the force algorithm find nice minima for the nodes and display them
    /*fd.computeIncremental({
        iter: 20,
        property: 'end',
        onComplete: function() { fd.animate({'duration': 1000}); }
    });  */
}

function checkUpDown(nodeTo, node) {
    var upDown = 1;
    nodeTo.eachAdjacency(function(n2) {
        if (n2.nodeTo.data.dimension == WHAT) {
            n2.nodeTo.eachAdjacency(function(n3) {
                if (n3.nodeTo.data.dimension == WHERE) {
                    if(n3.nodeTo.getPos("current").y < node.getPos("current").y) {
                        upDown = -1;
                    } else {
                        upDown = 1;
                    }
                }
            });
        }
    });
    return upDown;
}

// Zoom functions //

/**
 * Increments zoom by one step
 */
function incZoomSlider() {
    var sliderValue = $("#slider-vertical").slider("value");
    applyZoom(((zoomMax - zoomMin) / zoomSteps * (sliderValue + 1) + zoomMin) / zoomLevel, true);
}


/**
 * Decrements zoom by one step
 */
function decZoomSlider() {
    var sliderValue = $("#slider-vertical").slider("value");
    applyZoom(((zoomMax - zoomMin) / zoomSteps * (sliderValue - 1) + zoomMin) / zoomLevel, true);
}


/**
 * Applies the given zoom level to the canvas
 * @param newLevel the new zoom level
 * @param updateSlider whether to update the slider or not
 */
function applyZoom(newLevel, updateSlider) {
    // Hides a radial menu when zoomed as the scaling does not quite work
    jQuery("#radial_menu").radmenu("hide");
    jQuery("#radial_menu_opened_edge").radmenu("hide");
    $('.popover').remove();

    // Reset the zoom of the canvas if the canvas has been moved or resized
    if (fd.canvas.scaleOffsetX != zoomLevel) {
        newLevel = zoomLevel / fd.canvas.scaleOffsetX;
    }

    // Enforce zoom limits
    else if (zoomLevel * newLevel < zoomMax && zoomLevel * newLevel > zoomMin) {
        zoomLevel = zoomLevel * newLevel;
    } else if (zoomLevel * newLevel > zoomMin) {
        newLevel = zoomMax / zoomLevel;
        zoomLevel = zoomMax;
    } else {
        newLevel = zoomMin / zoomLevel;
        zoomLevel = zoomMin;
    }

    // Zoom the nodes with CSS3's transform
    $("#infovis-label div.node")
        .css("-webkit-transform", "scale(" + zoomLevel + ")")
        .css("-moz-transform",    "scale(" + zoomLevel + ")")
        .css("-ms-transform",     "scale(" + zoomLevel + ")")
        .css("-o-transform",      "scale(" + zoomLevel + ")")
        .css("transform",         "scale(" + zoomLevel + ")");

    // Apply ForceDirected's zoom
    fd.canvas.scale(newLevel, newLevel);

    // Update the slider if necessary
    if (updateSlider) {
        $("#slider-vertical").slider("value", (zoomLevel - zoomMin) * zoomSteps / (zoomMax - zoomMin));
    }
}


/**
 * Functionality for adjusting the graph's weighting (relations)
 * @param weightingValue, value from the slider element
 * @param maxNodeRelevance maximum relevance value of nodes
 * @param maxEdgeRelevance maximum weigth of relations
 */
function applySimplicity(simplicityValue, maxNodeRelevance, maxEdgeRelevance) {
    $('#simplicity').html(simplicityValue + ' %');
    if (sliderTimer !== null) { clearTimeout(sliderTimer); }
    sliderTimer = setTimeout(function() {
        showSimpleGraph(simplicityValue / 100 * maxNodeRelevance, weightingValue / 100 * maxEdgeRelevance, []);
        sliderTimer = null;
    }, 200);
}


/**
 * Functionality for adjusting the graph's weighting (relations)
 * @param weightingValue, value from the slider element
 * @param maxNodeRelevance maximum relevance value of nodes
 * @param maxEdgeRelevance maximum weigth of relations
 */
function applyWeighting(weightingValue, maxNodeRelevance, maxEdgeRelevance) {
    $('#weighting').html(weightingValue + ' %');
    if (sliderTimer !== null) { clearTimeout(sliderTimer); }
    sliderTimer = setTimeout(function() {
        showSimpleGraph(simplicityValue / 100 * maxNodeRelevance, weightingValue / 100 * maxEdgeRelevance, []);
        sliderTimer = null;
    }, 200);
}


function init() {
    // Add slider functionality to the element
    $("#slider-vertical").slider({
        orientation: "vertical",
        range: "min",
        min: 0,
        max: zoomSteps,
        value: zoomSteps / 2,
        slide: function(event, ui) {
            applyZoom(((zoomMax - zoomMin) / zoomSteps * ui.value + zoomMin) / zoomLevel, false);
        }
    });

    // Loop through to find out max values for node and edge relevance
    var maxNodeRelevance = 0;
    var maxEdgeRelevance = 0;
    var data = window.arca.relationMap.simpleRelations;
    for (var first in data) {
        if (!data.hasOwnProperty(first)) { continue; }

        var firstNodeData = arca.classifications[first];
        if (firstNodeData.relevance > maxNodeRelevance) {
            // Add 0.01 so that all nodes disappear
            maxNodeRelevance = firstNodeData.relevance + 0.01;
        }

        for (var second in data[first]) {
            if (!data[first].hasOwnProperty(second)) { continue; }

            var relationData = data[first][second];
            if (relationData.strength > maxEdgeRelevance) {
                // Add 0.01 so that all relations disappear
                maxEdgeRelevance = relationData.strength + 0.01;
            }
        }
    }

    // Add slider functionality to the graph configuration section (simplicity)
    $("#slider-simplicity").slider({
        orientation: "horizontal",
        range: "min",
        min: 0,
        max: 100,
        value: 0,
        slide: function(event, ui) {
            simplicityValue = ui.value;
            applySimplicity(ui.value, maxNodeRelevance, maxEdgeRelevance);
        }
    });
    // Add slider functionality to the graph configuration section (weighting)
    $("#slider-weighting").slider({
        orientation: "horizontal",
        range: "min",
        min: 0,
        max: 100,
        value: 0,
        slide: function(event, ui) {
            weightingValue = ui.value;
            applyWeighting(ui.value, maxNodeRelevance, maxEdgeRelevance);
        }
    });
}
