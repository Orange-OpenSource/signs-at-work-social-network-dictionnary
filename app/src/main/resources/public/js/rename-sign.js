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

var signName = document.getElementById("signName");
var oldsignName;
var submitRenameModal = document.getElementById("submit-rename-modal");
var submitForceRenameModal = document.getElementById("submit-force-rename-modal");

var regexName = new RegExp('^[0-9A-zÀ-ÖØ-öø-ÿ- ()œæŒÆ\'°/:]{1,255}$');

function checkSignName() {
  var valueSignName = signName.value;

  if (valueSignName != '') {
    if (!regexName.test(valueSignName)) {
      $('.errorRegexSignName').removeClass("hidden");
     submitRenameModal.disabled = true;
    } else {
      $('.errorRegexSignName').addClass("hidden");
     submitRenameModal.disabled = false;
    }
  } else {
    $('.errorRegexSignName').addClass("hidden");
    submitRenameModal.disabled = true;
  }
}

function onRenameSign(signId, videoId, force) {

  console.log("force "+force);
  if (signName.value != oldsignName) {
    sign = {
      name: signName.value
    };
    $.ajax({
      url: "/ws/sec/signs/" + signId + "/videos/" + videoId + "/rename/?force=" + force,
      type: 'put',
      data: JSON.stringify(sign),
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



$('#rename-sign').on('hidden.bs.modal', function (e) {
  var errorRename = document.getElementById('errorRename');
  var warningRename = document.getElementById('warningRename');
  errorRename.style.display = "none";
  warningRename.style.display = "none";
  signName.value= oldsignName;
  submitRenameModal.style.display="block";
  submitForceRenameModal.style.display = "none";
  submitRenameModal.disabled = true;
  $('.errorRegexSignName').addClass("hidden");
})


function resetRenameError(event) {
  var errorRename = document.getElementById('errorRename');
  var warningRename = document.getElementById('warningRename');
  errorRename.style.display = "none";
  warningRename.style.display = "none";
  submitRenameModal.style.display="block";
  submitForceRenameModal.style.display = "none";
  if (oldsignName == signName.value) {
    submitRenameModal.disabled = true;
    $('.errorRegexSignName').addClass("hidden")
  } else {
    checkSignName();
  }
}


function main() {
  signName.addEventListener('keyup', resetRenameError);
  oldsignName = signName.value;
  submitRenameModal.disabled = true;
}

(function AfterLoad($) {

  main();

})($);