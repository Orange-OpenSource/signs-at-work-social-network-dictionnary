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
console.log("Cool, app.js is loaded :)");

var $form = $('#requestInfo');
var $span = $('#requestSpan');
$form.on('submit', function(event) {
    event.preventDefault();
    $.ajax({
       url: $form.attr('action'),
       type: 'post',
       data: $form.serialize(),
       success: function(response) {
           // if the response contains any errors, replace the form
          if ($(response).find('.has-error').length) {
              var $response_span = $(response).find('#requestSpan');
              $span.replaceWith($response_span);
              $form.addClass('has-error');
          }
          else {
              var url = "/sec/request/";
              window.location = url;
                    // in this case we can actually replace the form
                    // with the response as well, unless we want to
                    // show the success message a different way
             }
          }
})});

var $new_request = $('#new_request');
$new_request.on('hidden.bs.modal', function() {
   console.log("close modal");
    if ($('#requestInfo').find('#requestSpan').length) {
        var url = "/sec/request/";
        window.location = url;
    }
});
