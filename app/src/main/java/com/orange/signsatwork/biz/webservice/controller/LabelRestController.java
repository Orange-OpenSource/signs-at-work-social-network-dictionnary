package com.orange.signsatwork.biz.webservice.controller;

import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.persistence.model.LabelViewData;
import com.orange.signsatwork.biz.persistence.model.RequestViewData;
import com.orange.signsatwork.biz.persistence.model.SignViewData;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.security.AppSecurityAdmin;
import com.orange.signsatwork.biz.view.model.AuthentModel;
import com.orange.signsatwork.biz.webservice.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class LabelRestController {

  @Autowired
  private Services services;

  @Autowired
  MessageByLocaleService messageByLocaleService;

  @Autowired
  private AppSecurityAdmin appSecurityAdmin;

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_ADMIN_LABELS, method = RequestMethod.POST, headers = {"content-type=application/json"})
  public LabelResponseApi label(@RequestBody LabelCreationViewApi labelCreationViewApi, HttpServletResponse response, Principal principal) {
    LabelResponseApi labelResponseApi = new LabelResponseApi();
    labelCreationViewApi.clearXss();
    if (services.label().withLabelName(labelCreationViewApi.getName()) != null) {
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      labelResponseApi.errorMessage = messageByLocaleService.getMessage("label_already_exist");
      return labelResponseApi;
    }

    User user = services.user().withUserName(principal.getName());

    Label label = services.label().create(labelCreationViewApi.toLabel());
    String messageType = "CreateLabelMessage";
    String values = user.name() + ';' + label.name;
    MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
    services.messageServerService().addMessageServer(messageServer);
    labelResponseApi.labelId = label.id;
    /*String values = user.name() + ';' + messageByLocaleService.getMessage(CommunityType.Job.toString()) + ';' + communityCreationViewApi.getName();
    MessageServer messageServer = new MessageServer(new Date(), "CreateJobCommunityMessage", values, ActionType.NO);
    services.messageServerService().addMessageServer(messageServer);*/

    response.setStatus(HttpServletResponse.SC_OK);
    return labelResponseApi;
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_SEC_LABEL_RENAME, method = RequestMethod.PUT)
  public LabelResponseApi renameLabel(@RequestBody LabelCreationViewApi labelCreationViewApi, @PathVariable Long labelId, @RequestParam("force") Boolean force, HttpServletRequest requestHttp, HttpServletResponse response, Principal principal) throws
    InterruptedException {
    Boolean isAdmin = false;
    LabelResponseApi labelResponseApi = new LabelResponseApi();

    if (!AuthentModel.hasRole("ROLE_ADMIN")) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      labelResponseApi.errorMessage = messageByLocaleService.getMessage("forbidden_action");
      return labelResponseApi;
    }

    User user = services.user().withUserName(principal.getName());
    User admin = services.user().getAdmin();

    labelCreationViewApi.clearXss();


    Label label = services.label().withId(labelId);
    if (!label.name.equals(labelCreationViewApi.getName())) {
      Labels labelsWithSameNameIgnoreCase = services.label().withNameIgnoreCase(labelCreationViewApi.getName().trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"));
      List<Label> labelsWithNameIgnoreCase = labelsWithSameNameIgnoreCase.stream().filter(l -> l.id != label.id).collect(Collectors.toList());
      if (labelsWithNameIgnoreCase.isEmpty()) {
          List<Object[]> queryLabels = services.label().searchBis(labelCreationViewApi.getName().trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE").toUpperCase());
          List<LabelViewData> labelViewData = queryLabels.stream()
            .map(objectArray -> new LabelViewData(objectArray))
            .filter(o -> o.id != labelId)
            .collect(Collectors.toList());
          List<LabelViewData> withSameName = new ArrayList<>();
          for (LabelViewData l : labelViewData) {
            if (!l.name.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE").equalsIgnoreCase(labelCreationViewApi.getName().trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE"))) {
              withSameName.add(l);
            }
          }
          String labelsWithSameName = null;
          if (!withSameName.isEmpty()) {
            labelsWithSameName = withSameName.stream().map(l -> l.name).collect(Collectors.joining(","));
          }
          if (labelsWithSameName != null) {
              if (force) {
                services.label().renameLabel(labelId, labelCreationViewApi.getName());
                response.setStatus(HttpServletResponse.SC_OK);
                return labelResponseApi;
              } else {
                labelResponseApi.warningMessage = messageByLocaleService.getMessage("same_name_exist", new Object[]{labelsWithSameName});
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                return labelResponseApi;
              }
            } else {
            services.label().renameLabel(labelId, labelCreationViewApi.getName());
            response.setStatus(HttpServletResponse.SC_OK);
            return labelResponseApi;
          }
      } else {
        labelResponseApi.errorMessage = messageByLocaleService.getMessage("label.name_already_exist");
        response.setStatus(HttpServletResponse.SC_CONFLICT);
        return labelResponseApi;
      }
    }

    response.setStatus(HttpServletResponse.SC_OK);

    return labelResponseApi;
  }

  @Secured("ROLE_ADMIN")
  @RequestMapping(value = RestApi.WS_ADMIN_LABEL, method = RequestMethod.DELETE)
  public LabelResponseApi deleteLabel(@PathVariable long labelId, HttpServletResponse response, HttpServletRequest request, Principal principal)  {
    List<String> emails;
    Boolean isAdmin = appSecurityAdmin.isAdmin(principal);
    LabelResponseApi labelResponseApi = new LabelResponseApi();
    Label label = services.label().withId(labelId);

    if (!isAdmin) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      labelResponseApi.errorMessage = messageByLocaleService.getMessage("you_must_be_admin");
      return labelResponseApi;
    }

    services.label().delete(label);
    String messageType = "DeleteLabelMessage";
    User user = services.user().withUserName(principal.getName());
    String values = user.name() + ';' + label.name;
    MessageServer messageServer = new MessageServer(new Date(), messageType, values, ActionType.NO);
    services.messageServerService().addMessageServer(messageServer);

    response.setStatus(HttpServletResponse.SC_OK);
    return labelResponseApi;
  }


}
