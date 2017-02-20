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
  console.log("onSearch");
}


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
  var modeSearch = new Boolean(false);

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
    console.log("total: " + signsCount + ", hidden: " + signViewsHidden.length + ", displayedSignsCount: " + displayedSignsCount);
  }

  function onScroll(event) {
    var noMoreHiddenSigns = signViewsHidden.length === 0;
    var closeToBottom = $(window).scrollTop() + $(window).height() > $(document).height() - $(window).height()/5;
    var search_criteria = document.getElementById("search-criteria");
    console.log("search_criteria " + search_criteria.value)
    if(!noMoreHiddenSigns && closeToBottom && search_criteria.value == "") {
      showNextSignViews();
    }
  }

  function search(event) {
    console.log("search");
    var g = $(this).val();

    if (g!="") {
      $("#signs-container").children("div").each(function () {
        $("#reset").css("visibility", "visible");
        $("#reset").show();
        var s = $(this).attr("id");
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




  function main() {
    // show first signs at load

    var search_criteria = document.getElementById("search-criteria");
    search_criteria.addEventListener('keyup', search);
    if (search_criteria.classList.contains("search-hidden")) {
      initWithFirstSigns();
      modeSearch=false;
    } else {
      modeSearch = true;
    }

    // then wait to reach the page bottom to load next views
    document.addEventListener('scroll', onScroll);
    var button_reset = document.getElementById("reset");
    button_reset.addEventListener('click', onReset);
  }

  main();

})($);