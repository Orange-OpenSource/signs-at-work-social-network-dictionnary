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
  $('#accountRequest #accountProfile ').removeClass("hidden");
};

var inputLastName =document.getElementById('lastName');
var inputFirstName =document.getElementById('firstName');
var inputEntity =document.getElementById('entity');
var inputEmail =document.getElementById('mail');
var buttonOnNext =document.getElementById('buttonOnNext');

var lastName = new Boolean(false);
var firstName = new Boolean(false);
var entity = new Boolean(false);
var mail = new Boolean(false);


var regexName = new RegExp('[A-Za-z]');
var regexEntity = new RegExp('[\\sA-Za-z_:-\\\\/\\\\]');
var regexEmail = new RegExp('[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}');

inputLastName.addEventListener('keyup',checkLastName);
inputFirstName.addEventListener('keyup',checkFirstName);
inputEntity.addEventListener('keyup',checkEntity);
inputEmail.addEventListener('keyup',checkEmail);
buttonOnNext.addEventListener('click',onNext);

$('html').bind('keypress', function(e)
{
  if(e.keyCode == 13)
  {
    return false;
  }
});

function checkLastName() {
  var valueLastName = inputLastName.value;

  if (!regexName.test(valueLastName)) {
    $('#lastName').addClass("alert alert-warning");
    $('.blink_me.errorRegexLastName').removeClass("hidden");
    lastName = false;
  }else {
    lastName = true;
    $('#lastName').removeClass("alert alert-warning");
    $('.errorRegexLastName').addClass("hidden");
  }
}

function checkFirstName() {
  var valueFirstName = inputFirstName.value;

  if (!regexName.test(valueFirstName)) {
    $('#firstName').addClass("alert alert-warning");
    $('.errorRegexFirstName').removeClass("hidden");
    firstName = false;
  } else {
    firstName = true;
    $('#firstName').removeClass("alert alert-warning");
    $('.errorRegexFirstName').addClass("hidden");
  }
}

function checkEntity() {
  var valueEntity = inputEntity.value;

  if(!regexEntity.test(valueEntity)) {
    $('#entity').addClass("alert alert-warning");
    $('.errorRegexEntity').removeClass("hidden");
    entity = false;
  }else {
    entity = true;
    $('#entity').removeClass("alert alert-warning");
    $('.errorRegexEntity').addClass("hidden");
  }
}

function checkEmail() {
  var valueEmail = inputEmail.value;

  if(!regexEmail.test(valueEmail)) {
    $('#mail').addClass("alert alert-warning");
    $('.errorRegexEmail').removeClass("hidden");
    mail = false;
  }else {
    mail = true;
    $('#mail').removeClass("alert alert-warning");
    $('.errorRegexEmail').addClass("hidden");
  }
}

function onNext(){

  if( (lastName !=false) &&
    (firstName !=false) &&
    (entity !=false) &&
    (mail !=false)) {
    $('#accountRequest #accountProfile').addClass("hidden");
    $('#privacySettings').removeClass("hidden");
    $('.btn').removeClass("hidden");
  }else{
    $('.errorSubmit').removeClass("hidden");
    $('#mail').addClass("alert alert-warning");
    $('#entity').addClass("alert alert-warning");
    $('#firstName').addClass("alert alert-warning");
    $('#lastName').addClass("alert alert-warning");

  }


};
