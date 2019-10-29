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

$(function() {
  $('#logo-password').click(function () {
    console.log("click on show password");
    if ($('#logo-password').hasClass("see_password")) {
      $('#logo-password').removeClass("see_password");
      $('#logo-password').addClass("not_see_password");
      $("#password").attr("type", "text");
    } else if ($('#logo-password').hasClass("not_see_password")) {
      $('#logo-password').removeClass("not_see_password");
      $('#logo-password').addClass("see_password");
      $('#password').attr("type", "password");
    }
  });
  $('#logo-confirm-password').click(function () {
    console.log("click on show confirm-password");
    if ($('#logo-confirm-password').hasClass("see_password")) {
      $('#logo-confirm-password').removeClass("see_password");
      $('#logo-confirm-password').addClass("not_see_password");
      $("#confirm-password").attr("type", "text");
    } else if ($('#logo-confirm-password').hasClass("not_see_password")) {
      $('#logo-confirm-password').removeClass("not_see_password");
      $('#logo-confirm-password').addClass("see_password");
      $('#confirm-password').attr("type", "password");
    }
  });

});

function savePassword(userId) {
  console.log("save Password " + userId);
  if($("#password").val() != $("#confirm-password").val()) {
    $('#globalError').show();
  } else {
    $('#globalError').hide();
    user = {
      password: $("#password").val()
    };
    $.ajax({
      url: "/user/" + userId + "/savePassword",
      type: 'post',
      data: JSON.stringify(user),
      contentType: "application/json",
      success: function (response) {
        console.log(response);
        $("#validate_save_password").modal('show');
        setTimeout(function () {
          $("#validate_save_password").modal('hide');
          window.location = "/login";
        }, 3000);
      },
      error: function (response) {
      }
    })
  }
}