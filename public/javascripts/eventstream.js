// Event stream functions for RCA cases

/**
 * Reads an event from the AJAX event stream and processes it.
 */
function readEventStream() {
    console.log("readEventStream()");
    $.ajax({
        url: arca.ajax.waitMessage({lastReceived: arca.lastReceived}),
        dataType: 'json',
        success: function (events) {
            $(events).each(function () {
                console.log("Read event of type " + this.data.type);
                if (this.data.type === 'deletecauseevent') {
                    console.log("Handling deletecauseevent");
                    fd.graph.removeNode(this.data.causeId);
                    fd.plot();
                    $("div.node#" + this.data.causeId).remove();
                    console.log("Done handling deletecauseevent");
                }

                else if (this.data.type === 'deleterelationevent') {
                    console.log("Handling deleterelationevent");
                    fd.graph.removeAdjacence(this.data.causeId, this.data.toId);
                    fd.plot();
                    console.log("Done handling deleterelationevent");
                }

                else if (this.data.type === 'addrelationevent') {
                    console.log("Handling addrelationevent");
                    addRelationHandler(this.data);
                    console.log("Done handling addrelationevent");
                }

                else if (this.data.type === 'addcorrectionevent') {
                    console.log("Handling addcorrectionevent");
                    $("#" + this.data.correctionTo).addClass('corrected');
                    fd.plot();
                    console.log("Done handling addcorrectionevent");
                }

                else if (this.data.type === 'addcauseevent') {
                    console.log("Handling addcauseevent");
                    addCauseHandler(this.data);
                    console.log("Done handling addcauseevent");
                }

                else if (this.data.type === 'causeRenameEvent') {
                    console.log("Handling causeRenameEvent");
                    causeRenameHandler(this.data);
                    console.log("Done handling causeRenameEvent");
                }

                else if (this.data.type === 'amountOfLikesEvent') {
                    console.log("Handling amountOfLikesEvent");
                    updateLikes(this.data.causeId, this.data.amountOfLikes);
                    console.log("Done handling amountofLikesEvent");
                }

                else if (this.data.type === 'nodemovedevent') {
                    console.log("Handling nodemovedevent");
                    nodeMoveHandler(this.data);
                    console.log("Done handling nodemovedevent");
                }

                else if (this.data.type === 'addclassificationevent') {
                    console.log("Handling addclassificationevent");
                    insertClassificationHandler(this.data);
                    console.log("Done handling addclassificationevent");
                }

                else if (this.data.type == 'editclassificationevent') {
                    console.log("Handling editclassificationevent");
                    editClassificationHandler(this.data);
                    console.log("Done handling editclassificationevent");
                }

                else if (this.data.type == 'removeclassificationevent') {
                    console.log("Handling removeclassificationevent");
                    removeClassificationHandler(this.data);
                    console.log("Done handling removeclassificationevent");
                }

                else if (this.data.type == 'causeclassificationevent') {
                    console.log("Handling causeclassificationevent");
                    causeClassificationHandler(this.data);
                    console.log("Done handling causeclassificationevent");
                }

                arca.lastReceived = this.id;
            });

            // Use setTimeout instead of a normal call to prevent recursion
            setTimeout(readEventStream, 4);
        },

        error: function (jqXHR, status, error) {
            // TODO: Show an error message - "please reload"?
            console.log("Error reading event stream: " + status + "; " + error);
            setTimeout(readEventStream, 1000);
        }
    });
}


/**
 * Adds a relation between two nodes, used as a stream event handler
 * @param data JSON data as returned from the
 */
function addRelationHandler(data) {
    console.log("Adding adjacence from " + data.causeFrom + " to " + data.causeTo);
    fd.graph.addAdjacence(
        fd.graph.getNode(data.causeFrom),
        fd.graph.getNode(data.causeTo),
        {
            "$type": "relationArrow",
            "$direction": [data.causeTo, data.causeFrom],
            "$dim": 15,
            "$color": "#23A4FF",
            "weight": 1
        }
    );

    fd.plot();
}


/**
 * Adds a cause to the graph, used as a stream event handler
 * @param data JSON data as returned from the event stream
 */
function addCauseHandler(data) {
    resetNodeEdges(selectedNode);
    var newNode = {
        "id": data.causeTo,
        "name": data.text,
        "data": {
            "parent": data.causeFrom,
            "creatorId": '' + data.creatorId,
            "likeCount": 0,
            "hasUserLiked": false
        },
        "adjacencies": []
    };
    console.log("Adding new node:");
    console.log(newNode);
    arca.graphJson.push(newNode);

    var oldNode = fd.graph.getNode(data.causeFrom);
    var newNodesXCoordinate = 100;
    var newNodesYCoordinate = 100;

    console.log("Adding adjacence");
    fd.graph.addAdjacence(oldNode, newNode);

    console.log("Setting position");
    newNode = fd.graph.getNode(data.causeTo);
    newNode.data.nodeLevel = oldNode.data.nodeLevel + 1;
    newNode.setPos(oldNode.getPos('end'), 'current');
    newNode.getPos('end').y = oldNode.getPos('end').y + newNodesYCoordinate;
    newNode.getPos('end').x = oldNode.getPos('end').x + newNodesXCoordinate;
    newNode.data.xCoordinate = newNodesXCoordinate;
    newNode.data.yCoordinate = newNodesYCoordinate;

    console.log("Plotting");
    fd.plot();
    fd.animate({
        modes: ['linear'],
        transition: $jit.Trans.Elastic.easeOut,
        duration: 1500
    });

    console.log("Applying zoom");
    applyZoom(1, false); // refresh zooming for the new node
    $("#infovis-label div.node").disableSelection();
}


/**
 * Renames a cause, used as a stream event handler.
 * @param data JSON data as returned from the event stream
 */
function causeRenameHandler(data) {
    console.log("Renaming " + data.causeId + " to " + data.newName);
    var oldNode = fd.graph.getNode(data.causeId);
    oldNode.name = data.newName;
    $("#" + data.causeId).html(data.newName);
}


/**
 * Moves a node and stores its location, used as a stream event handler
 * @param data JSON data as returned from the event stream
 */
function nodeMoveHandler(data) {
    var nodeToMove = fd.graph.getNode(data.causeId);
    var nodeParent = fd.graph.getNode(nodeToMove.data.parent);
    console.log("Moving node " + nodeToMove.name + " to " + data.x + ", " + data.y);
    var intX = parseInt(data.x);
    var intY = parseInt(data.y);
    nodeToMove.data.xCoordinate = intX;
    nodeToMove.data.yCoordinate = intY;

    var xPos, yPos;
    if (nodeParent != undefined) {
        console.log("Found parent node, applying offset");
        xPos = nodeParent.getPos('end').x + intX;
        yPos = nodeParent.getPos('end').y + intY;
    } else {
        console.log("No parent node found");
        xPos = intX;
        yPos = intY;
    }

    var nodePos = new $jit.Complex(xPos, yPos);
    nodeToMove.setPos(nodePos, 'end');

    console.log("Animating");
    updateChildrenVectors(nodeToMove);
    fd.animate({
        modes: ['linear'],
        transition: $jit.Trans.Elastic.easeOut,
        duration: 900
    });
}


/**
 * Inserts a classification to the frontend, used as a stream event handler
 * @param data JSON data as returned from the event stream
 */
function insertClassificationHandler(data) {
    // Add to the actual data
    console.log("Adding data to global list");
    arca.classifications[data.id] = {
        id: data.id,
        title: data.name,
        dimension: data.dimension,
        abbreviation: data.abbreviation,
        explanation: data.explanation
    };
    console.log("Added data:");
    console.log(arca.classifications[data.id]);

    // Add to select elements
    console.log("Adding to classification lists");
    var select = $('.classificationList.classificationType-' + data.dimension);
    select.each(function (i, e) {
        console.log("Adding to list #" + i);
        e = $(e);
        if (e.find('option[value="' + data.id + '"]').length != 0) { return false; }
        e.append('<option value="' + data.id + '">' + data.name + '</option>');
    });

    if (data.dimension == 2) {
        // Add the tag area if it doesn't exist already
        console.log("Where classification, adding tag editor area if necessary");
        if ($('#addTagArea-' + data.id).length == 0) {
            console.log("Adding tag area");
            $('#tagAreaLeft').append('<div id="addTagArea-' + data.id + '">' + data.name + '</div>');
            $('#addTagArea-' + data.id).click(addTagArea);
        } else {
            console.log("Tag area found for the ID, not adding");
        }
    } else {
        console.log("What classification, adding the tag editor entry");
        $('#tagAreaRight').append('<div id="addTag-' + data.id + '">' + data.name + '</div>');
        $('#addTag-' + data.id).click(addTag);
    }
}


/**
 * Removes a classification from the frontend, used as a stream event handler
 * @param data JSON data as returned from the event stream
 */
function removeClassificationHandler(data) {
    // Remove from the actual data
    console.log("Removing classification " + data.id);
    console.log("Removing from the global list");
    delete arca.classifications[data.id];

    // Remove option elements
    console.log("Removing from select elements");
    $('select.classificationList option[value="' + data.id + '"]').remove();

    // Remove from tag editor
    if (data.dimension == 2) {
        console.log("Removing tag area from editor");
        removeTagArea(data.id);
        $('#addTagArea-' + data.id).remove();
    } else {
        console.log("Removing tag entry from editor");
        $('#addTag-' + data.id).remove();
        $('div[id^=childTags-]').each(function (i, e) {
            $(e).removeTag(data.id);
        });
    }
}


/**
 * Updates a classification's name in all <select> elements, used as a stream event handler
 * @param data JSON data as returned from the event stream
 */
function editClassificationHandler(data) {
    if (arca.classifications[data.id].dimension != data.dimension) {
        // If the dimension is changed, simply remove the old entry and read it under the correct
        // dimension, otherwise we'd have to repeat insertClassificationHandler() a lot here
        console.log("Classification dimension changed, removing and inserting the classification");
        removeClassificationHandler(data);
        console.log("Classification removed for dimension change");
        insertClassificationHandler(data);
        console.log("Classification added for dimension change");
    } else {
        // Edit the data
        console.log("Editing global list");
        arca.classifications[data.id].title = data.name;
        arca.classifications[data.id].dimension = data.dimension;
        arca.classifications[data.id].abbreviation = data.abbreviation;
        arca.classifications[data.id].explanation = data.explanation;

        // Rename select elements
        console.log("Renaming in select elements");
        $('select.classificationList option[value="' + data.id + '"]').text(data.name);

        // Rename tag editor's left side element
        console.log("Renaming tag area in editor");
        $('#addTagArea-' + data.id).text(data.name);

        // Rename tag editor's right side element
        console.log("Renaming tag entry in editor");
        $('#addTag-' + data.id).text(data.name);

        // Rename tag editor's active tags
        console.log("Renaming tag editor's active tags");
        $('span[id$=_tag_' + data.id + '] span').text(data.name);
    }
}


/**
 * Updates a cause's classification, used as a stream event handler
 * @param data JSON data as returned from the event stream
 */
function causeClassificationHandler(data) {
    console.log("Updating the cause's classification");
    var index = findCause(data.causeId);
    arca.graphJson[index].data.classifications = data.classifications;

    // Update the selected node data if it was the modified one
    if (selectedNode && selectedNode.id == data.causeId) {
        console.log("Cause was selected, updating");
        selectedNode.data.classifications = data.classifications;
    }
}