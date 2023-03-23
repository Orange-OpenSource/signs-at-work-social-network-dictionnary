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

console.log("Cool, sign-definition.js is loaded :)");

var signDefinitionText = document.getElementById("sign-definition-text");
var oldSignDefinitionText;
var submitAddSignDefinitionText = document.getElementById('submit-add-sign-definition-text');


function eraseText() {
  var eraseText = document.getElementById('erase_text');
  signDefinitionText.value="";
  eraseText.style.visibility = "hidden";
  if (oldSignDefinitionText != signDefinitionText.value) {
    submitAddSignDefinitionText.disabled = false;
  } else {
    submitAddSignDefinitionText.disabled = true;
  }
};


function checkSignDefinitionText(event) {
  var eraseText = document.getElementById('erase_text');
  if (oldSignDefinitionText != signDefinitionText.value) {
    submitAddSignDefinitionText.disabled = false;
  } else {
    submitAddSignDefinitionText.disabled = true;
  }
  if (signDefinitionText.value != "") {
    eraseText.style.visibility = "visible";
  } else {
    eraseText.style.visibility = "hidden";
  }
}

$('#add_sign_definition_text').on('hidden.bs.modal', function (e) {
  var eraseText = document.getElementById('erase_text');
  signDefinitionText.value= oldSignDefinitionText;
  submitAddSignDefinitionText.disabled = true;
  if (signDefinitionText.value != "") {
    eraseText.style.visibility = "visible";
  } else {
    eraseText.style.visibility = "hidden";
  }
})

var errorDeletedSignDefinitionSpan = document.getElementById('errorDeletedSignDefinitionSpan');


var $formDeleteVideoForSignDefinition = $('#deleteVideoFileForSignDefinition');
$formDeleteVideoForSignDefinition.on('submit', function(event) {
  document.getElementById('submitButtonDeleteSignDefinition').disabled = true;
  $(".spinner").removeClass("spinner_hidden").addClass("spinner-delete_show");
  $(".spinner").css("z-index","1500").visibility="visible";
  /*  $("#submitButtonDelete").css("color","black");*/
  var $form = $(this);
  var formdata = new FormData($form[0]);
  var data = (formdata !== null) ? formdata : $form.serialize();

  event.preventDefault();
  $.ajax({
    url: $formDeleteVideoForSignDefinition.attr('action'),
    type: 'post',
    data: data,
    contentType:false,
    processData: false,
    //dataType: 'json',
    success: function(response) {
      var url = response;
      errorDeletedSignDefinitionSpan.style.visibility="hidden";
      $(".spinner").visibility="hidden";
      $("#delete_definition_sign").modal('hide');
      $("#validate_delete_definition_sign").modal('show');
      setTimeout(function(){
        $('#validate_delete_definition_sign').modal('hide');
        location.reload();
      }, 3000);

    },
    error: function(response) {
      errorDeletedSignDefinitionSpan.textContent = response.responseText;
      errorDeletedSignDefinitionSpan.style.visibility="visible";
      $(".spinner").css("z-index","-1").css("opacity","0.1");
      $(".spinner").visibility="hidden";
      console.log("Erreur " + response.responseText);
    }
  })

});

$formDeleteVideoForSignDefinition.on('input', function(event) {
  document.getElementById('errorDeletedSignDefinitionSpan').style.visibility="hidden";
});

var $delete_definition_sign = $('#delete_definition_sign');
$delete_definition_sign.on('hidden.bs.modal', function() {
  console.log("hidden delete_definition_sign modal");
  document.getElementById('submitButtonDeleteSignDefinition').disabled = false;
  if ($('#delete_definition_sign').find('#errorDeletedSignDefinitionSpan').length) {
    errorDeletedSignDefinitionSpan.style.visibility="hidden";
  }
});

function main() {
  signDefinitionText.addEventListener('keyup', checkSignDefinitionText);
  oldSignDefinitionText = signDefinitionText.value;
  submitAddSignDefinitionText.disabled = true;

}

(function AfterLoad($) {

  main();

})($);