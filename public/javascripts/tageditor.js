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

    if (!arca.graphJson.hasOwnProperty(findCause(selectedNode.id))) {
        // Error state, TODO
    }
    var tags = arca.graphJson[findCause(selectedNode.id)].data.classifications;
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
    $('#tagAreaRight').addClass('disabled');
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
    if ($('#childTags-' + id).length == 0) { return; }

    // If the tag area to be removed was selected, disable the right-side menu
    if ($('#tagArea-' + id).hasClass("selected")) {
        $('#tagAreaRight').addClass('disabled');
    }

    // Remove the current tag area, show the tag area adding button, remove the TagsInput from the are
    $('#childTags-' + id).removeTagsInput();
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
            if (!classifications.hasOwnProperty(i)) { continue; }
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
    var tags, id;

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