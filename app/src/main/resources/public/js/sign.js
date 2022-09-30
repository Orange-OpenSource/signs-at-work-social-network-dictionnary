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

var $formUploadSelectedVideoFile = $('#uploadSelectedVideoFile');
$formUploadSelectedVideoFile.on('submit', function(event) {
    $(".spinner").removeClass("spinner_hidden").addClass("spinner_show");
    $(".spinner").css("z-index", "1500").visibility = "visible";
    $(".spinner").css("opacity", "1");
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


function onClick() {
  event.preventDefault();
  document.getElementById('InputFile').click()
}

function onAddVideoToFavoritesForm(videoId) {
  console.log("onAddVideoToFavoritesForm ", videoId);
  if ($("#AddVideoToFavoritesForm").isChanged()) {
    var videoFavoritesIds = [];
    i = 1;
    $("#favorites-container").children("li").children("label").each(function () {
      if (!this.classList.contains("disabled")) {
        if (document.getElementById("videoFavoritesIds" + i).checked) {
          var selectedFavoriteId = document.getElementById("videoFavoritesIds" + i).value;
          videoFavoritesIds.push(selectedFavoriteId);
        }
      }
        i = i + 1;
    });

    $.ajax({
      url: "/ws/sec/video/" + videoId + "/add/favorites",
      type: 'post',
      data: JSON.stringify(videoFavoritesIds),
      contentType: "application/json",
      success: function (response) {
        console.log(response);
        $('#add_sign_to_favorite').modal('hide');
        location.reload();
      },
      error: function (response) {
      }
    })
  } else {
    $('#add_sign_to_favorite').modal('hide');
  }
};


$.fn.extend({
  trackChanges: function() {
    $(":input",this).change(function() {
      $(this.form).data("changed", true);
    });
  }
  ,
  isChanged: function() {
    return this.data("changed");
  }
});


$(document).ready(function(){
  $('input[type="file"]').change(function(e){
    $("#add_video_file_dailymotion").modal('show');
    document.getElementById('submitButtonFileDailymotion').disabled=false;
  });
  $("#AddVideoToFavoritesForm").trackChanges();
});
