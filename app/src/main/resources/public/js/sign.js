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
  if (document.getElementById("InputFile").value) {
    $(".spinner").removeClass("spinner_hidden").addClass("spinner_show");
    $(".spinner").css("z-index", "1500").visibility = "visible";
    $("#submitButtonFileDailymotion").css("color", "black");
    var $form = $(this);
    var formdata = new FormData($form[0]);
    var data = (formdata !== null) ? formdata : $form.serialize();

    event.preventDefault();
    $.ajax({
      url: $formUploadSelectedVideoFile.attr('action'),
      type: 'post',
      data: data,
      contentType: false,
      processData: false,
      success: function (response) {
        var url = response;
        errorSelectedSpan.style.visibility = "hidden";
        $(".spinner").visibility = "hidden";
        console.log("Success " + response);
        console.log(window.location.href);
        window.history.replaceState({}, 'foo', url);
        console.log(window.location.href);
        window.location = url;
      },
      error: function (response) {
        errorSelectedSpan.textContent = response.responseText;
        errorSelectedSpan.style.visibility = "visible";
        $(".spinner").css("z-index", "-1").css("opacity", "0.1");
        $(".spinner").visibility = "hidden";
        console.log("Erreur " + response.responseText);
      }
    })
  } else {
    event.preventDefault();
    errorSelectedSpan.textContent = "Vous devez séléctionner un fichier";
    errorSelectedSpan.style.visibility = "visible";
  }

});

$formUploadSelectedVideoFile.on('input', function(event) {
  document.getElementById('errorSelectedSpan').style.visibility="hidden";
});

var $add_video_file_dailymotion = $('#add_video_file_dailymotion');
$add_video_file_dailymotion.on('hidden.bs.modal', function() {
 console.log("hidden add_video_file_dailymotion modal");
  var fileName = document.getElementById("fileName");

  if ($('#uploadSelectedVideoFile').find('#errorSelectedSpan').length) {
    errorSelectedSpan.style.display="none";
    /*$('#signNameSelected').val("");*/
  }
  document.getElementById('submitButtonFileDailymotion').disabled=true;

  document.getElementById("InputFile").value="";

  document.getElementById("subtitle_for_modal_video").style.display="";
  if (fileName != null) {
    fileName.value="";
    document.getElementById('fileName').style.display = "";
  }
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
  var fileName = document.getElementById("fileName");

  $('input[type="file"]').change(function(e){
    $("#add_video_file_dailymotion").modal('show');
    document.getElementById('submitButtonFileDailymotion').disabled=false;

    if (fileName != null) {
      fileName.textContent = e.target.files[0].name;
      document.getElementById('fileName').style.display = "";
    }
  });

});