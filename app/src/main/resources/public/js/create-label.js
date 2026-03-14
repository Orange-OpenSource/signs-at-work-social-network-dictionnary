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
console.log("Cool, create_label.js is loaded :)");


var inputLabelName = document.getElementById('labelName');
var submitCreateModal = document.getElementById("submit-create-modal");
var submitForceCreateModal = document.getElementById("submit-force-create-modal");

var regexName = new RegExp('^[0-9A-zÀ-ÖØ-öø-ÿ- ()œæŒÆ\'°/:]{1,255}$');

inputLabelName.addEventListener('keyup',checkLabelName);
submitCreateModal.disabled = true;


function checkLabelName() {
  var valueLabelName = inputLabelName.value;

  errorCreate.style.display = "none";
  warningCreate.style.display = "none";
  if (valueLabelName != '') {
    if (!regexName.test(valueLabelName)) {
      $('.errorRegexLabelName').removeClass("hidden");
      submitCreateModal.disabled = true;
    } else {
      $('.errorRegexLabelName').addClass("hidden");
      submitCreateModal.disabled = false;
    }
  } else {
    $('.errorRegexLabelName').addClass("hidden");
    submitCreateModal.disabled = true;
  }
}

$('#create_label').on('hidden.bs.modal', function (e) {
  var errorCreate = document.getElementById('errorCreate');
  var warningCreate = document.getElementById('warningCreate');
  labelName.value = '';
  errorCreate.style.display = "none";
  warningCreate.style.display = "none";
  submitCreateModal.style.display="block";
  submitCreateModal.disabled = true;
  submitForceCreateModal.style.display = "none";
  $('.errorRegexLabelName').addClass("hidden");
})

$('#create-new-label_add_sign').on('hidden.bs.modal', function (e) {
  var errorCreate = document.getElementById('errorCreate');
  var warningCreate = document.getElementById('warningCreate');
  labelName.value = '';
  errorCreate.style.display = "none";
  warningCreate.style.display = "none";
  submitCreateModal.style.display="block";
  submitCreateModal.disabled = true;
  submitForceCreateModal.style.display = "none";
  $('.errorRegexLabelName').addClass("hidden");

  if ($('.modal:visible').length) {
    $('body').addClass('modal-open');
  }
})

$('#create-new-label_add_sign').on('shown.bs.modal', function () {
  const input = $('#labelName');
  input.val('');
  input.focus();
});

function onCreateLabel(force) {

  console.log("force "+force);

    label = {
      name: labelName.value
    };
    $.ajax({
      url: "/ws/sec/labels/?force=" + force,
      type: 'post',
      data: JSON.stringify(label),
      contentType: "application/json",
      success: function (response) {
        console.log(response);
        errorCreate.style.display = "none";
        location.reload();
      },
      error: function (response) {
        console.log(response.responseJSON);
        if (response.responseJSON.errorMessage != null) {
          errorCreate.textContent = response.responseJSON.errorMessage;
          errorCreate.style.display = "block";
          submitCreateModal.disabled = true;
        } else {
          if (response.responseJSON.warningMessage != null) {
            warningCreate.textContent = response.responseJSON.warningMessage;
            warningCreate.style.display = "block";
            submitCreateModal.style.display="none";
            submitForceCreateModal.style.display = "block";
          }
        }

      }
    })

};


function onCreateLabelAddToSign(signId, videoId, force, labelsIdBelowSign) {

  console.log("force "+force);
  var signLabelsIds;

  if (Array.isArray(labelsIdBelowSign)) {
    signLabelsIds = labelsIdBelowSign.map(Number); // s'assurer que ce sont des nombres
  } else {
    try {
      signLabelsIds = JSON.parse(labelsIdBelowSign);
    } catch (e) {
      signLabelsIds = [Number(labelsIdBelowSign)];
    }
  }
  console.log("signLabelsIds après parse :", signLabelsIds);

  const signLabelsIdsCheck = [];
  const signLabelsIdsNoCheck = [];
  let i = 1;

  $("#labels-container").children("li").children("label").each(function () {
    if (!this.classList.contains("disabled")) {
      const checkbox = document.getElementById("signLabelsIds" + i);
      if (checkbox) {
        const selectedLabelId = checkbox.value;
        if (checkbox.checked) {
          signLabelsIdsCheck.push(selectedLabelId);
        } else {
          signLabelsIdsNoCheck.push(selectedLabelId);
        }
      }
    }
    i++;
  });

  console.log("✔ signLabelsIdsCheck :", signLabelsIdsCheck);
  console.log("✖ signLabelsIdsNoCheck :", signLabelsIdsNoCheck);

  const signLabelViewApi = {
    signLabelsIds: signLabelsIds,
    signLabelsIdsCheck: signLabelsIdsCheck,
    signLabelsIdsNoCheck: signLabelsIdsNoCheck
  };

  label = {
    name: labelName.value,
    signLabelViewApi: signLabelViewApi
  };
  $.ajax({
    url: "/ws/sec/label/create_label_add_sign/" + signId + "?force=" + force,
    type: 'post',
    data: JSON.stringify(label),
    contentType: "application/json",
    success: function (response) {
      console.log("✅ Backend OK :", response.labelMessage);
      errorCreate.style.display = "none";
      // 🔹 Fermer la modale
      $('#create-new-label_add_sign').modal('hide');
      $('#add_sign_to_label').modal('hide');

      // 🔹 Mettre à jour la liste des labels
      document.getElementById("labels").innerHTML = response.labelMessage;

      // 🔹 Mettre à jour l'attribut data pour la prochaine ouverture
      document
        .getElementById("validate_modal_add_label")
        .setAttribute("data-labelsidbelowsign", JSON.stringify(signLabelsIdsCheck));

      // 🔹 Changer l’icône de statut
      const statusDiv = document.getElementById("sign-label-status");
      if (signLabelsIdsCheck.length > 0) {
        statusDiv.classList.remove("add_black");
        statusDiv.classList.add("chevron");
      } else {
        statusDiv.classList.remove("chevron");
        statusDiv.classList.add("add_black");
      }

      document.getElementById("messageLabel").style.visibility = "visible";
      // Après chaque rechargement dynamique :

      // 🔹 Rechargement dynamique de la modale (sans recharger toute la page)
      $.ajax({
        url: "/sign/" + signId + "/" + videoId + "/labels",
        success: function (response) {
          const newBody = $(response).find(".modal-body").html();
          $("#add_sign_to_label .modal-body").html(newBody);
          console.log("♻️ Modal rechargée avec succès");
          attachChangeListener(); // 🔥 important
        },
        error: function () {
          console.error("❌ Erreur lors du rechargement de la modale");
        }
      });
    },
    error: function (response) {
      console.log(response.responseJSON);
      if (response.responseJSON.errorMessage != null) {
        errorCreate.textContent = response.responseJSON.errorMessage;
        errorCreate.style.display = "block";
        submitCreateModal.disabled = true;
      } else {
        if (response.responseJSON.warningMessage != null) {
          warningCreate.textContent = response.responseJSON.warningMessage;
          warningCreate.style.display = "block";
          submitCreateModal.style.display="none";
          submitForceCreateModal.style.display = "block";
        }
      }

    }
  })

};

function onContinueLabel(backUrl) {
  var url = backUrl;
  window.location = url;
};