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
  "é": "e",
  "è": "e",
  "ê": "e",
  "à": "a",
  "â": "a",
  "î": "i",
  "ô": "o",
  "ù": "u",
  "î": "i",
  "ç": "c"
};

var normalize = function( term ) {
  var ret = "";
  for ( var i = 0; i < term.length; i++ ) {
    ret += accentMap[ term.charAt(i) ] || term.charAt(i);
  }
  return ret;
};

/*function onSearch(){
  $("#search-criteria").show();
  var search_criteria = document.getElementById("search-criteria");
  search_criteria.classList.remove("search-hidden");
  $("#videos-container").children("label").each(function () {
    $(this).hide();
    $("#button-top").css("visibility", "hidden");
    $("#button-top").hide();
    $("#button-bottom").css("visibility", "hidden");
    $("#button-bottom").hide();
  });
}

function onBack(signId, videoId){
  if ($("#associateForm").isChanged()) {
    $("#validate_modif").modal('show');
  } else {
    var url = "/sign/"+signId+"/"+videoId;
    window.location = url;
  }


}*/

function onBackFavorite(backUrl){
  if ($("#associateForm").isChanged()) {
    $("#validate_favorite_modif").modal('show');
  } else {
    var url = backUrl;
    window.location = url;
  }


}


/*function onContinue(signId, videoId) {
  var url = "/sign/"+signId+"/"+videoId;
  window.location = url;
};*/

function onContinueFavorite(backUrl) {
  var url = backUrl;
  window.location = url;
};

/*
function displayVideo(url, name, idForName, nbVideo) {

  console.log(url);
  console.log(name);
  console.log(idForName);
  console.log(nbVideo);
  if (nbVideo == 1) {
    document.getElementById("videoName").innerText = name;
  } else {
    document.getElementById("videoName").innerText = name + ' (' + idForName + ')';
  }

  document.getElementById("videoUrl").src = url+'?endscreen-enable=false&autoplay=1&sharing-enable=false&wmode=transparent';
};

function onAssociateRequest(signId, videoId) {
  var associateVideosIds = [];
  i=1;
  $("#videos-container").children("label").each(function () {
    if (document.getElementById("associateVideosIds"+i).checked) {
      var selectedVideoId = document.getElementById("associateVideosIds"+i).value;
      associateVideosIds.push(selectedVideoId);
    }
    i= i+1;
  });

  $.ajax({
    url: "/ws/sec/sign/" + signId + "/" + videoId + "/associate",
    type: 'post',
    data: JSON.stringify(associateVideosIds),
    contentType: "application/json",
    success: function(response) {
      var url = "/sign/"+signId+"/"+videoId;
      window.location = url;
    },
    error: function(response) {
    }
  })

};

function onAssociateFavoriteRequest(favoriteId) {
  var favoriteVideosIds = [];
  i=1;
  $("#videos-container").children("label").each(function () {
    if (document.getElementById("favoriteVideosIds"+i).checked) {
      var selectedVideoId = document.getElementById("favoriteVideosIds"+i).value;
      favoriteVideosIds.push(selectedVideoId);
    }
    i= i+1;
  });

  $.ajax({
    url: "/ws/sec/favorite/" + favoriteId + "/add/videos",
    type: 'post',
    data: JSON.stringify(favoriteVideosIds),
    contentType: "application/json",
    success: function(response) {
      $("#validate_favorite_modif").modal('show');
      setTimeout(function(){
        $('#validate_favorite_modif').modal('hide');
        var url = "/sec/favorite/"+favoriteId;
        window.location = url;
      }, 3000);

    },
    error: function(response) {
    }
  })

};
*/



function onAssociateFavoriteCommunities(favoriteId, communityId) {
  if ($("#shareFavoriteForm").isChanged() || (communityId != 0)) {
    var communityListName = document.getElementById('community_list_name');
    var favoriteCommunitiesIds = [];
    i = 1;
    $("#communities-container").children("label").each(function () {
      if (document.getElementById("favoriteCommunitiesIds" + i).checked) {
        var selectedCommunityId = document.getElementById("favoriteCommunitiesIds" + i).value;
        favoriteCommunitiesIds.push(selectedCommunityId);
      }
      i = i + 1;
    });

    $.ajax({
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
          var url = "/sec/favorite/" + favoriteId;
          window.location = url;
        }, 3000);

      },
      error: function (response) {
      }
    })
  } else {
    var url = "/sec/favorite/" + favoriteId;
    window.location = url;
  }

};


function onCreateFavoriteCommunity(name, favoriteId) {
  if ($("#FavoriteCreateCommunityForm").isChanged()) {
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
      url: "/ws/sec/community/create",
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
          var url = "/sec/favorite/share/?id=" + favoriteId + "&communityId=" + communityId;
          window.location = url;
        }, 3000);
      },
      error: function (response) {
      }
    })
  } else {
    var url = "/sec/favorite/share/?id=" + favoriteId + "&communityId=0";
    window.location = url;
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
      $("#button-top").css("visibility", "visible");
      $("#button-top").show();
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

(function main($) {
  $("#shareFavoriteForm").trackChanges();
  $("#FavoriteCreateCommunityForm").trackChanges();
  search_user .addEventListener('keyup', search);
  button_reset.addEventListener('click', onReset);
})($);
/*
(function signViewsLazyLoading($) {
  var VIDEO_HIDDEN_CLASS = 'video-view-hidden';
  var NB_VIDEO_VIEWS_INC = 8;
  var REVEAL_DURATION_MS = 1000;


  var videosContainer = document.getElementById("videos-container");
  /!** Live node list (updated while we iterate over it...) *!/
  if (videosContainer != null) {
    var videoViewsHidden = videosContainer.getElementsByClassName(VIDEO_HIDDEN_CLASS);
    var videosCount =  $("#videos-container").children("div").length;
  }

  var displayedVideosCount = 0;
  var search_criteria = document.getElementById("search-criteria");
  var signNotAvailable = document.getElementById("sign-not-available");


  var accentMap = {
    "é": "e",
    "è": "e",
    "ê": "e",
    "à": "a",
    "â": "a",
    "î": "i",
    "ô": "o",
    "ù": "u",
    "î": "i",
    "ç": "c"
  };

  var normalize = function( term ) {
    var ret = "";
    for ( var i = 0; i < term.length; i++ ) {
      ret += accentMap[ term.charAt(i) ] || term.charAt(i);
    }
    return ret;
  };

  if (!String.prototype.startsWith) {
    String.prototype.startsWith = function(searchString, position){
      position = position || 0;
      return this.substr(position, searchString.length) === searchString;
    };
  }

  function showVideoView(videoView) {
    videoView.style.opacity = "0";
    videoView.className = videoView.className.replace(VIDEO_HIDDEN_CLASS, '');
    var img = videoView.getElementsByTagName('img')[0];
    var thumbnailUrl = img.dataset.src;
    img.src = thumbnailUrl;
    $(videoView).fadeTo(REVEAL_DURATION_MS, 1);
  }

  function showNextVideoViews() {
    var viewsToReveal = [];
    if (videoViewsHidden != null) {
      for (var i = 0; i < NB_VIDEO_VIEWS_INC && i < videoViewsHidden.length; i++) {
        viewsToReveal.push(videoViewsHidden[i]);
      }
      for (var i = 0; i < viewsToReveal.length; i++) {
        showVideoView(viewsToReveal[i]);
        displayedVideosCount++;
      }
      console.log("total: " + videosCount + ", hidden: " + videoViewsHidden.length + ", displayedVideosCount: " + displayedVideosCount);
    }
  }

  function onScroll(event) {
    var noMoreHiddenVideos = videoViewsHidden.length === 0;
    var closeToBottom = $(window).scrollTop() + $(window).height() > $(document).height() - $(window).height() / 5;
    if (search_criteria.value == "") {
      if (!noMoreHiddenVideos && closeToBottom) {
        showNextVideoViews();
      }
    }

  }

  function search(event) {
    var display = 0;
    var g = normalize($(this).val());

    if (g!="") {
      $("#videos-container").children("label").each(function () {
        $("#reset").css("visibility", "visible");
        var s = normalize($(this).attr("id"));
        var img = $(this).find("img")[0];
        if (s.toUpperCase().indexOf(g.toUpperCase()) != -1) {
          if ($(this).hasClass(VIDEO_HIDDEN_CLASS)) {
            $(this).removeClass(VIDEO_HIDDEN_CLASS);
            var thumbnailUrl = img.dataset.src;
            img.src = thumbnailUrl;
            displayedVideosCount++;
          }
          $(this).show();
          display++;
        }
        else {
          $(this).hide();
        }
      });
      if (display == 0) {
        $(signNotAvailable).show();
      } else {
        $(signNotAvailable).hide();
      }
    } else {
      $("#reset").css("visibility", "hidden");
      $("#videos-container").children("label").each(function () {
        if (!$(this).hasClass(VIDEO_HIDDEN_CLASS)) {
          $(this).addClass(VIDEO_HIDDEN_CLASS);
          $(this).hide();
        }});
      displayedVideosCount = 0;
      initWithFirstVideos();
    }
  }

  function scrollBarVisible() {
    return $(document).height() > $(window).height();
  }

function initWithFirstVideos() {
  do {
    showNextVideoViews();
  } while(!scrollBarVisible() && displayedVideosCount != videosCount);

}

  function onReset(event) {

    $(':input', '#myform')
      .not(':button, :submit, :reset, :hidden')
      .val('');
    $("#reset").css("visibility", "hidden");
    $("#videos-container").children("label").each(function () {
      if (!$(this).hasClass(VIDEO_HIDDEN_CLASS)) {
        $(this).addClass(VIDEO_HIDDEN_CLASS);
        $(this).hide();
      }});
    displayedVideosCount = 0;
    initWithFirstVideos();

  }


  function main() {
    // show first signs at load

    document.addEventListener('scroll', onScroll);
    search_criteria.addEventListener('keyup', search);
    var button_reset = document.getElementById("reset");
    if (button_reset != null) {
      button_reset.addEventListener('click', onReset);
    }

    initWithFirstVideos();

    $("#associateForm").trackChanges();
  }

  main();

})($);
*/