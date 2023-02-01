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

console.log("Cool, request-admin.js is loaded :)");


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

function onPrioriseRequest(id){
  $.ajax({
    url: "/ws/sec/request/"+ id +"/priorise",
    type: 'post',
    success: function(response) {
      $("#priorise_request").modal('show');
      setTimeout(function(){
        $('#priorise_request').modal('hide');
        window.location = "/sec/admin/requests";
      }, 3000);
    },
    error: function(response) {
    }
  })

};



function main() {

}

(function displayRequest($) {
  main();

})($);

