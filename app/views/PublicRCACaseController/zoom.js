    // variable for current zoom level
    var zoomLevel = 1;
    // variable for minimum zoom level
    var zoomMin = 1 / 32;
    // variable for maximum zoom level
    var zoomMax = 2;
    // steps in zoom slider
    var zoomSteps = 64;

    // increment zoom by one step
    incZoomSlider = function() {
        var sliderValue = $("#slider-vertical").slider("value");
        applyZoom(((zoomMax - zoomMin) / zoomSteps * (sliderValue + 1) + zoomMin) / zoomLevel, true);
    }

    // decrement zoom by one step
    decZoomSlider = function() {
        var sliderValue = $("#slider-vertical").slider("value");
        applyZoom(((zoomMax - zoomMin) / zoomSteps * (sliderValue - 1) + zoomMin) / zoomLevel, true);
    }

    // add slider element to body
    $("body").append('<div id="zoomSlider"><img id="zoomin" src="@{'/public/images/zoom-in.png'}" onclick="incZoomSlider()"/>' +
                                                       '<div id="slider-vertical"/>' +
                                                       '<img id="zoomout" src="@{'/public/images/zoom-out.png'}" onclick="decZoomSlider()" /></div>');

    // zooming routine
    function applyZoom(newLevel, updateSlider) {
        // hides a radial menu when zoomed as the scaling does not work yet.
        jQuery("#radial_menu").radmenu("hide");
        $('.popover').remove();
        // is true if canvas has been scaled or resized, this will reset the zoom of the canvas
        if (fd.canvas.scaleOffsetX != zoomLevel) {
            newLevel = zoomLevel / fd.canvas.scaleOffsetX;
        }
        // check if zoomMax and zoomMin have been crossed
        else if (zoomLevel * newLevel < zoomMax && zoomLevel * newLevel > zoomMin) {
            zoomLevel = zoomLevel * newLevel;
        } else if (zoomLevel * newLevel > zoomMin) {
            newLevel = zoomMax / zoomLevel;
            zoomLevel = zoomMax;
        } else {
            newLevel = zoomMin / zoomLevel;
            zoomLevel = zoomMin;
        }

        // zooming nodes with CSS3 transform
        var nodes = $("#infovis-label div.node");
        nodes.css("-webkit-transform", "scale(" + zoomLevel + ")");
        nodes.css("-moz-transform",  "scale(" + zoomLevel + ")");
        nodes.css("-ms-transform",  "scale(" + zoomLevel + ")");
        nodes.css("-o-transform",  "scale(" + zoomLevel + ")");
        nodes.css("transform",  "scale(" + zoomLevel + ")");

        // force directed canvas zooming
        fd.canvas.scale(newLevel, newLevel);

        // lets update slider value if necessary
        if (updateSlider) {
            $("#slider-vertical").slider("value", (zoomLevel - zoomMin) * zoomSteps / (zoomMax - zoomMin));
        }
    };

    // apply slider to slider div
    $(function() {
        $( "#slider-vertical" ).slider({
                                           orientation: "vertical",
                                           range: "min",
                                           min: 0,
                                           max: zoomSteps,
                                           value: zoomSteps / 2,
                                           slide: function( event, ui ) {
                                               applyZoom(((zoomMax - zoomMin) / zoomSteps * ui.value + zoomMin) / zoomLevel, false);
                                           }
                                       });
    });