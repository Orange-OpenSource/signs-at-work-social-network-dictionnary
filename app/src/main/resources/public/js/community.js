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


var search_user = document.getElementById("search_user");
var button_reset = document.getElementById("reset_search_user");

var accentMap = {
  "à": "a", "â": "a", "á": "a", "ä": "a", "ã": "a", "å": "a", "ā": "a", "æ" : "ae",
  "À": "A", "Á": "A", "Â": "A", "Ã": "A", "Ä": "A", "Å" : "A", "À": "A", "Æ" : "AE",
  "ç": "c", "ć": "c", "č": "c",
  "Ç": "c",
  "é": "e", "è": "e", "ê": "e", "ë": "e", "ę": "e", "ė": "e", "ē": "e",
  "È": "E", "É": "E", "Ê": "E", "Ë": "E",
  "î": "i", "ï": "i", "ì": "i", "í": "i", "į": "i", "ī": "i",
  "Ì": "I", "Í": "I", "Î": "I", "Ï": "I",
  "ñ": "n", "ń": "n",
  "Ñ" : "N",
  "ô": "o", "ö": "o", "ò": "o", "ó": "o", "õ": "o", "ø": "o", "ō": "o", "œ" : "oe",
  "Ò": "O", "Ó": "O", "Ô": "O", "Õ" : "O", "Õ": "O", "Ö": "O","Œ" : "OE",
  "û": "u", "ù": "u", "ü": "u", "ú": "u", "ū": "u",
  "Ù": "U", "Ú": "U", "Û": "U", "Ü": "U",
  "ÿ": "y",
  "Ÿ" : "Y"
};

var normalize = function( term ) {
  var ret = "";
  for ( var i = 0; i < term.length; i++ ) {
    ret += accentMap[ term.charAt(i) ] || term.charAt(i);
  }
  return ret;
};



function onContinueCommunity(backUrl) {
  var url = backUrl;
  window.location = url;
};

function onCreateCommunity(name, isAdmin) {
  if ($("#FavoriteCreateCommunityForm").isChanged()) {
    var url;
    var communityId;
    var userListName = document.getElementById('user_list_name');
    var communityUsersIds = [];
    i = 1;
    $("#users-container").children("label").each(function () {
      if (document.getElementById("communityUsersIds" + i).checked) {
        var selectedUserId = document.getElementById("communityUsersIds" + i).value;
        communityUsersIds.push(selectedUserId);
      }
      i = i + 1;
    });

    community = {
      name: name,
      communityUsersIds: communityUsersIds
    };
    $.ajax({
      url: "/ws/sec/communities",
      type: 'post',
      data: JSON.stringify(community),
      contentType: "application/json",
      success: function (response) {
        console.log(response);
        communityId = response.communityId;
        userListName.textContent = response.errorMessage;
        $("#validate_create_community_favorite").modal('show');
        setTimeout(function () {
          $('#validate_create_community_favorite').modal('hide');
          if (isAdmin === "true") {
            url = "/sec/admin/community/" + communityId;
          } else {
            url = "/sec/community/" + communityId;
          }
          console.log(window.location.href);
          window.history.replaceState({}, 'foo', url);
          console.log(window.location.href);
          window.location = url;
        }, 3000);
      },
      error: function (response) {
      }
    })
  } else {
    window.history.back();
  }

};

function onModifyCommunity(communityId) {
  if ($("#ModifyCommunityForm").isChanged()) {
    var url;
    var communityId;
    var userListName = document.getElementById('user_list_name');
    var communityUsersIds = [];
    i = 1;
    $("#users-container").children("label").each(function () {
      if (document.getElementById("communityUsersIds" + i).checked) {
        var selectedUserId = document.getElementById("communityUsersIds" + i).value;
        communityUsersIds.push(selectedUserId);
      }
      i = i + 1;
    });

    community = {
      communityUsersIds: communityUsersIds
    };
    $.ajax({
      url: "/ws/sec/communities/" + communityId + "/datas",
      type: 'put',
      data: JSON.stringify(community),
      contentType: "application/json",
      success: function (response) {
        console.log(response);
        communityId = response.communityId;
        userListName.textContent = response.errorMessage;
        $("#validate_modify_community").modal('show');
        setTimeout(function () {
          $('#validate_modify_community').modal('hide');
          window.history.back();
        }, 3000);
      },
      error: function (response) {
      }
    })
  } else {
    window.history.back();

  }

};


function search(event) {
  var display = 0;
  var g = normalize($(this).val());

  if (g!="") {
    $("#users-container").children("label").each(function () {
      $("#reset_search_user").css("visibility", "visible");
      var userName = $(this).attr("id");
      if (userName != null) {
        var s = normalize(userName);
        if (s.toUpperCase().indexOf(g.toUpperCase()) != -1) {
          $(this).show();
        }
        else {
          $(this).hide();
        }
      }
    });
  } else {
    $("#reset_search_user").css("visibility", "hidden");
    $("#users-container").children("label").each(function () {
      $(this).show();
    });
  }
}

$.fn.extend({
  trackChanges: function() {
    $(":input",this).change(function() {
      $(this.form).data("changed", true);
/*      $("#button-top").css("visibility", "visible");
      $("#button-top").show();*/
    });
  }
  ,
  isChanged: function() {
    return this.data("changed");
  }
});

function onReset(event) {

  $(':input', '#myform')
    .not(':button, :submit, :reset, :hidden')
    .val('');
  $("#reset_search_user").css("visibility", "hidden");

  $("#users-container").children("label").each(function () {
    $(this).show();
  });

}

//Get the button
let mybutton = document.getElementById("btn-back-to-top");

// When the user scrolls down 20px from the top of the document, show the button
window.onscroll = function () {
  scrollFunction();
};

function scrollFunction() {
  if (
    document.body.scrollTop > 20 ||
    document.documentElement.scrollTop > 20
  ) {
    mybutton.style.display = "block";
  } else {
    mybutton.style.display = "none";
  }
}
// When the user clicks on the button, scroll to the top of the document
mybutton.addEventListener("click", backToTop);

function backToTop() {
  document.body.scrollTop = 0;
  document.documentElement.scrollTop = 0;
}

(function main($) {
  $("#shareFavoriteForm").trackChanges();
  $("#ModifyCommunityForm").trackChanges();
  $("#FavoriteCreateCommunityForm").trackChanges();
  search_user.addEventListener('keyup', search);
  button_reset.addEventListener('click', onReset);
})($);

