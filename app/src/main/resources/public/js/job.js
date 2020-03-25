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
    var communityName;
    var communityId;
    i = 1;
    $("#communities-container").children("label").each(function () {
      if (document.getElementById("userCommunitiesIds" + i).checked) {
        var selectedCommunityId = document.getElementById("userCommunitiesIds" + i).value;
        userCommunitiesIds.push(selectedCommunityId);
        communityName = $(this).attr("id");
        communityId = selectedCommunityId;
        console.log(communityName);
      }
      i = i + 1;
    });

    console.log(userCommunitiesIds);
    var data = {
      "job": communityName
    };
   /* event.preventDefault();*/
    $.ajax({
      url: "/ws/sec/users/me/datas",
      type: 'put',
      data: JSON.stringify(data),
      contentType: "application/json",
      success: function(response) {
        console.log("Success " + response);
        window.history.back();
      },
      error: function(response) {
        console.log("Erreur " + response.responseText);
      }
    })
  } else {
    window.history.back();
  }

};




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



(function main($) {
  $("#associateJobToUserForm").trackChanges();
})($);

