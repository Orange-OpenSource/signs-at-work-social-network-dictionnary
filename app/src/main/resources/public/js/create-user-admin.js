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
console.log("Cool, create-user-admin.js is loaded :)");


var inputLastName = document.getElementById('lastName');
var inputFirstName = document.getElementById('firstName');
var inputEmail = document.getElementById('mail');
var submitCreateUser = document.getElementById("submit-create-user");
var errorCreateUser = document.getElementById('errorCreateUser');

var lastName = new Boolean(false);
var firstName = new Boolean(false);
var mail = new Boolean(false);


var regexName = new RegExp('^[A-zÀ-ÖØ-öø-ÿ]{1,30}$');
var regexEmail = new RegExp('[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}');

inputLastName.addEventListener('keyup',checkLastName);
inputFirstName.addEventListener('keyup',checkFirstName);
inputEmail.addEventListener('keyup',checkEmail);
submitCreateUser.disabled = true;

var messageServerId = "";

function checkLastName() {
  var valueLastName = inputLastName.value;

  if (!regexName.test(valueLastName)) {
    $('.errorRegexLastName').removeClass("hidden");
    lastName = false;
    submitCreateUser.disabled = true;
  }else {
    lastName = true;
    $('.errorRegexLastName').addClass("hidden");
    if ((firstName != false) && (mail != false)) {
      submitCreateUser.disabled = false;
    }
  }
}

function checkFirstName() {
  var valueFirstName = inputFirstName.value;

  if (!regexName.test(valueFirstName)) {
    $('.errorRegexFirstName').removeClass("hidden");
    firstName = false;
    submitCreateUser.disabled = true;
  } else {
    firstName = true;
    $('.errorRegexFirstName').addClass("hidden");
    if ((lastName != false) && (mail != false)) {
      submitCreateUser.disabled = false;
    }
  }
}


function checkEmail() {
  var valueEmail = inputEmail.value;
  /*errorCreateUser.style.visibility = "hidden";*/
  if(!regexEmail.test(valueEmail)) {
    $('.errorRegexEmail').removeClass("hidden");
    mail = false;
    submitCreateUser.disabled = true;
  }else {
    mail = true;
    $('.errorRegexEmail').addClass("hidden");
    if ((lastName != false) && (firstName != false)) {
      submitCreateUser.disabled = false;
    }
  }
}

function onCreateUser(messageServer, messageId) {
  console.log("messageServer " + messageServer );
  console.log("messageId " + messageId );
  var message = messageServer.split(";");
  console.log("message " + message);
  $("#firstName").val(message[0]);
  $("#lastName").val(message[1]);
  $("#mail").val(message[2]);
  $("#create-user").modal('show');
  messageServerId = messageId;
}

var $create_user = $('#create-user');
$create_user.on('hidden.bs.modal', function() {
 console.log("hidden create-user");
 $("#firstName").val("");
 $("#firstName").removeClass("disabled");
 $("#lastName").val("");
 $("#lastName").removeClass("disabled");
 $("#mail").val("");
 $("#mail").removeClass("disabled");
 $('.errorRegexFirstName').addClass("hidden");
 $('.errorRegexLastName').addClass("hidden");
 $('.errorRegexEmail').addClass("hidden");
 errorCreateUser.style.display = "none";
 submitCreateUser.disabled = true;
});

var $formCreateUser= $('#input-create-user');
$formCreateUser.on('submit', function(event) {
    document.getElementById('submit-create-user').disabled=true;
     user = {
        firstName: inputFirstName.value,
        lastName: inputLastName.value,
        email: inputEmail.value,
        username: inputEmail.value,
        role: role.value,
        messageServerId : messageServerId
      };
    event.preventDefault();
    $.ajax({
      url: '/ws/admin/users',
      type: 'post',
      data: JSON.stringify(user),
      contentType: "application/json",
      success: function (response) {
        var url = response;
        errorCreateUser.style.display = "none";
        console.log("Success " + response);
        url = "/sec/admin/create-users";
        console.log(window.location.href);
        window.history.replaceState({}, 'foo', url);
        console.log(window.location.href);
        window.location = url;
      },
      error: function (response) {
        errorCreateUser.textContent = response.responseJSON.errorMessage;
        errorCreateUser.style.display = "inline-block";
        console.log("Erreur " + response.responseText);
      }
    })
});

var $modalCreateUser= $('#create-user');
$modalCreateUser.on('show.bs.modal', function() {
  if (inputFirstName.value !== "") {
    $("#firstName").addClass("disabled");
     checkFirstName();
  }
  if (inputLastName.value !== "") {
      $("#lastName").addClass("disabled");
      checkLastName();
  }
  if (inputEmail.value !== "") {
      $("#mail").addClass("disabled");
      checkEmail();
  }
});

$('#cancel-create-user').click(function() {
  console.log("cancel create user");
  if (messageServerId != 0) {
    document.getElementById('submit-create-user').disabled=true;
     user = {
        messageServerId : messageServerId
      };
    event.preventDefault();
    $.ajax({
      url: '/ws/admin/users',
      type: 'post',
      data: JSON.stringify(user),
      contentType: "application/json",
      success: function (response) {
        var url = response;
        errorCreateUser.style.display = "none";
        console.log("Success " + response);
        url = "/sec/admin/create-users";
        console.log(window.location.href);
        window.history.replaceState({}, 'foo', url);
        console.log(window.location.href);
        window.location = url;
      },
      error: function (response) {
        errorCreateUser.textContent = response.responseJSON.errorMessage;
        errorCreateUser.style.display = "inline-block";
        console.log("Erreur " + response.responseText);
      }
    })
  }
});