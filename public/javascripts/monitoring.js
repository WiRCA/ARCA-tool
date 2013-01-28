var ajaxRequest;
var caseRequest;
var caseSelections;
var whatToShow;
var selectedCauseStatuses;
var selectedCorrectionStatuses;
var selectedCases;
var allCases;

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
    initGraph('graph', 'radial_menu', 940, 705, false);
    showSimpleGraph(0, 0, []);
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
            $("#causesAndCorrections").empty();
        }
    });


    $("#selectFilter, #rcaCaseSelect").on("change", "input", function (event) {
        collectSelectionsWhatToShow();

        if ((allCases || selectedCases != "") && whatToShow != "") {
            $("#causesAndCorrections, #graph").html("<img src=\"" + arca.imgs.spinner + "\">");

            if (ajaxRequest != undefined) {
                ajaxRequest.abort();
            }
            if (caseRequest != undefined) {
                caseRequest.abort();
            }

            ajaxRequest = $.get(arca.ajax.causesAndCorrections({
                    whatToShow:whatToShow,
                    selectedCases:selectedCases,
                    allCases:allCases,
                    selectedCauseStatuses:selectedCauseStatuses,
                    selectedCorrectionStatuses:selectedCorrectionStatuses,
                    csvExport:'false'
                }), function (html) {
                    $("#causesAndCorrections").html(html).show();
                }
            );

            caseRequest = $.get(arca.ajax.dimensionDiagram({selectedCases:selectedCases}), showDimensionDiagram);
        } else {
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