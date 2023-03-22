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

console.log("Cool, request-description.js is loaded :)");

var requestDescriptionText = document.getElementById("request-description-text");
var oldRequestDescriptionText;
var submitAddRequestDescriptionText = document.getElementById('submit-add-request-description-text');


function eraseText() {
  var eraseText = document.getElementById('erase_text');
  requestDescriptionText.value="";
  eraseText.style.visibility = "hidden";
  if (oldRequestDescriptionText != requestDescriptionText.value) {
    submitAddRequestDescriptionText.disabled = false;
  } else {
    submitAddRequestDescriptionText.disabled = true;
  }
};


function checkRequestDescriptionText(event) {
  var eraseText = document.getElementById('erase_text');
  if (oldRequestDescriptionText != requestDescriptionText.value) {
    submitAddRequestDescriptionText.disabled = false;
  } else {
    submitAddRequestDescriptionText.disabled = true;
  }
  if (requestDescriptionText.value != "") {
    eraseText.style.visibility = "visible";
  } else {
    eraseText.style.visibility = "hidden";
  }
}

$('#add_description_request').on('hidden.bs.modal', function (e) {
  var eraseText = document.getElementById('erase_text');
  requestDescriptionText.value= oldRequestDescriptionText;
  submitAddRequestDescriptionText.disabled = true;
  if (requestDescriptionText.value != "") {
    eraseText.style.visibility = "visible";
  } else {
    eraseText.style.visibility = "hidden";
  }
})

var errorDeleteRequestDescriptionSpan = document.getElementById('errorDeleteRequestDescriptionSpan');


var $formDeleteVideoForRequestDescription = $('#deleteVideoFileForRequestDescription');
$formDeleteVideoForRequestDescription.on('submit', function(event) {
  document.getElementById('submitButtonDeleteRequestDescription').disabled = true;
  $(".spinner").removeClass("spinner_hidden").addClass("spinner-delete_show");
  $(".spinner").css("z-index","1500").visibility="visible";
  /*  $("#submitButtonDelete").css("color","black");*/
  var $form = $(this);
  var formdata = new FormData($form[0]);
  var data = (formdata !== null) ? formdata : $form.serialize();

  event.preventDefault();
  $.ajax({
    url: $formDeleteVideoForRequestDescription.attr('action'),
    type: 'post',
    data: data,
    contentType:false,
    processData: false,
    //dataType: 'json',
    success: function(response) {
      var url = response;
      errorDeleteRequestDescriptionSpan.style.visibility="hidden";
      $(".spinner").visibility="hidden";
      $("#delete_description_request").modal('hide');
      $("#validate_delete_description_request").modal('show');
      setTimeout(function(){
        $('#validate_delete_description_request').modal('hide');
        location.reload();
      }, 3000);

    },
    error: function(response) {
      errorDeleteRequestDescriptionSpan.textContent = response.responseText;
      errorDeleteRequestDescriptionSpan.style.visibility="visible";
      $(".spinner").css("z-index","-1").css("opacity","0.1");
      $(".spinner").visibility="hidden";
      console.log("Erreur " + response.responseText);
    }
  })

});

$formDeleteVideoForRequestDescription.on('input', function(event) {
  document.getElementById('errorDeleteRequestDescriptionSpan').style.visibility="hidden";
});

var $delete_description_request = $('#delete_description_request');
$delete_description_request.on('hidden.bs.modal', function() {
  console.log("hidden delete_description_request modal");
  document.getElementById('submitButtonDeleteRequestDescription').disabled = false;
  if ($('#delete_description_request').find('#errorDeleteRequestDescriptionSpan').length) {
    errorDeleteRequestDescriptionSpan.style.visibility="hidden";
  }
});

function main() {
  requestDescriptionText.addEventListener('keyup', checkRequestDescriptionText);
  oldRequestDescriptionText = requestDescriptionText.value;
  submitAddRequestDescriptionText.disabled = true;

}

(function AfterLoad($) {

  main();

})($);