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
console.log("Cool, create_favorite.js is loaded :)");


var inputFavoriteName = document.getElementById('favoriteName');
var submitCreateFavorite = document.getElementById("submit-create-favorite");

var regexName = new RegExp('^[0-9A-zÀ-ÖØ-öø-ÿ- ()\'°/:]{1,255}$');

inputFavoriteName.addEventListener('keyup',checkFavoriteName);
submitCreateFavorite.disabled = true;


function checkFavoriteName() {
  var valueFavoriteName = inputFavoriteName.value;

  if (valueFavoriteName != '') {
    if (!regexName.test(valueFavoriteName)) {
      $('.errorRegexFavoriteName').removeClass("hidden");
      submitCreateFavorite.disabled = true;
    } else {
      $('.errorRegexFavoriteName').addClass("hidden");
      submitCreateFavorite.disabled = false;
    }
  } else {
    $('.errorRegexFavoriteName').addClass("hidden");
    submitCreateFavorite.disabled = true;
  }
}

$('#create-new-favorite').on('hidden.bs.modal', function (e) {
  $('.errorRegexFavoriteName').addClass("hidden");
  inputFavoriteName.value = '';
  submitCreateFavorite.disabled = true;
})
