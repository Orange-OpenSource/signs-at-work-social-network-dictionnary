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
console.log("Cool, sign.js is loaded :)");

var errorSelectedSpan = document.getElementById('errorSelectedSpan');
/*var cancelSelectVideo = document.getElementById('cancel-select-video');*/


var $formUploadSelectedVideoFile = $('#uploadSelectedVideoFile');
$formUploadSelectedVideoFile.on('submit', function(event) {
  /*if (document.getElementById("InputFile").value) {*/
    $(".spinner").removeClass("spinner_hidden").addClass("spinner_show");
    $(".spinner").css("z-index", "1500").visibility = "visible";
    $(".spinner").css("opacity", "1");
/*    $("#submitButtonFileDailymotion").css("color", "black");*/
    var $form = $(this);
    var formdata = new FormData($form[0]);
    var data = (formdata !== null) ? formdata : $form.serialize();
    document.getElementById('submitButtonFileDailymotion').disabled=true;
    event.preventDefault();
    $.ajax({
      url: $formUploadSelectedVideoFile.attr('action'),
      type: 'post',
      data: data,
      contentType: false,
      processData: false,
      success: function (response) {
        var url = response;
        errorSelectedSpan.style.display = "none";
        $(".spinner").visibility = "hidden";
        console.log("Success " + response);
        console.log(window.location.href);
        window.history.replaceState({}, 'foo', url);
        console.log(window.location.href);
        window.location = url;
      },
      error: function (response) {
        errorSelectedSpan.textContent = response.responseText;
        errorSelectedSpan.style.display = "inline-block";
        $(".spinner").css("z-index", "-1").css("opacity", "0.1");
        $(".spinner").visibility = "hidden";
        console.log("Erreur " + response.responseText);
      }
    })
  /*} else {
    event.preventDefault();
    errorSelectedSpan.textContent = "Vous devez séléctionner un fichier";
    errorSelectedSpan.style.display = "block";
  }*/

});

$formUploadSelectedVideoFile.on('input', function(event) {
  document.getElementById('errorSelectedSpan').style.display="none";
});

var $add_video_file_dailymotion = $('#add_video_file_dailymotion');
$add_video_file_dailymotion.on('hidden.bs.modal', function() {
 console.log("hidden add_video_file_dailymotion modal");

  if ($('#uploadSelectedVideoFile').find('#errorSelectedSpan').length) {
    errorSelectedSpan.style.display="none";
    /*$('#signNameSelected').val("");*/
  }
  document.getElementById('submitButtonFileDailymotion').disabled=true;

  document.getElementById("InputFile").value="";

  document.getElementById("subtitle_for_modal_video").style.display="";

});

/*if (cancelSelectVideo) {
  cancelSelectVideo.onclick = function () {
    $('#add_sign_definition_LSF').modal('hide');
    location.reload();
  };
}*/

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