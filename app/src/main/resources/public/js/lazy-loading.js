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


var search_criteria = document.getElementById("search-criteria");
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

var dropdownFilter = document.getElementById("dropdown-filter");

let savedDropdownState = {};
let savedDropdownMenu = "";
let initialSigns = []; // Array of IDs or elements
let savedDropdownHtml = "";

$(document).ready(function () {
  const signsContainer = document.getElementById("signs-container");
  if (signsContainer) {
    $('#signs-container').children('div').each(function() {
      initialSigns.push(this.cloneNode(true)); // ⚠️ on stocke des clones
    });
  }

  const dropdown = document.getElementById("dropdown-filter");
  if (dropdown) {
    savedDropdownHtml = dropdown.outerHTML;
  }

  const button = document.getElementById("dropdownMenu1");
  if (button) {
    savedDropdownState = {
      buttonClass: button.className,
      titleIconClass: document.getElementById("dropdownTitleIcon").className,
      directionClass: document.getElementById("dropdownDirection").className,
      titleText: document.getElementById("dropdownTitleText").textContent
    };
  }

  const menu = document.getElementById("dropdownMenuContent");
  if (menu) {
    savedDropdownMenu = menu.innerHTML;
  }
});

function restoreDropdownState() {

  let dropdown = document.getElementById("dropdown-filter");

  // 🔴 Si le dropdown a été supprimé, on le recrée
  if (!dropdown && savedDropdownHtml) {
    const container = document.getElementById("search-by-word");
    // ou le parent réel dans ton HTML

    container.insertAdjacentHTML('beforeend', savedDropdownHtml);
    dropdown = document.getElementById("dropdown-filter");
  }

  if (!dropdown) return;

  // 🔴 restaurer l'état interne
  const button = document.getElementById("dropdownMenu1");
  const icon = document.getElementById("dropdownTitleIcon");
  const direction = document.getElementById("dropdownDirection");
  const text = document.getElementById("dropdownTitleText");

  if (!button || !icon || !direction || !text) return;

  button.className = savedDropdownState.buttonClass;
  icon.className = savedDropdownState.titleIconClass;
  direction.className = savedDropdownState.directionClass;
  text.textContent = savedDropdownState.titleText;
}

function restoreDropdownMenu() {

  let menu = document.getElementById("dropdownMenuContent");

  if (!menu && savedDropdownHtml) {
    restoreDropdownState(); // recrée le dropdown
    menu = document.getElementById("dropdownMenuContent");
  }

  if (!menu) return;

  menu.innerHTML = savedDropdownMenu;
}


function restoreSignsContainer() {

  // 🔴 Si on est en mode vidéo, recréer le container signes
  if (!signsContainer) {
    const frame = document.getElementById("frame-signs");
    frame.innerHTML = '<div id="signs-container"></div>';
    signsContainer = document.getElementById("signs-container");
  }

  const $container = $('#signs-container');
  $container.empty();

  // 🔴 Réinjecter les signes d'origine
  initialSigns.forEach(function(div){
    const clone = div.cloneNode(true);
    $(clone)
      .addClass(SIGN_HIDDEN_CLASS)
      .css({ display: 'none', float: 'left' });

    $container.append(clone);
  });

  // 🔴 Réinitialiser l'état lazy loading
  displayedSignsCount = 0;
  signViewsHidden = signsContainer.getElementsByClassName(SIGN_HIDDEN_CLASS);

  // 🔴 Mode normal : afficher les premières vignettes
  if (modeSearch === "false") {
    signsCount = initialSigns.length;
    showNextSignViews();
    updateSignesCount(signsCount);
    nb.innerHTML = `(${signsCount})`;
  } else {
    // 🔹 Mode recherche : ne rien afficher
    $('#signes-count').css('visibility', 'hidden');
  }
}


function updateSignesCount(count) {
  const el = document.getElementById("signes-count");
  const SIGN_SINGULAR = el.dataset.singular;
  const SIGN_PLURAL = el.dataset.plural;
  el.textContent = count + " " + (count > 1 ? SIGN_PLURAL : SIGN_SINGULAR);
  el.style.visibility = "visible";
}

// -------- FILTRAGE PAR CATEGORIES ----------
$(document).on("click", ".filter-btns .filter", function (e) {
  e.preventDefault();

  // Active / désactive visuellement
  $(this).toggleClass("btn-active");

  applyCategoryFilterFromState();
});

function applyCategoryFilterFromState() {

  var display = 0;

  // Récupère les filtres actifs depuis le DOM
  var activeFilters = [];
  $(".filter-btns .btn-active").each(function () {
    var raw = $(this).data("filter") || $(this).text();
    raw = String(raw).trim();
    if (raw.length) activeFilters.push(raw);
  });

  console.log("Filtres actifs :", activeFilters);

  // Cas 1 : aucun filtre
  if (activeFilters.length === 0) {
    activeFilter = false;

    if (modeSearch === "false") {
      $("#signs-container").children("div").each(function () {
        if ($(this).hasClass(SIGN_HIDDEN_CLASS)) {
          showSignView(this);
        } else {
          $(this).show();
        }
      });
      updateSignesCount(signsCount);
    } else {
      $("#signs-container").children("div").each(function () {
        if (!$(this).hasClass(SIGN_HIDDEN_CLASS)) {
          $(this).addClass(SIGN_HIDDEN_CLASS);
          $(this).hide();
        }
      });
      document.getElementById("signes-count").style.visibility = "hidden";
    }
    return;
  } else {
    activeFilter = true;
  }

  // Cas 2 : filtrage "ET"
  $("#signs-container").children("div").each(function () {

    var tokens = String(this.className)
      .split(/\s+/)
      .map(function (c) { return c; });

    var matchAll = activeFilters.every(function (f) {
      return tokens.indexOf(f) !== -1;
    });

    if (matchAll) {
      if ($(this).hasClass(SIGN_HIDDEN_CLASS)) {
        showSignView(this);
      } else {
        $(this).show();
      }
      display++;
    } else {
      $(this).hide();
    }
  });

  console.log("display ", display);
  updateSignesCount(display);
}

$(document).on("click", ".sign-link", function (e) {
  console.log("CLICK SIGN LINK", this.href);
  const baseUrl = this.href;

  // -----------------------------
  // récupérer la recherche texte
  // -----------------------------
  const query = search_criteria ? search_criteria.value.trim() : "";

  // -----------------------------
  // récupérer les tags actifs
  // -----------------------------
  const tags = [];
  $(".filter-btns .btn-active").each(function () {
    tags.push($(this).data("filter"));
  });

  // -----------------------------
  // construire les paramètres
  // -----------------------------
  const params = new URLSearchParams();

  if (modeSearch !== null) {
    params.set("isSearch", modeSearch);
  }

  if (query.length > 0) {
    params.set("q", query);
  }

  if (tags.length > 0) {
    params.set("tags", tags.join(","));
  }

  // -----------------------------
  // redirection finale
  // -----------------------------
  const finalUrl = params.toString()
    ? baseUrl + "?" + params.toString()
    : baseUrl;

  window.location.href = finalUrl;
  console.log("finalUrl ", finalUrl);

  e.preventDefault();
});

var nb = document.getElementById("nb");

if (!String.prototype.startsWith) {
  String.prototype.startsWith = function(searchString, position){
    position = position || 0;
    return this.substr(position, searchString.length) === searchString;
  };
}

function showSignView(signView) {
  $(signView).removeClass(SIGN_HIDDEN_CLASS);

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

      if ((search_criteria.value == "") && (!activeFilter)) {
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
  displayedVideosCount = 0;
  $(addNewSuggestRequest).hide();

  const query = $(this).val().trim();

  // -----------------------------
  // 🔴 CAS 1 : RESET (champ vide)
  // -----------------------------
  if (query === "") {

    $("#reset").css("visibility", "hidden");

    if (signsContainer != null) {
      $(signAvailable).hide();
      restoreSignsContainer();
    } else {
      $(videoAvailable).hide();

      $("#videos-container").children("div").each(function () {
        $(this).css({ 'float': 'left' });

        if (!$(this).hasClass(VIDEO_HIDDEN_CLASS)) {
          $(this).addClass(VIDEO_HIDDEN_CLASS);
          $(this).hide();
        }
      });

      displayedVideosCount = 0;

      if (modeSearch === "false") {
        initWithFirstVideos();
      } else {
        $(nb).hide();
      }
    }

    return;
  }

  $("#reset").css("visibility", "visible");

  // =====================================================
  // 🔍 CAS 2 : SIGNES
  // =====================================================
  if (signsContainer != null) {

    const list = [];

    $("#signs-container").children("div").each(function () {
      list.push($(this).attr("id"));
    });

    const result = computeFullRanking(list, query);

    const finalOrder = [
      ...result.R1.map(x => x.entry),
      ...result.R2.map(x => x.entry),
      ...result.R3.map(x => x.entry)
    ];

    const $container = $("#signs-container");

    $container.children("div").each(function () {
      $(this).hide();
      $(this).css({ 'float': 'left' });
    });

    finalOrder.forEach(entry => {

      const el = document.getElementById(entry.value);
      if (!el) return;

      if ($(el).hasClass(SIGN_HIDDEN_CLASS)) {
        showSignView(el);
      } else {
        $(el).show();
      }

      $container.append(el);
      display++;
    });

    nb.innerHTML = "(" + display + ")";
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
            $(this).css({ 'margin': 'auto', 'float': 'none' });
          }
        });
      }
    }
  }

    // =====================================================
    // 🎬 CAS 3 : VIDEOS (🔥 version améliorée)
  // =====================================================
  else {

    const list = [];

    $("#videos-container").children("div").each(function () {
      list.push($(this).attr("id"));
    });

    const result = computeFullRanking(list, query);

    const finalOrder = [
      ...result.R1.map(x => x.entry),
      ...result.R2.map(x => x.entry),
      ...result.R3.map(x => x.entry)
    ];

    const $container = $("#videos-container");

    // cacher tout
    $container.children("div").each(function () {
      $(this).hide();
      $(this).css({ 'float': 'left' });
    });

    // afficher + lazy load
    finalOrder.forEach(entry => {

      const el = document.getElementById(entry.value);
      if (!el) return;

      if ($(el).hasClass(VIDEO_HIDDEN_CLASS)) {
        showVideoView(el);
      } else {
        $(el).show();
      }

      $container.append(el);
      display++;
    });

    nb.innerHTML = "(" + display + ")";
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
            $(this).css({ 'margin': 'auto', 'float': 'none' });
          }
        });
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
  updateSignesCount(signsCount);
  $(nb).show();
  do {
    showNextSignViews();
  } while(!scrollBarVisible() && displayedSignsCount != signsCount);

}

function initWithFirstVideos() {
  nb.innerHTML = "("+videosCount+")";
  updateSignesCount(videosCount);
  $(nb).show();
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

function switchToHorizontalLayout() {
  $('#vertical-display').hide();
  $('#horizontal-display').show();
  $('#page-content').show();
}

function activateWordMode() {

  $('#mode-word').addClass("active");
  $('#mode-category').removeClass('active');

  $('#mode-category .icon')
    .removeClass('search-sticker_blue-background')
    .addClass('search-sticker');

  $('#mode-word .icon')
    .removeClass('search-ABC_black')
    .addClass('search-ABC_blue-background');

  $('#label-category').hide();
  $('#label-word').show();

  $('#search-by-category').hide();
  $('#search-by-word').show();

  $('#dropdown-filter').show();
}

function activateCategoryMode() {

  $('#mode-category').addClass("active");
  $('#mode-word').removeClass('active');

  $('#mode-word .icon')
    .removeClass('search-ABC_blue-background')
    .addClass('search-ABC_black');

  $('#mode-category .icon')
    .removeClass('search-sticker')
    .addClass('search-sticker_blue-background');

  $('#label-word').hide();
  $('#label-category').show();

  $('#search-by-word').hide();
  $('#search-by-category').show();

  $('#dropdown-filter').hide();
}

function restoreSelectedTags(tagIds) {

  const normalized = tagIds.map(id => String(id));

  $(".filter-btns .filter").each(function () {

    const buttonId = String($(this).attr("data-filter")).trim();

    if (normalized.includes(buttonId)) {
      $(this).addClass("btn-active");
    }
  });

  // 🔥 appliquer le filtrage sans simuler un clic
  applyCategoryFilterFromState();
}

function restoreSearchStateFromUrl() {

  const params = new URLSearchParams(window.location.search);

  const isSearch = params.get("isSearch");
  const query = params.get("q");
  const tags = params.get("tags");

  console.log("restoreSearchState", { isSearch, query, tags });

  // -----------------------------
  // CAS 1 : recherche texte
  // -----------------------------
  if (query && query.length > 0) {

    // si on était sur l’écran vertical, on passe en horizontal
    if (isSearch === "true") {
      switchToHorizontalLayout();
      activateWordMode();
    }

    search_criteria.value = query;

    setTimeout(function () {
      searchSignAfterReload(query);
    }, 0);

    return;
  }

  // -----------------------------
  // CAS 2 : recherche par tags
  // -----------------------------
  if (tags && tags.length > 0) {

    switchToHorizontalLayout();
    activateCategoryMode();

    const tagList = tags.split(",").map(t => t.trim());
    restoreSelectedTags(tagList);

    return;
  }

  // -----------------------------
  // CAS 3 : isSearch=true sans filtre
  // -----------------------------
  if (isSearch === "true") {
    // rester en vertical
    return;
  }

  // -----------------------------
  // CAS 4 : liste normale
  // -----------------------------
  // rien à faire
}

let scrollInitialized = false;

function main() {
  console.log("main "+window.location.href+" "+window.location.search);

  const params = new URLSearchParams(window.location.search);
  modeSearch = params.get("isSearch");

  // ⚠️ éviter d'ajouter plusieurs listeners
  if (!scrollInitialized) {
    document.addEventListener('scroll', onScroll);
    scrollInitialized = true;
  }

  search_criteria.addEventListener('keyup', search);

  var button_reset = document.getElementById("reset");
  if (button_reset != null) {
    button_reset.addEventListener('click', onReset);
  }

  if (modeSearch === "false") {
    if (signsContainer != null) {
      initWithFirstSigns();
    } else if (videosContainer != null) {
      initWithFirstVideos();
    }
  } else {
    $(nb).hide();
  }

}


function onFiltreSign(event, href, isModeCategorie) {
  console.log("onFiltre");
  event.preventDefault();
  console.log("href "+href);
  $.ajax({
    url: href,
    context: document.body,
    success: function (response) {
      console.log("Success ");

      const newFrame = $(response);
      $('#frame-signs').replaceWith(newFrame);

      signsContainer = document.getElementById("signs-container");
      signViewsHidden = signsContainer.getElementsByClassName(SIGN_HIDDEN_CLASS);
      signsCount = $("#signs-container").children("div").length;
      displayedSignsCount = 0;
      videosContainer = null;
      addNewSuggestRequest = document.getElementById("add-new-suggest-request");
      nb = document.getElementById("nb");
      signAvailable = document.getElementById("sign_available");
      dropdownFilter = document.getElementById("dropdown-filter");
      if (isModeCategorie) {
        $('#dropdown-filter').hide();
      }
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

      const newFrame = $(response);
      $('#frame-signs').replaceWith(newFrame);

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
  document.getElementById("videoUrl").src = url;
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
  document.getElementById("videoUrl").src = url;
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
      document.getElementById("videoUrl").src = data[indice].url;
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
            document.getElementById("videoUrl").src = data[indice].url;
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
window.addEventListener("scroll", scrollFunction);

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
  restoreSearchStateFromUrl();
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


  $(function() {

    // --- Quand on clique sur "catégories"
    $('#search-mode-category').on('click', function() {
      $('#vertical-display').hide();
      $('#horizontal-display').show();
      $('#mode-category').addClass("active");
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
        /*restoreInitialOrder("#signs-container", initialSigns);*/
        restoreSignsContainer();
        /*if (modeSearch === "false") {
          updateSignesCount(signsCount);
          nb.innerHTML = "("+signsCount+")";
        }*/
        $('#dropdown-filter').hide();
      $('#page-content').show();
    });


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
      activeFilter = false;
      /*restoreInitialOrder("#signs-container", initialSigns);*/
      restoreSignsContainer();
      /*if (modeSearch === "false") {
        updateSignesCount(signsCount);
      } else {
        document.getElementById("signes-count").style.visibility = "hidden";
      }*/
      $('#dropdown-filter').hide();
    }
  });

    $('#search-mode-word').on('click', function() {
      $('#vertical-display').hide();
      $('#horizontal-display').show();
      $('#mode-word').addClass("active");
      $('#mode-category .icon').removeClass('search-sticker_blue-background').addClass('search-sticker');
      $('#mode-word .icon').removeClass('search-ABC_black').addClass('search-ABC_blue-background');

      $('#label-category').hide();
      $('#label-word').fadeIn(200);

      $('#search-by-category').hide();
      $('#search-by-word').slideDown();

      $('#mode-category').removeClass('active');
      $(this).addClass('active');
      $('#dropdown-filter').slideDown();

      $("#category-list").children("button").each(function () {
        if ($(this).hasClass("btn-active")) {
          $(this).removeClass("btn-active");
        }
      });

      /*restoreInitialOrder("#signs-container", initialSigns);*/
      restoreSignsContainer();
      /*if (modeSearch === "false") {
        updateSignesCount(signsCount);
        nb.innerHTML = "("+signsCount+")";
      }*/
      $('#page-content').show();
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

    /*restoreInitialOrder("#signs-container", initialSigns);*/
    activeFilter = false;
    restoreSignsContainer();
    restoreDropdownState();
    restoreDropdownMenu();
    $('.dropdown-toggle').dropdown();
    /*if (modeSearch === "false") {
      nb.innerHTML = "("+signsCount+")";
    } else {
      $(nb).hide();
    }*/
});

  // --- Voir plus / Voir moins dans les catégories ---
  let expanded = false;
  const $toggle = $('#toggle-categories');
  const seeMore = $toggle.data('see-more');
  const seeLess = $toggle.data('see-less');

    $('#toggle-categories').on('click', function(e) {
      e.preventDefault();
      expanded = !expanded;
      if (expanded) {
        $('#category-list').css('max-height', 'none');
        $(this).html(seeLess + ' <span class="glyphicon glyphicon-triangle-top charte_color"></span>');
      } else {
        $('#category-list').css('max-height', '70px');
        $(this).html(seeMore + ' <span class="glyphicon glyphicon-triangle-bottom charte_color"></span>');
      }
    });


    $('#search-toggle-categories').on('click', function(e) {
      e.preventDefault();
      expanded = !expanded;
      if (expanded) {
        $('#search-category-list').css('max-height', 'none');
        $(this).html(seeLess + ' <span class="glyphicon glyphicon-triangle-top charte_color"></span>');
      } else {
        $('#search-category-list').css('max-height', '70px');
        $(this).html(seeMore + ' <span class="glyphicon glyphicon-triangle-bottom charte_color"></span>');
      }
    });

    $('#search-container-input input').on('input', function() {

      const value = $(this).val();

      if (value.length > 0) {

        $('#vertical-display').hide();
        $('#horizontal-display').show();

        // Mode WORD actif
        $('#mode-word').addClass("active");
        $('#mode-category').removeClass('active');

        // Icônes
        $('#mode-category .icon').removeClass('search-sticker_blue-background').addClass('search-sticker');
        $('#mode-word .icon').removeClass('search-ABC_black').addClass('search-ABC_blue-background');

        // Labels
        $('#label-category').hide();
        $('#label-word').show();

        // Sections
        $('#search-by-category').hide();
        $('#search-by-word').show();

        // Copier la valeur
        $('#search-criteria').val(value).focus();

        $('#page-content').show();

        // Reset catégories
        $("#category-list").children("button").removeClass("btn-active");

        restoreSignsContainer();

        /*if (modeSearch === "false") {
          updateSignesCount(signsCount);
          nb.innerHTML = "(" + signsCount + ")";
        }*/
      }
    });

    $(document).on('click', '#search-category-list button', function() {

      console.log("CLICK OK");

      const filterValue = $(this).data('filter');

      // Basculer affichage
      $('#vertical-display').hide();
      $('#horizontal-display').show();
      $('#page-content').show();

      // Mode CATEGORY actif
      $('#mode-category').addClass("active");
      $('#mode-word').removeClass('active');

      // Icônes
      $('#mode-word .icon').removeClass('search-ABC_blue-background').addClass('search-ABC_black');
      $('#mode-category .icon').removeClass('search-sticker').addClass('search-sticker_blue-background');

      // Labels
      $('#label-word').hide();
      $('#label-category').show();

      // Sections
      $('#search-by-word').hide();
      $('#search-by-category').show();


      // Reset input
      $('#search-criteria').val('');

      restoreSignsContainer();

      if (modeSearch === "false") {
        updateSignesCount(signsCount);
      } else {
        $('#signes-count').css('visibility', 'hidden');
      }

      $('#dropdown-filter').hide();

      // Synchroniser sélection + déclencher filtre
      const $buttons = $('#category-list').children('button');

      const $targetBtn = $buttons.filter(function() {
        return $(this).data('filter') == filterValue;
      });

      $buttons.removeClass('btn-active'); // reset
      $targetBtn.trigger('click');        // 🔥 clé du problème

    });

});







