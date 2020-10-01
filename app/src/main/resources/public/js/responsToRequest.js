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




function onRequestClick(id, name){
  var uploadRecordedVideoFile = document.getElementById("uploadRecordedVideoFile");
  uploadRecordedVideoFile.action = "/ws/sec/uploadRecordedVideoFile/" + id;
  var signNameRecording = document.getElementById("signNameRecording");
  signNameRecording.value =$.trim(name);

  var uploadSelectedVideoFile = document.getElementById("uploadSelectedVideoFile");
  uploadSelectedVideoFile.action = "/ws/sec/uploadSelectedVideoFile/" + id;
  var signNameSelected = document.getElementById("signNameSelected");
  signNameSelected.value =$.trim(name);

  console.log("request id =" + id );
}


function onClick() {
  event.preventDefault();
  document.getElementById('InputFile').click()
}

$(document).ready(function(){
  var InputFileLabel = document.getElementById('InputFileLabel');
  var url;
  var videoFileToUpload = document.getElementById("videoFileToUpload")

  $('input[type="file"]').change(function(e){

    document.getElementById('submitButtonFileDailymotion').disabled=false;
    if (InputFileLabel != null) {
      document.getElementById('InputFileLabel').style.display = "none";
    }
    if (this.files[0] != null) {
      url = window.URL.createObjectURL(this.files[0])
      document.getElementById("subtitle_for_modal_video").style.display="none";
      document.getElementById("videoUrl").src = url +'?endscreen-enable=false&autoplay=1&sharing-enable=false&wmode=transparent';
      document.getElementById("videoFileToUpload").style.display="";
      document.getElementById("InputFile").style.display="none";
    }
  });

});