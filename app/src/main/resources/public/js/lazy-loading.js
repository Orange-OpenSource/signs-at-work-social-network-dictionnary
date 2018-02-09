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

var SIGN_HIDDEN_CLASS = 'sign-view-hidden';
var VIDEO_HIDDEN_CLASS = 'video-view-hidden';
var NB_SIGN_VIEWS_INC = 6;
var NB_VIDEO_VIEWS_INC = 6;
var REVEAL_DURATION_MS = 1000;

var signsContainer = document.getElementById("signs-container");
/** Live node list (updated while we iterate over it...) */
var signViewsHidden = signsContainer.getElementsByClassName(SIGN_HIDDEN_CLASS);

var signsCount = signsContainer.children.length;

var displayedSignsCount = 0;
var videosContainer = document.getElementById("videos-container");
/** Live node list (updated while we iterate over it...) */
if (videosContainer != null) {
  var videoViewsHidden = videosContainer.getElementsByClassName(VIDEO_HIDDEN_CLASS);
  var videosCount = videosContainer.children.length;
}

var displayedVideosCount = 0;
var modeSearch = new Boolean(false);

var search_criteria = document.getElementById("search-criteria");

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

function showSignView(signView) {
  signView.style.opacity = "0";
  signView.className = signView.className.replace(SIGN_HIDDEN_CLASS, '');
  var img = signView.getElementsByTagName('img')[0];
  var thumbnailUrl = img.dataset.src;
  img.src = thumbnailUrl;
  $(signView).fadeTo(REVEAL_DURATION_MS, 1);
}

function showVideoView(videoView) {
  videoView.style.opacity = "0";
  videoView.className = videoView.className.replace(VIDEO_HIDDEN_CLASS, '');
  var img = videoView.getElementsByTagName('img')[0];
  var thumbnailUrl = img.dataset.src;
  img.src = thumbnailUrl;
  $(videoView).fadeTo(REVEAL_DURATION_MS, 1);
}

function showNextSignViews() {
  var viewsToReveal = [];
  for (var i = 0; i < NB_SIGN_VIEWS_INC && i < signViewsHidden.length; i++) {
    viewsToReveal.push(signViewsHidden[i]);
  }
  for (var i = 0; i < viewsToReveal.length; i++) {
    showSignView(viewsToReveal[i]);
    displayedSignsCount++;
  }
  console.log("total: " + signsCount + ", hidden: " + signViewsHidden.length + ", displayedSignsCount: " + displayedSignsCount);
}

function showNextVideoViews() {
  var viewsToReveal = [];
  for (var i = 0; i < NB_VIDEO_VIEWS_INC && i < videoViewsHidden.length; i++) {
    viewsToReveal.push(videoViewsHidden[i]);
  }
  for (var i = 0; i < viewsToReveal.length; i++) {
    showVideoView(viewsToReveal[i]);
    displayedVideosCount++;
  }
  console.log("total: " + videosCount + ", hidden: " + videoViewsHidden.length + ", displayedVideosCount: " + displayedVideosCount);
}

function onScroll(event) {
  if (signsContainer != null) {
    var noMoreHiddenSigns = signViewsHidden.length === 0;
    var closeToBottom = $(window).scrollTop() + $(window).height() > $(document).height() - $(window).height() / 5;
    if (!modeSearch) {
      //console.log("search hidden");
      if (!noMoreHiddenSigns && closeToBottom) {
        showNextSignViews();
      }
    } else {
      //console.log("search show");
    }
  } else {
    var noMoreHiddenVideos = videoViewsHidden.length === 0;
    var closeToBottom = $(window).scrollTop() + $(window).height() > $(document).height() - $(window).height()/5;
    if (!modeSearch) {
      //console.log("search hidden");
      if(!noMoreHiddenVideos && closeToBottom) {
        showNextVideoViews();
      }
    } else {
      //console.log("search show");
    }
  }
}


function search(event) {
  if (signsContainer != null) {
    var g = normalize($(this).val());

    if (g != "") {
      $("#signs-container").children("div").each(function () {
        $("#reset").css("visibility", "visible");
        $("#reset").show();
        var s = normalize($(this).attr("id"));
        var img = $(this).find("img")[0];
        if (s.toUpperCase().startsWith(g.toUpperCase()) == true) {
          if ($(this).hasClass("sign-view-hidden")) {
            $(this).removeClass('sign-view-hidden');
            var thumbnailUrl = img.dataset.src;
            img.src = thumbnailUrl;
            displayedSignsCount++;
          }
          $(this).show();

        }
        else {
          $(this).hide();
        }
      });
    } else {
      $("#reset").css("visibility", "hidden");
      $("#reset").hide();
      if (modeSearch == true) {
        $("#signs-container").children("div").each(function () {
          $(this).hide();
        });
      } else {
        $("#signs-container").children("div").each(function () {
          $(this).show();
        });
      }
    }
  } else {
    var g = normalize($(this).val());

    if (g!="") {
      $("#videos-container").children("div").each(function () {
        $("#reset").css("visibility", "visible");
        $("#reset").show();
        var s = normalize($(this).attr("id"));
        var img = $(this).find("img")[0];
        if (s.toUpperCase().startsWith(g.toUpperCase()) == true) {
          if ($(this).hasClass("video-view-hidden")) {
            $(this).removeClass('video-view-hidden');
            var thumbnailUrl = img.dataset.src;
            img.src = thumbnailUrl;
            displayedVideosCount++;
          }
          $(this).show();

        }
        else {
          $(this).hide();
        }
      });
    } else {
      $("#reset").css("visibility", "hidden");
      $("#reset").hide();
      if (modeSearch == true) {
        $("#videos-container").children("div").each(function () {
          $(this).hide();
        });
      } else {
        $("#video-container").children("div").each(function () {
          $(this).show();
        });
      }
    }
  }
}



function searchSignAfterReload(search_value) {
  console.log("search_value "+search_value);
  var g = normalize(search_value);

  if (g!="") {
    $("#signs-container").children("div").each(function () {
      $("#reset").css("visibility", "visible");
      $("#reset").show();
      var s = normalize($(this).attr("id"));
      var img = $(this).find("img")[0];
      if (s.toUpperCase().startsWith(g.toUpperCase()) == true) {
        if ($(this).hasClass("sign-view-hidden")) {
          $(this).removeClass('sign-view-hidden');
          var thumbnailUrl = img.dataset.src;
          img.src = thumbnailUrl;
          displayedSignsCount++;
        }
        $(this).show();

      }
      else {
        $(this).hide();
      }
    });
  } else {
    $("#reset").css("visibility", "hidden");
    $("#reset").hide();
    if (modeSearch == true) {
      $("#signs-container").children("div").each(function () {
        $(this).hide();
      });
    } else {
      $("#signs-container").children("div").each(function () {
        $(this).show();
      });
    }
  }
}

function searchVideoAfterReload(search_value) {
  console.log("search_value "+search_value);
  var g = normalize(search_value);

  if (g!="") {
    $("#videos-container").children("div").each(function () {
      $("#reset").css("visibility", "visible");
      $("#reset").show();
      var s = normalize($(this).attr("id"));
      var img = $(this).find("img")[0];
      if (s.toUpperCase().startsWith(g.toUpperCase()) == true) {
        if ($(this).hasClass("video-view-hidden")) {
          $(this).removeClass('video-view-hidden');
          var thumbnailUrl = img.dataset.src;
          img.src = thumbnailUrl;
          displayedVideosCount++;
        }
        $(this).show();

      }
      else {
        $(this).hide();
      }
    });
  } else {
    $("#reset").css("visibility", "hidden");
    $("#reset").hide();
    if (modeSearch == true) {
      $("#videos-container").children("div").each(function () {
        $(this).hide();
      });
    } else {
      $("#video-container").children("div").each(function () {
        $(this).show();
      });
    }
  }
}

function scrollBarVisible() {
  return $(document).height() > $(window).height();
}

function initWithFirstSigns() {
  do {
    showNextSignViews();
  } while(!scrollBarVisible() && displayedSignsCount != signsCount);
}

function initWithFirstVideos() {
  do {
    showNextVideoViews();
  } while(!scrollBarVisible() && displayedVideosCount != videosCount);
}

function onReset(event) {

  if (signsContainer != null) {
    $(':input', '#myform')
      .not(':button, :submit, :reset, :hidden')
      .val('');
    $("#reset").css("visibility", "hidden");
    $("#reset").hide();
    if (modeSearch == true) {
      $("#signs-container").children("div").each(function () {
        $(this).hide();
      });
    } else {
      $("#signs-container").children("div").each(function () {
        $(this).show();
      });
    }
  } else {
    $(':input', '#myform')
      .not(':button, :submit, :reset, :hidden')
      .val('');
    $("#reset").css("visibility", "hidden");
    $("#reset").hide();
    if (modeSearch == true) {
      $("#videos-container").children("div").each(function () {
        $(this).hide();
      });
    } else {
      $("#videos-container").children("div").each(function () {
        $(this).show();
      });
    }
  }

}



function main() {
  // show first signs at load

 /* var search_criteria = document.getElementById("search-criteria");*/
   if (search_criteria == null) {
     if (signsContainer != null) {
       initWithFirstSigns();
     } else {
       initWithFirstVideos();
     }
     modeSearch = false;
     document.addEventListener('scroll', onScroll);
   } else {
     search_criteria.addEventListener('keyup', search);
     if (search_criteria.classList.contains("search-hidden")) {
       if (signsContainer != null) {
         initWithFirstSigns();
       } else {
         initWithFirstVideos();
       }
       modeSearch = false;
       document.addEventListener('scroll', onScroll);
     } else {
       modeSearch = true;
       var button_reset = document.getElementById("reset");
       if (button_reset != null) {
         button_reset.addEventListener('click', onReset);
       }
     }
   }

}

function onFiltreSign(event, href) {
  console.log("onFiltre");
  event.preventDefault();
  console.log("href "+href);
  $.ajax({
    url: href,
    context: document.body,
    success: function (response) {
      console.log("Success ");
      document.getElementById("frame-signs").innerHTML = response;
      signsContainer = document.getElementById("signs-container");
      signViewsHidden = signsContainer.getElementsByClassName(SIGN_HIDDEN_CLASS);
      signsCount = signsContainer.children.length;
      displayedSignsCount = 0;
      videosContainer = null;
      main();
      if (modeSearch == true) {
        console.log("search value "+search_criteria.value);
        searchSignAfterReload(search_criteria.value);
      }
    },
    error: function (response) {
      console.log("Erreur ");
    }
  })
}


function onFiltreVideo(event, href) {
  console.log("onFiltre");
  event.preventDefault();
  console.log("href "+href);
  $.ajax({
    url: href,
    context: document.body,
    success: function (response) {
      console.log("Success ");
      document.getElementById("frame-signs").innerHTML = response;
      videosContainer = document.getElementById("videos-container");
      videoViewsHidden = videosContainer.getElementsByClassName(VIDEO_HIDDEN_CLASS);
      videosCount = videosContainer.children.length;
      displayedVideosCount = 0;
      signsContainer = null;
      main();
      if (modeSearch == true) {
        console.log("search value "+search_criteria.value);
        searchVideoAfterReload(search_criteria.value);
      }
    },
    error: function (response) {
      console.log("Erreur ");
    }
  })
}

(function signOrvideoViewsLazyLoading($) {

  main();


})($);
