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
var inputNewEmail = document.getElementById('new_mail');
var inputEmail = document.getElementById('mail');
var inputRole = document.getElementById('role');
var submitCreateUser = document.getElementById("submit-create-user");
var errorCreateUser = document.getElementById('errorCreateUser');
var errorChangeEmail = document.getElementById('errorChangeEmail');

var lastName = new Boolean(false);
var firstName = new Boolean(false);
var mail = new Boolean(false);
var role = new Boolean(false);


var regexName = new RegExp('^[A-zÀ-ÖØ-öø-ÿ- œæŒÆ]{1,30}$');
var regexEmail = new RegExp('[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}');

inputLastName.addEventListener('keyup',checkLastName);
inputFirstName.addEventListener('keyup',checkFirstName);
inputEmail.addEventListener('keyup',checkEmail);
inputRole.addEventListener("change",checkRole);
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
    if ((firstName != false) && (mail != false) && (role != false)) {
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
    if ((lastName != false) && (mail != false) && (role != false)) {
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
    if ((lastName != false) && (firstName != false) && (role != false)) {
      submitCreateUser.disabled = false;
    }
  }
}

function checkRole() {
  var valueRole = inputRole.value;
  console.log("inputRole "+inputRole.value)
  if(!inputRole.value) {
    role = false;
    submitCreateUser.disabled = true;
  }else {
    role = true;
    $('#role').css("color","#4866F7");
    $('.errorRegexEmail').addClass("hidden");
    if ((lastName != false) && (firstName != false) && (mail != false)) {
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
 $("#role").val("");
 $('#role').css("color","#8C8C8C");
 $('.errorRegexFirstName').addClass("hidden");
 $('.errorRegexLastName').addClass("hidden");
 $('.errorRegexEmail').addClass("hidden");
 errorCreateUser.style.display = "none";
 messageServerId=0;
 submitCreateUser.disabled = true;
});

var $formCreateUser= $('#input-create-user');
$formCreateUser.on('submit', function(event) {
    var confirmCreateUserText = document.getElementById('confirm_create_user_text');
    document.getElementById('submit-create-user').disabled=true;
     user = {
        firstName: inputFirstName.value,
        lastName: inputLastName.value,
        email: inputEmail.value,
        username: inputEmail.value,
        role: inputRole.value,
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
        $("#create-user").modal('hide');
        confirmCreateUserText.textContent = response.errorMessage;
        $("#confirm_create_user").modal('show');
        setTimeout(function(){
          $('#confirm_create_user').modal('hide');
          url = "/sec/admin/create-users";
          console.log(window.location.href);
          window.history.replaceState({}, 'foo', url);
          console.log(window.location.href);
          window.location = url;
        }, 3000);
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
    var confirmCancelCreateUserText = document.getElementById('confirm_cancel_create_user_text');
    document.getElementById('submit-create-user').disabled=true;
     user = {
        firstName: inputFirstName.value,
        lastName: inputLastName.value,
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
        $("#create-user").modal('hide');
        confirmCancelCreateUserText.textContent = response.errorMessage;
        $("#confirm_cancel_create_user").modal('show');
        setTimeout(function(){
          $('#confirm_cancel_create_user').modal('hide');
          url = "/sec/admin/create-users";
          console.log(window.location.href);
          window.history.replaceState({}, 'foo', url);
          console.log(window.location.href);
          window.location = url;
        }, 3000);
      },
      error: function (response) {
        errorCreateUser.textContent = response.responseJSON.errorMessage;
        errorCreateUser.style.display = "inline-block";
        console.log("Erreur " + response.responseText);
      }
    })
  }
});

function onChangeEmail(messageServer, messageId) {
  console.log("messageServer " + messageServer );
  console.log("messageId " + messageId );
  var message = messageServer.split(";");
  console.log("message " + message);
  $("#new_mail").val(message[2]);
  $("#change-email").modal('show');
  messageServerId = messageId;
}


var $formChangeEmail= $('#input-change-email');
$formChangeEmail.on('submit', function(event) {
    var confirmChangeEmailText = document.getElementById('confirm_change_email_text');
    document.getElementById('submit-change-email').disabled=true;
     user = {
        email: inputNewEmail.value,
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
        errorChangeEmail.style.display = "none";
        $("#change-email").modal('hide')
        confirmChangeEmailText.textContent = response.errorMessage;;
        $("#confirm_change_email").modal('show');
        setTimeout(function(){
          $('#confirm_change_email').modal('hide');
          url = "/sec/admin/create-users";
          console.log(window.location.href);
          window.history.replaceState({}, 'foo', url);
          console.log(window.location.href);
          window.location = url;
        }, 3000);
      },
      error: function (response) {
        errorChangeEmail.textContent = response.responseJSON.errorMessage;
        errorChangeEmail.style.display = "inline-block";
        console.log("Erreur " + response.responseText);
      }
    })
});

$('#cancel-change-email').click(function() {
  console.log("cancel change email");
  if (messageServerId != 0) {
    var confirmCancelChangeEmailText = document.getElementById('confirm_cancel_change_email_text');
    document.getElementById('submit-change-email').disabled=true;
     user = {
        firstName: inputFirstName.value,
        lastName: inputLastName.value,
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
        errorChangeEmail.style.display = "none";
        $("#change-email").modal('hide');
        confirmCancelChangeEmailText.textContent = response.errorMessage;
        $("#confirm_cancel_change_email").modal('show');
        setTimeout(function(){
          $('#confirm_cancel_change_email').modal('hide');
          url = "/sec/admin/create-users";
          console.log(window.location.href);
          window.history.replaceState({}, 'foo', url);
          console.log(window.location.href);
          window.location = url;
        }, 3000);
      },
      error: function (response) {
        errorChangeEmail.textContent = response.responseJSON.errorMessage;
        errorChangeEmail.style.display = "inline-block";
        console.log("Erreur " + response.responseText);
      }
    })
  }
});