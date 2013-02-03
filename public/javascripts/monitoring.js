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

var ajaxRequest,
    caseRequest,
    tableRequest,
    caseSelections,
    whatToShow,
    selectedCauseStatuses,
    selectedCorrectionStatuses,
    selectedCases,
    allCases;


function show_help() {
    $('#info_button').attr("disabled", true);
    $("#help-block-message").show();
    $("#help-block-message-close").click(function () {
        $(this).parent().hide();
        $('#info_button').attr("disabled", false);
    });
}


function initMonitoring(loggedIn) {
    $("#help-block-message-close").click(function () {
        $(this).parent().hide();
    });

    // If the user is not logged in, only the "public cases" checkbox is enabled
    if (!loggedIn) {
        $("input[name='casesRadio'][value='myCases']").attr("checked", false);
        $("input[name='casesRadio'][value='sharedCases']").attr("checked", false);
        $("input[name='casesRadio'][value='myCases']").attr("disabled", true);
        $("input[name='casesRadio'][value='sharedCases']").attr("disabled", true);
    }

    caseSelections = "";
    $("input[name='casesRadio']:checked").each(function () {
        caseSelections += $(this).attr("value") + ",";
    });

    if (caseSelections != "") {
        $("#rcaCaseSelect").html("<img src=\"" + arca.imgs.spinner + "\">");
        if (ajaxRequest != undefined) {
            ajaxRequest.abort();
        }

        ajaxRequest = $.get(arca.ajax.caseSelecting({showCases:caseSelections}), function (html) {
            $("#rcaCaseSelect").html(html);
        });
    } else {
        $("#rcaCaseSelect").html("");
        $("#causesAndCorrections").html("");
    }
}


function collectSelectionsWhatToShow() {
    whatToShow = "";
    $("input[name='whatToShow']:checked").each(function () {
        whatToShow += $(this).attr("value") + ",";
    });
    selectedCauseStatuses = "";
    $("input[name='causeStatuses']:checked").each(function () {
        selectedCauseStatuses += $(this).attr("value") + ",";
    });
    selectedCorrectionStatuses = "";
    $("input[name='correctionStatuses']:checked").each(function () {
        selectedCorrectionStatuses += $(this).attr("value") + ",";
    });
    selectedCases = "";
    $("input[name='selectedCases']:checked").each(function () {
        selectedCases += $(this).attr("value") + ",";
    });
    allCases = $("input[name='casesRadio'][value='allCases']:checked").length == 1;
}


$(function () {
    $('.well .head').each(function () {
        $(this).next().parent().css("padding", "10px 19px");
    });
});


function showDimensionDiagram(data, status, xhr) {
    arca.relationMap = data.map;
    arca.classifications = data.classifications;
    if (fd !== undefined) { fd.graph.empty(); }
    $('#graph').empty().show();
    $('#dimensionDiagramSection').slideDown();
    initGraph('graph', 'radial_menu', 940, 705, false);
    showSimpleGraph(0, 0, [], true, true, true);
}


function showClassificationTable(data, status, xhr) {
    arca.classificationTable = data;
    $('#classificationTableSection').slideDown();
    $('.classificationCharts').show();
    generateClassificationTable('classificationTable', true, true, true);
}


// Event handlers
$(function() {
    $("#showCauses").on("change", function () {
        if ($("#showCauses:checked").length > 0) {
            $("#causeList").show();
            $("#showCauses").attr("checked", "checked");
        }
    });


    $('#selectFilter, #rcaCaseSelect').on("click", function (event) {
        if (event.target.name == 'selectAllCases') {
            $("input[name='selectedCases']").attr("checked", "checked");
            $("input[name='selectedCases']").eq(0).change();
        }
    });


    $("#showCauses").on("change", function () {
        if ($("#showCauses:checked").length != 1) {
            $("#causeList input").removeAttr("checked");
            $("#correctionStatusesUl").show();
        }
    });


    $("#correctionStatusesUl").on("change", "input", function () {
        if ($("input[name='correctionStatuses']:checked").length > 0) {
            $("#showCorrections").attr("checked", "checked");
        }
    });


    $("input[name='whatToShow'][value='corrections']").change(function () {
        if ($("input[name='whatToShow'][value='corrections']:checked").length != 1) {
            $("input[name='correctionStatuses']").removeAttr("checked");
        }
    });


    $("input[name='casesRadio']").change(function (event) {
        caseSelections = "";
        $("input[name='casesRadio']:checked").each(function () {
            caseSelections += $(this).attr("value") + ",";
        });

        if (caseSelections != "") {
            $("#rcaCaseSelect").html("<img src=\"" + arca.imgs.spinner + "\">");
            if (ajaxRequest != undefined) {
                ajaxRequest.abort();
            }

            ajaxRequest = $.get(arca.ajax.caseSelecting({showCases: caseSelections}), function (html) {
                $("#rcaCaseSelect").html(html).show();
            });
        } else {
            $("#rcaCaseSelect").empty();
            $('#causesAndCorrectionsSection').slideUp();
            $("#causesAndCorrections").empty();
        }
    });


    $("#selectFilter, #rcaCaseSelect").on("change", "input", function (event) {
        collectSelectionsWhatToShow();

        if ((allCases || selectedCases != "") && whatToShow != "") {
            $("#causesAndCorrections, #graph").html("<img src=\"" + arca.imgs.spinner + "\">");

            if (ajaxRequest != undefined) { ajaxRequest.abort(); }
            if (caseRequest != undefined) { caseRequest.abort(); }
            if (tableRequest != undefined) { tableRequest.abort(); }

            ajaxRequest = $.get(arca.ajax.causesAndCorrections({
                    whatToShow:whatToShow,
                    selectedCases:selectedCases,
                    allCases:allCases,
                    selectedCauseStatuses:selectedCauseStatuses,
                    selectedCorrectionStatuses:selectedCorrectionStatuses,
                    csvExport:'false'
                }), function (html) {
                    $('#causesAndCorrectionsSection').slideDown();
                    $("#causesAndCorrections").html(html).show();
                }
            );

            if ($('#showDimensionDiagram:checked').val()) {
                caseRequest = $.get(arca.ajax.dimensionDiagram({selectedCases:selectedCases}), showDimensionDiagram);
            } else {
                $('#dimensionDiagramSection').slideUp();
            }

            if ($('#showClassificationTable:checked').val()) {
                tableRequest = $.get(arca.ajax.classificationTable({selectedCases:selectedCases}), showClassificationTable);
            } else {
                $('#classificationTableSection').slideUp();
            }
        } else {
            $('#causesAndCorrectionsSection').slideUp();
            $("#causesAndCorrections").html("");
        }
    });


    $('#export-csv-link').on("click", function () {
        collectSelectionsWhatToShow();
        if ((allCases || selectedCases != "") && whatToShow != "") {
            arca.ajax.causesAndCorrections({
                whatToShow: whatToShow,
                selectedCases: selectedCases,
                allCases: allCases,
                selectedCauseStatuses: selectedCauseStatuses,
                selectedCorrectionStatuses: selectedCorrectionStatuses,
                csvExport: 'true'
            });
        }
    });


    $(".pagination li a").on("click", function (event) {
        var clickedPage = $(this);
        var pagination = $(this).parents(".pagination");
        var paginationId = $(pagination).attr("id").replace("pagination-", "");
        var currentPage = $(pagination).find("li.active a");
        var nextButton = $(pagination).find("li.next");
        var prevButton = $(pagination).find("li.prev");
        var isNext = $(this).parent().hasClass("next");
        var isPrev = $(this).parent().hasClass("prev");
        if (isNext) {
            clickedPage = $(currentPage).parent().next().children("a");
        }
        if (isPrev) {
            clickedPage = $(currentPage).parent().prev().children("a");
        }
        var nextSelected = $(clickedPage).parent();
        if ($(nextSelected).hasClass("next") || $(nextSelected).hasClass("prev")) {
            return;
        }
        if ($(nextSelected).next().hasClass("next")) {
            $(nextButton).addClass("disabled");
        } else {
            $(nextButton).removeClass("disabled");
        }
        if ($(nextSelected).prev().hasClass("prev")) {
            $(prevButton).addClass("disabled");
        } else {
            $(prevButton).removeClass("disabled");
        }
        $(currentPage).parent().removeClass("active");
        $("." + paginationId + "-page-" + $(currentPage).html()).hide();
        $(nextSelected).addClass("active");
        $("." + paginationId + "-page-" + $(clickedPage).html()).show();
    });
});