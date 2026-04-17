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
  let activeFilter = false;

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

  function normalize(str) {
    return str
      .normalize("NFD")
      .replace(/\p{Diacritic}/gu, "")
      .toLowerCase();
  }
  function buildKey(query) {
    return normalize(query);
  }
  function prefix(str, n) {
    return str.slice(0, n);
  }
  function hasAccent(ch) {
    const decomposed = ch.normalize("NFD");
    const base = decomposed.replace(/\p{Diacritic}/gu, "");
    return base.length === 1 && decomposed.length > 1 && base !== ch;
  }
  function baseChar(ch) {
    return ch.normalize("NFD").replace(/\p{Diacritic}/gu, "");
  }
  function hasCedilla(ch) {
    return ch === "ç" || ch === "Ç";
  }
  function compareChar(ref, ch) {
    const refBase = baseChar(ref);
    const chBase = baseChar(ch);
    const refHasAccent = hasAccent(ref);
    const chHasAccent = hasAccent(ch);
    let accentMatch = 0;
    if (refHasAccent === chHasAccent) {
      if (refHasAccent) {
        accentMatch = ref.normalize("NFD") === ch.normalize("NFD") ? 0 : 1;
      }
      else {
        accentMatch = 0;
      }
    }
    else {
      accentMatch = 2;
    }
    const caseMatch = (ref === ref.toLowerCase()) === (ch === ch.toLowerCase()) ? 0 : 1;
    let cedillaPenalty = 0;
    if (hasCedilla(ref)) {
      cedillaPenalty = hasCedilla(ch) ? 0 : 1;
    }
    if (refBase.toLowerCase() !== chBase.toLowerCase()) {
      accentMatch = 2;
    }
    return { accentMatch, caseMatch, cedillaPenalty };
  }
  function prefixScore(word, ref) {
    var _a, _b;
    const n = ref.length;
    let sumAccent = 0;
    let sumCase = 0;
    let sumCedilla = 0;
    const wPref = prefix(word, n);
    for (let i = 0; i < n; i++) {
      const refCh = (_a = ref[i]) !== null && _a !== void 0 ? _a : "";
      const wCh = (_b = wPref[i]) !== null && _b !== void 0 ? _b : "";
      const { accentMatch, caseMatch, cedillaPenalty } = compareChar(refCh, wCh);
      sumAccent += accentMatch;
      sumCase += caseMatch;
      sumCedilla += cedillaPenalty;
    }
    return [sumAccent, sumCase, sumCedilla];
  }
  function isSingleWord(str) {
    return !str.includes(" ");
  }
  function computeR1(list, query) {
    const entries = list.map((v, i) => ({ value: v, index: i }));
    const key = normalize(query);
    return entries
      .filter((e) => isSingleWord(e.value) && normalize(e.value).startsWith(key))
      .map((e) => ({
        entry: e,
        score: prefixScore(e.value, query),
      }))
      .sort((a, b) => {
        const [aAcc, aCase, aCed] = a.score;
        const [bAcc, bCase, bCed] = b.score;
        if (aAcc !== bAcc)
          return aAcc - bAcc;
        if (aCase !== bCase)
          return aCase - bCase;
        if (aCed !== bCed)
          return aCed - bCed;
        const aKey = normalize(a.entry.value);
        const bKey = normalize(b.entry.value);
        if (aKey < bKey)
          return -1;
        if (aKey > bKey)
          return 1;
        return a.entry.index - b.entry.index;
      });
  }
  function splitWords(str) {
    return str.split(/\s+/).filter((w) => w.length > 0);
  }
  function computeR2(list, query) {
    const entries = list.map((v, i) => ({ value: v, index: i }));
    const key = normalize(query);
    const r2 = [];
    for (const e of entries) {
      const words = splitWords(e.value);
      if (words.length < 2)
        continue;
      let pivotIdx = -1;
      for (let i = 0; i < words.length; i++) {
        const wNorm = normalize(words[i]);
        if (wNorm.startsWith(key)) {
          pivotIdx = i;
          break;
        }
      }
      if (pivotIdx === -1)
        continue;
      const pivot = words[pivotIdx];
      const pivotScore = prefixScore(pivot, query);
      r2.push({
        entry: e,
        pivotIndex: pivotIdx,
        pivotScore,
      });
    }
    r2.sort((a, b) => {
      const [aAcc, aCase, aCed] = a.pivotScore;
      const [bAcc, bCase, bCed] = b.pivotScore;
      if (aAcc !== bAcc)
        return aAcc - bAcc;
      if (aCase !== bCase)
        return aCase - bCase;
      if (aCed !== bCed)
        return aCed - bCed;
      if (a.pivotIndex !== b.pivotIndex)
        return a.pivotIndex - b.pivotIndex;
      const aKey = normalize(a.entry.value);
      const bKey = normalize(b.entry.value);
      if (aKey < bKey)
        return -1;
      if (aKey > bKey)
        return 1;
      return a.entry.index - b.entry.index;
    });
    return r2;
  }
  function localScoreAtPos(original, query, pos) {
    const sub = original.slice(pos, pos + query.length);
    return prefixScore(sub, query);
  }
  function computeR3(list, query, r1, r2) {
    const key = normalize(query);
    const r1Indices = new Set(r1.map((x) => x.entry.index));
    const r2Indices = new Set(r2.map((x) => x.entry.index));
    const entries = list.map((v, i) => ({ value: v, index: i }));
    const r3 = [];
    for (const e of entries) {
      if (r1Indices.has(e.index) || r2Indices.has(e.index))
        continue;
      const sNorm = normalize(e.value);
      const pos = sNorm.indexOf(key);
      if (pos === -1)
        continue;
      const localScore = localScoreAtPos(e.value, query, pos);
      r3.push({
        entry: e,
        pos,
        localScore,
      });
    }
    r3.sort((a, b) => {
      if (a.pos !== b.pos)
        return a.pos - b.pos;
      const [aAcc, aCase, aCed] = a.localScore;
      const [bAcc, bCase, bCed] = b.localScore;
      if (aAcc !== bAcc)
        return aAcc - bAcc;
      if (aCase !== bCase)
        return aCase - bCase;
      if (aCed !== bCed)
        return aCed - bCed;
      const aKey = normalize(a.entry.value);
      const bKey = normalize(b.entry.value);
      if (aKey < bKey)
        return -1;
      if (aKey > bKey)
        return 1;
      return a.entry.index - b.entry.index;
    });
    return r3;
  }
  function computeFullRanking(list, query) {
    const r1 = computeR1(list, query);
    const r2 = computeR2(list, query);
    const r3 = computeR3(list, query, r1, r2);
    const usedIndices = new Set();
    r1.forEach((x) => usedIndices.add(x.entry.index));
    r2.forEach((x) => usedIndices.add(x.entry.index));
    r3.forEach((x) => usedIndices.add(x.entry.index));
    const entries = list.map((v, i) => ({ value: v, index: i }));
    const rest = entries.filter((e) => !usedIndices.has(e.index));
    const finalOrder = [
      ...r1.map((x) => x.entry),
      ...r2.map((x) => x.entry),
      ...r3.map((x) => x.entry),
    ];
    return { R1: r1, R2: r2, R3: r3, finalOrder };
  }

  function getSignsList() {
    const list = [];
    $("#signs-container").children("div").each(function () {
      list.push($(this).attr("id"));
    });
    return list;
  }



  function updateVideosCount(count) {
    const el = document.getElementById("videos-count");
    const VIDEO_SINGULAR = el.dataset.singular;
    const VIDEO_PLURAL = el.dataset.plural;
    el.textContent = count + " " + (count > 1 ? VIDEO_PLURAL : VIDEO_SINGULAR);
    el.style.visibility = "visible";
  }

  // -------- FILTRAGE PAR CATEGORIES ----------
  $(document).on("click", ".filter-btns .filter", function (e) {
    e.preventDefault();
    var display = 0;
    // Active / désactive visuellement
    $(this).toggleClass("btn-active");

    // Récupère les filtres actifs
    var activeFilters = [];
    $(".filter-btns .btn-active").each(function () {
      var raw = $(this).data("filter") || $(this).text();
      raw = String(raw).trim();
      if (raw.length) activeFilters.push(raw);
    });

    console.log("Filtres actifs :", activeFilters);

    // Cas 1 : aucun filtre => afficher toutes les tuiles
    if (activeFilters.length === 0) {
      activeFilter = false;
      $("#videos-container").children("label").each(function () {
        if ($(this).hasClass(VIDEO_HIDDEN_CLASS)) {
          showVideoView(this);
        } else {
          $(this).show();
        }
      });
      document.getElementById("videos-count").style.visibility = "hidden";
      return;
    } else {
      activeFilter = true;
    }

    // Cas 2 : filtrage "ET"
    $("#videos-container").children("label").each(function () {

      // classes de la tuile → normalisées
      var tokens = String(this.className)
        .split(/\s+/)
        .map(function (c) { return c; });

      var matchAll = activeFilters.every(function (f) {
        return tokens.indexOf(f) !== -1;
      });

      if (matchAll) {
        if ($(this).hasClass(VIDEO_HIDDEN_CLASS)) {
          showVideoView(this); // nécessaire pour lazy-load
        } else {
          $(this).show();
        }
        display++;
      } else {
        $(this).hide();
      }
    });
    if (activeFilters) {
      console.log("display ",display);
      updateVideosCount(display);
    }
  });

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
    if ((search_criteria.value == "") && (!activeFilter)) {
      if (!noMoreHiddenVideos && closeToBottom) {
        showNextVideoViews();
      }
    }

  }

  function search(event) {
    let display = 0;
    const query = $(this).val();

    if (query !== "") {
      $("#reset").css("visibility", "visible");

      // 🔥 1. Construire la liste des labels
      const labels = $("#videos-container").children("label");

      const list = [];
      labels.each(function () {
        list.push($(this).attr("id")); // texte à matcher
      });

      // 🔥 2. Appliquer TON algo
      const result = computeFullRanking(list, query);

      // 🔥 3. Masquer tout
      labels.each(function () {
        $(this).hide();
      });

      // 🔥 4. Afficher dans le bon ordre
      result.finalOrder.forEach((entry) => {
        const label = labels.eq(entry.index);
        const img = label.find("img")[0];

        // lazy load si nécessaire
        if (label.hasClass(VIDEO_HIDDEN_CLASS)) {
          label.removeClass(VIDEO_HIDDEN_CLASS);
          if (img && img.dataset.src) {
            img.src = img.dataset.src;
          }
          displayedVideosCount++;
        }

        label.show();
        display++;
      });

      // 🔥 5. Gestion "aucun résultat"
      if (display === 0) {
        $(signNotAvailable).show();
      } else {
        $(signNotAvailable).hide();
      }

    } else {
      // 🔥 RESET IDENTIQUE au backspace (IMPORTANT)

      $("#reset").css("visibility", "hidden");

      $("#videos-container").children("label").each(function () {
        if (!$(this).hasClass(VIDEO_HIDDEN_CLASS)) {
          $(this).addClass(VIDEO_HIDDEN_CLASS);
          $(this).hide();
        }
      });

      displayedVideosCount = 0;
      $(signNotAvailable).hide();

      initWithFirstVideos(); // 🔥 clé du comportement correct
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

    // vider input
    search_criteria.value = "";

    // 🔥 simuler un search vide
    search.call(search_criteria);
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

  // --- Quand on clique sur "catégories"
  $('#mode-category').on('click', function() {
    if (!$(this).hasClass('active')) {
      // Changement d’icônes
      $('#mode-word .icon').removeClass('search-ABC_blue-background').addClass('search-ABC_black');
      $('#mode-category .icon').removeClass('search-sticker').addClass('search-sticker_blue-background');

      // Texte
      $('#label-word').hide();
      $('#label-category').fadeIn(200);

      // Zones de contenu
      $('#search-by-word').hide();
      $('#search-by-category').slideDown();

      // États actifs
      $('#mode-word').removeClass('active');
      $(this).addClass('active')
      search_criteria.value = "";
      onReset();
      $('#dropdown-filter').hide();
    }
  });

  // --- Quand on clique sur "mots"
  $('#mode-word').on('click', function() {
    if (!$(this).hasClass('active')) {
      $('#mode-category .icon').removeClass('search-sticker_blue-background').addClass('search-sticker');
      $('#mode-word .icon').removeClass('search-ABC_black').addClass('search-ABC_blue-background');

      $('#label-category').hide();
      $('#label-word').fadeIn(200);

      $('#search-by-category').hide();
      $('#search-by-word').slideDown();

      $('#mode-category').removeClass('active');
      $(this).addClass('active');
      $('#dropdown-filter').slideDown();
    }
    $("#category-list").children("button").each(function () {
      if ($(this).hasClass("btn-active")) {
        $(this).removeClass("btn-active");
      }
    });
    activeFilter = false;
    onReset();
    document.getElementById("videos-count").style.visibility = "hidden";
  });

  // --- Voir plus / Voir moins dans les catégories ---
  let expanded = false;
  $('#toggle-categories').on('click', function(e) {
    e.preventDefault();
    expanded = !expanded;
    if (expanded) {
      $('#category-list').css('max-height', 'none');
      $(this).html('Voir moins <span class="glyphicon glyphicon-triangle-top charte_color"></span>');
    } else {
      $('#category-list').css('max-height', '70px');
      $(this).html('Voir plus <span class="glyphicon glyphicon-triangle-bottom charte_color"></span>');
    }
  });
})($);
