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

$(function() {// element variables
    var shareDialog = $("#rcacase-share-dialog");
    var shareInvitedEmailInput = $("#rcacase-share-dialog-invitedEmail-input");
    var shareSuccessInfo = $("#rcacase-share-dialog-success");
    var shareInvalidEmailInfo = $("#rcacase-share-dialog-invalidEmail");
    var shareAlreadyInvitedInfo = $("#rcacase-share-dialog-alreadyInvited");

    $("a[rel=popover]").popover({
        offset: 10
    });
    $(shareSuccessInfo).hide();
    $(shareInvalidEmailInfo).hide();
    $(shareAlreadyInvitedInfo).hide();

    $(shareDialog).dialog({
        autoOpen: false,
        height: 400,
        width: 500,
        modal: true,
        buttons: [{
            text: i18n("rcacase.share.dialog.close"),
            click: function() {
                $( this ).dialog( "close" );
            }
        }],
        close: function() {
            $("table#rcacase-share-dialog-persons").remove();
            $(shareInvitedEmailInput).val("");
            $("#rcacase-share-dialog-rcaCaseId-input").val("");
            $("#rcacase-share-dialog-invitedEmail-error").hide();
            $(shareInvalidEmailInfo).hide();
            $(shareAlreadyInvitedInfo).hide();
        }
    });
    $("#rcacase-share-dialog-share-form").submit(function() {
        var rcaId = $("#rcacase-share-dialog-rcaCaseId-input").val();
        var email = $.trim($(shareInvitedEmailInput).val());

        /* Check if the input is an empty string */
        if(email == undefined || email == "") {
            $(shareInvalidEmailInfo).show();
            $(shareInvalidEmailInfo).delay(1700).fadeOut(1600, "linear");
            $("#rcacase-share-dialog-invitedEmail-error").show();
            return false;
        }

        /* Check if user exists already */
        if ($('#rcacase-share-dialog-persons').find('tr').find('td[name="' + email + '"]').length > 0) {
            $(shareInvitedEmailInput).val("");
            $(shareAlreadyInvitedInfo).show();
            $(shareAlreadyInvitedInfo).delay(1700).fadeOut(1600, "linear");
            $("#rcacase-share-dialog-invitedEmail-error").hide();
            return false
        }

        $.getJSON(window.arca.ajax.inviteUserAction({rcaCaseId: rcaId, invitedEmail: email}), function(data) {
            if (data.invalidEmail == "true") {
                $(shareInvalidEmailInfo).show();
                $(shareInvalidEmailInfo).delay(1700).fadeOut(1600, "linear");
                $("#rcacase-share-dialog-invitedEmail-error").show();
            } else {
                var invited = data.invited == "true" ? "invited-" : "";
                $('<tr id="user-' + invited + data.id + '">'+
                  '<td>'+data.name+'</td>'+
                  '<td name="' + data.email + '">'+data.email+'</td>'+
                  '<td><a href="#" style="color:red;" id="remove-user-share">'+
                  i18n("rcacase.share.dialog.remove")+'</a></td>'+
                  '</tr>').appendTo("table#rcacase-share-dialog-persons");
                $(shareInvitedEmailInput).val("");
                $("#rcacase-share-dialog-invitedEmail-error").hide();
                $(shareSuccessInfo).show();
                $(shareSuccessInfo).delay(1700).fadeOut(1600, "linear");
            }
        });
        return false;
    });

    $("#ownCasesTable").tablesorter({
        // Modified, descending
        sortList:[[1,1]],
        headers: {
            1: { sorter: "shortDate", dateFormat: "ddmmyyyy" },
            2: { sorter: "shortDate", dateFormat: "ddmmyyyy" },
            3: { sorter: false}
        }
    });
    $("#privateCasesTable").tablesorter({
        sortList:[[1,1]],
        headers: {
            1: { sorter: "shortDate", dateFormat: "ddmmyyyy" },
            2: { sorter: "shortDate", dateFormat: "ddmmyyyy" },
            4: { sorter: false}
        }
    });
    $("#publicCasesTable").tablesorter({
        sortList:[[1,1]],
        headers: {
            1: { sorter: "shortDate", dateFormat: "ddmmyyyy" },
            2: { sorter: "shortDate", dateFormat: "ddmmyyyy" },
            4: { sorter: false}
        }
    });
});

$("a[id^='rcacase-share-']").live("click", function () {
    var shareDialog = $("#rcacase-share-dialog");
    var id = this.id.replace("rcacase-share-", "");
    $.getJSON(window.arca.ajax.getRCACaseUsers({rcaCaseId: id}), function(data) {
        var users = [];
        for (i in data) {
            var user = data[i];
            var removeLink = window.arca.currentUsername == user.email ? "" :
                             '<a href="#" style="color:red;" id="remove-user-share">' + i18n("rcacase.share.dialog.remove") + '</a>';
            var invited = user.invitedUser == "true" ? "invited-" : "";
            users.push('<tr id="user-' + invited + user.id + '">'+
                       '<td>'+user.name+'</td>'+
                       '<td name="' + user.email + '">'+user.email+'</td>'+
                       '<td>' + removeLink + '</td>'+'</tr>');
        }
        $("<table/>", {'id': 'rcacase-share-dialog-persons',
            'html': "<tr><th>" + i18n('rcacase.share.dialog.name') + "</th>" +
                    "<th>" + i18n('rcacase.share.dialog.email') + "</th>" +
                    "<th>" + i18n('rcacase.share.dialog.actions') + "</th></tr>"+
                    users.join('')
        }).appendTo($(shareDialog));

        $("#rcacase-share-dialog-invitedEmail-error").hide();
        $("#rcacase-share-dialog-rcaCaseId-input").val(id);
        $(shareDialog).dialog( "open" );
        var width = $("table#rcacase-share-dialog-persons").width();
        $(shareDialog).dialog( "option", "width", width+100 );
        $(shareDialog).css("height", "auto");
    });
});

$("a#remove-user-share").live("click", function() {
    var rcaId = $("#rcacase-share-dialog-rcaCaseId-input").val();
    var email = $(this).parent().prev().html();
    var tr = $(this).parents("tr");
    var trId = $(tr).attr("id");
    var isInvitedUser = trId.indexOf("user-invited-") != -1;
    $.getJSON(window.arca.ajax.removeUserAction({rcaCaseId: rcaId, isInvitedUser: isInvitedUser, email: email}), function(data) {
        if (data.success == "true") {
            $(tr).remove();
        }
    });
});