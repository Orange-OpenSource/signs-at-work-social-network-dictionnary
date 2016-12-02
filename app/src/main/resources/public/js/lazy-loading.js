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

(function signViewsLazyLoading() {

  var HIDDEN_CLASS = 'sign-view-hidden';
  var NB_SIGN_VIEWS_INC = 4;
  var signsContainer = document.getElementById("signs-container");
  var signsCount = signsContainer.children.length;
  var displayedSignsCount = 0;

  function showSignView(signView) {
    signView.className = signView.className.replace(HIDDEN_CLASS, '');
    var img = signView.getElementsByTagName('img')[0];
    var thumbnailUrl = img.dataset.src;
    img.src = thumbnailUrl;
  }

  function showNextSignViews() {
    var signViewsHidden = signsContainer.getElementsByClassName(HIDDEN_CLASS);
    for (var i = 0; i < NB_SIGN_VIEWS_INC && i < signViewsHidden.length; i++) {
      showSignView(signViewsHidden[i]);
      displayedSignsCount++;
    }
  }

  function onScroll(event) {
    if($(window).scrollTop() + $(window).height() > $(document).height() - $(window).height()/3) {
      showNextSignViews();
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

  function main() {
    // show first signs at load
    initWithFirstSigns();

    // then wait to reach the page bottom to load next views
    document.addEventListener('scroll', onScroll);
  }

  main();

})();