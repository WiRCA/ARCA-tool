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

google.load("visualization", "1", {packages:["corechart"]});
function drawChart(chartData, divName, title) {
    var data = google.visualization.arrayToDataTable(chartData);
    var options = {
        title: title
    };
    var chartElement = document.getElementById(divName);
    chartElement.className = 'classificationCharts';
    var chart = new google.visualization.PieChart(chartElement);
    chart.draw(data, options);
}


/**
 * Replaces the output elements with empty ones if nothing is selected to be shown
 * @return the amount of choices to show
 */
function replaceElementsIfEmpty(showAll, showCorrected, showLiked) {
    var i = 0;
    if (showAll) { i++; }
    if (showCorrected) { i++; }
    if (showLiked) { i++; }
    if (i == 0) {
        $('#classificationTable').replaceWith('<table id="classificationTable"></table>');
        $('#chart_div_percentOfCauses').replaceWith('<p id="chart_div_percentOfCauses" ></p>');
        $('#chart_div_percentOfCauses_2').replaceWith('<p id="chart_div_percentOfCauses_2" ></p>');
        $('#chart_div_percentOfCorrectionCauses').replaceWith('<p id="chart_div_percentOfCorrectionCauses" ></p>');
        $('#chart_div_percentOfCorrectionCauses_2').replaceWith('<p id="chart_div_percentOfCorrectionCauses_2" ></p>');
        $('#chart_div_percentOfProposedCauses').replaceWith('<p id="chart_div_percentOfProposedCauses" ></p>');
        $('#chart_div_percentOfProposedCauses_2').replaceWith('<p id="chart_div_percentOfProposedCauses_2" ></p>');
    }
    return i;
}


/**
 * Generates the HTML classification table and fills it with corresponding data
 * @param targetElement the
 */
function generateClassificationTable(targetElement, showAll, showLiked, showCorrected) {
    var buttonsChecked = replaceElementsIfEmpty(showAll, showCorrected, showLiked);
    if (buttonsChecked == 0) {
        return;
    }

    var rows = arca.classificationTable.rowNames.length;
    var cols = arca.classificationTable.colNames.length;
    var table = $('<table id="' + targetElement + '" class="condensed-table bordered-table"><tbody>');

    var percentOfCausesRowDataArray = new Array();
    var percentOfProposedCausesRowDataArray = new Array();
    var percentOfCorrectionCausesRowDataArray = new Array();
    var percentOfCausesRowDataArray_2 = new Array();
    var percentOfProposedCausesRowDataArray_2 = new Array();
    var percentOfCorrectionCausesRowDataArray_2 = new Array();

    var pieSlice = ['Dimension', 'What'];
    var pieSlice_2 = ['Dimension', 'Where'];

    percentOfCausesRowDataArray.push(pieSlice);
    percentOfCorrectionCausesRowDataArray.push(pieSlice);
    percentOfProposedCausesRowDataArray.push(pieSlice);
    percentOfCausesRowDataArray_2.push(pieSlice_2);
    percentOfCorrectionCausesRowDataArray_2.push(pieSlice_2);
    percentOfProposedCausesRowDataArray_2.push(pieSlice_2);

    for (var r = -1; r < rows; r++) {
        var tr = $('<tr>');
        for (var c = -1; c < cols; c++) {
            // Header row / column
            if (r == -1 || c == -1) {
                // Top-left cell
                if (r == -1 && c == -1) {
                    $('<th>#</th>').appendTo(tr);
                }

                // Top row
                else if (r == -1) {
                    $('<th colspan=' + buttonsChecked + '">' + arca.classificationTable.colNames[c] + '</th>').appendTo(tr);
                }

                // First column
                else if (c == -1) {
                    $('<th>' + arca.classificationTable.rowNames[r] + '</th>').appendTo(tr);
                    pieSlice = new Array();
                    pieSlice.push(arca.classificationTable.rowNames[r]);
                }
            }

            // Normal cell
            else {
                // Format the numbers to one decimal place
                var percentOfCauses = Math.round(arca.classificationTable.table[r][c].percentOfCauses * 10) / 10;
                var percentOfCorrectionCauses = Math.round(arca.classificationTable.table[r][c].percentOfCorrectionCauses * 10) / 10;
                var percentOfProposedCauses = Math.round(arca.classificationTable.table[r][c].percentOfProposedCauses * 10) / 10;

                if (showAll) {
                    if (percentOfCauses == 0 || isNaN(percentOfCauses)) {
                        $('<td> - </td>').appendTo(tr);
                    } else {
                        $('<td id="causes' + (r + 1) + '_' + (c + 1) + '" title="' + arca.localization.causes + '">' + percentOfCauses + ' %</td>').appendTo(tr);
                        if (c == cols - 1) {
                            pieSlice.push(percentOfCauses);
                            percentOfCausesRowDataArray.push(pieSlice.slice());
                            pieSlice.pop();
                        }
                        if (r == rows - 1) {
                            pieSlice_2 = new Array();
                            pieSlice_2.push(arca.classificationTable.colNames[c]);
                            pieSlice_2.push(percentOfCauses);
                            percentOfCausesRowDataArray_2.push(pieSlice_2.slice());
                        }
                    }
                }

                if (showLiked) {
                    if (percentOfProposedCauses == 0 || isNaN(percentOfProposedCauses)) {
                        $('<td> - </td>').appendTo(tr);
                    } else {
                        $('<td title="' + arca.localization.liked + '">' + percentOfProposedCauses +
                          ' %</td>').appendTo(tr);
                        if (c == cols - 1) {
                            pieSlice.push(percentOfProposedCauses);
                            percentOfProposedCausesRowDataArray.push(pieSlice.slice());
                            pieSlice.pop();
                        }
                        if (r == rows - 1) {
                            pieSlice_2 = new Array();
                            pieSlice_2.push(arca.classificationTable.colNames[c]);
                            pieSlice_2.push(percentOfProposedCauses);
                            percentOfProposedCausesRowDataArray_2.push(pieSlice_2.slice());
                        }
                    }
                }

                if (showCorrected) {
                    if (percentOfCorrectionCauses == 0 || isNaN(percentOfCorrectionCauses)) {
                        $('<td> - </td>').appendTo(tr);
                    } else {
                        $('<td title="' + arca.localization.corrections + '">' + percentOfCorrectionCauses +
                          ' %</td>').appendTo(tr);
                        if (c == cols - 1) {
                            pieSlice.push(percentOfCorrectionCauses);
                            percentOfCorrectionCausesRowDataArray.push(pieSlice.slice());
                            pieSlice.pop();
                        }
                        if (r == rows - 1) {
                            pieSlice_2 = new Array();
                            pieSlice_2.push(arca.classificationTable.colNames[c]);
                            pieSlice_2.push(percentOfCorrectionCauses);
                            percentOfCorrectionCausesRowDataArray_2.push(pieSlice_2.slice());
                        }
                    }
                }
            }
        }
        tr.appendTo(table);
    }

    // Close and replace the table element
    $('</tbody></table>').appendTo(table);
    $('#' + targetElement).replaceWith(table);

    // Draw the pie charts
    if (showAll) {
        drawChart(percentOfCausesRowDataArray, 'chart_div_percentOfCauses', arca.localization.piechartforprocessareacauses);
        drawChart(percentOfCausesRowDataArray_2, 'chart_div_percentOfCauses_2', arca.localization.piechartforcauseclasscauses);
    } else {
        $('#chart_div_percentOfCauses').replaceWith('<p id="chart_div_percentOfCauses"></p>');
        $('#chart_div_percentOfCauses_2').replaceWith('<p id="chart_div_percentOfCauses_2"></p>');
    }

    if (showCorrected) {
        drawChart(percentOfCorrectionCausesRowDataArray, 'chart_div_percentOfCorrectionCauses',
                  arca.localization.piechartforprocessareacorrections);
        drawChart(percentOfCorrectionCausesRowDataArray_2, 'chart_div_percentOfCorrectionCauses_2',
                  arca.localization.piechartforcauseclasscorrections);
    } else {
        $('#chart_div_percentOfCorrectionCauses').replaceWith('<p id="chart_div_percentOfCorrectionCauses"></p>');
        $('#chart_div_percentOfCorrectionCauses_2').replaceWith('<p id="chart_div_percentOfCorrectionCauses_2"></p>');
    }

    if (showLiked) {
        drawChart(percentOfProposedCausesRowDataArray, 'chart_div_percentOfProposedCauses', arca.localization.piechartforprocessarealiked);
        drawChart(percentOfProposedCausesRowDataArray_2, 'chart_div_percentOfProposedCauses_2', arca.localization.piechartforcauseclassliked);
    } else {
        $('#chart_div_percentOfProposedCauses').replaceWith('<p id="chart_div_percentOfProposedCauses"></p>');
        $('#chart_div_percentOfProposedCauses_2').replaceWith('<p id="chart_div_percentOfProposedCauses_2"></p>');
    }
}