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
  $("#signs-container").children("label").each(function () {
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


function onAssociateRequest(signId, videoId) {
  var associateVideosIds = [];
  i=1;
  $("#signs-container").children("label").each(function () {
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
  $("#signs-container").children("label").each(function () {
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
      var url = "/sec/favorite/"+favoriteId;
      window.location = url;
    },
    error: function(response) {
    }
  })

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


(function signViewsLazyLoading($) {
  var HIDDEN_CLASS = 'sign-view-hidden';
  //var NB_SIGN_VIEWS_INC = 16;
  var NB_SIGN_VIEWS_INC = 6;
  var REVEAL_DURATION_MS = 1000;

  var signsContainer = document.getElementById("signs-container");
  /** Live node list (updated while we iterate over it...) */
  var signViewsHidden = signsContainer.getElementsByClassName(HIDDEN_CLASS);

  var signsCount = signsContainer.children.length;

  var displayedSignsCount = 0;

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

  function showSignView(signView) {
    signView.style.opacity = "0";
    signView.className = signView.className.replace(HIDDEN_CLASS, '');
    var img = signView.getElementsByTagName('img')[0];
    var thumbnailUrl = img.dataset.src;
    img.src = thumbnailUrl;
    $(signView).fadeTo(REVEAL_DURATION_MS, 1);
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
    if ((signViewsHidden.length === 0) && scrollBarVisible()){
      $("#button-bottom").css("visibility", "visible");
      $("#button-bottom").show();
    }
    console.log("total: " + signsCount + ", hidden: " + signViewsHidden.length + ", displayedSignsCount: " + displayedSignsCount);
  }

  function onScroll(event) {
    var noMoreHiddenSigns = signViewsHidden.length === 0;
    var closeToBottom = $(window).scrollTop() + $(window).height() > $(document).height() - $(window).height()/5;
    var search_criteria = document.getElementById("search-criteria");
    if(!noMoreHiddenSigns && closeToBottom && search_criteria.value == "") {
      showNextSignViews();
    }
  }

  function search(event) {
    var g = normalize($(this).val());

    if (g!="") {
      $("#signs-container").children("label").each(function () {
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
        if (!$("#associateForm").isChanged()) {
          $("#button-top").css("visibility", "hidden");
          $("#button-top").hide();
          $("#button-bottom").css("visibility", "hidden");
          $("#button-bottom").hide();
        }
        $("#signs-container").children("label").each(function () {
          $(this).hide();
        });
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

  function onReset(event) {

      $(':input', '#myform')
        .not(':button, :submit, :reset, :hidden')
        .val('');
      $("#reset").css("visibility", "hidden");
      $("#reset").hide();
      if (!$("#associateForm").isChanged()) {
        $("#button-top").css("visibility", "hidden");
        $("#button-top").hide();
        $("#button-bottom").css("visibility", "hidden");
        $("#button-bottom").hide();
      }

        $("#signs-container").children("label").each(function () {
          $(this).hide();
        });


  }


  function main() {
    // show first signs at load

    var search_criteria = document.getElementById("search-criteria");
    search_criteria.addEventListener('keyup', search);

    initWithFirstSigns()

    // then wait to reach the page bottom to load next views
    document.addEventListener('scroll', onScroll);
    var button_reset = document.getElementById("reset");
    button_reset.addEventListener('click', onReset);

    $("#associateForm").trackChanges();
  }

  main();

})($);