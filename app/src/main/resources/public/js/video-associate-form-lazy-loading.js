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

function onSearch(){
  $("#search-criteria").show();
  var search_criteria = document.getElementById("search-criteria");
  search_criteria.classList.remove("search-hidden");
  $("#videos-container").children("label").each(function () {
    $(this).hide();
/*    $("#button-top").css("visibility", "hidden");
    $("#button-top").hide();*/
/*    $("#button-bottom").css("visibility", "hidden");
    $("#button-bottom").hide();*/
  });
}

function onBack(signId, videoId){
  if ($("#associateForm").isChanged()) {
    $("#validate_modif").modal('show');
  } else {
    var url = "/sign/"+signId+"/"+videoId;
    window.location = url;
  }


}

function onBackFavorite(backUrl){
  if ($("#associateForm").isChanged()) {
    $("#validate_favorite_modif").modal('show');
  } else {
    var url = backUrl;
    window.location = url;
  }


}


function onContinue(signId, videoId) {
  var url = "/sign/"+signId+"/"+videoId;
  window.location = url;
};

function onContinueFavorite(backUrl) {
  var url = backUrl;
  window.location = url;
};

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
  if ($("#associateForm").isChanged()) {
    var favoriteVideosIds = [];
    i = 1;
    $("#videos-container").children("label").each(function () {
      if (document.getElementById("favoriteVideosIds" + i).checked) {
        var selectedVideoId = document.getElementById("favoriteVideosIds" + i).value;
        favoriteVideosIds.push(selectedVideoId);
      }
      i = i + 1;
    });

    $.ajax({
      url: "/ws/sec/favorite/" + favoriteId + "/add/videos",
      type: 'post',
      data: JSON.stringify(favoriteVideosIds),
      contentType: "application/json",
      success: function (response) {
        $("#validate_favorite_modif").modal('show');
        setTimeout(function () {
          $('#validate_favorite_modif').modal('hide');
          /*window.history.go(-2);*/
          window.history.back();

        }, 3000);

      },
      error: function (response) {
        console.log("erreur");
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


(function videoViewsLazyLoading($) {
  var VIDEO_HIDDEN_CLASS = 'video-view-hidden';
  var NB_VIDEO_VIEWS_INC = 8;
  var REVEAL_DURATION_MS = 1000;


  var videosContainer = document.getElementById("videos-container");
  /** Live node list (updated while we iterate over it...) */
  var videoViewsHidden, videosCount;
  if (videosContainer) {
    var videoViewsHidden = videosContainer.getElementsByClassName(VIDEO_HIDDEN_CLASS);
    videosCount = videosContainer.children.length;
  } else {
    videoViewsHidden = '';
    videosCount = 0;
  }

  var displayedVideosCount = 0;
  var search_criteria = document.getElementById("search-criteria");
  var signNotAvailable = document.getElementById("sign_not_available");


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
    videoView.className = videoView.className.replace(VIDEO_HIDDEN_CLASS, '');
    var img = videoView.getElementsByTagName('img')[0];
    var thumbnailUrl = img.dataset.src;
    img.src = thumbnailUrl;
    $(videoView).fadeTo(REVEAL_DURATION_MS, 1);
  }

  function showNextVideoViews() {
    var viewsToReveal = [];
/*    if (videoViewsHidden != null) {*/
      for (var i = 0; i < NB_VIDEO_VIEWS_INC && i < videoViewsHidden.length; i++) {
        viewsToReveal.push(videoViewsHidden[i]);
      }
      for (var i = 0; i < viewsToReveal.length; i++) {
        showVideoView(viewsToReveal[i]);
        displayedVideosCount++;
      }
      console.log("total: " + videosCount + ", hidden: " + videoViewsHidden.length + ", displayedVideosCount: " + displayedVideosCount);
  /*  }*/
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
    $(signNotAvailable).hide();
    initWithFirstVideos();

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