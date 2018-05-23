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
console.log("Cool, rate.js is loaded :)");


function onRatePositif(signId, videoId){
  $.ajax({
    url: "/ws/sec/sign/"+ signId +"/"+ videoId + "/rate-positive",
    type: 'post',
    success: function(response) {
       // $("#after-rate-sign-positive").modal('show');
      $("#after-rate-sign").modal('show');
      $("#sentiment-after-rate").addClass("containerRight_signes sentiment_positif")
      $('#positif').css('display', 'block');
    },
    error: function(response) {
      }
  })

};

// kanban 473325 suite retour test utilisateurs
function onRateNeutre(signId, videoId){
  $.ajax({
    url: "/ws/sec/sign/"+ signId +"/"+ videoId + "/rate-neutral",
    type: 'post',
    success: function(response) {
      $("#after-rate-sign").modal('show');
      $("#sentiment-after-rate").addClass("containerRight_signes sentiment_neutre")
    },
    error: function(response) {
    }
  })

};

function onRateNegatif(signId, videoId){
  $.ajax({
    url: "/ws/sec/sign/"+ signId +"/"+ videoId + "/rate-negative",
    type: 'post',
    success: function(response) {
      $("#after-rate-sign").modal('show');
      $("#sentiment-after-rate").addClass("containerRight_signes sentiment_negatif")
      $('#negatif').css('display', 'block');
    },
    error: function(response) {
    }
  })

};


function onContinueAfterRate(signId, videoId) {
  var url = "/sign/" + signId +"/" + videoId;
  window.location = url;
};

