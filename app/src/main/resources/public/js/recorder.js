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
console.log("Cool, recorder.js is loaded :)");

function captureUserMedia(mediaConstraints, successCallback, errorCallback) {
  navigator.mediaDevices.getUserMedia(mediaConstraints).then(successCallback).catch(errorCallback);
}

var videoElement = document.getElementById('video');
//var downloadURL = document.getElementById('download-url');

var startRecording = document.getElementById('start-recording');
var stopRecording = document.getElementById('stop-recording');

//var progressBar = document.querySelector('#progress-bar');
//var percentage = document.querySelector('#percentage');
var videoFile = {};
var errorSpan = document.getElementById('errorSpan');


startRecording.onclick = function() {
  startRecording.disabled = true;
  stopRecording.disabled = false;
  document.getElementById('start-recording').disabled = true;
  document.getElementById('stop-recording').disabled = false;


  captureUserMedia00(function(stream) {
    window.audioVideoRecorder = window.RecordRTC(stream, {
      type: 'video',
      disableLogs: false
    });
    window.audioVideoRecorder.startRecording();
  });
};

stopRecording.onclick = function() {
  stopRecording.disabled = true;
  startRecording.disabled = false;
  document.getElementById('stop-recording').disabled = true;
  document.getElementById('start-recording').disabled = false;
  document.getElementById('continue').disabled = false;

  window.audioVideoRecorder.stopRecording(function(url) {
    //downloadURL.innerHTML = '<a href="' + url + '" download="RecordRTC.webm" target="_blank">Save RecordRTC.webm to Disk!</a><hr>';
    videoElement.src = url;
    videoElement.muted = false;
    videoElement.play();

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
    signNameRecording: null,
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

var $form = $('#uploadVideoFile');
$form.on('submit', function(event) {
  videoFile.signNameRecording = $('#signNameRecording').val();
    event.preventDefault();
    $.ajax({
      url: $form.attr('action'),
      type: 'post',
      data: JSON.stringify(videoFile),
      contentType: "application/json",
      success: function(response) {
        var url = "/sign/"+response;
        window.location = url;
        errorSpan.style.visibility="hidden";
        console.log("Success " + response);
      },
      error: function(response) {
        errorSpan.textContent = response.responseText;
        errorSpan.style.visibility="visible";
        console.log("Erreur " + response.responseText);
      }
    })

});

$form.on('input', function(event) {
  document.getElementById('errorSpan').style.visibility="hidden";
});

var $add_video_file_recording = $('#add_video_file_recording');
$add_video_file_recording.on('hidden.bs.modal', function() {
 console.log("hidden add_video_file_recording modal");
  audioVideoRecorder.clearRecordedData();
  document.getElementById('stop-recording').disabled = true;
  document.getElementById('start-recording').disabled = false;
  document.getElementById('video').removeAttribute("src");
  document.getElementById('video').removeAttribute("controls");
  document.getElementById('video').pause();
  if ($('#uploadVideoFile').find('#errorSpan').length) {
    errorSpan.style.visibility="hidden";
    $('#signNameRecording').val("");
  }
});