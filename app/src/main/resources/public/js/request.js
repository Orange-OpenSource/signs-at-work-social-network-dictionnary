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
  console.log("submit requestInfo");
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
           var url = "/sec/request/";
           window.location = url;
           requestSpan.style.visibility="hidden";
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
        $('#requestName').val("");
        $('#requestTextDescription').val("");
    }
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
  document.getElementById('submitButtonFileDailymotion').disabled = true;
  $(".spinner").removeClass("spinner_hidden").addClass("spinner_show");
  $(".spinner").css("z-index","1500").visibility="visible";
  $("#submitButtonFileDailymotion").css("color","black");
  var $form = $(this);
  var formdata = new FormData($form[0]);
  formdata.append('requestName', $('#requestName').val());
  formdata.append('requestTextDescription', $('#requestTextDescription').val());
  var data = (formdata !== null) ? formdata : $form.serialize();

  event.preventDefault();
  $.ajax({
    url: $formRequestDescription.attr('action'),
    type: 'post',
    data: data,
    contentType: false,
    processData: false,
    success: function(response) {
      var url = "/sec/my-request-detail/"+response.requestId;
      window.location = url;
      errorSelectedSpan.style.visibility="hidden";
      $(".spinner").visibility="hidden";
    },
    error: function(response) {
      var returnedData = JSON.parse(response.responseText);
      errorSelectedSpan.textContent = returnedData.errorMessage;
      if (returnedData.errorType == 2) {
        seeSignButton.style.visibility="visible";
        seeSignButton.href="/sign/"+returnedData.signId;
      }
      errorSelectedSpan.style.visibility="visible";
      $(".spinner").css("z-index","-1").css("opacity","0.1");
      $(".spinner").visibility="hidden";
    }
  })});
