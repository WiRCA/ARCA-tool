var fd = null;

// Add the function $.disableSelection() to jQuery
(function ($) {
    $.fn.disableSelection = function () {
        return this.each(function () {
            $(this)
                .attr('unselectable', 'on')
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
            enable: true,

            // Change cursor style when the mouse cursor is on a non-root node
            onMouseEnter: function (node) {
                fd.canvas.getElement().style.cursor = 'move';
            },

            // Restore mouse cursor when moving outside a node
            onMouseLeave: function () {
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
            }
        },

        // Number of iterations for the FD algorithm
        iterations: 30,

        // Edge length
        levelDistance: 150,

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
                "$type": "line",
                "$dim": 15,
                "$color": "#0000aa",
                "weight": 3,
                "$lineWidth": 3
            }
        }]
    };
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
                    "$type": "line",
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


$(document).ready(function() {
    initGraph();
    showSimpleGraph(0, 0, []);
});