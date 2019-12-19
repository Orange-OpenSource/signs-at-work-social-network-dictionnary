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



function editProfil() {
/*  if ($('#nameVideo-record').is(":hidden")) {
    $('#nameVideo-record').show();
    $('#changeName').css('pointer-events', '');
    $('#name-pen').show();
    $('#changeEntity').css('pointer-events', '');
    $('#entity-pen').show();
    $('#changeJobName').css('pointer-events', '');
    $('#jobName-pen').show();
    $('#jobVideo-record').show();
    $('#changeJobDescriptionText').css('pointer-events', '');
    $('#jobText-pen').show();
  } else {
    $('#nameVideo-record').hide();
    $('#changeName').css('pointer-events', 'none');
    $('#name-pen').hide();
    $('#changeEntity').css('pointer-events', 'none');
    $('#entity-pen').hide();
    $('#changeJobName').css('pointer-events', 'none');
    $('#jobName-pen').hide();
    $('#jobVideo-record').hide();
    $('#changeJobDescriptionText').css('pointer-events', 'none');
    $('#jobText-pen').hide();
  }*/
  if ($('#nameVideo-record').is(":hidden")) {
    $('#nameVideo-record').show();
  } else {
    $('#nameVideo-record').hide();
  }
  if ($('#name-pen').is(":hidden")) {
    $('#changeName').css('pointer-events', '');
    $('#name-pen').show();
  } else {
    $('#changeName').css('pointer-events', 'none');
    $('#name-pen').hide();
  }

  if ($('#entity-pen').is(":hidden")) {
    $('#changeEntity').css('pointer-events', '');
    $('#entity-pen').show();
  } else {
    $('#changeEntity').css('pointer-events', 'none');
    $('#entity-pen').hide();
  }

  if ($('#jobName-pen').is(":hidden")) {
    $('#changeJobName').css('pointer-events', '');
    $('#jobName-pen').show();
  } else {
    $('#changeJobName').css('pointer-events', 'none');
    $('#jobName-pen').hide();
  }
  if ($('#jobVideo-record').is(":hidden")) {
    $('#jobVideo-record').show();
  } else {
    $('#jobVideo-record').hide();
  }
  if ($('#jobText-pen').is(":hidden")) {
    $('#changeJobDescriptionText').css('pointer-events', '');
    $('#jobText-pen').show();
  } else {
    $('#changeJobDescriptionText').css('pointer-events', 'none');
    $('#jobText-pen').hide();
  }
}

function displayVideo(url, name) {
  console.log(url);
  console.log(name);
  document.getElementById("videoName").innerText = name;

  document.getElementById("videoUrl").src = url+'?endscreen-enable=false&autoplay=1&sharing-enable=false&wmode=transparent';
};
