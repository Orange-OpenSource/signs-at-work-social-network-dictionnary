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


function onRenameLabel(labelId) {

  if (labelName.value != oldLabelName) {
    label = {
      name: labelName.value
    };
    $.ajax({
      url: "/ws/sec/labels/" + labelId + "/datas",
      type: 'put',
      data: JSON.stringify(label),
      contentType: "application/json",
      success: function (response) {
        console.log(response);
        errorRename.style.visibility = "hidden";
        $('#rename_label').modal('hide');
        renamed_label.textContent = response.errorMessage;
        $("#validate_rename_label").modal('show');
        setTimeout(function () {
          $('#validate_rename_label').modal('hide');
          location.reload();
        }, 3000);
      },
      error: function (response) {
        console.log(response.responseJSON);
        errorRename.textContent = response.responseJSON.errorMessage;
        errorRename.style.visibility = "visible";
      }
    })
  } else {
    submitRenameModal.disabled = true;
  }

};



$('#rename_label').on('hidden.bs.modal', function (e) {
  var errorRename = document.getElementById('errorRename');
  errorRename.style.visibility = "hidden";
  labelName.value= oldLabelName;
  submitRenameModal.disabled = true;
  $('.errorRegexCommunityName').addClass("hidden");
})


function resetRenameError(event) {
  var errorRename = document.getElementById('errorRename');
  errorRename.style.visibility = "hidden";
  if (oldLabelName == labelName.value) {
    submitRenameModal.disabled = true;
  } else {
    checkLabelName();
  }
}


function main() {
  labelName.addEventListener('keyup', resetRenameError);
  oldLabelName = labelName.value;
  submitRenameModal.disabled = true;

}

(function AfterLoad($) {

  main();

})($);