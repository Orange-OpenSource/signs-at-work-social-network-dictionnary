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

(function videoViewsLazyLoading($) {
  var HIDDEN_CLASS = 'video-view-hidden';
  var NB_VIDEO_VIEWS_INC = 8;
  var REVEAL_DURATION_MS = 1000;

  var videosContainer = document.getElementById("videos-container");
  /** Live node list (updated while we iterate over it...) */
  var videoViewsHidden = videosContainer.getElementsByClassName(HIDDEN_CLASS);

  var videosCount = videosContainer.children.length;

  var displayedVideosCount = 0;
  var modeSearch = new Boolean(false);

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
    videoView.className = videoView.className.replace(HIDDEN_CLASS, '');
    var img = videoView.getElementsByTagName('img')[0];
    var thumbnailUrl = img.dataset.src;
    img.src = thumbnailUrl;
    $(videoView).fadeTo(REVEAL_DURATION_MS, 1);
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

  function search(event) {
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

  function main() {
    // show first signs at load
    //initWithFirstVideos();

    var search_criteria = document.getElementById("search-criteria");
    if (search_criteria == null) {
      initWithFirstVideos();
      modeSearch = false;
      document.addEventListener('scroll', onScroll);
    } else {
      search_criteria.addEventListener('keyup', search);
      if (search_criteria.classList.contains("search-hidden")) {
        initWithFirstVideos();
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

  main();

})($);
