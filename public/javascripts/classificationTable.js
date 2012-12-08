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

google.load("visualization", "1", {packages:["corechart"]});
//google.setOnLoadCallback(generateClassificationTable());
function drawChart(chartData, divName, title) {
    //var chartDat = [['Task', 'Hours per Day'],['Work', 0],['Eat', 2],['Commute', 2],['Watch TV', 2],['Sleep', 7]];
    var data = google.visualization.arrayToDataTable(chartData);

    var options = {
    title: title
    };
    var chartElement = document.getElementById(divName);
    chartElement.className = 'classificationCharts';
    var chart = new google.visualization.PieChart(chartElement);
    chart.draw(data, options);
}

//Calculates how many checkbox buttons are checked
function howManyRadioButtonsChecked() {
    var i = 0;
    if (document.getElementById('all').checked) {
        i++;
    }
    if (document.getElementById('liked').checked) {
        i++;
    }
    if (document.getElementById('corrections').checked) {
        i++;
    }
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