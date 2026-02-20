function request(url, data, methodType, dataType){
    $.ajax({
        url:url,
        type:methodType,
        data: data,
        dataType:dataType,
        success: innerSuccessRequest,
        error: errorRequest
    });
}

function innerSuccessRequest(result) {
    successRequest(result);
}
