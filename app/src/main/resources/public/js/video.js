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
console.log("Cool, video.js is loaded :)");

var errorDeletedSpan = document.getElementById('errorDeletedSpan');


var $formDeleteVideo = $('#deleteVideo');
$formDeleteVideo.on('submit', function(event) {
  document.getElementById('submitButtonDelete').disabled = true;
  $(".spinner").removeClass("spinner_hidden").addClass("spinner_show");
  $(".spinner").css("z-index","1500").visibility="visible";
  $("#submitButtonDelete").css("color","black");
  var $form = $(this);
  var formdata = new FormData($form[0]);
  var data = (formdata !== null) ? formdata : $form.serialize();

    event.preventDefault();
    $.ajax({
      url: $formDeleteVideo.attr('action'),
      type: 'post',
      data: data,
      contentType:false,
      processData: false,
      //dataType: 'json',
      success: function(response) {
        var url = response;
        window.location = url;
        errorDeletedSpan.style.visibility="hidden";
        $(".spinner").visibility="hidden";
        console.log("Success " + response);
      },
      error: function(response) {
        errorDeletedSpan.textContent = response.responseText;
        errorDeletedSpan.style.visibility="visible";
        $(".spinner").css("z-index","-1").css("opacity","0.1");
        $(".spinner").visibility="hidden";
        console.log("Erreur " + response.responseText);
      }
    })

});

$formDeleteVideo.on('input', function(event) {
  document.getElementById('errorDeletedSpan').style.visibility="hidden";
});

var $video_delete_modal = $('#video_delete_modal');
$video_delete_modal.on('hidden.bs.modal', function() {
 console.log("hidden video_delete_modal modal");
  document.getElementById('submitButtonDelete').disabled = true;
  if ($('#deleteVideo').find('#errorDeletedSpan').length) {
    errorDeletedSpan.style.visibility="hidden";
  }
});