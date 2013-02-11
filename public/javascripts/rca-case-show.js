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

// Variables //
// (defined within show.html: arca)

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
// Placeholder for the #radial_menu_relation jQuery object
var $radial_menu_relation;
// Selected edge data
var selectedEdge;
// Placeholder for the ForceDirected object
var fd;
// Selected node data
var selectedNode;
// The current "from" node for adding relations
var relationFromNode = null;

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
    jQuery("#radial_menu_relation").radmenu("hide");
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


// Other functions //

/**
 * A quick and dirty (and hopefully temporary) cause data finder function
 * @param id the ID of the cause we're looking for
 * @return index of the case in arca.graphJson
 */
function findCause(id) {
    for (var i = 0; i < arca.graphJson.length; i++) {
        if (arca.graphJson[i].id == id) { return i; }
    }
    return null;
}


/**
 * Counts the amount of direct ancestors a node has
 * @param node the node ID
 * @return int
 */
function countParentNodes(node) {
    if (node) {
        return countParentNodes(fd.graph.getNode(node.data.parent)) + 1;
    } else {
        return 0;
    }
}


/**
 * Updates the amount of likes for the selected cause ID
 * @param id the cause ID
 * @param count the new amount of likes
 */
function updateLikes(id, count) {
    var likeBox = $("#likeBox-" + id);
    if (count > 0) {
        if (count > 1) {
            likeBox.text(count + arca.multiplePoints);
        } else {
            likeBox.text(count + arca.singlePoint);
        }
        likeBox.fadeIn(400);
    } else {
        likeBox.fadeOut(400);
    }
}


/**
 * Resets a node's edges to the default style
 * @param node the node data
 */
function resetNodeEdges(node) {
    if (node != undefined && node != null) {
        if (node.id == arca.rootNodeId) {
            $("#" + node.id).removeClass("nodeBoxSelected").addClass("rootNodeBox");
        } else {
            $("#" + node.id).removeClass("nodeBoxSelected").addClass("nodeBox");
        }

        node.eachAdjacency(function (adj) {
            adj.setDataset('current', {
                lineWidth: '2',
                color: '#23A4FF'
            });
        });
    }
}


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


// Browser functionality identification
var labelType, useGradients, nativeTextSupport, animate;
(function () {
    var ua = navigator.userAgent,
        iStuff = ua.match(/iPhone/i) || ua.match(/iPad/i),
        typeOfCanvas = typeof HTMLCanvasElement,
        nativeCanvasSupport = (typeOfCanvas == 'object' || typeOfCanvas == 'function'),
        textSupport = nativeCanvasSupport
            && (typeof document.createElement('canvas').getContext('2d').fillText == 'function');

    labelType = (!nativeCanvasSupport || (textSupport && !iStuff)) ? 'Native' : 'HTML';
    nativeTextSupport = labelType == 'Native';
    useGradients = nativeCanvasSupport;
    animate = !(iStuff || !nativeCanvasSupport);
})();


/**
 * Updates the children vectors (ie. arrows) of the selected node, recursively
 * @param node the ID of the node to update
 */
function updateChildrenVectors (node) {
    node.eachAdjacency(function (adj) {
        if (adj.data.$type != "arrow" &&
                adj.nodeTo.data.nodeLevel > node.data.nodeLevel && !(adj.nodeTo.data.locked)) {
            var xAdjPos = node.getPos('end').x + adj.nodeTo.data.xCoordinate;
            var yAdjPos = node.getPos('end').y + adj.nodeTo.data.yCoordinate;
            adj.nodeTo.pos.setc(xAdjPos, yAdjPos);
            adj.nodeTo.setPos(new $jit.Complex(xAdjPos, yAdjPos), 'end');
            updateChildrenVectors(adj.nodeTo);
        }
    }, null, null);
}


/**
 * Resizes the ForceDirected canvas according to the window size
 */
function doResize() {
    fd.canvas.resize(window.innerWidth + 1000, window.innerHeight + 1000);
    $("#infovis").css("width", window.innerWidth + 1000);
    $("#infovis").css("height", window.innerHeight + 1000);
    applyZoom(1, false);
}


function init() {
    // Bind the Escape key to exit the relation mode
    $(document).bind('keydown', function (e) {
        if (e.which == 27) { // Keycode 27 == Esc
            e.preventDefault();
            relationFromNode = null;
            $("#relation-help-message").fadeOut(80, "linear");
        }
    });

    $radial_menu = $("#radial_menu");

    jQuery("#radial_menu").radmenu({
        // The list class inside which to look for menu items
        listClass: 'list',
        // The items - NOTE: the HTML inside the item is copied into the menu item
        itemClass: 'item',
        // The menu radius in pixels
        radius: 50,
        // The animation speed in milliseconds
        animSpeed: 2000,
        // The X axis offset of the center
        centerX: -5,
        // The Y axis offset of the center
        centerY: -10,
        // The name of the selection event
        selectEvent: "click",

        // The actual functionality for the menu
        onSelect: function ($selected) {
            $('.twipsy').remove();

            // Removal of a cause
            if ($selected[0].id == "radmenu-event-removeCause") {
                $.post(arca.ajax.deleteCause({causeId: selectedNode.id}));
                jQuery("#radial_menu").radmenu("hide");
            }

            // Renaming of a cause
            else if ($selected[0].id == "radmenu-event-renameCause") {
                $('#renamedName').val(selectedNode.title);
                $('#renameCause-modal').modal('show');
            }

            // Adding a new cause
            else if ($selected[0].id == "radmenu-event-addCause") {
                $('#addcause-modal').modal('show');
            }

            // Adding a new relation
            else if ($selected[0].id == "radmenu-event-addRelation") {
                relationFromNode = selectedNode;
                $("#relation-help-message").show();
                radmenu_fadeOut();
                resetNodeEdges(selectedNode);
            }

            // Adding a correction
            else if ($selected[0].id == "radmenu-event-addCorrection") {
                var $modal_body = $("#my_modal_body");
                $.get(
                    arca.ajax.getCorrections(
                        {causeId: selectedNode.id}
                    ),
                    function (data) {
                        $modal_body.html(" ");
                        if (data) {
                            $("#my_modal_body").html(data);
                        }
                    }
                );
                $('#addcorrection-modal').modal('show');
            }

            // Liking a cause
            else if ($selected[0].id == "radmenu-event-likeCause") {
                likeCause(selectedNode);
            }

            // Disliking a cause
            else if ($selected[0].id == "radmenu-event-dislikeCause") {
                dislikeCause(selectedNode);
            }

            // Editing a cause's classifications
            else if ($selected[0].id == "radmenu-event-tagCause") {
                $('#tagcause-modal').modal('show');
                populateTagEditor();
            }
        },

        // The base angle offset in degrees
        angleOffset: 90
    });

    $("#infovis").css("width", window.innerWidth + 1000);
    $("#infovis").css("height", window.innerHeight + 1000);

    $radial_menu_relation = $("#radial_menu_relation");

    jQuery("#radial_menu_relation").radmenu({
       // The list class inside which to look for menu items
       listClass: 'list',
       // The items - NOTE: the HTML inside the item is copied into the menu item
       itemClass: 'item',
       // The menu radius in pixels
       radius: 0,
       // The animation speed in milliseconds
       animSpeed: 2000,
       // The X axis offset of the center
       centerX: 0,
       // The Y axis offset of the center
       centerY: 0,
       // The name of the selection event
       selectEvent: "click",

       // The actual functionality for the menu
       onSelect: function ($selected) {
           $('.twipsy').remove();

           // Removal of a relation
           if ($selected[0].id == "radmenu-event-removeRelation") {
               $.post(arca.ajax.deleteRelation({causeId: selectedEdge.nodeFrom.id, toId: selectedEdge.nodeTo.id}));
               jQuery("#radial_menu_relation").radmenu("hide");
           }
       },

       // The base angle offset in degrees
       angleOffset: 0
   });

    var resizeTimer;
    $(window).resize(function () {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(doResize, 100);
    });

    // Initialize the ForceDirected for the canvas
    fd = new $jit.ForceDirected({
        // ID of the visualization container
        injectInto: 'infovis',

        // Dimensions
        width: window.innerWidth + 1000,
        height: window.innerHeight + 1000,

        // Enable zooming and panning by scrolling and drag/drop
        Navigation: {
            enable: true,
            // Enable panning events only if we're dragging the empty
            // canvas (and not a node).
            panning: 'avoid nodes',
            zooming: 0 // Disable default zooming as it's customized
        },

        // Change node and edge styles.
        // These properties are also set per node with dollar prefixed data-properties in the JSON structure.
        Node: {
            overridable: true,
            color: "#83548B",
            dim: 1
        },

        Edge: {
            overridable: true,
            type: 'relationArrow',
            dim: 15,
            color: '#23A4FF',
            lineWidth: 2
        },

        // Native canvas text styling
        Label: {
            type: 'HTML', // Native or HTML
            size: 10,
            style: 'bold'
        },

        // Tooltips
        Tips: {
            enable: false
        },

        // Add node events
        Events: {
            enable: true,

            // Change cursor style when the mouse cursor is on a non-root node and we're not in relation mode
            onMouseEnter: function (node) {
                if (node.data.isRootNode || relationFromNode) {
                    fd.canvas.getElement().style.cursor = 'pointer';
                } else if (!relationFromNode) {
                    fd.canvas.getElement().style.cursor = 'move';
                }
            },

            // Restore mouse cursor when moving outside a node
            onMouseLeave: function () {
                fd.canvas.getElement().style.cursor = '';
            },

            // Update node positions when dragged
            // TODO: Add a notification like "You cannot move the root node"
            onDragMove: function (node, eventInfo, e) {
                jQuery("#radial_menu").radmenu("hide");
                jQuery("#radial_menu_relation").radmenu("hide");
                if (!node.data.isRootNode) {
                    var pos = eventInfo.getPos();
                    node.pos.setc(pos.x, pos.y);
                    node.setPos(new $jit.Complex(pos.x, pos.y), 'end');
                    fd.plot();
                }
            },

            onDragEnd: function (node) {
                var nodeParent = fd.graph.getNode(node.data.parent);
                var xPos, yPos;
                if (nodeParent != undefined) {
                    xPos = node.getPos('end').x - nodeParent.getPos('end').x;
                    yPos = node.getPos('end').y - nodeParent.getPos('end').y;
                } else {
                    xPos = node.getPos('end').x;
                    yPos = node.getPos('end').y;
                }

                // The root node cannot be moved globally
                if (!node.data.isRootNode) {
                    $.post(arca.ajax.moveNode({causeId: node.id, x: xPos, y: yPos}));
                    updateChildrenVectors(node);
                }
            },

            // Implement zooming for nodes
            onMouseWheel: function (delta, e) {
                var zoom = (50 / 1000 * delta) + 1;
                applyZoom(zoom, true);
            },

            // Implement the same handler for touchscreens
            onTouchMove: function (node, eventInfo, e) {
                $jit.util.event.stop(e); // Stop the default touchmove event
                this.onDragMove(node, eventInfo, e);
            },

            // Add the click handler for opening a radial menu for nodes
            onClick: function (node, eventInfo, e) {
                $("#help-message").hide();
                if (node) {
                    if (relationFromNode) {
                        $.post(arca.ajax.addRelation({fromId: relationFromNode.id,
                                                      toID: node.id}));
                        relationFromNode = null;
                        $("#relation-help-message").fadeOut(80, "linear");
                    } else {
                        resetNodeEdges(selectedNode);
                        jQuery("#corrections_link").fadeOut(400);
                        show_radial_menu(node);
                        node.eachAdjacency(function (adj) {
                            adj.setDataset('current', {
                                lineWidth: '5',
                                color: '#4CC417'
                            });
                        }, null, null);
                        fd.plot();
                    }
                } else {
                    resetNodeEdges(selectedNode);
                    jQuery("#radial_menu").radmenu("hide");
                    jQuery("#corrections_link").fadeOut(400);
                    $("#addCorrectiveForm").popover('hide');
                    fd.plot();
                }
                if (eventInfo.getEdge()) {
                    show_edge_radial_menu(eventInfo);
                    fd.plot();
                } else {
                    jQuery("#radial_menu_relation").radmenu("hide");
                    fd.plot();
                }
            }
        },

        // Number of iterations for the FD algorithm
        iterations: 0,

        // Edge length
        levelDistance: 0,

        // Add text to the labels
        onCreateLabel: function (domElement, node) {
            // The root node has its own style
            if (node.id == arca.rootNodeId) {
                $(domElement).addClass('rootNodeBox');
            } else {
                $(domElement).addClass('nodeBox');
            }

            // Nodes that have corrections have their own style
            $(domElement).html(node.name);
            if (node.data.hasCorrections) {
                $(domElement).addClass('corrected');
            }

            // Nodes that have been classified have their own style
            $(domElement).html(node.name);
            if (node.data.isClassified) {
                $(domElement).addClass('classified');
            }


            // Choose the singular or plural "point" / "points"
            var pointString;
            if (node.data.likeCount > 1) {
                pointString = arca.multiplePoints;
            } else {
                pointString = arca.singlePoint;
            }

            // Add the like box to the node if there are any
            $(domElement).append(
                "<div id='likeBoxWrapper-" + node.id + "' class='likeBoxWrapper'>" +
                    "<div id='likeBox-" + node.id + "' class='likeBox label success'>" +
                        node.data.likeCount + " " + pointString +
                    "</div>" +
                "</div>");

            if (node.data.likeCount == 0) {
                domElement.childNodes[1].childNodes[0].style.display = "none";
            }
        },

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

    $('#infovis').live("mousedown", function() {
        jQuery("#radial_menu").radmenu("hide");
        jQuery("#radial_menu_relation").radmenu("hide");
        $('.popover').remove();
        jQuery("#corrections_link").hide();
    });

    $("div[rel=popover]").popover({
        offset: 10,
        html: true
    }).click(function(e) {
        e.preventDefault();
    });

    $("#help-message").delay(5000).fadeOut("slow");

    // Start the event stream reader
    readEventStream();

    // Initialize the graph from the JSON data
    fd.loadJSON(arca.graphJson);
    fd.plot();

    // Calculate the node coordinates by their ancestors
    var rootNodes = new Array();
    fd.graph.eachNode(function (node) {
        var nodeLevel;
        if (node.data.parent) {
            nodeLevel = countParentNodes(node);
        } else {
            nodeLevel = 1;
            rootNodes.push(node);
        }
        node.data.nodeLevel = nodeLevel;
        if (nodeLevel == 1 && !node.data.isRootNode) {
            node.setPos(new $jit.Complex(node.data.xCoordinate, node.data.yCoordinate), 'current');
            node.setPos(new $jit.Complex(node.data.xCoordinate, node.data.yCoordinate), 'end');
        } else {
            node.setPos(new $jit.Complex(0, -300 + nodeLevel * 80), 'current');
            node.setPos(new $jit.Complex(0, -300 + nodeLevel * 80), 'end');
        }
    }, null, null);

    // Draw the arrows for each node
    var rootNode;
    for (rootNode in rootNodes) {
        if (!rootNodes.hasOwnProperty(rootNode)) { continue; }
        updateChildrenVectors(rootNodes[rootNode]);
    }

    fd.animate({
        modes: ['linear'],
        transition: $jit.Trans.Elastic.easeOut,
        duration: 1500
    });
}


$(document).ready(function () {
    $("#help-message-close").click(function () {
        $(this).parent().hide();
    });

    init();

    $("#correction-help-message").hide();
    $("#relation-help-message").hide();
    $("#addCorrectiveForm").hide();
    $("#ideaHeader").hide();
    $("#infovis").disableSelection();

    // Initialize the cause adding dialog
    $('#addcause-modal').modal({
        keyboard: true,
        backdrop: true,
        show: false
    }).bind('show', function () {
        $('#causeName').val('').focus();
    });


    // Initialize the cause renaming dialog
    $('#renameCause-modal').modal({
        keyboard: true,
        backdrop: true,
        show: false
    }).bind('shown', function () {
        $('#renamedName').focus();
    });


    // Initialize the correction adding dialog
    $('#addcorrection-modal').modal({
        keyboard: true,
        backdrop: true,
        show: false
    }).bind('show', function () {
        $('#ideaName').val('');
        $('#ideaDescription').val('');
        $('#ideaName').focus();
    });


    // Initialize the classification adding modal dialog
    $('#addClassification-modal').modal({
        keyboard: true,
        backdrop: true,
        show: false
    }).bind('shown', function () {
        $('#classificationName').focus();
    });


    // Initialize the classification editing modal dialog
    $('#editClassification-modal').modal({
        keyboard: true,
        backdrop: true,
        show: false
    }).bind('shown', function () {
        $('#edit-classificationName').focus();
    });


    // Initialize the classification removing modal dialog
    $('#removeClassification-modal').modal({
        keyboard: true,
        backdrop: true,
        show: false
    });


    // Initialize the cause tagging modal dialog
    $('#tagcause-modal').modal({
        keyboard: true,
        backdrop: true,
        show: false
    });

    initTagEditor();

    // Show the classification editing functionality if the user is the case owner
    if (arca.ownerId == arca.currentUser) {
        $('#tagArea').show();
        $('#tagIcon').click(function() { $('#addTag, #editTag, #removeTag').slideToggle(); });
        $('#addTag').click(function() { $('#addClassification-modal').modal('show'); });
        $('#editTag').click(function() {
            $('#editClassification-modal').modal('show');
            $('#edit-classificationId').change(function () {
                var id = $('#edit-classificationId').val();
                var c = arca.classifications[id];
                $('#edit-classificationTitle').val(c.title);
                $('#edit-classificationType').val(c.dimension);
                $('#edit-classificationAbbreviation').val(c.abbreviation);
                $('#edit-classificationExplanation').val(c.explanation);
            });
        });
        $('#removeTag').click(function() {
            $('#removeClassification-modal .error-field').hide();
            $('#removeClassification-modal').modal('show');
        });
    }
});