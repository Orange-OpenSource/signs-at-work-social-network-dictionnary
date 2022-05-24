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
console.log("Cool, user.js is loaded :)");

var usersContainer = document.getElementById("users-container");
if (usersContainer != null) {
  var users = usersContainer.getElementsByClassName("user_name");
  var usersCount =  users.length;
}
var search_user = document.getElementById("search_user");
var button_reset = document.getElementById("reset_search_user");

var accentMap = {
  "é": "e",
  "è": "e",
  "ê": "e",
  "à": "a",
  "â": "a",
  "î": "i",
  "ô": "o",
  "ù": "u",
  "û": "u",
  "î": "i",
  "ç": "c",
  "œ" : "oe",
  "æ" : "ae"
};

var normalize = function( term ) {
  var ret = "";
  for ( var i = 0; i < term.length; i++ ) {
    ret += accentMap[ term.charAt(i) ] || term.charAt(i);
  }
  return ret;
};


function search(event) {
  var display = 0;
  var g = normalize($(this).val());

  if (g!="") {
    $("#users-container").children("div").each(function () {
      $("#reset_search_user").css("visibility", "visible");
      var userName = $(this).attr("id");
      if (userName != null) {
        var s = normalize(userName);
        if (s.toUpperCase().indexOf(g.toUpperCase()) != -1) {
          $(this).show();
          display++;
        }
        else {
          $(this).hide();
        }
      }
    });
    nb.innerHTML = display;
    $(nb).show();
  } else {
    $("#reset_search_user").css("visibility", "hidden");
    $("#users-container").children("div").each(function () {
      $(this).show();
    });
    nb.innerHTML = usersCount;
    $(nb).show();
  }
}


function onReset(event) {

  $(':input', '#myform')
    .not(':button, :submit, :reset, :hidden')
    .val('');
  $("#reset_search_user").css("visibility", "hidden");

  $("#users-container").children("div").each(function () {
    $(this).show();
  });
  nb.innerHTML = usersCount;
  $(nb).show();

}

function onDeleteUser(id){
  var errorDeleteUser = document.getElementById('errorDeleteUser'+id);

  $.ajax({
    url: "/ws/admin/users/"+ id,
    type: 'delete',
    success: function(response) {
     location.reload();
    },
    error: function(response) {
      console.log(response.responseJSON);
      errorDeleteUser.textContent = response.responseJSON.errorMessage;
      errorDeleteUser.style.visibility = "visible";
    }
  })
}

function onDisableUser(id){
  var errorDisableUser = document.getElementById('errorDisableUser'+id);

  $.ajax({
   url: "/ws/admin/users/"+ id + '?enable=false',
   type: 'put',
    success: function(response) {
     location.reload();
    },
    error: function(response) {
      console.log(response.responseJSON);
      errorDisableUser.textContent = response.responseJSON.errorMessage;
      errorDisableUser.style.visibility = "visible";
    }
  })
}

function onEnableUser(id){
  var errorEnableUser = document.getElementById('errorEnableUser'+id);

  $.ajax({
    url: "/ws/admin/users/"+ id + '?enable=true',
    type: 'put',
    success: function(response) {
     location.reload();
    },
    error: function(response) {
      console.log(response.responseJSON);
      errorEnableUser.textContent = response.responseJSON.errorMessage;
      errorEnableUser.style.visibility = "visible";
    }
  })
}

function main() {
 /* if ($(search_user).hasClass("search-hidden")) {*/
    $("#users-container").children("div").each(function () {
      $(this).show();
    });
/*  } else {
    $("#users_container").children("div").each(function () {
      $(this).hide();
      $(nb).hide();
    });
  }*/
  search_user.addEventListener('keyup', search);
  button_reset.addEventListener('click', onReset);
}

(function displayUser($) {
  main();

})($);
