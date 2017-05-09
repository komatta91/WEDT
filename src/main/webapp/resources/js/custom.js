var finalScore;
var googleDistance;
var angleMeasure;
function init() {
    $('.selectpicker').selectpicker();
    var options = {
        ajax : {
            type: "POST",
            contentType: "application/json",
            url: 'EntryList',
            data:  function() {
                var data = {};
                data["language"] = $('#language option:selected').val();
                data["search"] = '{{{q}}}';
                return  data;
            },
            dataType: 'json',
            timeout: 600000
        },
        log: false,
        cache: false,
        preprocessData: function (data) {
            var res = [];
            for (var elem in data) {
                var ob = { value: data[elem], text: data[elem] };
                res.push(ob);
            }
            return res;

        }
    };

    $('#firstEntry').ajaxSelectPicker(options);
    $('#secondEntry').ajaxSelectPicker(options);

    $('#language').on('change', function(){
        finalScore.refresh(1);
        googleDistance.refresh(1);
        angleMeasure.refresh(1);
        $("#calculatedResult").html('');
        var language = $('#language option:selected').val();
        $("#firstEntry").selectpicker('deselectAll');
        $("#secondEntry").selectpicker('deselectAll');
        if (language) {
            $("#firstEntry")
                .html('')
                .removeAttr('disabled')
                .selectpicker('refresh');
            $("#secondEntry")
                .html('')
                .removeAttr('disabled')
                .selectpicker('refresh');

        } else {
            $("#firstEntry")
                .html('')
                .attr('disabled',true)
                .selectpicker('refresh');
            $("#secondEntry")
                .html('')
                .attr('disabled',true)
                .selectpicker('refresh');
        }
    });

    $('#firstEntry').on('change', function(){
        finalScore.refresh(1);
        googleDistance.refresh(1);
        angleMeasure.refresh(1);

    });

    $('#secondEntry').on('change', function(){
        finalScore.refresh(1);
        googleDistance.refresh(1);
        angleMeasure.refresh(1);
    });


    finalScore = new JustGage({
        id: "finalScore",
        value: 1,
        min: 0,
        max: 1,
        label: "Final Score",
        pointer: true,
        reverse: true,
        textRenderer: function(val) {
            return Number((parseFloat(val)).toFixed(5));
        },
    });

    googleDistance = new JustGage({
        id: "googleDistance",
        value: 1,
        min: 0,
        max: 1,
        label: "Google Distance",
        pointer: true,
        reverse: true,
        textRenderer: function(val) {
            return Number((parseFloat(val)).toFixed(5));
        },
    });

    angleMeasure = new JustGage({
        id: "angleMeasure",
        value: 1,
        min: 0,
        max: 1,
        label: "Angle Measure",
        pointer: true,
        reverse: true,
        textRenderer: function(val) {
            return Number((parseFloat(val)).toFixed(5));
        },
    });

};

function getRequestData() {
    var data = {};
    if ($("#language").val() && $("#language").val().length == 1) {
        data["language"] = $("#language").val()[0];
    }
    if ($("#firstEntry").val() && $("#firstEntry").val().length == 1) {
        data["firstEntry"] = $("#firstEntry").val()[0];
    }
    if ($("#secondEntry").val() && $("#secondEntry").val().length == 1) {
        data["secondEntry"] = $("#secondEntry").val()[0];
    }
    return JSON.stringify( data );
};

function sendAjax( url, data, successFunc, errorFunc ) {
    $.ajax({
        type: "POST",
        contentType: "application/json",
        url: url,
        data: data,
        dataType: 'json',
        timeout: 600000,
        success: successFunc,
        error: errorFunc
    });
};


function calculate() {
    waitingDialog.show('Pleas wait, calculating...', {progressType: 'success'});
    sendAjax( "Calculate", getRequestData(),
        function ( data ) {
            finalScore.refresh(data.finalScore);
            googleDistance.refresh(data.googleDistance);
            angleMeasure.refresh(data.angle);
            waitingDialog.hide();
        },
        function (e) {
            console.log( e );
            waitingDialog.hide();
            $("#calculatedResult").html("Result: " + e)
        });
}