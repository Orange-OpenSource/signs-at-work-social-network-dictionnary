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
console.log("Cool, create_sign.js is loaded :)");


var inputSignName = document.getElementById('signName');
var submitCreateSign = document.getElementById("submit-create-sign");

var regexName = new RegExp('^[0-9A-zÀ-ÖØ-öø-ÿ- ()\'°/:]{1,255}$');

inputSignName.addEventListener('keyup',checkSignName);
submitCreateSign.disabled = true;


function checkSignName() {
  var valueSignName = inputSignName.value;

  if (valueSignName != '') {
    if (!regexName.test(valueSignName)) {
      $('.errorRegexSignName').removeClass("hidden");
      submitCreateSign.disabled = true;
    } else {
      $('.errorRegexSignName').addClass("hidden");
      submitCreateSign.disabled = false;
    }
  } else {
    $('.errorRegexSignName').addClass("hidden");
    submitCreateSign.disabled = true;
  }
}

$('#new-suggest').on('hidden.bs.modal', function (e) {
  $('.errorRegexSignName').addClass("hidden");
  inputSignName.value = '';
  submitCreateSign.disabled = true;
})
