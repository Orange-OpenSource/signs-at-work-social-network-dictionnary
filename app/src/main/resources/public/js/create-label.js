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
console.log("Cool, create_label.js is loaded :)");


var inputLabelName = document.getElementById('labelName');
var submitCreateModal = document.getElementById("submit-create-modal");
var submitForceCreateModal = document.getElementById("submit-force-create-modal");

var regexName = new RegExp('^[0-9A-zÀ-ÖØ-öø-ÿ- ()œæŒÆ\'°/:]{1,255}$');

inputLabelName.addEventListener('keyup',checkLabelName);
submitCreateModal.disabled = true;


function checkLabelName() {
  var valueLabelName = inputLabelName.value;

  errorCreate.style.display = "none";
  warningCreate.style.display = "none";
  if (valueLabelName != '') {
    if (!regexName.test(valueLabelName)) {
      $('.errorRegexLabelName').removeClass("hidden");
      submitCreateModal.disabled = true;
    } else {
      $('.errorRegexLabelName').addClass("hidden");
      submitCreateModal.disabled = false;
    }
  } else {
    $('.errorRegexLabelName').addClass("hidden");
    submitCreateModal.disabled = true;
  }
}

$('#create_label').on('hidden.bs.modal', function (e) {
  var errorCreate = document.getElementById('errorCreate');
  var warningCreate = document.getElementById('warningCreate');
  labelName.value = '';
  errorCreate.style.display = "none";
  warningCreate.style.display = "none";
  submitCreateModal.style.display="block";
  submitCreateModal.disabled = true;
  submitForceCreateModal.style.display = "none";
  $('.errorRegexLabelName').addClass("hidden");
})

$('#create-new-label_add_sign').on('hidden.bs.modal', function (e) {
  var errorCreate = document.getElementById('errorCreate');
  var warningCreate = document.getElementById('warningCreate');
  labelName.value = '';
  errorCreate.style.display = "none";
  warningCreate.style.display = "none";
  submitCreateModal.style.display="block";
  submitCreateModal.disabled = true;
  submitForceCreateModal.style.display = "none";
  $('.errorRegexLabelName').addClass("hidden");
})

function onCreateLabel(force) {

  console.log("force "+force);

    label = {
      name: labelName.value
    };
    $.ajax({
      url: "/ws/sec/labels/?force=" + force,
      type: 'post',
      data: JSON.stringify(label),
      contentType: "application/json",
      success: function (response) {
        console.log(response);
        errorCreate.style.display = "none";
        location.reload();
      },
      error: function (response) {
        console.log(response.responseJSON);
        if (response.responseJSON.errorMessage != null) {
          errorCreate.textContent = response.responseJSON.errorMessage;
          errorCreate.style.display = "block";
          submitCreateModal.disabled = true;
        } else {
          if (response.responseJSON.warningMessage != null) {
            warningCreate.textContent = response.responseJSON.warningMessage;
            warningCreate.style.display = "block";
            submitCreateModal.style.display="none";
            submitForceCreateModal.style.display = "block";
          }
        }

      }
    })

};


function onCreateLabelAddToSign(signId, force) {

  console.log("force "+force);

  label = {
    name: labelName.value
  };
  $.ajax({
    url: "/ws/sec/label/create_label_add_sign/" + signId + "?force=" + force,
    type: 'post',
    data: JSON.stringify(label),
    contentType: "application/json",
    success: function (response) {
      console.log(response);
      errorCreate.style.display = "none";
      location.reload();
    },
    error: function (response) {
      console.log(response.responseJSON);
      if (response.responseJSON.errorMessage != null) {
        errorCreate.textContent = response.responseJSON.errorMessage;
        errorCreate.style.display = "block";
        submitCreateModal.disabled = true;
      } else {
        if (response.responseJSON.warningMessage != null) {
          warningCreate.textContent = response.responseJSON.warningMessage;
          warningCreate.style.display = "block";
          submitCreateModal.style.display="none";
          submitForceCreateModal.style.display = "block";
        }
      }

    }
  })

};

function onContinueLabel(backUrl) {
  var url = backUrl;
  window.location = url;
};