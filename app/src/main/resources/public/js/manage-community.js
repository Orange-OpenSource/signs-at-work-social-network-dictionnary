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

var communityName = document.getElementById("communityName");
var oldCommunityName;
var submitRenameModal = document.getElementById("submit-rename-modal");

function onDeleteCommunity(communityId) {

    $.ajax({
      url: "/ws/sec/communities/" + communityId,
      type: 'delete',
      success: function (response) {
        console.log(response);
        $('#delete_community').modal('hide');
        $("#validate_delete_community").modal('show');
        setTimeout(function () {
          $('#validate_delete_community').modal('hide');
          var url = "/sec/communities";
          window.location = url;
        }, 3000);
      },
      error: function (response) {
      }
    })


};

function onRenameCommunity(communityId) {

  if (communityName.value != oldCommunityName) {
    community = {
      name: communityName.value
    };
    $.ajax({
      url: "/ws/sec/communities/" + communityId + "/datas",
      type: 'put',
      data: JSON.stringify(community),
      contentType: "application/json",
      success: function (response) {
        console.log(response);
        errorRename.style.visibility = "hidden";
        $('#rename_community').modal('hide');
        renamed_community.textContent = response.errorMessage;
        $("#validate_rename_community").modal('show');
        setTimeout(function () {
          $('#validate_rename_community').modal('hide');
          window.history.back();
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


function onRemoveMeFromCommunity(communityId, userId) {
    community = {
      userIdToRemove: userId
    };
    $.ajax({
      url: "/ws/sec/communities/" + communityId + "/datas",
      type: 'put',
      data: JSON.stringify(community),
      contentType: "application/json",
      success: function (response) {
        console.log(response);
        errorRename.style.visibility = "hidden";
        $('#remove_me_from_community').modal('hide');
        renamed_community.textContent = response.errorMessage;
        $("#validate_remove_me_from_community").modal('show');
        setTimeout(function () {
          $('#validate_remove_me_from_community').modal('hide');
          window.history.back();
        }, 3000);
      },
      error: function (response) {
        console.log(response.responseJSON);
        errorRename.textContent = response.responseJSON.errorMessage;
        errorRename.style.visibility = "visible";
      }
    })
};


$('#rename_community').on('hidden.bs.modal', function (e) {
  var errorRename = document.getElementById('errorRename');
  errorRename.style.visibility = "hidden";
  communityName.value= oldCommunityName;
  submitRenameModal.disabled = true;
})


function resetRenameError(event) {
  var errorRename = document.getElementById('errorRename');
  errorRename.style.visibility = "hidden";
  if (oldCommunityName == communityName.value) {
    submitRenameModal.disabled = true;
  } else {
    submitRenameModal.disabled = false;
  }
}


function main() {
  communityName.addEventListener('keyup', resetRenameError);
  oldCommunityName = communityName.value;
  submitRenameModal.disabled = true;
}

(function ResetErrorWhenchangeNameValue($) {

  main();

})($);