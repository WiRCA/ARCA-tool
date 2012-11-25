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
// Placeholder for the ForceDirected object
var fd;
// Selected node data
var selectedNode;

// AJAX functions //

/**
 * Sends an AJAX request for adding a new cause, reads data from #causeName, #add-causeClassification-{1, 2}
 */
function addNewCause() {
    var name = $.trim($("#causeName").val());
    var classification1 = $("#add-causeClassification-1").val();
    var classification2 = $("#add-causeClassification-2").val();
    if (name == undefined || name == "") {
        $("#causeName").parents(".clearfix").addClass("error");
        return;
    } else {
        $("#causeName").parents(".clearfix").removeClass("error");
    }

    radmenu_fadeOut();
    $("#addcause-modal").modal('hide');
    $.post(arca.ajax.addNewCause({causeId: selectedNode.id,
                                  name: encodeURIComponent(name),
                                  classification1: classification1,
                                  classification2: classification2}));
}


/**
 * Sends an AJAX request for renaming a cause, reads data from #renamedName
 */
function renameCause() {
    var name = $.trim($("#renamedName").val());
    if (name == undefined || name == "") {
        $("#renamedName").parents(".clearfix").addClass("error");
        return;
    } else {
        $("#renamedName").parents(".clearfix").removeClass("error");
    }
    radmenu_fadeOut();
    $("#renameCause-modal").modal('hide');
    $.post(arca.ajax.renameCause({causeId: selectedNode.id, name: encodeURIComponent(name)}));
}


/**
 * Sends an AJAX request for liking a cause
 * @param selected the selected node
 */
function likeCause(selected) {
    if (arca.ownerId != arca.currentUser) {
        radmenu_fadeOut();
    }

    $.post(arca.ajax.likeCause({causeId: selected.id}), function (result) {
        selected.data.hasUserLiked = result.hasliked;
        selected.data.likeCount = result.count;
        if (arca.ownerId != arca.currentUser) {
            radmenu_updateLikeButtons(selected);
        }
    });
}


/**
 * Sends an AJAX request for disliking (unliking) a cause
 * @param selected the selected node
 */
function dislikeCause(selected) {
    if (arca.ownerId != arca.currentUser) {
        radmenu_fadeOut();
    }

    $.post(arca.ajax.dislikeCause({causeId: selected.id}), function (result) {
        selected.data.hasUserLiked = result.hasliked;
        selected.data.likeCount = result.count;
        if (arca.ownerId != arca.currentUser) {
            radmenu_updateLikeButtons(selected);
        }
    });
}


/**
 * Sends an AJAX request for adding a corrective idea
 * Reads data from #ideaName, #ideaDescription
 */
function addCorrectiveIdea() {
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
        arca.ajax.addCorrectiveIdea(
            {toId: selectedNode.id, name: name, description: description}
        ),
        function (data) {
            radmenu_fadeOut();
            $("#addcorrection-modal").modal('hide');
            $("#correction-help-message").show();
            $("#correction-help-message").delay(1700).fadeOut(1600, "linear");
        }
    );
}


/**
 * Sends an AJAX request for editing a classification
 * Reads data from #edit-classification{Id, Title, Type, Abbreviation, Explanation}
 * @return boolean
 */
function editClassification() {
    var id = $('#edit-classificationId').val();
    var name = $('#edit-classificationTitle').val();
    var type = $('#edit-classificationType').val();
    var abbreviation = $('#edit-classificationAbbreviation').val();
    var explanation = $('#edit-classificationExplanation').val();
    $.getJSON(
        arca.ajax.editClassification({
            classificationId: id,
            name: name,
            type: type,
            abbreviation: abbreviation,
            explanation: explanation
        })
    ).success(function (data) {
        if ("error" in data) {
            $('#editClassification-modal .error-field').text(data.error).show();
        } else {
            $('#editClassification-modal').modal('hide');
        }
    });
    return false;
}


/**
 * Sends an AJAX request for adding a new classification
 * Reads data from $classification{Name, Type, Abbreviation, Explanation}
 * @return boolean
 */
function addClassification() {
    var name = $('#classificationName').val();
    var type = $('#classificationType').val();
    var abbreviation = $('#classificationAbbreviation').val();
    var explanation = $('#classificationExplanation').val();
    $.getJSON(
        arca.ajax.addClassification({
            name: name,
            type: type,
            abbreviation: abbreviation,
            explanation: explanation
        })
    ).success(function (data) {
        if ("error" in data) {
            $('#addClassification-modal .error-field').text(data.error).show();
        } else {
            $('#addClassification-modal').modal('hide');
        }
    });
    return false;
}


/**
 * Sends an AJAX request for removing a classification
 * Reads data from #remove-classificationId
 * @return boolean
 */
function removeClassification() {
    var id = $('#remove-classificationId').val();
    $.getJSON(
        arca.ajax.removeClassification({classificationId: id})
    ).success(function (data) {
        if ("error" in data) {
            $('#removeClassification-modal .error-field').text(data.error).show();
        } else {
            $('#removeClassification-modal').modal('hide');
        }
    });
    return false;
}


/**
 * Sends an AJAX request for (re)classifying a cause
 */
function tagCause() {
    var causeId = selectedNode.id;
    $.getJSON(
        arca.ajax.tagCause({causeId: causeId,
                            classifications: constructTagString()})
    ).success(function (data) {
        $('#tagcause-modal').modal('hide');
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


// Event stream functions //

/**
 * Reads an event from the AJAX event stream and processes it.
 */
function readEventStream() {
    $.ajax({
        url: arca.ajax.waitMessage({lastReceived: arca.lastReceived}),
        success: function (events) {
            $(events).each(function () {
                if (this.data.type === 'deletecauseevent') {
                    fd.graph.removeNode(this.data.causeId);
                    fd.plot();
                    $("div.node#" + this.data.causeId).remove();
                }

                else if (this.data.type === 'addrelationevent') {
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

                else if (this.data.type === 'addcorrectionevent') {
                    $("#" + this.data.correctionTo).addClass('nodeBoxCorrection');
                    fd.plot();
                }

                else if (this.data.type === 'addcauseevent') {
                    resetNodeEdges(selectedNode);
                    var newNode = {
                        "id": this.data.causeTo,
                        "name": this.data.text,
                        "data": {
                            "parent": this.data.causeFrom,
                            "creatorId": '' + this.data.creatorId,
                            "likeCount": 0,
                            "hasUserLiked": false,
                            "classification1": this.data.classificationId1,
                            "classification2": this.data.classificationId2
                        }
                    };
                    arca.graphJson.push(newNode);

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

                else if (this.data.type === 'addclassificationevent') {
                    insertClassificationHandler(this.data);
                }

                else if (this.data.type == 'editclassificationevent') {
                    editClassificationHandler(this.data);
                }

                else if (this.data.type == 'removeclassificationevent') {
                    removeClassificationHandler(this.data);
                }

                else if (this.data.type == 'causeclassificationevent') {
                    causeClassificationHandler(this.data);
                }

                arca.lastReceived = this.id
            });

            readEventStream();
       },
       dataType: 'json'
   });
}


/**
 * Inserts a classification to all relevant <select> elements, used as a stream event handler
 * @param data JSON data as returned from the event stream
 */
function insertClassificationHandler(data) {
    arca.classifications[data.id] = {
        name: data.name,
        dimension: data.dimension,
        abbreviation: data.abbreviation,
        explanation: data.explanation
    };
    var select = $('.classificationList.classificationType-' + data.dimension);
    select.each(function (i, e) {
        e = $(e);
        if (e.find('option[value="' + data.id + '"]').length != 0) { return false; }
        e.append('<option value="' + data.id + '">' + data.name + '</option>');
    });

    if (data.dimension == 1) {
        $('#tagAreaLeft').append('<div id="addTagArea-' + data.id + '">' + data.name + '</div>').click(addTagArea);
    } else {
        $('#tagAreaRight').append('<div id="addTag-' + data.id + '">' + data.name + '</div>').click(addTag);
    }
}


/**
 * Removes a classification from all <select> elements, used as a stream event handler
 * @param data JSON data as returned from the event stream
 */
function removeClassificationHandler(data) {
    delete arca.classifications[data.id];
    $('select.classificationList option[value="' + data.id + '"]').remove();
}


/**
 * Updates a classification's name in all <select> elements, used as a stream event handler
 * @param data JSON data as returned from the event stream
 */
function editClassificationHandler(data) {
    arca.classifications[data.id].name = data.name;
    arca.classifications[data.id].dimension = data.dimension;
    arca.classifications[data.id].abbreviation = data.abbreviation;
    arca.classifications[data.id].explanation = data.explanation;
    $('select.classificationList option[value="' + data.id + '"]').text(data.name);
}


/**
 * Updates a cause's classification, used as a stream event handler
 * @param data JSON data as returned from the event stream
 */
function causeClassificationHandler(data) {
    var index = findCause(data.causeId);
    arca.graphJson[index].data.classifications = data.classifications;

    // Update the selected node data if it was the modified one
    if (selectedNode.id == data.causeId) {
        selectedNode.data.classifications = data.classifications;
    }
}


// Tag editor functions //

/**
 * Initializes the tag editor
 */
function initTagEditor() {
    $('#tagAreaLeft > div').click(addTagArea);
    $('#tagAreaRight:not(.disabled) > div').click(addTag);
}


/**
 * Populates the tag editor with the tags of the currently selected cause
 */
function populateTagEditor() {
    // Reset the editor to its default state
    $('div[id^=addTagArea-]').show();
    $('input[id^=childTags-]').each(function() { $(this).removeTagsInput(); });
    $('#tagAreaMiddle > div').remove();

    var tags = selectedNode.data.classifications;
    var i, j, curr;

    // Group the tags under a where->what object
    var grouped_tags = {};
    for (i = 0; i < tags.length; i++) {
        curr = tags[i];
        if (!grouped_tags.hasOwnProperty(curr[0])) {
            grouped_tags[curr[0]] = [curr[1]];
        } else {
            grouped_tags[curr[0]].push(curr[1]);
        }
    }

    // Sort and add the UI elements for the areas
    var areas = Object.getOwnPropertyNames(grouped_tags);
    areas.sort();
    for (i = 0; i < areas.length; i++) {
        curr = areas[i];
        addTagArea(curr, false);
    }

    // Then add each tag as necessary
    for (i in grouped_tags) {
        if (!grouped_tags.hasOwnProperty(i)) { continue; }
        selectTagArea(i);
        for (j = 0; j < grouped_tags[i].length; j++) {
            addTag(grouped_tags[i][j]);
        }
    }

    // Finally unselect all tag areas and update the right-side menu
    $('div[id^=tagArea-].selected').removeClass('selected');
    updateTagMenu();
}


/**
 * Adds a tag area to the tag editor
 * @param e jQuery event object or numeric area ID
 * @param batch whether or not the function is being run in batch mode (ie. while populating the tag editor)
 */
function addTagArea(e, batch) {
    // TODO: Test for potential XSS - is this escaped and if it is, where?

    // Check if we have a jQuery object or a numeric ID
    var id;
    if (e.hasOwnProperty('target')) {
        id = e.target.id.substring(11); // "addTagArea-".length == 11
    } else {
        id = e;
    }

    // Create the tag area for the relevant classification ID
    var name = arca.classifications[id].title;
    $('#tagAreaMiddle').append(
        '<div id="tagArea-' + id + '" class="tagAreaSection">' +
            '<span class="tagAreaTitle">' + name + '</span>' +
            '<a href="javascript:removeTagArea(' + id + ')" class="removeTagArea">X</a>' +
            '<input class="childTags" id="childTags-' + id + '" name="childTags-' + id + '" />' +
        '</div>'
    );
    $('#tagArea-' + id).click(selectTagArea);

    // Enable tag input on the element
    $('#childTags-' + id).tagsInput({
        interactive: false,
        width: '95%',
        height: '30px',
        onRemoveTag: updateTagMenu
    });

    // Select the given tag area
    if (!batch) {
        selectTagArea(id);
    }

    // Hide the tag area adding button for the ID
    $('#addTagArea-' + id).hide();
}


/**
 * Removes a tag area completely.
 * @param id numeric tag area ID
 */
function removeTagArea(id) {
    // If the tag area to be removed was selected, disable the right-side menu
    if ($('#tagArea-' + id).hasClass("selected")) {
        $('#tagAreaRight').addClass('disabled');
    }

    // Remove the current tag area, show the tag area adding button
    $('#tagArea-' + id).remove();
    $('#addTagArea-' + id).show();

    // Update the right-side menu (purely visual)
    updateTagMenu();
}


/**
 * Selects a tag area by jQuery event or numeric ID
 * @param evt jQuery event object or a numeric tag area ID
 */
function selectTagArea(evt) {
    // Check if we have a jQuery object or a numeric ID
    var id;
    if (evt.hasOwnProperty("delegateTarget")) {
        // Ensure that we did not click on the remove button
        if ($(evt.target).hasClass("removeTagArea")) { return; }
        id = evt.delegateTarget.id.substring(8); // "tagArea-".length == 8
    } else {
        id = evt;
    }

    // Unselect all tag areas, select the current one and enable the right-side tag menu
    $('div[id^=tagArea-].selected').removeClass('selected');
    $('#tagArea-' + id).addClass('selected');
    $('#tagAreaRight').removeClass('disabled');

    updateTagMenu();
}


/**
 * Updates the right-hand tag menu of the tag editor
 */
function updateTagMenu() {
    // Empty the area
    $('#tagAreaRight > div').remove();

    // Get the list of classifications
    var classifications = arca.classifications;

    // Filter the list by removing classifications in an invalid dimension and already selected ones
    // First get the selected area ID
    var id = $('div[id^=tagArea-].selected').attr('id');
    var filtered = [];
    var i;

    // An area is selected: Filter selected ones away
    if (id) {
        // Parse the area ID (ie. WHERE classification ID) from the element ID
        id = id.substring(8); // "tagArea-".length == 8

        // Get the selected tags
        var tags = $('#childTags-' + id);
        if (tags.length > 0) {
            tags = tags.val().split(",")
        } else {
            tags = [];
        }

        // Filter the classifications
        for (i in classifications) {
            if (!classifications.hasOwnProperty(i)) { continue; }

            // Check if the classification is in the WHAT dimension and that it is not in the tag list already
            // '' + id coerces the ID to a string, which is the type of the items in the list
            if (classifications[i].dimension == 1 && tags.indexOf('' + classifications[i].id) == -1) {
                filtered.push(classifications[i]);
            }
        }
    }

    // No area selected, just add classifications in the correct dimension
    else {
        for (i in classifications) {
            if (classifications[i].dimension == 1) {
                filtered.push(classifications[i]);
            }
        }
    }

    // Append the items to the tag menu
    for (i = 0; i < filtered.length; i++) {
        $('#tagAreaRight').append('<div id="addTag-' + filtered[i].id + '">' + filtered[i].title + '</div>');
    }

    // Add click handlers to all of them
    $('div[id^=addTag-]').click(addTag);
}


/**
 * Adds a tag to the selected tag area
 * @param evt jQuery event object or a numeric ID
 */
function addTag(evt) {
    // Check that we have a selected tag area
    if ($('#[id^=tagArea-].selected').length == 0) { return; }

    // Check if we have a jQuery event object or a numeric ID
    if (evt.hasOwnProperty('delegateTarget')) {
        var id = evt.delegateTarget.id.substring(7); // "addTag-".length == 7
    } else {
        id = evt;
    }
    $('#[id^=tagArea-].selected .childTags').addTag(id, arca.classifications[id].title);
    $(evt.delegateTarget).hide();
}


/**
 * Constructs the actual data string to send to the server from the tag editor
 * @return string
 */
function constructTagString() {
    var out = [];
    var tags, id, i;

    // Loop through each childTags input field, which have the actual tag data
    $('input[id^=childTags-]').each(function (i, e) {
        // Parse the ID from the element ID
        id = e.id.substring(10); // "childTags-".length == 10

        // Get the tags and add them to the list as a "parent:child" string
        tags = e.value.split(",");
        if (tags[0] != '') {
            for (i = 0; i < tags.length; i++) {
                out.push(id + ":" + tags[i]);
            }
        }
    });

    // Return the pairs as a string delimited by semicolon
    return out.join(";");
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
    var likeBox = $("#" + id + " div.label");
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
    });
};


function radmenu_updateLikeButtons (selectedNode) {
    if (arca.currentUser != 'null') {
        // hide like buttons if user is not logged in
        jQuery("#radial_menu").radmenu("items")[5].style.visibility = "hidden";
        // dislike
        jQuery("#radial_menu").radmenu("items")[6].style.visibility = "hidden";
    } else if (!selectedNode.data.hasUserLiked) {
        // show like button if user has not liked
        jQuery("#radial_menu").radmenu("items")[5].style.visibility = "visible";
        // hide dislike button if user has not liked
        jQuery("#radial_menu").radmenu("items")[6].style.visibility = "hidden";
    } else if (selectedNode.data.hasUserLiked && arca.currentUser != arca.ownerId) {
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
}


function radmenu_fadeOut () {
    jQuery("#radial_menu").radmenu(
        "hide",
        function (items) {
            items.fadeOut(4000);
        }
    );
}


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
    var menupos, menuwidth, relationFromNode;

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
                // TODO: Potentially unsecure, change causes to .escapeJavaScript().raw() like classifications
                $('#renamedName').val($("<div/>").html(selectedNode.name).text());
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
            type: 'line',
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

            // Change cursor style when the mouse cursor is on a non-root node
            onMouseEnter: function (node) {
                if (node.data.isRootNode) {
                    fd.canvas.getElement().style.cursor = 'pointer';
                } else {
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
            onClick: function (node) {
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
                        });
                        fd.plot();
                    }
                } else {
                    resetNodeEdges(selectedNode);
                    jQuery("#radial_menu").radmenu("hide");
                    jQuery("#corrections_link").fadeOut(400);
                    $("#addCorrectiveForm").popover('hide');
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
                $(domElement).addClass('nodeBoxCorrection');
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
                "<div id='likeBoxWrapper'>" +
                    "<div id='likeBox' class='label success'>" +
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

    $('#infovis').live("mousedown", function(event) {
        jQuery("#radial_menu").radmenu("hide");
        $('.popover').remove();
        jQuery("#corrections_link").hide();
    });

    $("div[rel=popover]").popover({
        offset: 10,
        html: true
    }).click(function(e) {
        isVisible = true; // TODO: $('div').visible() oslt?
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
    });

    // Draw the arrows for each node
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
    }).bind('shown', function() {
        var index = findCause(selectedNode.id);
        if (index === null) { return; }
        var data = arca.graphJson[index].data;
        $('#tag-causeClassification-1').val(data.classification1);
        $('#tag-causeClassification-2').val(data.classification2);
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
        $('#removeTag').click(function() { $('#removeClassification-modal').modal('show'); });
    }
});