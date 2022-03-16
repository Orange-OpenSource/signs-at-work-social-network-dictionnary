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


console.log("Cool, article.js is loaded :)");


function displayVideo(url, name) {
   console.log(url);
    console.log(name);
    document.getElementById("videoName").innerText = name;

   if  (url.includes('http')) {
    console.log("http");
    document.getElementById("videoOnDailyMotion").style.display="block"
    document.getElementById("videoOnServer").style.display="none"
    document.getElementById("videoUrl").src = url+'?endscreen-enable=false&autoplay=1&sharing-enable=false&wmode=transparent&disable-queue=1';
    } else {
      document.getElementById("videoOnDailyMotion").style.display="none"
      document.getElementById("videoOnServer").style.display="block"
      document.getElementById("videoUrlOnServer").src= url;
      document.getElementById("videoplayer").load();
    }
};
