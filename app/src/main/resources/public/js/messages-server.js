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

var messagesServerContainer = document.getElementById("messagesServer_container");
if (messagesServerContainer != null) {
  var messagesServer = messagesServerContainer.getElementsByClassName("text_hyphenation");
  var messagesServerCount =  messagesServer.length;
}
var search_criteria = document.getElementById("search-criteria");
var button_reset = document.getElementById("reset_search_messages_server");

var accentMap = {
  "é": "e",
  "è": "e",
  "ê": "e",
  "à": "a",
  "â": "a",
  "î": "i",
  "ô": "o",
  "ù": "u",
  "û": "u",
  "î": "i",
  "ç": "c",
  "œ" : "oe",
  "æ" : "ae"
};

var normalize = function( term ) {
  var ret = "";
  for ( var i = 0; i < term.length; i++ ) {
    ret += accentMap[ term.charAt(i) ] || term.charAt(i);
  }
  return ret;
};

function onFiltreMessagesServer(event, href) {
  console.log("onFiltreMessagesServer");
  event.preventDefault();
  console.log("href "+href);
  $.ajax({
    url: href,
    context: document.body,
    success: function (response) {
      console.log("Success ");
      document.getElementById("frame-messages-server").innerHTML = response;
      messagesServerContainer = document.getElementById("messagesServer_container");
      messagesServer = messagesServerContainer.getElementsByClassName("text_hyphenation");
      messagesServerCount =  messagesServer.length;
      console.log("messagesServerCount after filter "+messagesServerCount);
      if (messagesServerCount != 0) {
        if (search_criteria != null) {
          if (search_criteria.value != "") {
            searchAfterReload(search_criteria.value);
          } else {
            main();
          }
        }
      }
    },
    error: function (response) {
      console.log("Erreur ");
    }
  })
}

function onSearch(){
  $("#search-criteria").show();
  $(nb).hide();
  var search_criteria = document.getElementById("search-criteria");
  search_criteria.classList.remove("search-hidden");
  $("#messagesServer_container").children("div").each(function () {
    $(this).hide();
  });
}

function search(event) {
  var display = 0;
  var g = normalize($(this).val());

  if (g!="") {
    $("#messagesServer_container").children("div").each(function () {
      $("#reset_search_messages_server").css("visibility", "visible");
      var text = $(this).attr("id");
      if (text != null) {
        var s = normalize(text);
        if (s.toUpperCase().indexOf(g.toUpperCase()) != -1) {
          $(this).show();
          display++;
        }
        else {
          $(this).hide();
        }
      }
    });
    nb.innerHTML = '('+display+')';
    $(nb).show();
  } else {
    $("#reset_search_messages_server").css("visibility", "hidden");
    $("#messagesServer_container").children("div").each(function () {
      $(this).show();
    });
    nb.innerHTML = '('+messagesServerCount+')';
    $(nb).show();
  }
}

function searchAfterReload(search_value) {
  var display = 0;
  var g = normalize(search_value);

  if (g!="") {
    $("#messagesServer_container").children("div").each(function () {
      $("#reset_search_messages_server").css("visibility", "visible");
      var text = $(this).attr("id");
      if (text != "") {
        var s = normalize(text);
        if (s.toUpperCase().indexOf(g.toUpperCase()) != -1) {
          $(this).show();
          display++;
        }
        else {
          $(this).hide();
        }
      }
    });
    nb.innerHTML = '('+display+')';
    $(nb).show();
  } else {
    $("#reset_search_messages_server").css("visibility", "hidden");
    $("#messagesServer_container").children("div").each(function () {
      $(this).show();
    });
    nb.innerHTML = '('+messagesServerCount+')';
    $(nb).show();
  }
}

function onReset(event) {

  $(':input', '#myform')
    .not(':button, :submit, :reset, :hidden')
    .val('');
  $("#reset_search_messages_server").css("visibility", "hidden");

  $("#messagesServer_container").children("div").each(function () {
    $(this).show();
  });
  nb.innerHTML = '('+messagesServerCount+')';
  $(nb).show();

}

function main() {
 /* if ($(search_criteria).hasClass("search-hidden")) {*/
    $("#messagesServer_container").children("div").each(function () {
      $(this).show();
    });
/*  } else {
    $("#messagesServer_container").children("div").each(function () {
      $(this).hide();
      $(nb).hide();
    });
  }*/
  search_criteria.addEventListener('keyup', search);
  button_reset.addEventListener('click', onReset);
}

(function displayMessageServer($) {
  main();

})($);

