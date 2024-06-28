/*
 * #%L
 * Telsigne
 * %%
 * Copyright (C) 2016 Orange
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
console.log("Cool, request.js is loaded :)");




var $formRequest = $('#requestInfo');
var seeSignButton = document.getElementById('seeSignButton');
var requestSpan = document.getElementById('requestSpan');
$formRequest.on('submit', function(event) {
  console.log("submit");
    event.preventDefault();
    request = {
      requestName: $('#requestName').val(),
      requestTextDescription: $('#requestTextDescription').val()
    };
    $.ajax({
       url: $formRequest.attr('action'),
       type: 'post',
       data: JSON.stringify(request),
       contentType: "application/json",
       success: function(response) {
         $("#confirm-request-created").modal('show');
         setTimeout(function(){
           $('#confirm-request-created').modal('hide');
           requestSpan.style.visibility="hidden";
           var url = "/sec/my-request-detail/"+response.requestId;
           console.log(window.location.href);
           window.history.replaceState({}, 'foo', url);
           console.log(window.location.href);
           window.location = url;
           /*location.reload();*/
         }, 3000);
       },
       error: function(response) {
         var returnedData = JSON.parse(response.responseText);
         requestSpan.textContent = returnedData.errorMessage;
         if (returnedData.errorType == 2) {
           seeSignButton.style.visibility="visible";
           seeSignButton.href="/sign/"+returnedData.signId;
         }
         requestSpan.style.visibility="visible";
       }
})});

$formRequest.on('input', function(event) {
    document.getElementById('requestSpan').style.visibility="hidden";
});

var $new_request = $('#new_request');
$new_request.on('hidden.bs.modal', function() {
    if ($('#requestInfo').find('#requestSpan').length) {
        requestSpan.style.visibility="hidden";
        seeSignButton.style.visibility="hidden";
       /* $('#requestName').val("");
        $('#requestTextDescription').val("");*/
    }
  $('#requestName').val(oldRequestName);
});

var oldRequestName;
$new_request.on('show.bs.modal', function() {
  oldRequestName= $('#requestName').val();
});

var $modify_request = $('#modify_request');
$modify_request.on('hidden.bs.modal', function() {
  if ($('#requestInfo').find('#requestSpan').length) {
    requestSpan.style.visibility="hidden";
    seeSignButton.style.visibility="hidden";
  }

});

var $formRequestDescription = $('#uploadSelectedVideoFile');
var errorSelectedSpan = document.getElementById('errorSelectedSpan');
$formRequestDescription.on('submit', function(event) {
  console.log("submit uploadSelectedVideoFile");
  console.log("requestName "+ $('#requestName').val());
  console.log("requestTextDescription " + $('#requestTextDescription').val());
  //document.getElementById('submitButtonFileDailymotion').disabled = true;
  /*if (document.getElementById("InputFile").value) {*/
        $(".spinner").removeClass("spinner_hidden").addClass("spinner_show");
        $(".spinner").css("z-index","1500").visibility="visible";
        $(".spinner").css("opacity", "1");
        /*$("#submitButtonFileDailymotion").css("color","black");*/
        var $form = $(this);
        var formdata = new FormData($form[0]);
        formdata.append('requestName', $('#requestName').val());
        formdata.append('requestTextDescription', $('#requestTextDescription').val());
        var data = (formdata !== null) ? formdata : $form.serialize();
        document.getElementById('submitButtonFileDailymotion').disabled=true;
        event.preventDefault();
        $.ajax({
          url: $formRequestDescription.attr('action'),
          type: 'post',
          data: data,
          contentType: false,
          processData: false,
          success: function(response) {
            $("#confirm-request-created").modal('show');
            setTimeout(function(){
              $('#confirm-request-created').modal('hide');
 /*             var url = "/sec/my-request-detail/"+response.requestId;
              console.log(window.location.href);
              window.history.replaceState({}, 'foo', url);
              console.log(window.location.href);
              window.location = url;*/
              location.reload();
              errorSelectedSpan.style.display="none";
              $(".spinner").visibility="hidden";
            }, 3000);
          },
          error: function(response) {
            var returnedData = JSON.parse(response.responseText);
            errorSelectedSpan.textContent = returnedData.errorMessage;
            if (returnedData.errorType == 2) {
              seeSignButton.style.visibility="visible";
              seeSignButton.href="/sign/"+returnedData.signId;
            }
            errorSelectedSpan.style.display="inline-block";
            $(".spinner").css("z-index","-1").css("opacity","0.1");
            $(".spinner").visibility="hidden";
          }
        })
/*  } else {
    event.preventDefault();
    errorSelectedSpan.textContent = "Vous devez séléctionner un fichier";
    errorSelectedSpan.style.visibility = "visible";
  }*/

  });

$formRequestDescription.on('input', function(event) {
  document.getElementById('errorSelectedSpan').style.display="none";
});

var $add_video_file_dailymotion = $('#add_video_file_dailymotion');
$add_video_file_dailymotion.on('hidden.bs.modal', function() {
  console.log("hidden add_video_file_dailymotion modal");
  //document.getElementById('submitButtonFileDailymotion').disabled = false;
  if ($('#uploadSelectedVideoFile').find('#errorSelectedSpan').length) {
    errorSelectedSpan.style.display="none";
  }
  document.getElementById('submitButtonFileDailymotion').disabled=true;

  document.getElementById("InputFile").value="";

  document.getElementById("subtitle_for_modal_video").style.display="";
});

var $add_request_description_LSF = $('#add_request_description_LSF');
$add_request_description_LSF.on('show.bs.modal', function() {
  console.log("show $add_request_description_LSF modal");
});

$add_request_description_LSF.on('hidden.bs.modal', function() {
  console.log("hidden $add_request_description_LSF modal");
});

function onClick() {
  event.preventDefault();
  document.getElementById('InputFile').click()
}




$(document).ready(function(){

  $('input[type="file"]').change(function(e){
    $("#add_video_file_dailymotion").modal('show');
    document.getElementById('submitButtonFileDailymotion').disabled=false;
  });

});