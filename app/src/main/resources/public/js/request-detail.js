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
console.log("Cool, request-detail.js is loaded :)");


function onPrioriseRequest(id){
  $.ajax({
    url: "/ws/sec/request/"+ id +"/priorise",
    type: 'post',
    success: function(response) {
      $("#priorise_request").modal('show');
      setTimeout(function(){
        $('#priorise_request').modal('hide');
        window.location = "/sec/my-requests/mostrecent?isMostRecent=false&isSearch=false";
      }, 3000);

    },
    error: function(response) {
      }
  })

};

function onDeleteRequest(id){
  $.ajax({
    url: "/ws/sec/request/"+ id +"/delete",
    type: 'post',
    success: function(response) {
      $('#delete_request').modal('hide');
      $("#confirm_delete").modal('show');
      setTimeout(function(){
        $('#confirm_delete').modal('hide');
        window.location = "/sec/my-requests/mostrecent?isMostRecent=false&isSearch=false";
      }, 3000);

    },
    error: function(response) {
      }
  })

};

function onContinue() {
  var url = "/sec/requests/";
  window.location = url;
};

