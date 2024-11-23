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

function displayVideo(url, name, idForName, nbVideo) {

  console.log(url);
  console.log(name);
  console.log(idForName);
  console.log(nbVideo);
  if (nbVideo == 1) {
    document.getElementById("videoName").innerText = name;
  } else {
    document.getElementById("videoName").innerText = name + '_' + idForName;
  }

 if  (url.includes('http')) {
  console.log("http");
  document.getElementById("videoOnDailyMotion").style.display="block"
  document.getElementById("videoOnServer").style.display="none"
  document.getElementById("videoUrl").src = url;
  } else {
    document.getElementById("videoOnDailyMotion").style.display="none"
    document.getElementById("videoOnServer").style.display="block"
    document.getElementById("videoUrlOnServer").src= url;
    document.getElementById("videoplayer").load();
  }
  /*document.getElementById("videoUrl").src = url+'?endscreen-enable=false&autoplay=1&sharing-enable=false&wmode=transparent&disable-queue=1';*/
}

(function videoViewsLazyLoading($) {
  var HIDDEN_CLASS = 'video-view-hidden';
  var NB_VIDEO_VIEWS_INC = 8;
  var REVEAL_DURATION_MS = 1000;

  var videosContainer = document.getElementById("videos-container");
  /** Live node list (updated while we iterate over it...) */
  var videoViewsHidden, videosCount;
  if (videosContainer) {
    videoViewsHidden = videosContainer.getElementsByClassName(HIDDEN_CLASS);
    videosCount = videosContainer.children.length;
  } else {
    videoViewsHidden = '';
    videosCount = 0;
  }


  var displayedVideosCount = 0;
  var modeSearch = new Boolean(false);

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


  main();
  var $play_video = $('#play_video');
  $play_video.on('hidden.bs.modal', function() {
    console.log("hidden play_video");
    console.log(document.getElementById("videoOnDailyMotion").style.display);

    if ((document.getElementById("videoOnDailyMotion").style.display) === "none") {
      document.getElementById("videoUrlOnServer").src = "";
      document.getElementById("videoplayer").load();
    } else {
      document.getElementById("videoUrl").src = "";
    }
  });

})($);


