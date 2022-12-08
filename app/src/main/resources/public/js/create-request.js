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
console.log("Cool, create_request.js is loaded :)");


var inputRequestName = document.getElementById('requestName');
var submitCreateRequest = document.getElementById("submit-create-request");

var regexName = new RegExp('^[0-9A-zÀ-ÖØ-öø-ÿ- ()\'°/:]{1,255}$');

inputRequestName.addEventListener('keyup',checkRequestName);
submitCreateRequest.disabled = true;


function checkRequestName() {
  var valueRequestName = inputRequestName.value;

  if (valueRequestName != '') {
    if (!regexName.test(valueRequestName)) {
      $('.errorRegexRequestName').removeClass("hidden");
      submitCreateRequest.disabled = true;
    } else {
      $('.errorRegexRequestName').addClass("hidden");
      submitCreateRequest.disabled = false;
    }
  } else {
    $('.errorRegexRequestName').addClass("hidden");
    submitCreateRequest.disabled = true;
  }
}

$('#new-suggest').on('hidden.bs.modal', function (e) {
  $('.errorRegexRequestName').addClass("hidden");
  inputRequestName.value = '';
  submitCreateRequest.disabled = true;
})
