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

function onChangeBrowserHistory(isAdmin) {
  console.log("onChangeBrowserHistory");
  console.log("old "+window.location.href);
  if (isAdmin === "true") {
    window.history.replaceState({}, 'foo', "/sec/admin/manage_communities");
  } else {
    window.history.replaceState({}, 'foo', "/sec/communities");
  }
  console.log("new "+window.location.href);

}

function onChangeFavoriteBrowserHistory(favoriteId) {
  console.log("onChangeFavoriteBrowserHistory");
  console.log("old "+window.location.href);
  window.history.replaceState({}, 'foo', "/sec/favorite/"+favoriteId);
  console.log("new "+window.location.href);

}