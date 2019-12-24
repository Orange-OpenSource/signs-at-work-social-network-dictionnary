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




function onContinueFavorite(backUrl) {
  var url = backUrl;
  window.location = url;
};


function onAssociateJobToUser() {
  if ($("#associateJobToUserForm").isChanged()) {
    var communityListName = document.getElementById('community_list_name');
    var userCommunitiesIds = [];
    i = 1;
    $("#communities-container").children("label").each(function () {
      if (document.getElementById("userCommunitiesIds" + i).checked) {
        var selectedCommunityId = document.getElementById("userCommunitiesIds" + i).value;
        userCommunitiesIds.push(selectedCommunityId);
      }
      i = i + 1;
    });

    console.log(userCommunitiesIds);
/*   $.ajax({
      url: "/ws/sec/favorite/" + favoriteId + "/add/communities",
      type: 'post',
      data: JSON.stringify(favoriteCommunitiesIds),
      contentType: "application/json",
      success: function (response) {
        console.log(response);
        communityListName.textContent = response;
        $("#validate_share_favorite_modif").modal('show');
        setTimeout(function () {
          $('#validate_share_favorite_modif').modal('hide');
      /!*    var url = "/sec/favorite/" + favoriteId;
          window.location = url;*!/
          /!*window.history.go(-2);*!/
          window.history.back();
        }, 3000);
      },
      error: function (response) {
      }
    })*/
  } else {
/*   var url = "/sec/favorite/" + favoriteId;
    window.location = url;*/
    window.history.back();
  }

};


function onCreateFavoriteCommunity(name, favoriteId) {
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
          if (favoriteId == 0) {
            url = "/sec/community/" + communityId;
          } else {
            url = "/sec/favorite/share/?id=" + favoriteId + "&communityId=" + communityId;
          }

          window.location = url;
        }, 3000);
      },
      error: function (response) {
      }
    })
  } else {
    /*var url = "/sec/favorite/share/?id=" + favoriteId + "&communityId=0";
    window.location = url;*/
    window.history.back();

  }

};


$.fn.extend({
  trackChanges: function() {
    $(":input",this).change(function() {
      $(this.form).data("changed", true);
      $("#button-top").css("visibility", "visible");
      $("#button-top").show();
    });
  }
  ,
  isChanged: function() {
    return this.data("changed");
  }
});



(function main($) {
  $("#associateJobToUserForm").trackChanges();
  $("#FavoriteCreateCommunityForm").trackChanges();
})($);

