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



var email = 'denis.boisset@orange.com';
var subject = 'Demande de création utilisateur';



var $formCreateUser = $('#createUser');
$formCreateUser.on('submit', function(event) {
  var username = $('#username').val();
  var mail = $('#email').val();
  console.log("submit");
  var mailto_link = 'mailto:' + email + '?subject=' + subject + '&body=' + "Demande de création du compte " + username + " avec le mail " + mail;

  win = window.open(mailto_link, 'emailWindow');
  if (win && win.open && !win.closed)
    win.close();

});
