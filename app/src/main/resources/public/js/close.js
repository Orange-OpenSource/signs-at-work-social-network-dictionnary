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



var validNavigation = 0;

function endSession()
{
  // Browser or Broswer tab is closed
  // Write code here

       $.ajax({
         url: "/ws/sec/close",
         type: 'post',
         success: function(response) {
         },
         error: function(response) {
         }
       })
}

function bindDOMEvents() {
  /*

   * unload works on both closing tab and on refreshing tab.

   */
  $(window).bind('beforeunload', function()
  {
    if (validNavigation==0)
    {
      endSession();
    }
  });

// Attach the event keypress to exclude the F5 refresh
  $(document).keydown(function(e)
  {
    var key=e.which || e.keyCode;
    if (key == 116)
    {
      validNavigation = 1;
    }
  });

// Attach the event click for all links in the page
  $("a").bind("click", function()
  {
    validNavigation = 1;
  });

  // Attach the event submit for all forms in the page
  $("form").bind("submit", function()
  {
    validNavigation = 1;
  });

  // Attach the event click for all inputs in the page
  $("input[type=submit]").bind("click", function()
  {
    validNavigation = 1;
  });

}


// Wire up the events as soon as the DOM tree is ready
$(document).ready(function()
{
  bindDOMEvents();
/*  $("#sidebar").mCustomScrollbar({
    theme: "minimal"
  });*/

/*  $('.overlay').fadeOut();*/

  $('#dismiss, .overlay').on('click', function () {
    $('#sidebar').removeClass('active');
    $('.overlay').fadeOut();
  });

  $('#sidebarCollapse').on('click', function () {
    $('#sidebar').addClass('active');
    $('.overlay').fadeIn();
    $('.collapse.in').toggleClass('in');
    $('a[aria-expanded=true]').attr('aria-expanded', 'false');
  });
});