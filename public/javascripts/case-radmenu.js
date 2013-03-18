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

// Radial menu functions for the case view

/**
 * Updates the like buttons for the given node according to whether the user can like/dislike it
 * @param selectedNode the selected node data
 */
function radmenu_updateLikeButtons(selectedNode) {
    if (arca.currentUser != 'null') {
        // hide like buttons if user is not logged in
        $('#radmenu-event-likeCause').hide();
        // dislike
        $('#radmenu-event-dislikeCause').hide();
    } else if (!selectedNode.data.hasUserLiked) {
        // show like button if user has not liked
        $('#radmenu-event-likeCause').show();
        // hide dislike button if user has not liked
        $('#radmenu-event-dislikeCause').hide();
    } else if (selectedNode.data.hasUserLiked && arca.currentUser != arca.ownerId) {
        // hide like button if user has liked and is not the owner of the rca case
        $('#radmenu-event-likeCause').hide();
        // show dislike button if user has liked
        $('#radmenu-event-dislikeCause').show();
    } else {
        // show like button if user has liked and is the owner
        // show dislike button if user has liked and is the owner
        $('#radmenu-event-likeCause, #radmenu-event-dislikeCause').show();
    }
}


/**
 * Fades the radial menu for a node out
 */
function radmenu_fadeOut() {
    jQuery("#radial_menu").radmenu(
        "hide",
        function (items) {
            items.fadeOut(4000);
        }
    );
}


/**
 * Fades the radial menu for an edge out
 */
function radmenu_relation_fadeOut() {
    jQuery("#radial_menu_relation").radmenu(
        "hide",
        function (items) {
            items.fadeOut(4000);
        }
    );
}


/**
 * Fades the radial menu for an edge in
 */
function radmenu_relation_fadeIn() {
    jQuery("#radial_menu_relation").radmenu(
        "show",
        function (items) {
            items.fadeIn(400);
        }
    );
}


/**
 * Fades the radial menu in
 * @param selectedNode the selected node
 */
function radmenu_fadeIn (selectedNode) {
    jQuery("#radial_menu").radmenu(
        "show",
        function (items) {
            items.fadeIn(400);

            if (selectedNode.id == arca.rootNodeId || !(selectedNode.data.creatorId == arca.currentUser)) {
                // hide remove-cause button, if user has no rights to remove selected
                // cause
                jQuery("#radial_menu").radmenu("items")[3].style.visibility = "hidden";

                // hide rename-cause button, if user has no rights to rename selected cause
                jQuery("#radial_menu").radmenu("items")[4].style.visibility = "hidden";
            }

            if (arca.currentUser == 'null') {
                // hide like buttons if user is not logged in
                jQuery("#radial_menu").radmenu("items")[5].style.visibility = "hidden";
                // dislike
                jQuery("#radial_menu").radmenu("items")[6].style.visibility = "hidden";
            } else if (!selectedNode.data.hasUserLiked) {
                // hide dislike button if user has not liked
                jQuery("#radial_menu").radmenu("items")[6].style.visibility = "hidden";
            }

            else if (selectedNode.data.hasUserLiked && arca.ownerId != arca.currentUser) {
                // hide like button if user has liked and is not the owner of the rca case
                jQuery("#radial_menu").radmenu("items")[5].style.visibility = "hidden";
            }
        }
    );
}


/**
 * Shows the radial menu for an edge
 * @param eventInfo the click event info data
 */
function show_edge_radial_menu(eventInfo) {
    selectedEdge = eventInfo.getEdge();
    var nodeFrom = eventInfo.getEdge().nodeFrom;
    var nodeTo = eventInfo.getEdge().nodeTo;

    var fromPos = $("#" + nodeFrom.id).offset();
    var toPos = $("#" + nodeTo.id).offset();

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

    jQuery("#radial_menu_relation").radmenu("opts").radius = 30;

    // show the menu directly over the placeholder
    $radial_menu_relation.css({
                                  "left": widthMid + "px",
                                  "top": heightMid + "px"
                              }).show();

    radmenu_relation_fadeIn();
    $("#radial_menu_relation").disableSelection();
}


/**
 * Shows the radial menu for the given node
 * @param given_node the node data
 */
function show_radial_menu(given_node) {
    // Set the global selectedNode to the current node data
    selectedNode = given_node;

    $("#" + given_node.id).addClass("nodeBoxSelected");

    // Get the position of the placeholder element
    var menupos = $("#" + given_node.id).offset();
    var menuwidth = $("#" + given_node.id).width();
    var menuheight = $("#" + given_node.id).height();

    var isFirefox = 0; // This is 1 if Firefox is used.
    var isChrome = 0;  // This is 1 if Chrome or Safari is used.
    var isIE = 0;      // This is 1 if IE is used.

    // Identify the browser
    if ($.browser.mozilla) {
        isFirefox = 1;
    } else if ($.browser.msie) {
        isIE = 1;
    } else {
        isChrome = 1;
    }

    // Border radius needs to be taken to account while calculating height and width for a node
    var halfNodeWidth = isChrome * ((menuwidth / 2) * zoomLevel - 10)
                            + isFirefox * (menuwidth / 2 - 10)
        + isIE * ((menuwidth / 2) * zoomLevel - 10);
    var halfNodeHeight = isChrome * ((menuheight / 2) * zoomLevel - 20)
                             + isFirefox * (menuheight / 2 - 20)
        + isIE * ((menuheight / 2) * zoomLevel - 20);
    var newRadius = Math.sqrt(Math.pow(halfNodeWidth, 2) + Math.pow(halfNodeHeight, 2));

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