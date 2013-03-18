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

// Event stream functions for RCA cases

/**
 * Reads an event from the AJAX event stream and processes it.
 */
function readEventStream() {
    $.ajax({
        url: arca.ajax.waitMessage({lastReceived: arca.lastReceived}),
        dataType: 'json',
        success: function (events) {
            $(events).each(function () {
                if (this.data.type === 'deletecauseevent') {
                    fd.graph.removeNode(this.data.causeId);
                    fd.plot();
                    $("div.node#" + this.data.causeId).remove();
                }

                else if (this.data.type === 'deleterelationevent') {
                    fd.graph.removeAdjacence(this.data.causeId, this.data.toId);
                    fd.plot();
                }

                else if (this.data.type === 'addrelationevent') {
                    addRelationHandler(this.data);
                }

                else if (this.data.type === 'addcorrectionevent') {
                    $("#" + this.data.correctionTo).addClass('corrected');
                    fd.plot();
                }

                else if (this.data.type === 'addcauseevent') {
                    addCauseHandler(this.data);
                }

                else if (this.data.type === 'causeRenameEvent') {
                    causeRenameHandler(this.data);
                }

                else if (this.data.type === 'amountOfLikesEvent') {
                    updateLikes(this.data.causeId, this.data.amountOfLikes);
                }

                else if (this.data.type === 'nodemovedevent') {
                    nodeMoveHandler(this.data);
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

                arca.lastReceived = this.id;
            });

            // Use setTimeout instead of a normal call to prevent recursion
            setTimeout(readEventStream, 4);
        },

        error: function (jqXHR, status, error) {
            // In an error state, wait one second before making another connection
            setTimeout(readEventStream, 1000);
        }
    });
}


/**
 * Adds a relation between two nodes, used as a stream event handler
 * @param data JSON data as returned from the
 */
function addRelationHandler(data) {
    fd.graph.addAdjacence(
        fd.graph.getNode(data.causeTo),
        fd.graph.getNode(data.causeFrom),
        {
            "$type": "relationArrow",
            //"$direction": [data.causeTo, data.causeFrom],
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
            "hasUserLiked": false,
            "classifications": ''
        },
        "adjacencies": []
    };
    arca.graphJson.push(newNode);

    var oldNode = fd.graph.getNode(data.causeFrom);
    var newNodesXCoordinate = parseInt(data.x);
    var newNodesYCoordinate = parseInt(data.y);
    fd.graph.addAdjacence(oldNode, newNode);
    newNode = fd.graph.getNode(data.causeTo);
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

    if (data.classify == true && arca.currentUser == newNode.data.creatorId && arca.currentUser != null) {
        selectedNode = newNode;
        $('#tagcause-modal').modal('show');
        populateTagEditor();
    }
}


/**
 * Renames a cause, used as a stream event handler.
 * @param data JSON data as returned from the event stream
 */
function causeRenameHandler(data) {
    var oldNode = fd.graph.getNode(data.causeId);
    oldNode.title = data.newName;
    var node = $('#' + data.causeId);
    var children = node.children();
    // Here .html is used instead of .text, as CauseRenameEvent escapes HTML
    node.html(data.newName);
    node.append(children);
}


/**
 * Moves a node and stores its location, used as a stream event handler
 * @param data JSON data as returned from the event stream
 */
function nodeMoveHandler(data) {
    var nodeToMove = fd.graph.getNode(data.causeId);
    var intX = parseInt(data.x);
    var intY = parseInt(data.y);
    var nodePos = new $jit.Complex(intX, intY);
    nodeToMove.setPos(nodePos, 'end');
    nodeToMove.data.xCoordinate = intX;
    nodeToMove.data.yCoordinate = intY;

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
    arca.classifications[data.id] = {
        id: data.id,
        title: data.name,
        dimension: data.dimension,
        abbreviation: data.abbreviation,
        explanation: data.explanation
    };

    // Add to select elements
    var select = $('.classificationList.classificationType-' + data.dimension);
    select.each(function (i, e) {
        e = $(e);
        if (e.find('option[value="' + data.id + '"]').length != 0) { return false; }
        e.append('<option value="' + data.id + '">' + data.name + '</option>');
    });

    if (data.dimension == 2) {
        // Add the tag area if it doesn't exist already
        if ($('#addTagArea-' + data.id).length == 0) {
            $('#tagAreaLeft').append('<div id="addTagArea-' + data.id + '">' + data.name + '</div>');
            $('#addTagArea-' + data.id).click(addTagArea);
        }
    } else {
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
    delete arca.classifications[data.id];

    // Remove option elements
    $('select.classificationList option[value="' + data.id + '"]').remove();

    // Remove from tag editor
    if (data.dimension == 2) {
        removeTagArea(data.id);
        $('#addTagArea-' + data.id).remove();
    } else {
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
        removeClassificationHandler(data);
        insertClassificationHandler(data);
    } else {
        // Edit the data
        arca.classifications[data.id].title = data.name;
        arca.classifications[data.id].dimension = data.dimension;
        arca.classifications[data.id].abbreviation = data.abbreviation;
        arca.classifications[data.id].explanation = data.explanation;

        // Rename select elements
        $('select.classificationList option[value="' + data.id + '"]').text(data.name);

        // Rename tag editor's left side element
        $('#addTagArea-' + data.id).text(data.name);

        // Rename tag editor's right side element
        $('#addTag-' + data.id).text(data.name);

        // Rename tag editor's active tags
        $('span[id$=_tag_' + data.id + '] span').text(data.name);
    }
}


/**
 * Updates a cause's classification, used as a stream event handler
 * @param data JSON data as returned from the event stream
 */
function causeClassificationHandler(data) {
    var index = findCause(data.causeId);
    arca.graphJson[index].data.classifications = data.classifications;

    // Update the selected node data if it was the modified one
    if (selectedNode && selectedNode.id == data.causeId) {
        selectedNode.data.classifications = data.classifications;
    }

    // Update the node visually
    var node = $('#' + data.causeId);
    if (data.classifications.length > 0) {
        node.addClass('classified');
    } else {
        node.removeClass('classified');
    }
}