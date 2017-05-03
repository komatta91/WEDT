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
        $("#calculatedResult").html('');
        var language = $('#language option:selected').val();
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
        $("#calculatedResult").html('');

    });

    $('#secondEntry').on('change', function(){
        $("#calculatedResult").html('');
    });

};

function getRequestData() {
    var data = {};
    if ($("#language").val().length == 1) {
        data["language"] = $("#language").val()[0];
    }
    if ($("#firstEntry").val() && $("#firstEntry").val().length == 1) {
        data["language"] = $("#firstEntry").val()[0];
    }
    if ($("#secondEntry").val() && $("#secondEntry").val().length == 1) {
        data["language"] = $("#secondEntry").val()[0];
    }
    console.log(data);
    console.log(JSON.stringify( data ));

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
    waitingDialog.show('Custom message');
    sendAjax( "Calculate", getRequestData(),
        function ( data ) {
            waitingDialog.hide();
            if (data >= 0)
            {
                $("#calculatedResult").html("Result: " + data)
            } else {
                $("#calculatedResult").html("Result: invalid data")
            }

        },
        function (e) {
            console.log( e );
            waitingDialog.hide();
            $("#calculatedResult").html("Result: " + e)
        });
}