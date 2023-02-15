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

console.log("Cool, requests-admin.js is loaded :)");

var requestsContainer = document.getElementById("requests_container");
if (requestsContainer != null) {
  var requests = requestsContainer.getElementsByClassName("request_name");
  var requestsCount =  requests.length;
}
var search_criteria = document.getElementById("search-criteria");
var button_reset = document.getElementById("reset");

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


function onSearch(){
  $("#search-criteria").show();
  $(nb).hide();
  var search_criteria = document.getElementById("search-criteria");
  search_criteria.classList.remove("search-hidden");
  $("#requests_container").children("div").each(function () {
    $(this).hide();
  });
}

function search(event) {
  var display = 0;
  var g = normalize($(this).val());

  if (g!="") {
    $("#requests_container").children("div").each(function () {
      $("#reset").css("visibility", "visible");
      var requestName = $(this).attr("id");
      if (requestName != null) {
        var s = normalize(requestName);
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
    $("#reset").css("visibility", "hidden");
    $("#requests_container").children("div").each(function () {
      $(this).show();
    });
    nb.innerHTML = '('+requestsCount+')';
    $(nb).show();
  }
}


function onReset(event) {
  $(':input', '#myform')
    .not(':button, :submit, :reset, :hidden')
    .val('');
  $("#reset").css("visibility", "hidden");

  $("#requests_container").children("div").each(function () {
    $(this).show();
  });
  nb.innerHTML = '('+requestsCount+')';
  $(nb).show();

}

function onDeleteRequest(id){
  var errorDeleteRequest = document.getElementById('errorDeleteRequest'+id);

  $.ajax({
    url: "/ws/admin/requests/"+ id,
    type: 'delete',
    success: function(response) {
      $("#delete_request"+id).modal('hide');
      $("#confirm-request-deleted"+id).modal('show');
      setTimeout(function(){
        $("#confirm-request-deleted"+id).modal('hide');
        errorDeleteRequest.style.visibility="hidden";
        var url = "/sec/admin/requests";
        console.log(window.location.href);
        window.history.replaceState({}, 'foo', url);
        console.log(window.location.href);
        window.location = url;
      }, 3000);
    },
    error: function(response) {
      console.log(response.responseJSON);
      errorDeleteRequest.textContent = response.responseJSON.errorMessage;
      errorDeleteRequest.style.visibility = "visible";
    }
  })
}

function main() {
    $("#requests_container").children("div").each(function () {
      $(this).show();
    });
    search_criteria.addEventListener('keyup', search);
    button_reset.addEventListener('click', onReset);
}

(function displayRequest($) {
  main();

})($);

