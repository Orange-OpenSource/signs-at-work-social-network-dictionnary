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


console.log("Cool, profil.js is loaded :)");

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

var nameVideoRecord = document.getElementById('nameVideo-record');
var jobVideoRecord = document.getElementById('jobVideo-record')

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
  document.getElementById("modal-footer_add_video_file_recording").style.display = "none";
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
  document.getElementById("modal-footer_add_video_file_recording").style.display = "block";
  videoElement.src = videoElement.srcObject = null;

  window.audioVideoRecorder.stopRecording(function(url) {
    //downloadURL.innerHTML = '<a href="' + url + '" download="RecordRTC.webm" target="_blank">Save RecordRTC.webm to Disk!</a><hr>';
    videoElement.src = url;
    videoElement.muted = false;
    videoElement.play();

    /*    videoElement.onended = function() {
          videoElement.pause();

          // dirty workaround for: "firefox seems unable to playback"
          videoElement.src = URL.createObjectURL(audioVideoRecorder.getBlob());
        };*/
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
    console.log("function");
    /* videoElement.src = URL.createObjectURL(stream);*/
    videoElement.srcObject = stream;
    videoElement.muted = true;
    videoElement.controls = true;
    videoElement.play();

    callback(stream);
  }, function(error) {
    console.log("error "+error.message);
    alert(JSON.stringify(error));
  });
}

function prepareFileToPost(video) {
  // getting unique identifier for the file name
  fileName = generateRandomString();

  // this object is used to allow submitting multiple recorded blobs



  videoFile = {
    name: fileName + '.' + video.blob.type.split('/')[1].split(';')[0],
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
     /* var url = response;
      window.location = url;*/
      errorSpan.style.visibility="hidden";
      $(".spinner").visibility="hidden";
      $("video").css("z-index","1500").css("opacity","1");
      console.log("Success " + response);
      location.reload();
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

var $profileEntity = $('#profileEntity');
$profileEntity.on('submit', function(event) {
  var entity_name = document.getElementById('entity_name').value;
  console.log(entity_name);
  var data = {
    "entity": entity_name
  };
  event.preventDefault();
  $.ajax({
    url: "/ws/sec/users/me/datas",
    type: 'put',
    data: JSON.stringify(data),
    contentType: "application/json",
    success: function(response) {
      console.log("Success " + response);
      location.reload();
    },
    error: function(response) {
      console.log("Erreur " + response.responseText);
    }
  })
});


var $profileName = $('#profileName');
$profileName.on('submit', function(event) {
  var first_name = document.getElementById('firstName').value;
  var last_name = document.getElementById('lastName').value;
  var data = {
    "firstName": first_name,
    "lastName": last_name
  };
  event.preventDefault();
  $.ajax({
    url: "/ws/sec/users/me/datas",
    type: 'put',
    data: JSON.stringify(data),
    contentType: "application/json",
    success: function(response) {
      console.log("Success " + response);
      location.reload();
    },
    error: function(response) {
      console.log("Erreur " + response.responseText);
    }
  })
});

var $profileJob = $('#profileJob');
$profileJob.on('submit', function(event) {
  var job = document.getElementById('job').value;
  var data = {
    "job": job
  };
  event.preventDefault();
  $.ajax({
    url: "/ws/sec/users/me/datas",
    type: 'put',
    data: JSON.stringify(data),
    contentType: "application/json",
    success: function(response) {
      console.log("Success " + response);
      location.reload();
    },
    error: function(response) {
      console.log("Erreur " + response.responseText);
    }
  })
});

var $profileDescription = $('#profileDescription');
$profileDescription.on('submit', function(event) {
  var jobDescription = document.getElementById('jobDescription').value;
  var data = {
    "jobDescriptionText": jobDescription
  };
  event.preventDefault();
  $.ajax({
    url: "/ws/sec/users/me/datas",
    type: 'put',
    data: JSON.stringify(data),
    contentType: "application/json",
    success: function(response) {
      console.log("Success " + response);
      location.reload();
    },
    error: function(response) {
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
  /*audioVideoRecorder.clearRecordedData();*/
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
  document.getElementById("modal-footer_add_video_file_recording").style.display = "none";
});

if (nameVideoRecord) {
  nameVideoRecord.onclick = function () {
    console.log("click on record name lsf");
    $('#uploadRecordedVideoFile').attr('action', '/ws/sec/uploadRecordedVideoFileForName');
    $('#uploadSelectedVideoFile').attr('action', '/ws/sec/uploadSelectedVideoFileForName');
    labelRecord = document.getElementById('label_record');
    document.getElementById('label_record').style.visibility = "visible";
    document.getElementById('label_record').style.display = "block";
    document.getElementById('label_record_job_description').style.display = "none";
    labelAfterRecord = document.getElementById('label_after_record');
    document.getElementById('add_video_file_dailymotion_title_name_lsf').style.visibility = "visible";
    document.getElementById('add_video_file_dailymotion_title_name_lsf').style.display = "block";
    document.getElementById('add_video_file_dailymotion_title_description_job_lsf').style.display = "none";
  };
}

if (jobVideoRecord) {
  jobVideoRecord.onclick = function () {
    console.log("click on record job description lsf");
    $('#uploadRecordedVideoFile').attr('action', '/ws/sec/uploadRecordedVideoFileForJobDescription');
    $('#uploadSelectedVideoFile').attr('action', '/ws/sec/uploadSelectedVideoFileForJobDescription');
    document.getElementById('label_record_job_description').style.visibility = "visible";
    document.getElementById('label_record_job_description').style.display = "block";
    document.getElementById('label_record').style.display = "none";
    labelRecord = document.getElementById('label_record_job_description');
    labelAfterRecord = document.getElementById('label_after_record_job_description');
    document.getElementById('add_video_file_dailymotion_title_description_job_lsf').style.visibility = "visible";
    document.getElementById('add_video_file_dailymotion_title_description_job_lsf').style.display = "block";
    document.getElementById('add_video_file_dailymotion_title_name_lsf').style.display = "none";
  };
}

function editProfil() {
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

  if ($('#user_name-pen').is(":hidden")) {
    $('#changeUserName').css('pointer-events', '');
    $('#user_name-pen').show();
  } else {
    $('#changeUserName').css('pointer-events', 'none');
    $('#user_name-pen').hide();
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
    if ($('#jobName').val() != null ) {
      $('#jobVideo-record').show();
    } else {
      $('#jobVideo-record').hide();
    }
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

var inputEmail = document.getElementById('email');
inputEmail.addEventListener('keyup',checkEmail);
var submitChangeMail = document.getElementById("submit-change-mail");
var regexEmail = new RegExp('[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}');
var errorUserExist = document.getElementById('errorUserExist');

function checkEmail() {
  $('#errorUserExist').addClass("hidden");
  var valueEmail = inputEmail.value;
  if(!regexEmail.test(valueEmail)) {
    $('.errorRegexEmail').removeClass("hidden");
    submitChangeMail.disabled = true;
  }else {
    $('.errorRegexEmail').addClass("hidden");
      submitChangeMail.disabled = false;
  }
}

function sendMail() {
  console.log("send Mail");
  user = {
    email: inputEmail.value
  };
  $.ajax({
    url: "/ws/sec/sendMailForChangeEmail",
    type: 'post',
    data: JSON.stringify(user),
    contentType: "application/json",
    success: function (response) {
      console.log(response);
      $('#add_email').modal('hide');
      $("#validate_send_mail").modal('show');
      setTimeout(function () {
        $("#validate_send_mail").modal('hide');
        location.reload();
      }, 3000);
    },
    error: function (response) {
      console.log(response.responseJSON);
      errorUserExist.textContent = response.responseJSON.errorMessage;
      $('#errorUserExist').removeClass("hidden");
      submitChangeMail.disabled = true;
    }
  })
}

var $add_email = $('#add_email');
$add_email.on('hidden.bs.modal', function() {
  $('#errorUserExist').addClass("hidden");
  submitChangeMail.disabled = true;
  $('#email').val("");
});

var inputLastName = document.getElementById('lastName');
var inputFirstName = document.getElementById('firstName');
var regexName = new RegExp('^[A-zÀ-ÖØ-öø-ÿ]{1,30}$');
inputLastName.addEventListener('keyup',checkLastName);
inputFirstName.addEventListener('keyup',checkFirstName);
var lastName = new Boolean(true);
var firstName = new Boolean(true);
var submitChangeName = document.getElementById("submit-change-name");



function checkLastName() {
  var valueLastName = inputLastName.value;

  if (!regexName.test(valueLastName)) {
    lastName = false;
    $('.errorRegexLastName').removeClass("hidden");
    submitChangeName.disabled = true;
  }else {
    lastName = true;
    $('.errorRegexLastName').addClass("hidden");
    if (firstName != false) {
      submitChangeName.disabled = false;
    }
  }
}

function checkFirstName() {
  var valueFirstName = inputFirstName.value;

  if (!regexName.test(valueFirstName)) {
    firstName = false;
    $('.errorRegexFirstName').removeClass("hidden");
    submitChangeName.disabled = true;
  } else {
    firstName = true;
    $('.errorRegexFirstName').addClass("hidden");
    if (lastName != false) {
      submitChangeName.disabled = false;
    }
  }
}

var $formUploadSelectedVideoFile = $('#uploadSelectedVideoFile');
$formUploadSelectedVideoFile.on('submit', function(event) {
  if (document.getElementById("InputFile").value) {
    $(".spinner").removeClass("spinner_hidden").addClass("spinner_show");
    $(".spinner").css("z-index", "1500").visibility = "visible";
    $("#submitButtonFileDailymotion").css("color", "black");
    var $form = $(this);
    var formdata = new FormData($form[0]);
    var data = (formdata !== null) ? formdata : $form.serialize();

    event.preventDefault();
    $.ajax({
      url: $formUploadSelectedVideoFile.attr('action'),
      type: 'post',
      data: data,
      contentType: false,
      processData: false,
      success: function (response) {
        var url = response;
        errorSelectedSpan.style.visibility = "hidden";
        $(".spinner").visibility = "hidden";
        console.log("Success " + response);
        location.reload();
      },
      error: function (response) {
        errorSelectedSpan.textContent = response.responseText;
        errorSelectedSpan.style.visibility = "visible";
        $(".spinner").css("z-index", "-1").css("opacity", "0.1");
        $(".spinner").visibility = "hidden";
        console.log("Erreur " + response.responseText);
      }
    })
  } else {
    event.preventDefault();
    errorSelectedSpan.textContent = "Vous devez séléctionner un fichier";
    errorSelectedSpan.style.visibility = "visible";
  }

});

$formUploadSelectedVideoFile.on('input', function(event) {
  document.getElementById('errorSelectedSpan').style.visibility="hidden";
});

var $add_video_file_dailymotion = $('#add_video_file_dailymotion');
$add_video_file_dailymotion.on('hidden.bs.modal', function() {
  console.log("hidden add_video_file_dailymotion modal");
  var fileName = document.getElementById("fileName");

  if ($('#uploadSelectedVideoFile').find('#errorSelectedSpan').length) {
    errorSelectedSpan.style.display="none";
    /*$('#signNameSelected').val("");*/
  }
  document.getElementById('submitButtonFileDailymotion').disabled=true;

  document.getElementById("InputFile").value="";

  document.getElementById("subtitle_for_modal_video").style.display="";
  if (fileName != null) {
    fileName.value="";
    document.getElementById('fileName').style.display = "";
  }
});

function onClick() {
  event.preventDefault();
  document.getElementById('InputFile').click()
}

$(document).ready(function(){
  var fileName = document.getElementById("fileName");

  $('input[type="file"]').change(function(e){
    $("#add_video_file_dailymotion").modal('show');
    document.getElementById('submitButtonFileDailymotion').disabled=false;

    if (fileName != null) {
      fileName.textContent = e.target.files[0].name;
      document.getElementById('fileName').style.display = "";
    }
  });

});