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
console.log("Cool, create_user.js is loaded :)");



function onAccepted() {
  $('#cguAccepted').css('display', 'none');
  $('#accountRequest').removeClass("hidden");
};

var inputLastName =document.getElementById('lastName');
var inputFirstName =document.getElementById('firstName');
var inputEmail =document.getElementById('mail');
var submitCreateUser = document.getElementById("submit-create-user");
var errorCreateUser = document.getElementById('errorCreateUser');

var lastName = new Boolean(false);
var firstName = new Boolean(false);
var mail = new Boolean(false);


var regexName = new RegExp('^[a-zA-Z_-]{1,30}$');
var regexEmail = new RegExp('[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}');

inputLastName.addEventListener('keyup',checkLastName);
inputFirstName.addEventListener('keyup',checkFirstName);
inputEmail.addEventListener('keyup',checkEmail);
submitCreateUser.disabled = true;


function checkLastName() {
  var valueLastName = inputLastName.value;

  if (!regexName.test(valueLastName)) {
    /*$('#lastName').addClass("alert alert-warning");*/
    $('.errorRegexLastName').removeClass("hidden");
    lastName = false;
    submitCreateUser.disabled = true;
  }else {
    lastName = true;
    /*$('#lastName').removeClass("alert alert-warning");*/
    $('.errorRegexLastName').addClass("hidden");
    if ((firstName != false) && (mail != false)) {
      submitCreateUser.disabled = false;
    }
  }
}

function checkFirstName() {
  var valueFirstName = inputFirstName.value;

  if (!regexName.test(valueFirstName)) {
   /* $('#firstName').addClass("alert alert-warning");*/
    $('.errorRegexFirstName').removeClass("hidden");
    firstName = false;
    submitCreateUser.disabled = true;
  } else {
    firstName = true;
    /*$('#firstName').removeClass("alert alert-warning");*/
    $('.errorRegexFirstName').addClass("hidden");
    if ((lastName != false) && (mail != false)) {
      submitCreateUser.disabled = false;
    }
  }
}


function checkEmail() {
  var valueEmail = inputEmail.value;
  errorCreateUser.style.visibility = "hidden";
  if(!regexEmail.test(valueEmail)) {
    /*$('#mail').addClass("alert alert-warning");*/
    $('.errorRegexEmail').removeClass("hidden");
    mail = false;
    submitCreateUser.disabled = true;
  }else {
    mail = true;
    /*$('#mail').removeClass("alert alert-warning");*/
    $('.errorRegexEmail').addClass("hidden");
    if ((lastName != false) && (firstName != false)) {
      submitCreateUser.disabled = false;
    }
  }
}

function sendMail() {
  console.log("send Mail");
    user = {
      firstName: inputFirstName.value,
      lastName: inputLastName.value,
      email: inputEmail.value
    };
    $.ajax({
      url: "/sendMail",
      type: 'post',
      data: JSON.stringify(user),
      contentType: "application/json",
      success: function (response) {
        console.log(response);
        $("#validate_send_mail").modal('show');
        setTimeout(function () {
          $("#validate_send_mail").modal('hide');
          window.location = "/login";
        }, 3000);
      },
      error: function (response) {
        console.log(response.responseJSON);
        errorCreateUser.textContent = response.responseJSON.errorMessage;
        errorCreateUser.style.visibility = "visible";
      }
    })
}

