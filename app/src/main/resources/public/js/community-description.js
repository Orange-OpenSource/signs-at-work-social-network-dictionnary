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

console.log("Cool, community-description.js is loaded :)");

var communityDescriptionText = document.getElementById("community-description-text");
var oldCommunityDescriptionText;
var submitAddCommunityDescriptionText = document.getElementById('submit-add-community-description-text');


function eraseText() {
  var eraseText = document.getElementById('erase_text');
  communityDescriptionText.value="";
  eraseText.style.visibility = "hidden";
  if (oldCommunityDescriptionText != communityDescriptionText.value) {
    submitAddCommunityDescriptionText.disabled = false;
  } else {
    submitAddCommunityDescriptionText.disabled = true;
  }
};


function checkCommunityDescriptionText(event) {
  var eraseText = document.getElementById('erase_text');
  if (oldCommunityDescriptionText != communityDescriptionText.value) {
    submitAddCommunityDescriptionText.disabled = false;
  } else {
    submitAddCommunityDescriptionText.disabled = true;
  }
  if (communityDescriptionText.value != "") {
    eraseText.style.visibility = "visible";
  } else {
    eraseText.style.visibility = "hidden";
  }
}

$('#add_community_description_text').on('hidden.bs.modal', function (e) {
  var eraseText = document.getElementById('erase_text');
  communityDescriptionText.value= oldCommunityDescriptionText;
  submitAddCommunityDescriptionText.disabled = true;
  if (communityDescriptionText.value != "") {
    eraseText.style.visibility = "visible";
  } else {
    eraseText.style.visibility = "hidden";
  }
})

function main() {
  communityDescriptionText.addEventListener('keyup', checkCommunityDescriptionText);
  oldCommunityDescriptionText = communityDescriptionText.value;
  submitAddCommunityDescriptionText.disabled = true;

}

(function AfterLoad($) {

  main();

})($);