function addNewCause () {
    var name = $.trim($("#causeName").val());
    if (name == undefined || name == "") {
        $("#causeName").parents(".clearfix").addClass("error");
        return;
    } else {
        $("#causeName").parents(".clearfix").removeClass("error");
    }

    radmenu_fadeOut();
    $("#addcause-modal").modal('hide');
    $.post(window.arca.ajax.addNewCause({causeId: globalSelectedNode, name: encodeURIComponent(name)}));
}


function renameCause () {
    var name = $.trim($("#renamedName").val());
    if (name == undefined || name == "") {
        $("#renamedName").parents(".clearfix").addClass("error");
        return;
    } else {
        $("#renamedName").parents(".clearfix").removeClass("error");
    }
    radmenu_fadeOut();
    $("#renameCause-modal").modal('hide');
    $.post(window.arca.ajax.renameCause({causeId: globalSelectedNode, name: encodeURIComponent(name)}));
}


function likeCause (selected) {
    if (window.arca.ownerId != window.arca.currentUser) {
        radmenu_fadeOut();
    }

    $.post(window.arca.ajax.likeCause({causeId: selected.id}), function (result) {
        selected.data.hasUserLiked = result.hasliked;
        selected.data.likeCount = result.count;
        if (window.arca.ownerId != window.arca.currentUser) {
            radmenu_updateLikeButtons(selected);
        }
    });
}


function dislikeCause (selected) {
    if (window.arca.ownerId != window.arca.currentUser) {
        radmenu_fadeOut();
    }

    $.post(window.arca.ajax.dislikeCause({causeId: selected.id}), function (result) {
        selected.data.hasUserLiked = result.hasliked;
        selected.data.likeCount = result.count;
        if (window.arca.ownerId != window.arca.currentUser) {
            radmenu_updateLikeButtons(selected);
        }
    });
}


function addCorrectiveIdea () {
    var name = $("#ideaName").val();
    if (name == undefined || name == "") {
        $("#ideaName").parents(".clearfix").addClass("error");
        return;
    } else {
        $("#ideaName").parents(".clearfix").removeClass("error");
    }

    var description = $("#ideaDescription").val();
    if (description == undefined || description == "") {
        $("#ideaDescription").parents(".clearfix").addClass("error");
        return;
    } else {
        $("#ideaDescription").parents(".clearfix").removeClass("error");
    }


    $.post(
        window.arca.ajax.addCorrectiveIdea(
            {toId: globalSelectedNode, name: name, description: description}
        ),
        function (data) {
            radmenu_fadeOut();
            $("#addcorrection-modal").modal('hide');
            $("#correction-help-message").show();
            $("#correction-help-message").delay(1700).fadeOut(1600, "linear");
        }
    );
}


function updateLikes (id, count) {
    var likeBox = $("#" + id + " div.label");
    if (count > 0) {
        if (count > 1) {
            likeBox.text(count + window.arca.multiplePoints);
        } else {
            likeBox.text(count + window.arca.singlePoint);
        }
        likeBox.fadeIn(400);
    } else {
        likeBox.fadeOut(400);
    }
}


radmenu_fadeOut = function () {
    jQuery("#radial_menu").radmenu(
        "hide",
        function (items) {
            items.fadeOut(4000);
        }
    );
};


function defaultEdgesForSelectedNode (selectedNode) {
    if (selectedNode != undefined && selectedNode != null) {
        if (selectedNode.id == window.arca.rootNodeId) {
            $("#" + selectedNode.id).removeClass("nodeBoxSelected").addClass("rootNodeBox");
        }
        else {
            $("#" + selectedNode.id).removeClass("nodeBoxSelected").addClass("nodeBox");
        }
        selectedNode.eachAdjacency(function (adj) {
            if (adj.data.$type == "arrow") {
                adj.setDataset('current', {
                    lineWidth: '2',
                    color: '#23A4FF'
                })
            } else {
                adj.setDataset('current', {
                    lineWidth: '2',
                    color: '#23A4FF'
                });
            }
        });
    }
}


function radmenu_fadeOut () {
    jQuery("#radial_menu").radmenu(
        "hide",
        function (items) {
            items.fadeOut(4000);
        }
    );
}


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
    });
};


function radmenu_updateLikeButtons (selectedNode) {
    if (window.arca.currentUser != 'null') {
        // hide like buttons if user is not logged in
        jQuery("#radial_menu").radmenu("items")[5].style.visibility = "hidden";
        // dislike
        jQuery("#radial_menu").radmenu("items")[6].style.visibility = "hidden";
    } else if (!selectedNode.data.hasUserLiked) {
        // show like button if user has not liked
        jQuery("#radial_menu").radmenu("items")[5].style.visibility = "visible";
        // hide dislike button if user has not liked
        jQuery("#radial_menu").radmenu("items")[6].style.visibility = "hidden";
    } else if (selectedNode.data.hasUserLiked && window.arca.currentUser != window.arca.ownerId) {
        // hide like button if user has liked and is not the owner of the rca case
        jQuery("#radial_menu").radmenu("items")[5].style.visibility = "hidden";
        // show dislike button if user has liked
        jQuery("#radial_menu").radmenu("items")[6].style.visibility = "visible";
    } else {
        // show like button if user has liked and is the owner
        jQuery("#radial_menu").radmenu("items")[5].style.visibility = "visible";
        // show dislike button if user has liked and is the owner
        jQuery("#radial_menu").radmenu("items")[6].style.visibility = "visible";
    }
};


function radmenu_fadeIn (selectedNode) {
    jQuery("#radial_menu").radmenu(
        "show",
        function (items) {
            items.fadeIn(400);

            if (selectedNode.id == window.arca.rootNodeId || !(selectedNode.data.creatorId == window.arca.currentUser)) {
                // hide remove-cause button, if user has no rights to remove selected
                // cause
                jQuery("#radial_menu").radmenu("items")[3].style.visibility = "hidden";

                // hide rename-cause button, if user has no rights to rename selected cause
                jQuery("#radial_menu").radmenu("items")[4].style.visibility = "hidden";
            }

            if (window.arca.currentUser == 'null') {
                // hide like buttons if user is not logged in
                jQuery("#radial_menu").radmenu("items")[5].style.visibility = "hidden";
                // dislike
                jQuery("#radial_menu").radmenu("items")[6].style.visibility = "hidden";
            } else if (!selectedNode.data.hasUserLiked) {
                // hide dislike button if user has not liked
                jQuery("#radial_menu").radmenu("items")[6].style.visibility = "hidden";
            }

            else if (selectedNode.data.hasUserLiked && window.arca.ownerId != window.arca.currentUser) {
                // hide like button if user has liked and is not the owner of the rca case
                jQuery("#radial_menu").radmenu("items")[5].style.visibility = "hidden";
            }
        }
    );
};


function init() {
    $(document).bind('keydown', function (e) {
        if (e.which == 27) {
            e.preventDefault();
            relationFromNode = null;
            $("#relation-help-message").fadeOut(80, "linear");
        }
    });

    var $radial_menu = $("#radial_menu"), menupos, menuwidth, selectedNode, relationFromNode;

    jQuery("#radial_menu").radmenu({
                                       listClass: 'list', // the list class to look within for items
                                       itemClass: 'item', // the items - NOTE: the HTML inside the item is
                                       // copied into the menu item
                                       radius: 50, // radius in pixels
                                       animSpeed: 2000, // animation speed in millis
                                       centerX: -5, // the center x axis offset
                                       centerY: -10, // the center y axis offset
                                       selectEvent: "click", // the select event (click)
                                       onSelect: function ($selected) { // show what is returned
                                           $('.twipsy').remove();
                                           if ($selected[0].id == "radmenu-event-removeCause") {
                                               $.post(window.arca.ajax.deleteCause({causeId: selectedNode.id}));
                                               jQuery("#radial_menu").radmenu("hide");
                                           }

                                           else if ($selected[0].id == "radmenu-event-renameCause") {
                                               $('#renamedName').val($("<div/>").html(selectedNode.name).text());
                                               $('#renameCause-modal').modal('show');
                                               globalSelectedNode = selectedNode.id;
                                           }

                                           else if ($selected[0].id == "radmenu-event-addCause") {
                                               $('#addcause-modal').modal('show');
                                               globalSelectedNode = selectedNode.id;
                                           }

                                           else if ($selected[0].id == "radmenu-event-addRelation") {
                                               relationFromNode = selectedNode;
                                               $("#relation-help-message").show();
                                               radmenu_fadeOut();
                                               defaultEdgesForSelectedNode(selectedNode);
                                           }

                                           else if ($selected[0].id == "radmenu-event-addCorrection") {
                                               var $modal_body = $("#my_modal_body");
                                               $.get(
                                                   window.arca.ajax.getCorrections(
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
                                               globalSelectedNode = selectedNode.id;
                                           }

                                           else if ($selected[0].id == "radmenu-event-likeCause") {
                                               likeCause(selectedNode);
                                           }

                                           else if ($selected[0].id == "radmenu-event-dislikeCause") {
                                               dislikeCause(selectedNode);
                                           }
                                       },
                                       angleOffset: 90 // in degrees
                                   });

    function show_radial_menu(given_node) {
        selectedNode = given_node;
        $("#" + given_node.id).addClass("nodeBoxSelected");

        // get the position of the placeholder element
        menupos = $("#" + given_node.id).offset();
        menuwidth = $("#" + given_node.id).width();
        menuheight = $("#" + given_node.id).height();

        isFirefox = 0; // this is 1 if Firefox is used.
        isChrome = 0; // this is 1 if Chrome or Safari is used.
        isIE = 0; // this is 1 if IE is used.

        // checks the browser
        if ($.browser.mozilla) {
            isFirefox = 1;
        }
        else if ($.browser.msie) {
            isIE = 1;
        }
        else {
            isChrome = 1;
        }

        // border radius needs to be concerned while calculating height and width for a node
        halfNodeWidth = isChrome * ((menuwidth / 2) * zoomLevel - 10)
                            + isFirefox * (menuwidth / 2 - 10)
            + isIE * ((menuwidth / 2) * zoomLevel - 10);
        halfNodeHeight = isChrome * ((menuheight / 2) * zoomLevel - 20)
                             + isFirefox * (menuheight / 2 - 20)
            + isIE * ((menuheight / 2) * zoomLevel - 20);
        newRadius = Math.sqrt(Math.pow(halfNodeWidth, 2) + Math.pow(halfNodeHeight, 2));

        // radius algorithm depends on amount of menu items in radial menu
        if (menuheight > menuwidth) {
            newRadius += 50;
        } else {
            newRadius += 40;
        }
        jQuery("#radial_menu").radmenu("opts").radius = newRadius;

        // show the menu directly over the placeholder
        $radial_menu.css({
                             "left": menupos.left + halfNodeWidth + "px",
                             "top": menupos.top + halfNodeHeight + "px"
                         }).show();

        radmenu_fadeIn(selectedNode);
        $("#radial_menu").disableSelection();
    }

    $("#infovis").css("width", window.innerWidth + 1000);
    $("#infovis").css("height", window.innerHeight + 1000);



    function doResize() {
        fd.canvas.resize(window.innerWidth + 1000, window.innerHeight + 1000);
        $("#infovis").css("width", window.innerWidth + 1000);
        $("#infovis").css("height", window.innerHeight + 1000);
        applyZoom(1, false);
    }

    var resizeTimer;
    $(window).resize(function () {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(doResize, 100);
    });

    // init ForceDirected
    var fd = new $jit.ForceDirected({
                                        //id of the visualization container
                                        injectInto: 'infovis',
                                        'width': window.innerWidth + 1000,
                                        'height': window.innerHeight + 1000,
                                        //Enable zooming and panning
                                        //by scrolling and DnD
                                        Navigation: {
                                            enable: true,
                                            //Enable panning events only if we're dragging the empty
                                            //canvas (and not a node).
                                            panning: 'avoid nodes',
                                            zooming: 0 //disable default zooming and implement it in zoom.js
                                        },

                                        // Change node and edge styles such as
                                        // color and width.
                                        // These properties are also set per node
                                        // with dollar prefixed data-properties in the
                                        // JSON structure.
                                        Node: {
                                            overridable: true,
                                            color: "#83548B",
                                            dim: 1
                                        },

                                        Edge: {
                                            overridable: true,
                                            type: 'line',
                                            dim: 15,
                                            color: '#23A4FF',
                                            lineWidth: 2
                                        },

                                        //Native canvas text styling
                                        Label: {
                                            type: 'HTML', //Native or HTML
                                            size: 10,
                                            style: 'bold'
                                        },

                                        //Add Tips
                                        Tips: {
                                            enable: false
                                        },

                                        // Add node events
                                        Events: {
                                            enable: true,
                                            //Change cursor style when hovering a node
                                            onMouseEnter: function (node) {
                                                if (node.data.isRootNode) {
                                                    fd.canvas.getElement().style.cursor = 'pointer';
                                                } else {
                                                    fd.canvas.getElement().style.cursor = 'move';
                                                }
                                            },

                                            onMouseLeave: function () {
                                                fd.canvas.getElement().style.cursor = '';
                                            },

                                            //Update node positions when dragged
                                            onDragMove: function (node, eventInfo, e) {
                                                jQuery("#radial_menu").radmenu("hide");
                                                if (!node.data.isRootNode) {
                                                    var pos = eventInfo.getPos();
                                                    node.pos.setc(pos.x, pos.y);
                                                    node.setPos(new $jit.Complex(pos.x, pos.y), 'end');
                                                    fd.plot();
                                                }
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

                                                // root node cannot be moved globally
                                                if (!node.data.isRootNode) {
                                                    $.post(window.arca.ajax.moveNode({causeId: node.id, x: xPos, y: yPos}));
                                                    updateChildrenVectors(node);
                                                }
                                            },

                                            //Implement zooming for nodes
                                            onMouseWheel: function (delta, e) {
                                                var zoom = (50 / 1000 * delta) + 1;
                                                applyZoom(zoom, true);
                                            },

                                            //Implement the same handler for touchscreens
                                            onTouchMove: function (node, eventInfo, e) {
                                                $jit.util.event.stop(e); //stop default touchmove event
                                                this.onDragMove(node, eventInfo, e);
                                            },

                                            //Add also a click handler to nodes
                                            onClick: function (node) {
                                                $("#help-message").hide();
                                                if (node) {
                                                    if (relationFromNode) {
                                                        $.post(window.arca.ajax.addRelation({fromId: relationFromNode.id,
                                                                               toID: node.id}));
                                                        relationFromNode = null;
                                                        $("#relation-help-message").fadeOut(80, "linear");
                                                    } else {
                                                        defaultEdgesForSelectedNode(selectedNode);
                                                        jQuery("#corrections_link").fadeOut(400);
                                                        show_radial_menu(node);
                                                        node.eachAdjacency(function (adj) {
                                                            adj.setDataset('current', {
                                                                lineWidth: '5',
                                                                color: '#4CC417'
                                                            });
                                                        });
                                                        fd.plot();
                                                    }
                                                } else {
                                                    defaultEdgesForSelectedNode(selectedNode);
                                                    jQuery("#radial_menu").radmenu("hide");
                                                    jQuery("#corrections_link").fadeOut(400);
                                                    $("#addCorrectiveForm").popover('hide');
                                                    fd.plot();
                                                }
                                            }
                                        },

                                        //Number of iterations for the FD algorithm
                                        iterations: 0,

                                        //Edge length
                                        levelDistance: 0,

                                        // Add text to the labels. This method is only triggered
                                        // on label creation and only for DOM labels (not native canvas ones).
                                        onCreateLabel: function (domElement, node) {
                                            // Root node looks different.
                                            if (node.id == window.arca.rootNodeId) {
                                                $(domElement).addClass('rootNodeBox');
                                            } else {
                                                $(domElement).addClass('nodeBox');
                                            }

                                            $(domElement).html(node.name);
                                            if (node.data.hasCorrections) {
                                                $(domElement).addClass('nodeBoxCorrection');
                                            }

                                            var pointString;
                                            if (node.data.likeCount > 1) {
                                                pointString = window.arca.multiplePoints;
                                            } else {
                                                pointString = window.arca.singlePoint;
                                            }

                                            $(domElement).append("<div id='likeBoxWrapper'>" +
                                                                 "<div id='likeBox' class='label success'>" +
                                                                 node.data.likeCount + " " + pointString +
                                                                 "</div>" +
                                                                 "</div>");
                                            if (node.data.likeCount == 0) {
                                                domElement.childNodes[1].childNodes[0].style.display = "none";
                                            }
                                        },

                                        // Change node styles when DOM labels are placed
                                        // or moved.
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

    // variable for current zoom level
    var zoomLevel = 1;
    // variable for minimum zoom level
    var zoomMin = 1 / 32;
    // variable for maximum zoom level
    var zoomMax = 2;
    // steps in zoom slider
    var zoomSteps = 64;

    // increment zoom by one step
    var incZoomSlider = function() {
        var sliderValue = $("#slider-vertical").slider("value");
        applyZoom(((zoomMax - zoomMin) / zoomSteps * (sliderValue + 1) + zoomMin) / zoomLevel, true);
    };

    // decrement zoom by one step
    var decZoomSlider = function() {
        var sliderValue = $("#slider-vertical").slider("value");
        applyZoom(((zoomMax - zoomMin) / zoomSteps * (sliderValue - 1) + zoomMin) / zoomLevel, true);
    };

    // add slider element to body
    $("body").append('<!--icons by http://glyphicons.com/-->' +
                     '<div id="zoomSlider">' +
                     '    <img id="zoomin" src="' + window.arca.zoomInUrl + '" onclick="incZoomSlider()"/>' +
                     '    <div id="slider-vertical"/>' +
                     '    <img id="zoomout" src="' + window.arca.zoomOutUrl + '" onclick="decZoomSlider()" />' +
                     '</div>');

    // zooming routine
    function applyZoom(newLevel, updateSlider) {
        // hides a radial menu when zoomed as the scaling does not work yet.
        jQuery("#radial_menu").radmenu("hide");
        $('.popover').remove();
        // is true if canvas has been scaled or resized, this will reset the zoom of the canvas
        if (fd.canvas.scaleOffsetX != zoomLevel) {
            newLevel = zoomLevel / fd.canvas.scaleOffsetX;
        }
        // check if zoomMax and zoomMin have been crossed
        else if (zoomLevel * newLevel < zoomMax && zoomLevel * newLevel > zoomMin) {
            zoomLevel = zoomLevel * newLevel;
        } else if (zoomLevel * newLevel > zoomMin) {
            newLevel = zoomMax / zoomLevel;
            zoomLevel = zoomMax;
        } else {
            newLevel = zoomMin / zoomLevel;
            zoomLevel = zoomMin;
        }

        // zooming nodes with CSS3 transform
        $("#infovis-label div.node")
            .css("-webkit-transform", "scale(" + zoomLevel + ")")
            .css("-moz-transform",    "scale(" + zoomLevel + ")")
            .css("-ms-transform",     "scale(" + zoomLevel + ")")
            .css("-o-transform",      "scale(" + zoomLevel + ")")
            .css("transform",         "scale(" + zoomLevel + ")");

        // force directed canvas zooming
        fd.canvas.scale(newLevel, newLevel);

        // lets update slider value if necessary
        if (updateSlider) {
            $("#slider-vertical").slider("value", (zoomLevel - zoomMin) * zoomSteps / (zoomMax - zoomMin));
        }
    }

    // apply slider to slider div
    $(function() {
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
    });

    var getNodes = function () {
        $.ajax({
                   url: window.arca.ajax.waitMessage({lastReceived: window.arca.lastReceived}),
                   success: function (events) {
                       $(events).each(function () {
                           if (this.data.type === 'deletecauseevent') {
                               fd.graph.removeNode(this.data.causeId);
                               fd.plot();
                               $("div.node#" + this.data.causeId).remove();
                           }

                           if (this.data.type === 'addrelationevent') {
                               //alert(this.data.causeFrom + " " + this.data.causeTo);
                               fd.graph.addAdjacence(
                                   fd.graph.getNode(this.data.causeFrom),
                                   fd.graph.getNode(this.data.causeTo),
                                   {
                                       "$type": "arrow",
                                       "$direction": [this.data.causeFrom, this.data.causeTo],
                                       "$dim": 15,
                                       "$color": "#23A4FF",
                                       "weight": 1
                                   }
                               );

                               fd.plot();
                           }

                           if (this.data.type === 'addcorrectionevent') {
                               $("#" + this.data.correctionTo).addClass('nodeBoxCorrection');
                               fd.plot();
                           }

                           else if (this.data.type === 'addcauseevent') {
                               // This is a small fix. Now twipsy hides as it's supposed to.
                               // $('.twipsy').remove();
                               defaultEdgesForSelectedNode(selectedNode);
                               var newNode = {
                                   "id": this.data.causeTo,
                                   "name": this.data.text,
                                   "data": {
                                       "parent": this.data.causeFrom,
                                       "creatorId": '' + this.data.creatorId,
                                       "likeCount": 0,
                                       "hasUserLiked": false
                                   }
                               };

                               var oldNode = fd.graph.getNode(this.data.causeFrom);
                               var newNodesXCoordinate = 100;
                               var newNodesYCoordinate = 100;
                               fd.graph.addAdjacence(oldNode, newNode);
                               newNode = fd.graph.getNode(this.data.causeTo);
                               newNode.data.nodeLevel = oldNode.data.nodeLevel + 1;
                               newNode.setPos(oldNode.getPos('end'), 'current');
                               newNode.getPos('end').y = oldNode.getPos('end').y + newNodesYCoordinate;
                               newNode.getPos('end').x = oldNode.getPos('end').x + newNodesXCoordinate;
                               newNode.data.xCoordinate = newNodesXCoordinate;
                               newNode.data.yCoordinate = newNodesYCoordinate;
                               fd.plot();
                               fd.animate({
                                              modes: ['linear'],
                                              transition: $jit.Trans.Elastic.easeOut,
                                              duration: 1500
                                          });
                               applyZoom(1, false); // refresh zooming for the new node
                               $("#infovis-label div.node").disableSelection();
                           }

                           else if (this.data.type === 'causeRenameEvent') {
                               var oldNode = fd.graph.getNode(this.data.causeId);
                               oldNode.name = this.data.newName;
                               $("#" + this.data.causeId).html(this.data.newName);
                           }

                           else if (this.data.type === 'amountOfLikesEvent') {
                               updateLikes(this.data.causeId, this.data.amountOfLikes);
                           }

                           else if (this.data.type === 'nodemovedevent') {
                               var nodeToMove = fd.graph.getNode(this.data.causeId);
                               var nodeParent = fd.graph.getNode(nodeToMove.data.parent);
                               var intX = parseInt(this.data.x);
                               var intY = parseInt(this.data.y);
                               nodeToMove.data.xCoordinate = intX;
                               nodeToMove.data.yCoordinate = intY;
                               if (nodeParent != undefined) {
                                   var xPos = nodeParent.getPos('end').x + intX;
                                   var yPos = nodeParent.getPos('end').y + intY;
                               } else {
                                   var xPos = intX;
                                   var yPos = intY;
                               }

                               var nodePos = new $jit.Complex(xPos, yPos);
                               nodeToMove.setPos(nodePos, 'end');

                               updateChildrenVectors(nodeToMove);
                               fd.animate({
                                              modes: ['linear'],
                                              transition: $jit.Trans.Elastic.easeOut,
                                              duration: 900
                                          });
                           }
                           window.arca.lastReceived = this.id
                       });
                       getNodes()
                   },
                   dataType: 'json'
               });
    };
    getNodes();

    // load JSON data.
    fd.loadJSON(window.arca.graphJson);
    fd.plot();

    // compute positions.
    function countParentNodes(node) {
        if (node) {
            return countParentNodes(fd.graph.getNode(node.data.parent)) + 1;
        } else {
            return 0;
        }
    }

    var rootNodes = new Array();
    //First calculate levels (y-coordinates) for nodes.
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
    });

    var rootNode;
    for (rootNode in rootNodes) {
        //noinspection JSUnfilteredForInLoop
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

    // initialize addCause modal window
    $('#addcause-modal').modal({
                                   keyboard: true,
                                   backdrop: true,
                                   show: false
                               });

    $('#addcause-modal').bind('show', function () {
        $('#causeName').val('');
    });

    $('#addcause-modal').bind('shown', function () {
        $('#causeName').focus();
    });

    // initialize renameCause modal window
    $('#renameCause-modal').modal({
                                      keyboard: true,
                                      backdrop: true,
                                      show: false
                                  });

    $('#renameCause-modal').bind('shown', function () {
        $('#renamedName').focus();
    });

    // initialize addCorrection modal window
    $('#addcorrection-modal').modal({
                                        keyboard: true,
                                        backdrop: true,
                                        show: false
                                    });

    $('#addcorrection-modal').bind('show', function () {
        $('#ideaName').val('');
    });

    $('#addcorrection-modal').bind('show', function () {
        $('#ideaDescription').val('');
    });

    $('#addcorrection-modal').bind('shown', function () {
        $('#ideaName').focus();
    });
});