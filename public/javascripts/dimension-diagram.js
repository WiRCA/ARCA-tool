/*
 * Copyright (C) 2012 by Eero Laukkanen, Risto Virtanen, Jussi Patana, Juha Viljanen,
 * Joona Koistinen, Pekka Rihtniemi, Mika Kekäle, Roope Hovi, Mikko Valjus,
 * Timo Lehtinen, Jaakko Harjuhahto
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
 * Initializes the graph for the canvas
 */
function initGraph() {

    $radial_menu = $("#radial_menu");

    jQuery("#radial_menu").radmenu({

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
               alert("not implemented yet");
               jQuery("#radial_menu").radmenu("hide");
           }

           // naming the edge
           else if ($selected[0].id == "radmenu-event-nameEdge") {
               alert("not implemented yet");
               jQuery("#radial_menu").radmenu("hide");
           }
       },

       // The base angle offset in degrees
       angleOffset: 90
   });

    $("#graph").css("width", window.innerWidth + 1000);
    $("#graph").css("height", window.innerHeight + 1000);

    var resizeTimer;
    $(window).resize(function () {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(doResize, 100);
    });

    fd = new $jit.ForceDirected({
        // ID of the visualization container
        injectInto: 'graph',

        // Dimensions
        width: window.innerWidth,
        height: window.innerHeight,

        // Enable zooming and panning by scrolling and drag/drop
        Navigation: {
            enable: true,
            panning: 'avoid nodes',
            zooming: 30
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
            type: 'relationLine',
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
                console.log("pim pom");
                fd.canvas.getElement().style.cursor = 'move';
            },

            // Restore mouse cursor when moving outside a node
            onMouseLeave: function (node, eventInfo, e) {
                fd.canvas.getElement().style.cursor = '';
            },

            // Update node positions when dragged
            onDragMove: function (node, eventInfo, e) {
                var pos = eventInfo.getPos();
                node.pos.setc(pos.x, pos.y);
                node.setPos(new $jit.Complex(pos.x, pos.y), 'end');
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

            onClick: function(node, eventInfo, e) {
                if (eventInfo.getEdge()) {
                    show_edge_radial_menu(eventInfo);
                    fd.plot();
                } else {
                    jQuery("#radial_menu").radmenu("hide");
                    fd.plot();
                }
            }
        },

        // Number of iterations for the FD algorithm
        iterations: 30,

        // Edge length
        levelDistance: 300,

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
                $(domElement).addClass('nodeBox');
            }
            $(domElement).html(node.name);
        }
    });
    implementEdgeTypes();

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

    // Initialize Twipsy
    $("a[rel=twipsy]").twipsy({live: true});
    $('a[rel=twipsy]').twipsy('show');

    $('#graph').live("mousedown", function(event) {
        jQuery("#radial_menu").radmenu("hide");
        $('.popover').remove();
    });

    fd.plot();

}

/**
 * Returns a new dummy node (makes showSimpleGraph() easier)
 * @param data the classification data from window.arca.classifications
 * @return object
 */
function newNode(data) {
    return {
        id: data.id,
        name: data.title,
        data: data,
        adjacencies: [{
            nodeTo: 0,
            "data": {
                "$dim": 15,
                "$color": "#0000aa",
                "weight": 3,
                "$lineWidth": 3
            }
        }]
    };
}

function radmenu_fadeIn (selectedEdge) {
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

function show_edge_radial_menu(eventInfo) {

    var pos = eventInfo.getPos();

    console.log(pos.x+" "+pos.y);
    pos.x +=  (fd.canvas.getSize().width/2);
    pos.y +=  (fd.canvas.getSize().height/2);
    console.log(pos.x+" "+pos.y);

    selectedEdge = eventInfo.getEdge();

    //$("#" + given_node.id).addClass("nodeBoxSelected");

    // Get the position of the placeholder element

    jQuery("#radial_menu").radmenu("opts").radius = 30;

    // show the menu directly over the placeholder

    $("#radial_menu").css({
        "left": pos.x   + "px",
        "top": pos.y  + "px"
    }).show();

     // jQuery("#radial_menu").radmenu("show");
    radmenu_fadeIn(selectedEdge);
    $("#radial_menu").disableSelection();
}

/**
 * Shows the graph, filtering nodes and edges by their relevance
 * @param minNodeRelevance minimum visible node relevance
 * @param minEdgeRelevance minimum visible edge relevance
 * @param keepNodes list of node IDs to keep regardless of relevance
 */
function showSimpleGraph(minNodeRelevance, minEdgeRelevance, keepNodes) {
    var data = window.arca.relationMap.simpleRelations;
    var graphData = [window.arca.rootNode];

    // This stores the index of the classification node in graphData, keyed by the classification ID
    var created = [];

    // Placeholders
    var firstNodeData, secondNodeData, relationData;

    // Filter the relation data to the actual graph data
    for (var first in data) {
        if (!data.hasOwnProperty(first)) { continue; }

        // Filter irrelevant nodes
        firstNodeData = window.arca.classifications[first];
        if (firstNodeData.relevance < minNodeRelevance) { continue; }

        // Create the node if necessary
        if (!created.hasOwnProperty(first)) {
            graphData.push(newNode(firstNodeData));
            created[first] = graphData.length - 1;
        }

        for (var second in data[first]) {
            if (!data[first].hasOwnProperty(second)) { continue; }

            relationData = data[first][second];

            // Filter irrelevant target nodes
            secondNodeData = window.arca.classifications[second];
            if (secondNodeData.relevance < minNodeRelevance) { continue; }

            // Filter irrelevant edges
            if (relationData.strength < minEdgeRelevance) { continue; }

            // Create the node if necessary
            if (!created.hasOwnProperty(second)) {
                graphData.push(newNode(secondNodeData));
                created[second] = graphData.length - 1;
            }

            // Add the adjacency
            graphData[created[first]].adjacencies.push({
                nodeTo: second,
                "data": {
                    "$dim": 15,
                    "$color": "#23A4FF",
                    "weight": 2,
                    "$lineWidth": 1 + (Math.log(relationData.strength) / Math.log(2))
                }
            });
        }
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
    fd.plot();

    // Let the force algorithm find nice minima for the nodes and display them
    fd.computeIncremental({
        iter: 20,
        property: 'end',
        onComplete: function() { fd.animate({'duration': 1000}); }
    });
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

$(document).ready(function() {
    initGraph();
    showSimpleGraph(0, 0, []);
});