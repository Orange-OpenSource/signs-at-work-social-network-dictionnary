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

console.log("Cool, community-description.js is loaded :)");

var communityDescriptionText = document.getElementById("community-description-text");
var oldCommunityDescriptionText;
var submitAddCommunityDescriptionText = document.getElementById('submit-add-community-description-text');


function eraseText() {
  var eraseText = document.getElementById('erase_text');
  communityDescriptionText.value="";
  eraseText.style.visibility = "hidden";
  if (oldCommunityDescriptionText != communityDescriptionText.value) {
    submitAddCommunityDescriptionText.disabled = false;
  } else {
    submitAddCommunityDescriptionText.disabled = true;
  }
};


function checkCommunityDescriptionText(event) {
  var eraseText = document.getElementById('erase_text');
  if (oldCommunityDescriptionText != communityDescriptionText.value) {
    submitAddCommunityDescriptionText.disabled = false;
  } else {
    submitAddCommunityDescriptionText.disabled = true;
  }
  if (communityDescriptionText.value != "") {
    eraseText.style.visibility = "visible";
  } else {
    eraseText.style.visibility = "hidden";
  }
}

$('#add_community_description_text').on('hidden.bs.modal', function (e) {
  var eraseText = document.getElementById('erase_text');
  communityDescriptionText.value= oldCommunityDescriptionText;
  submitAddCommunityDescriptionText.disabled = true;
  if (communityDescriptionText.value != "") {
    eraseText.style.visibility = "visible";
  } else {
    eraseText.style.visibility = "hidden";
  }
})

var errorDeleteCommunityDescriptionSpan = document.getElementById('errorDeleteCommunityDescriptionSpan');


var $formDeleteVideoForCommunityDescription = $('#deleteVideoFileForCommunityDescription');
$formDeleteVideoForCommunityDescription.on('submit', function(event) {
  document.getElementById('submitButtonDeleteCommunityDescription').disabled = true;
  $(".spinner").removeClass("spinner_hidden").addClass("spinner-delete_show");
  $(".spinner").css("z-index","1500").visibility="visible";
  /*  $("#submitButtonDelete").css("color","black");*/
  var $form = $(this);
  var formdata = new FormData($form[0]);
  var data = (formdata !== null) ? formdata : $form.serialize();

  event.preventDefault();
  $.ajax({
    url: $formDeleteVideoForCommunityDescription.attr('action'),
    type: 'post',
    data: data,
    contentType:false,
    processData: false,
    //dataType: 'json',
    success: function(response) {
      var url = response;
      errorDeleteCommunityDescriptionSpan.style.visibility="hidden";
      $(".spinner").visibility="hidden";
      $("#delete_description_community").modal('hide');
      $("#validate_delete_description_community").modal('show');
      setTimeout(function(){
        $('#validate_delete_description_community').modal('hide');
        location.reload();
      }, 3000);

    },
    error: function(response) {
      errorDeleteCommunityDescriptionSpan.textContent = response.responseText;
      errorDeleteCommunityDescriptionSpan.style.visibility="visible";
      $(".spinner").css("z-index","-1").css("opacity","0.1");
      $(".spinner").visibility="hidden";
      console.log("Erreur " + response.responseText);
    }
  })

});

$formDeleteVideoForCommunityDescription.on('input', function(event) {
  document.getElementById('errorDeleteCommunityDescriptionSpan').style.visibility="hidden";
});

var $delete_description_community = $('#delete_description_community');
$delete_description_community.on('hidden.bs.modal', function() {
  console.log("hidden delete_description_community modal");
  document.getElementById('submitButtonDeleteCommunityDescription').disabled = false;
  if ($('#delete_description_community').find('#errorDeleteCommunityDescriptionSpan').length) {
    errorDeleteCommunityDescriptionSpan.style.visibility="hidden";
  }
});

function main() {
  communityDescriptionText.addEventListener('keyup', checkCommunityDescriptionText);
  oldCommunityDescriptionText = communityDescriptionText.value;
  submitAddCommunityDescriptionText.disabled = true;

}

(function AfterLoad($) {

  main();

})($);