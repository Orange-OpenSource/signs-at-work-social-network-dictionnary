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
console.log("Cool, recorderProfilLSF.js is loaded :)");

function captureUserMedia(mediaConstraints, successCallback, errorCallback) {
  navigator.mediaDevices.getUserMedia(mediaConstraints).then(successCallback).catch(errorCallback);
}

var videoContainer = document.getElementById('container_video');
var labelRecord = document.getElementById('label_record');
var labelAfterRecord = document.getElementById('label_after_record');
var videoElement = document.getElementById('video');

var startRecording = document.getElementById('start-recording');
var retryRecording = document.getElementById('retry-recording');
var stopRecording = document.getElementById('stop-recording');
var cancelRecording = document.getElementById('cancel-recording');
document.getElementById('container-button').style.display = "none";

var videoFile = {};
var errorSpan = document.getElementById('errorSpan');
var counter = 3;
var t;

function timedCount() {
  document.getElementById("counter").textContent = counter;
  counter = counter - 1;
  if (counter < 0) {
    document.getElementById("counter").style.visibility="hidden";
    document.getElementById('start-recording').style.display = "none";
    document.getElementById('stop-recording').style.display = "inline-block";
    counter = 3;
    window.audioVideoRecorder.startRecording();
    return;
  }
  t = setTimeout(function(){ timedCount() }, 1000);
}

function startRecord() {
  videoContainer.style.display = "block";
  labelRecord.style.display = "none";
  labelAfterRecord.style.display = "none";
  startRecording.disabled = true;
  stopRecording.disabled = false;
  document.getElementById('start-recording').disabled = true;
  document.getElementById('stop-recording').disabled = false;

  captureUserMedia00(function (stream) {
    window.audioVideoRecorder = window.RecordRTC(stream, {
      type: 'video',
      disableLogs: false
    });
    document.getElementById('video').style.visibility = "visible";
    document.getElementById("counter").style.visibility = "visible";
    timedCount();
    //window.audioVideoRecorder.startRecording();
  });
}

startRecording.onclick = function() {
  startRecord();
};


retryRecording.onclick = function () {
  document.getElementById('container-button').style.display = "none";
  document.getElementById('btnChecked').style.display = "none";
  document.getElementById('start-recording').style.display = "block";
  if ($('#uploadRecordedVideoFile').find('#errorSpan').length) {
    errorSpan.style.visibility="hidden";
  }
  startRecord();
};


stopRecording.onclick = function() {
  labelAfterRecord.style.display="block";
  labelAfterRecord.style.visibility="visible";
  document.getElementById('container-button').style.display = "block";
  document.getElementById('btnChecked').style.display = "block";
  document.getElementById('stop-recording').style.display = "none";
  stopRecording.disabled = true;
  startRecording.disabled = false;
  document.getElementById('stop-recording').disabled = true;
  document.getElementById('retry-recording').disabled = false;
  document.getElementById('continue').disabled = false;


  window.audioVideoRecorder.stopRecording(function(url) {
    //downloadURL.innerHTML = '<a href="' + url + '" download="RecordRTC.webm" target="_blank">Save RecordRTC.webm to Disk!</a><hr>';
    videoElement.src = url;
    videoElement.muted = false;
    //videoElement.play();

    videoElement.onended = function() {
      videoElement.pause();

      // dirty workaround for: "firefox seems unable to playback"
      videoElement.src = URL.createObjectURL(audioVideoRecorder.getBlob());
    };
    audioVideoRecorder.getDataURL(function(audioVideoDataURL) {
      var video = {
        blob: audioVideoRecorder.getBlob(),
        dataURL: audioVideoDataURL
      };
      prepareFileToPost(video);
    });

  });
};

cancelRecording.onclick = function() {
  location.reload();
};



function captureUserMedia00(callback) {
  captureUserMedia({
    audio: false,
    video: true
  }, function(stream) {
    videoElement.src = URL.createObjectURL(stream);
    videoElement.muted = true;
    videoElement.controls = true;
    videoElement.play();

    callback(stream);
  }, function(error) {
    alert(JSON.stringify(error));
  });
}

function prepareFileToPost(video) {
  // getting unique identifier for the file name
  fileName = generateRandomString();

  // this object is used to allow submitting multiple recorded blobs



  videoFile = {
    name: fileName + '.' + video.blob.type.split('/')[1],
    type: video.blob.type,
    contents: video.dataURL
  };
}


// generating random string
function generateRandomString() {
  if (window.crypto) {
    var a = window.crypto.getRandomValues(new Uint32Array(3)),
      token = '';
    for (var i = 0, l = a.length; i < l; i++) token += a[i].toString(36);
    return token;
  } else {
    return (Math.random() * new Date().getTime()).toString(36).replace(/\./g, '');
  }
}

var $formUploadRecordedVideoFile = $('#uploadRecordedVideoFile');
$formUploadRecordedVideoFile.on('submit', function(event) {
  document.getElementById('continue').disabled = true;
  document.getElementById('cancel-recording').disabled = true;
  document.getElementById('retry-recording').disabled = true;
  $(".spinner").removeClass("spinner_hidden").addClass("spinner_show");
  $("video").css("z-index","-1").css("opacity","0.40");
  $("video").removeAttr("controls");
  $(".spinner").css("z-index","1500").visibility="visible";
  $("#continue").css("color","black");
    event.preventDefault();
    $.ajax({
      url: $formUploadRecordedVideoFile.attr('action'),
      type: 'post',
      data: JSON.stringify(videoFile),
      contentType: "application/json",
      success: function(response) {
        var url = response;
        window.location = url;
        errorSpan.style.visibility="hidden";
        $(".spinner").visibility="hidden";
        $("video").css("z-index","1500").css("opacity","1");
        console.log("Success " + response);
      },
      error: function(response) {
        errorSpan.textContent = response.responseText;
        errorSpan.style.visibility="visible";
        $(".spinner").css("z-index","-1").css("opacity","0.1");
        $(".spinner").visibility="hidden";
        $("video").css("z-index","1500").css("opacity","1");
        console.log("Erreur " + response.responseText);
      }
    })

});

$formUploadRecordedVideoFile.on('input', function(event) {
  document.getElementById('errorSpan').style.visibility="hidden";
});

var $add_video_file_recording = $('#add_video_file_recording');
$add_video_file_recording.on('hidden.bs.modal', function() {
 console.log("hidden add_video_file_recording modal");
  clearTimeout(t);
  audioVideoRecorder.clearRecordedData();
  videoContainer.style.display="none";
  labelRecord.style.visibility="visible";
  labelAfterRecord.style.display="none";
  document.getElementById('start-recording').style.display = "inline-block";
  document.getElementById('container-button').style.display = "none";
  document.getElementById('btnChecked').style.display = "none";
  document.getElementById('stop-recording').style.display = "none";
  document.getElementById('stop-recording').disabled = true;
  document.getElementById('start-recording').disabled = false;
  document.getElementById('video').removeAttribute("src");
  document.getElementById('video').removeAttribute("controls");
  document.getElementById('video').pause();
  document.getElementById('video').style.visibility="hidden";
  document.getElementById('continue').disabled = true;
  if ($('#uploadRecordedVideoFile').find('#errorSpan').length) {
    errorSpan.style.visibility="hidden";
  }
});