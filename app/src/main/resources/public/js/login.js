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

var userName = document.getElementById("userName");
var submitForgetPassword = document.getElementById("submit-forget-Password");


function onForgetPassword() {
    user = {
      username: userName.value
    };
    $.ajax({
      url: "/forgetPassword",
      type: 'post',
      data: JSON.stringify(user),
      contentType: "application/json",
      success: function (response) {
        console.log(response);
        errorForgetPassword.style.visibility = "hidden";
        $('#login_for_lost_password').modal('hide');
        /*renamed_favorite.textContent = response.errorMessage;*/
        $('#validate_forget_password').modal('show');
        setTimeout(function () {
          $('#validate_forget_password').modal('hide');
          window.location="/";
        }, 3000);
      },
      error: function (response) {
        console.log(response.responseJSON);
        errorForgetPassword.textContent = response.responseJSON.errorMessage;
        errorForgetPassword.style.visibility = "visible";
      }
    })

};

$('#login_for_lost_password').on('hidden.bs.modal', function (e) {
  var errorForgetPassword = document.getElementById('errorForgetPassword');
  errorForgetPassword.style.visibility = "hidden";
  userName.value='';
  submitForgetPassword.disabled = true;
})

function resetForgetPasswordError(event) {
  var errorForgetPassword = document.getElementById('errorForgetPassword');
  errorForgetPassword.style.visibility = "hidden";
  submitForgetPassword.disabled = false;
}


function main() {
  userName.addEventListener('keyup', resetForgetPasswordError);
  submitForgetPassword.disabled = true;
}

(function AfterLoad($) {

  main();

})($);

$(function() {
  $('#logo-password').click(function () {
    console.log("click on show password");
    if ($('#logo-password').hasClass("see_password")) {
      $('#logo-password').removeClass("see_password");
      $('#logo-password').addClass("not_see_password");
      $("#myPassword").attr("type", "text");
    } else if ($('#logo-password').hasClass("not_see_password")) {
      $('#logo-password').removeClass("not_see_password");
      $('#logo-password').addClass("see_password");
      $('#myPassword').attr("type", "password");
    }
  });
});