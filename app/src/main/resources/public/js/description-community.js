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




function addCommunityDescriptionText(communityId) {
  var communityDescriptionText = document.getElementById('community-description-text');
  community = {
    descriptionText: communityDescriptionText.value
  };
  $.ajax({
    url: "/ws/sec/communities/" + communityId + "/datas",
    type: 'put',
    data: JSON.stringify(community),
    contentType: "application/json",
    success: function (response) {
      console.log(response);
      communityId = response.communityId;
      location.reload();
    },
    error: function (response) {
    }
  })
}
