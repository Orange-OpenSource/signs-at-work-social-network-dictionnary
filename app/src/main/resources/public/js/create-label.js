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
var submitCreateLabel = document.getElementById("submit-create-label");

var regexName = new RegExp('^[0-9A-zÀ-ÖØ-öø-ÿ- ()œæŒÆ\'°/:]{1,255}$');

inputLabelName.addEventListener('keyup',checkLabelName);
submitCreateLabel.disabled = true;


function checkLabelName() {
  var valueLabelName = inputLabelName.value;

  if (valueLabelName != '') {
    if (!regexName.test(valueLabelName)) {
      $('.errorRegexLabelName').removeClass("hidden");
      submitCreateLabel.disabled = true;
    } else {
      $('.errorRegexLabelName').addClass("hidden");
      submitCreateLabel.disabled = false;
    }
  } else {
    $('.errorRegexLabelName').addClass("hidden");
    submitCreateLabel.disabled = true;
  }
}

$('#create_label').on('hidden.bs.modal', function (e) {
  $('.errorRegexCommunityName').addClass("hidden");
  inputLabelName.value = '';
  submitCreateLabel.disabled = true;
})

function onCreateLabel(name) {
    label = {
      name: name,
    };
    $.ajax({
      url: "/ws/admin/labels",
      type: 'post',
      data: JSON.stringify(label),
      contentType: "application/json",
      success: function (response) {
        console.log(response);
        $("#validate_create_label").modal('show');
        setTimeout(function () {
          $('#validate_create_label').modal('hide');
          url = "/sec/admin/manage_labels";
          window.history.replaceState({}, 'foo', url);
          window.location = url;
        }, 3000);
      },
      error: function (response) {
      }
    })
};

function onContinueLabel(backUrl) {
  var url = backUrl;
  window.location = url;
};