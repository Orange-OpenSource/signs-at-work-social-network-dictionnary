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

var favoriteName = document.getElementById("favoriteName");
var duplicateFavoriteName = document.getElementById("duplicateFavoriteName");
var oldFavoriteName;
var oldDuplicateFavoriteName;
var submitRenameModal = document.getElementById("submit-rename-modal");

function onDeleteFavorite(favoriteId) {

    $.ajax({
      url: "/ws/sec/favorites/" + favoriteId,
      type: 'delete',
      success: function (response) {
        console.log(response);
        $('#delete_favorite').modal('hide');
        $("#validate_delete_favorite").modal('show');
        setTimeout(function () {
          $('#validate_delete_favorite').modal('hide');
          window.history.back();
        }, 3000);
      },
      error: function (response) {
      }
    })


};

function onRenameFavorite(favoriteId) {

  if (favoriteName.value != oldFavoriteName) {
    favorite = {
      name: favoriteName.value
    };
    $.ajax({
      url: "/ws/sec/favorites/" + favoriteId,
      type: 'put',
      data: JSON.stringify(favorite),
      contentType: "application/json",
      success: function (response) {
        console.log(response);
        errorRename.style.visibility = "hidden";
        $('#rename_favorite').modal('hide');
        renamed_favorite.textContent = response.errorMessage;
        $("#validate_rename_favorite").modal('show');
        setTimeout(function () {
          $('#validate_rename_favorite').modal('hide');
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


function onDuplicateFavorite(favoriteId) {

  favorite = {
    name: duplicateFavoriteName.value
  };
  $.ajax({
    url: "/ws/sec/favorites/" + favoriteId + "/duplicate",
    type: 'post',
    data: JSON.stringify(favorite),
    contentType: "application/json",
    success: function (response) {
      console.log(response);
      errorDuplicate.style.visibility="hidden";
      $('#duplicate_favorite').modal('hide');
      duplicated_favorite.textContent = response.errorMessage;
      $("#validate_duplicate_favorite").modal('show');
      setTimeout(function () {
        $('#validate_duplicate_favorite').modal('hide');
        var url = "/sec/favorite/" + response.favoriteId;
        window.location = url;
      }, 3000);
    },
    error: function (response) {
      console.log(response.responseJSON);
      errorDuplicate.textContent = response.responseJSON.errorMessage;
      errorDuplicate.style.visibility="visible";
    }
  })


};

$('#rename_favorite').on('hidden.bs.modal', function (e) {
  var errorRename = document.getElementById('errorRename');
  errorRename.style.visibility = "hidden";
  favoriteName.value= oldFavoriteName;
  submitRenameModal.disabled = true;
})

$('#duplicate_favorite').on('hidden.bs.modal', function (e) {
  var errorDuplicate = document.getElementById('errorDuplicate');
  errorDuplicate.style.visibility = "hidden";
  duplicateFavoriteName.value = oldDuplicateFavoriteName;
})

function resetRenameError(event) {
  var errorRename = document.getElementById('errorRename');
  errorRename.style.visibility = "hidden";
  if (oldFavoriteName == favoriteName.name) {
    submitRenameModal.disabled = true;
  } else {
    submitRenameModal.disabled = false;
  }
}

function resetDuplicateError(event) {
  var errorDuplicate = document.getElementById('errorDuplicate');
  errorDuplicate.style.visibility = "hidden";
}

function main() {
  favoriteName.addEventListener('keyup', resetRenameError);
  duplicateFavoriteName.addEventListener('keyup', resetDuplicateError);
  oldFavoriteName = favoriteName.value;
  oldDuplicateFavoriteName = duplicateFavoriteName.value;
  submitRenameModal.disabled = true;
}

(function ResetErrorWhenchangeNameValue($) {

  main();

})($);