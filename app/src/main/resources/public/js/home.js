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
// Wire up the events as soon as the DOM tree is ready
$(document).ready(function()
{

  $('#search_menu').on('mouseover', function () {
    $('#search').removeClass('search_white_desktop');
    $('#search').addClass('search_black_desktop');
  });

  $('#search_menu').on('mouseout', function () {
    $('#search').removeClass('search_black_desktop');
    $('#search').addClass('search_white_desktop');
  });

  $('#member_me_menu').on('mouseover', function () {
    $('#member_me').removeClass('member_me_white');
    $('#member_me').addClass('member_me_black');
  });

  $('#member_me_menu').on('mouseout', function () {
    $('#member_me').removeClass('member_me_black');
    $('#member_me').addClass('member_me_white');
  });

  $('#server_message_menu').on('mouseover', function () {
    $('#server_message').removeClass('message_white_desktop');
    $('#server_message').addClass('message_black_desktop');
  });

  $('#server_message_menu').on('mouseout', function () {
    $('#server_message').removeClass('message_black_desktop');
    $('#server_message').addClass('message_white_desktop');
  });

  $('#group_menu').on('mouseover', function () {
    $('#group').removeClass('group_white');
    $('#group').addClass('group_black');
  });

  $('#group_menu').on('mouseout', function () {
    $('#group').removeClass('group_black');
    $('#group').addClass('group_white');
  });

  $('#pinlist_menu').on('mouseover', function () {
    $('#pinlist').removeClass('pinlist_white');
    $('#pinlist').addClass('pinlist_black');
  });

  $('#pinlist_menu').on('mouseout', function () {
    $('#pinlist').removeClass('pinlist_black');
    $('#pinlist').addClass('pinlist_white');
  });

/*  $('#settings_menu').on('mouseover', function () {
    $('#settings').removeClass('settings_white');
    $('#settings').addClass('settings_black');
  });

  $('#settings_menu').on('mouseout', function () {
    $('#settings').removeClass('settings_black');
    $('#settings').addClass('settings_white');
  });*/

  $('#about_menu').on('mouseover', function () {
    $('#about').removeClass('about_white');
    $('#about').addClass('about');
  });

  $('#about_menu').on('mouseout', function () {
    $('#about').removeClass('about');
    $('#about').addClass('about_white');
  });

  $('#close_menu').on('mouseover', function () {
    $('#close').removeClass('x-close_white');
    $('#close').addClass('x-close_black');
  });

  $('#close_menu').on('mouseout', function () {
    $('#close').removeClass('x-close_black');
    $('#close').addClass('x-close_white');
  });
});