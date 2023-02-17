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
console.log("Cool, create_community.js is loaded :)");


var inputCommunityName = document.getElementById('communityName');
var submitCreateCommunity = document.getElementById("submit-create-community");

var regexName = new RegExp('^[0-9A-zÀ-ÖØ-öø-ÿ- ()œæŒÆ\'°/:]{1,255}$');

inputCommunityName.addEventListener('keyup',checkCommunityName);
submitCreateCommunity.disabled = true;


function checkCommunityName() {
  var valueCommunityName = inputCommunityName.value;

  if (valueCommunityName != '') {
    if (!regexName.test(valueCommunityName)) {
      $('.errorRegexCommunityName').removeClass("hidden");
      submitCreateCommunity.disabled = true;
    } else {
      $('.errorRegexCommunityName').addClass("hidden");
      submitCreateCommunity.disabled = false;
    }
  } else {
    $('.errorRegexCommunityName').addClass("hidden");
    submitCreateCommunity.disabled = true;
  }
}

$('#create_community').on('hidden.bs.modal', function (e) {
  $('.errorRegexCommunityName').addClass("hidden");
  inputCommunityName.value = '';
  submitCreateCommunity.disabled = true;
})

function onCreateCommunity(name) {
    community = {
      name: name,
    };
    $.ajax({
      url: "/ws/admin/communities",
      type: 'post',
      data: JSON.stringify(community),
      contentType: "application/json",
      success: function (response) {
        console.log(response);
        $("#validate_create_community_favorite").modal('show');
        setTimeout(function () {
          $('#validate_create_community_favorite').modal('hide');
          url = "/sec/admin/manage_communities";
          window.history.replaceState({}, 'foo', url);
          window.location = url;
        }, 3000);
      },
      error: function (response) {
      }
    })
};

function onContinueCommunity(backUrl) {
  var url = backUrl;
  window.location = url;
};