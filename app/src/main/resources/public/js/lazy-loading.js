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
console.log("Cool, lazy-loading.js is loaded :)");

var SIGN_HIDDEN_CLASS = 'sign-view-hidden';
var VIDEO_HIDDEN_CLASS = 'video-view-hidden';
var NB_SIGN_VIEWS_INC = 8;
var NB_VIDEO_VIEWS_INC = 8;
var REVEAL_DURATION_MS = 1000;

var modeSearch;

var addNewSuggestRequest = document.getElementById("add-new-suggest-request");
var signsContainer = document.getElementById("signs-container");
/** Live node list (updated while we iterate over it...) */
if (signsContainer != null) {
  var signViewsHidden = signsContainer.getElementsByClassName(SIGN_HIDDEN_CLASS);
  var signsCount =  $("#signs-container").children("div").length;
}

var displayedSignsCount = 0;
var videosContainer = document.getElementById("videos-container");
/** Live node list (updated while we iterate over it...) */
if (videosContainer != null) {
  var videoViewsHidden = videosContainer.getElementsByClassName(VIDEO_HIDDEN_CLASS);
  var videosCount =  $("#videos-container").children("div").length;
}

var signAvailable = document.getElementById("sign_available");
var videoAvailable = document.getElementById("video_available");

var displayedVideosCount = 0;

var search_criteria = document.getElementById("search-criteria");

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

var dropdownFilter = document.getElementById("dropdown-filter");

var normalize = function( term ) {
  var ret = "";
  for ( var i = 0; i < term.length; i++ ) {
    ret += accentMap[ term.charAt(i) ] || term.charAt(i);
  }
  return ret;
};

var nb = document.getElementById("nb");

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
  if (!dropdownFilter.classList.contains("open")) {
    if (signsContainer != null) {
      var noMoreHiddenSigns = signViewsHidden.length === 0;
      var closeToBottom = $(window).scrollTop() + $(window).height() > $(document).height() - $(window).height() / 5;

      if (search_criteria.value == "") {
        if (!noMoreHiddenSigns && closeToBottom) {
          showNextSignViews();
        }
      }
    } else {
      if (videoViewsHidden != null) {
        var noMoreHiddenVideos = videoViewsHidden.length === 0;
        var closeToBottom = $(window).scrollTop() + $(window).height() > $(document).height() - $(window).height() / 5;
        if (search_criteria.value == "") {
          if (!noMoreHiddenVideos && closeToBottom) {
            showNextVideoViews();
          }
        }
      }
    }
  }

}


function search(event) {

  var display = 0;
  $(addNewSuggestRequest).hide();
  if (signsContainer != null) {
    var g = normalize($(this).val());

    if (g != "") {
      $("#signs-container").children("div").each(function () {
        $("#reset").css("visibility", "visible");
        $(this).css({'float': 'left'});
       /* $("#reset").show();*/
        var s = normalize($(this).attr("id"));
        var img = $(this).find("img")[0];
        /*if (s.toUpperCase().startsWith(g.toUpperCase()) == true) {*/
        if (s.toUpperCase().indexOf(g.toUpperCase()) != -1) {
          if ($(this).hasClass(SIGN_HIDDEN_CLASS)) {
            $(this).removeClass(SIGN_HIDDEN_CLASS);
            var thumbnailUrl = img.dataset.src;
            img.src = thumbnailUrl;
            displayedSignsCount++;

          }
          $(this).show();
          display++;
        }
        else {
          $(this).hide();
        }

      });

      console.log("display "+display);
      nb.innerHTML = "("+display+")";
      $(nb).show();
      if (display == 0) {
        $(signAvailable).hide();
        $(addNewSuggestRequest).show();
      } else {
        $(signAvailable).show();
        $(addNewSuggestRequest).hide();
        if (display == 1) {
         $("#signs-container").children("div").each(function () {
             if ($(this).css('display') == 'block') {
                $(this).css({'margin': 'auto', 'float': 'none'});
             }
            });
        }
      }
    } else {
      $(addNewSuggestRequest).hide();
      $(signAvailable).hide();
      $("#reset").css("visibility", "hidden");
      /*$("#reset").hide();*/
      $("#signs-container").children("div").each(function () {
        $(this).css({'float': 'left'});
        if (!$(this).hasClass(SIGN_HIDDEN_CLASS)) {
          $(this).addClass(SIGN_HIDDEN_CLASS);
          $(this).hide();
        }});
      displayedSignsCount = 0;
      if (modeSearch === "false") {
        initWithFirstSigns();
      } else {
        $(nb).hide();
      }
    }
  } else {
    var g = normalize($(this).val());

    if (g!="") {
      $("#videos-container").children("div").each(function () {
        $("#reset").css("visibility", "visible");
        $(this).css({'float': 'left'});
        /*$("#reset").show();*/
        var s = normalize($(this).attr("id"));
        var img = $(this).find("img")[0];
       /* if (s.toUpperCase().startsWith(g.toUpperCase()) == true) {*/
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
      console.log("display "+display);
      nb.innerHTML = "("+display+")";
      $(nb).show();
      if (display == 0) {
        $(videoAvailable).hide();
        $(addNewSuggestRequest).show();
      } else {
        $(videoAvailable).show();
        $(addNewSuggestRequest).hide();
        if (display == 1) {
         $("#videos-container").children("div").each(function () {
             if ($(this).css('display') == 'block') {
                $(this).css({'margin': 'auto', 'float': 'none'});
             }
            });
        }
      }
    } else {
      $(addNewSuggestRequest).hide();
      $(videoAvailable).hide();
      $("#reset").css("visibility", "hidden");
      /*$("#reset").hide();*/
      $("#videos-container").children("div").each(function () {
        $(this).css({'float': 'left'});
        if (!$(this).hasClass(VIDEO_HIDDEN_CLASS)) {
          $(this).addClass(VIDEO_HIDDEN_CLASS);
          $(this).hide();
        }});
      displayedVideosCount = 0;
      if (modeSearch === "false") {
        initWithFirstVideos();
      } else {
        $(nb).hide();
      }
    }
  }
}



function searchSignAfterReload(search_value) {
  var display = 0;
  $(addNewSuggestRequest).hide();
  console.log("search_value "+search_value);
  var g = normalize(search_value);

  if (g!="") {
    $("#signs-container").children("div").each(function () {
      $("#reset").css("visibility", "visible");
      $(this).css({'float': 'left'});
     /* $("#reset").show();*/
      var s = normalize($(this).attr("id"));
      var img = $(this).find("img")[0];
      /*if (s.toUpperCase().startsWith(g.toUpperCase()) == true) {*/
      if (s.toUpperCase().indexOf(g.toUpperCase()) != -1) {
        if ($(this).hasClass(SIGN_HIDDEN_CLASS)) {
          $(this).removeClass(SIGN_HIDDEN_CLASS);
          var thumbnailUrl = img.dataset.src;
          img.src = thumbnailUrl;
          displayedSignsCount++;
        }
        $(this).show();
        display++;
      }
      else {
        $(this).hide();
      }
    });
    console.log("display "+display);
    nb.innerHTML = "("+display+")";
    $(nb).show();
    if (display == 0) {
      $(signAvailable).hide();
      $(addNewSuggestRequest).show();
    } else {
      $(signAvailable).show();
      $(addNewSuggestRequest).hide();
      if (display == 1) {
       $("#signs-container").children("div").each(function () {
           if ($(this).css('display') == 'block') {
              $(this).css({'margin': 'auto', 'float': 'none'});
           }
          });
      }
    }
  } else {
    $(addNewSuggestRequest).hide();
    $(signAvailable).hide();
    $("#reset").css("visibility", "hidden");
    $("#reset").hide();
    $("#signs-container").children("div").each(function () {
     $(this).css({'float': 'left'});
      if (!$(this).hasClass(SIGN_HIDDEN_CLASS)) {
        $(this).addClass(SIGN_HIDDEN_CLASS);
        $(this).hide();
      }});
    displayedSignsCount = 0;
    if (modeSearch === "false") {
      initWithFirstSigns();
    } else {
      $(nb).hide();
    }
  }
}

function searchVideoAfterReload(search_value) {
  var display = 0;
  $(addNewSuggestRequest).hide();
  console.log("search_value "+search_value);
  var g = normalize(search_value);

  if (g!="") {
    $("#videos-container").children("div").each(function () {
      $("#reset").css("visibility", "visible");
      $(this).css({'float': 'left'});
      /*$("#reset").show();*/
      var s = normalize($(this).attr("id"));
      var img = $(this).find("img")[0];
      /*if (s.toUpperCase().startsWith(g.toUpperCase()) == true) {*/
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
    console.log("display "+display);
    nb.innerHTML = "("+display+")";
    $(nb).show();
    if (display == 0) {
      $(videoAvailable).hide();
      $(addNewSuggestRequest).show();
    } else {
      $(videoAvailable).show();
      $(addNewSuggestRequest).hide();
      if (display == 1) {
         $("#videos-container").children("div").each(function () {
             if ($(this).css('display') == 'block') {
                $(this).css({'margin': 'auto', 'float': 'none'});
             }
            });
        }
    }
  } else {
    $(addNewSuggestRequest).hide();
    $(videoAvailable).hide();
    $("#reset").css("visibility", "hidden");
    /*$("#reset").hide();*/
    $("#videos-container").children("div").each(function () {
      $(this).css({'float': 'left'});
      if (!$(this).hasClass(VIDEO_HIDDEN_CLASS)) {
        $(this).addClass(VIDEO_HIDDEN_CLASS);
        $(this).hide();
      }});
    displayedVideosCount = 0;
    if (modeSearch === "false") {
      initWithFirstVideos();
    } else {
      $(nb).hide();
    }
  }
}

function scrollBarVisible() {
  return $(document).height() > $(window).height();
}

function initWithFirstSigns() {
  nb.innerHTML = "("+signsCount+")";
  $(nb).show();
  do {
    showNextSignViews();
  } while(!scrollBarVisible() && displayedSignsCount != signsCount);

}

function initWithFirstVideos() {
  nb.innerHTML = "("+videosCount+")";
  $(nb).show();
  do {
    showNextVideoViews();
  } while(!scrollBarVisible() && displayedVideosCount != videosCount);

}

function onReset(event) {

  $(addNewSuggestRequest).hide();
  if (signsContainer != null) {
    $(':input', '#myform')
      .not(':button, :submit, :reset, :hidden')
      .val('');
    $("#reset").css("visibility", "hidden");
    /*$("#reset").hide();*/
    $(signAvailable).hide();
    $("#signs-container").children("div").each(function () {
      $(this).css({'float': 'left'});
      if (!$(this).hasClass(SIGN_HIDDEN_CLASS)) {
        $(this).addClass(SIGN_HIDDEN_CLASS);
        $(this).hide();
      }});
    displayedSignsCount = 0;
    if (modeSearch === "false") {
      initWithFirstSigns();
    } else {
      $(nb).hide();
    }
  } else {
    $(':input', '#myform')
      .not(':button, :submit, :reset, :hidden')
      .val('');
    $("#reset").css("visibility", "hidden");
    /*$("#reset").hide();*/
    $(videoAvailable).hide();
    $("#videos-container").children("div").each(function () {
      $(this).css({'float': 'left'});
      if (!$(this).hasClass(VIDEO_HIDDEN_CLASS)) {
        $(this).addClass(VIDEO_HIDDEN_CLASS);
        $(this).hide();
    }});
    displayedVideosCount = 0;
    if (modeSearch === "false") {
      initWithFirstVideos();
    } else {
      $(nb).hide();
    }
  }

}



function main() {
  // show first signs at load
  console.log("main "+window.location.href+" "+window.location.search);
  var url_param = window.location.search;
  var isSearch = url_param.indexOf("isSearch");
  console.log("isSearch "+isSearch);
  modeSearch = url_param.substring(url_param.indexOf("isSearch") +9);
  console.log("modeSearch "+modeSearch);
  if (modeSearch === "false") {
    console.log("false");
  } else {
    console.log("true");
  }

  document.addEventListener('scroll', onScroll);
  search_criteria.addEventListener('keyup', search);
  var button_reset = document.getElementById("reset");
  if (button_reset != null) {
    button_reset.addEventListener('click', onReset);
  }

  if (modeSearch === "false") {
    if (signsContainer != null) {
      initWithFirstSigns();
    } else {
      if (videosContainer != null) {
        initWithFirstVideos();
      }
    }
  } else {
    $(nb).hide();
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
      signsCount = $("#signs-container").children("div").length;
      displayedSignsCount = 0;
      videosContainer = null;
      addNewSuggestRequest = document.getElementById("add-new-suggest-request");
      nb = document.getElementById("nb");
      signAvailable = document.getElementById("sign_available");
      dropdownFilter = document.getElementById("dropdown-filter");
      if (signsCount == 0) {
        $(search_criteria).hide();
        $("#reset").css("visibility", "hidden");
      } else {
        $(search_criteria).show();
        if (search_criteria.value != "") {
          console.log("search value " + search_criteria.value);
          searchSignAfterReload(search_criteria.value);
        } else {
          $("#signs-container").children("div").each(function () {
            $(this).hide();
          });
          main();
        }
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
      if (videosContainer != null) {
        videoViewsHidden = videosContainer.getElementsByClassName(VIDEO_HIDDEN_CLASS);
        videosCount = $("#videos-container").children("div").length;
      } else {
        videosCount = 0;
      }
      displayedVideosCount = 0;
      signsContainer = null;
      addNewSuggestRequest = document.getElementById("add-new-suggest-request");
      nb = document.getElementById("nb");
      videoAvailable = document.getElementById("video_available");
      dropdownFilter = document.getElementById("dropdown-filter");
      if (videosCount == 0) {
        $(search_criteria).hide();
        $("#reset").css("visibility", "hidden");
      } else {
        $(search_criteria).show();
        if (search_criteria.value != "") {
          console.log("search value " + search_criteria.value);
          searchVideoAfterReload(search_criteria.value);
        } else {
          $("#videos-container").children("div").each(function () {
            $(this).hide();
          });
          main();
        }
      }
    },
    error: function (response) {
      console.log("Erreur ");
    }
  })
}

function displayVideoForFavoriteFilter(url, name, idForName, nbVideo) {

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
  document.getElementById("videoUrl").src = url+'?endscreen-enable=false&autoplay=1&sharing-enable=false&wmode=transparent&disable-queue=1&mute=true';
  } else {
    document.getElementById("videoOnDailyMotion").style.display="none"
    document.getElementById("videoOnServer").style.display="block"
    document.getElementById("videoUrlOnServer").src= url;
    document.getElementById("videoplayer").load();
  }
  /*document.getElementById("videoUrl").src = url+'?endscreen-enable=false&autoplay=1&sharing-enable=false&wmode=transparent&disable-queue=1';*/
}

function displayVideo(isAuthenticated, id, url, name, idForName, nbVideo) {
  var data;
  var indice = 0;
  var get_url;
  console.log(id);
  console.log(url);
  console.log(name);
  console.log(idForName);
  console.log(nbVideo);
  if (isAuthenticated == "true") {
    get_url = "/ws/sec/signs/"+ id + "/videos";
  } else {
    get_url = "/ws/signs/"+ id + "/videos";
  }
  console.log(get_url);

  if (nbVideo == 1) {
    document.getElementById("videoName").innerText = name;
    document.getElementById("variantes").style.display="none";
  } else {
    document.getElementById("videoName").innerText = name + '_' + idForName;
    document.getElementById("variantes").style.display="block";
    document.getElementById("nbVariante").style.visibility="visible";
    document.getElementById("previous_variante").style.visibility = "hidden";
    document.getElementById("next_variante").style.visibility = "visible";
    document.getElementById("nbVariante").innerText = indice + 1  + "/" + nbVideo;
    $.ajax({
      url: get_url,
      type: 'get',
      contentType: "application/json",
      success: function(response) {
        console.log(response);
        data = response;
        document.getElementById("videoName").innerText = data[0].videoName;
      },
      error: function(response) {
        console.log(response);
      }
    })
  }

 if  (url.includes('http')) {
  console.log("http");
  document.getElementById("videoOnDailyMotion").style.display="block"
  document.getElementById("videoOnServer").style.display="none"
  document.getElementById("videoUrl").src = url+'?endscreen-enable=false&autoplay=1&sharing-enable=false&wmode=transparent&disable-queue=1&mute=true';
  } else {
    document.getElementById("videoOnDailyMotion").style.display="none"
    document.getElementById("videoOnServer").style.display="block"
    document.getElementById("videoUrlOnServer").src= url;
    document.getElementById("videoplayer").load();
  }
 /* document.getElementById("videoUrl").src = url+'?endscreen-enable=false&autoplay=1&sharing-enable=false&wmode=transparent&disable-queue=1';*/

  document.getElementById("next_variante").onclick = function () {
    console.log("next variante "+indice);
    indice = indice + 1;

    console.log(data[indice]);
    console.log(data[indice].videoName);
    console.log(data[indice].url);
    document.getElementById("previous_variante").style.visibility = "visible";
    document.getElementById("nbVariante").innerText = indice + 1 + "/" + nbVideo;
    document.getElementById("videoName").innerText = data[indice].videoName;
     if  (data[indice].url.includes('http')) {
      console.log("http");
      document.getElementById("videoOnDailyMotion").style.display="block"
      document.getElementById("videoOnServer").style.display="none"
      document.getElementById("videoUrl").src = data[indice].url+'?endscreen-enable=false&autoplay=1&sharing-enable=false&wmode=transparent&disable-queue=1&mute=true';
      } else {
        document.getElementById("videoOnDailyMotion").style.display="none"
        document.getElementById("videoOnServer").style.display="block"
        document.getElementById("videoUrlOnServer").src= data[indice].url;
        document.getElementById("videoplayer").load();
      }
    /*document.getElementById("videoUrl").src = data[indice].url + '?endscreen-enable=false&autoplay=1&sharing-enable=false&wmode=transparent&disable-queue=1';*/
    if (indice == nbVideo -1) {
      document.getElementById("next_variante").style.visibility = "hidden";
    }
  };


  document.getElementById("previous_variante").onclick = function () {
    console.log("previous variante "+indice);
    indice = indice - 1;
    if (indice >= 0) {
      document.getElementById("nbVariante").innerText = indice +1 + "/" + nbVideo;
      console.log(data[indice]);
      console.log(data[indice].videoName);
      console.log(data[indice].url);
      document.getElementById("next_variante").style.visibility = "visible";
      document.getElementById("videoName").innerText = data[indice].videoName;
       if  (data[indice].url.includes('http')) {
            console.log("http");
            document.getElementById("videoOnDailyMotion").style.display="block"
            document.getElementById("videoOnServer").style.display="none"
            document.getElementById("videoUrl").src = data[indice].url+'?endscreen-enable=false&autoplay=1&sharing-enable=false&wmode=transparent&disable-queue=1&mute=true';
            } else {
              document.getElementById("videoOnDailyMotion").style.display="none"
              document.getElementById("videoOnServer").style.display="block"
              document.getElementById("videoUrlOnServer").src= data[indice].url;
              document.getElementById("videoplayer").load();
            }
      /*document.getElementById("videoUrl").src = data[indice].url + '?endscreen-enable=false&autoplay=1&sharing-enable=false&wmode=transparent&disable-queue=1';*/
      if (indice == 0) {
        document.getElementById("next_variante").style.visibility = "visible";
        document.getElementById("previous_variante").style.visibility = "hidden";
      }
    }

  };


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


(function signOrvideoViewsLazyLoading($) {

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
