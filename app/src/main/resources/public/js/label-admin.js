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

console.log("Cool, label-admin.js is loaded :)");

var labelName = document.getElementById("labelName");
var oldLabelName;
var submitRenameModal = document.getElementById("submit-rename-modal");
var submitForceRenameModal = document.getElementById("submit-force-rename-modal");

var regexName = new RegExp('^[0-9A-zÀ-ÖØ-öø-ÿ- ()œæŒÆ\'°/:]{1,255}$');

function checkLabelName() {
  var valueLabelName = labelName.value;

  if (valueLabelName != '') {
    if (!regexName.test(valueLabelName)) {
      $('.errorRegexLabelName').removeClass("hidden");
      submitRenameModal.disabled = true;
    } else {
      $('.errorRegexLabelName').addClass("hidden");
      submitRenameModal.disabled = false;
    }
  } else {
    $('.errorRegexLabelName').addClass("hidden");
    submitRenameModal.disabled = true;
  }
}

function onDeleteLabel(labelId) {
    $.ajax({
      url: "/ws/admin/label/" + labelId,
      type: 'delete',
      success: function (response) {
        console.log(response);
        $('#delete_label').modal('hide');
        $("#validate_delete_label").modal('show');
        setTimeout(function () {
          $('#validate_delete_label').modal('hide');
          var url = "/sec/admin/manage_labels";
          window.location = url;
        }, 3000);
      },
      error: function (response) {
      }
    })


};


function onRenameLabel(labelId, force) {

  console.log("force "+force);
  if (labelName.value != oldLabelName) {
    label = {
      name: labelName.value
    };
    $.ajax({
      url: "/ws/sec/labels/" + labelId + "/rename/?force=" + force,
      type: 'put',
      data: JSON.stringify(label),
      contentType: "application/json",
      success: function (response) {
        console.log(response);
        errorRename.style.display = "none";
        location.reload();
      },
      error: function (response) {
        console.log(response.responseJSON);
        if (response.responseJSON.errorMessage != null) {
          errorRename.textContent = response.responseJSON.errorMessage;
          errorRename.style.display = "block";
          submitRenameModal.disabled = true;
        } else {
          if (response.responseJSON.warningMessage != null) {
            warningRename.textContent = response.responseJSON.warningMessage;
            warningRename.style.display = "block";
            submitRenameModal.style.display="none";
            submitForceRenameModal.style.display = "block";
          }
        }

      }
    })
  } else {
    submitRenameModal.disabled = true;
  }


};



$('#rename_label').on('hidden.bs.modal', function (e) {
  var errorRename = document.getElementById('errorRename');
  var warningRename = document.getElementById('warningRename');
  errorRename.style.display = "none";
  warningRename.style.display = "none";
  labelName.value= oldLabelName;
  submitRenameModal.style.display="block";
  submitRenameModal.disabled = true;
  $('.errorRegexLabelName').addClass("hidden");
})


function resetRenameError(event) {
  var errorRename = document.getElementById('errorRename');
  var warningRename = document.getElementById('warningRename');
  errorRename.style.display = "none";
  warningRename.style.display = "none";
  if (oldLabelName == labelName.value) {
    submitRenameModal.disabled = true;
    $('.errorRegexLabelName').addClass("hidden");
  } else {
    checkLabelName();
  }
}


var errorSelectedFileIconSpan = document.getElementById('errorSelectedFileIconSpan');

var $formUploadSelectedPictureFile = $('#uploadSelectedPictureFile');
$formUploadSelectedPictureFile.on('submit', function(event) {
  $(".spinner").removeClass("spinner_hidden").addClass("spinner_show");
  $(".spinner").css("z-index", "1500").visibility = "visible";
  $(".spinner").css("opacity", "1");
  /*    $("#submitButtonFileDailymotion").css("color", "black");*/
  var $form = $(this);
  var formdata = new FormData($form[0]);
  var data = (formdata !== null) ? formdata : $form.serialize();
  document.getElementById('submitButtonPictureFile').disabled=true;
  event.preventDefault();
  $.ajax({
    url: $formUploadSelectedPictureFile.attr('action'),
    type: 'post',
    data: data,
    contentType: false,
    processData: false,
    success: function (response) {
      errorSelectedFileIconSpan.style.display = "none";
      $(".spinner").visibility = "hidden";
      location.reload();
    },
    error: function (response) {
      errorSelectedFileIconSpan.textContent = response.responseText;
      errorSelectedFileIconSpan.style.display = "inline-block";
      $(".spinner").css("z-index", "-1").css("opacity", "0.1");
      $(".spinner").visibility = "hidden";
      console.log("Erreur " + response.responseText);
    }
  })

});

$formUploadSelectedPictureFile.on('input', function(event) {
  document.getElementById('errorSelectedFileIconSpan').style.display="none";
});

var $add_file_icon_label = $('#add_file_icon_label');
$add_file_icon_label.on('hidden.bs.modal', function() {
  console.log("hidden add_file_icon_label modal");

  if ($('#uploadSelectedPictureFile').find('#errorSelectedFileIconSpan').length) {
    errorSelectedFileIconSpan.style.display="none";
    /*$('#signNameSelected').val("");*/
  }
  document.getElementById('submitButtonPictureFile').disabled=true;

  document.getElementById("InputFile").value="";

  document.getElementById("subtitle_for_modal_video").style.display="";

});


function main() {
  labelName.addEventListener('keyup', resetRenameError);
  oldLabelName = labelName.value;
  submitRenameModal.disabled = true;
  $('input[type="file"]').change(function(e){
    document.getElementById('submitButtonPictureFile').disabled=false;
  });

}

(function AfterLoad($) {

  main();

})($);