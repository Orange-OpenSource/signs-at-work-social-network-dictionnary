package com.orange.signsatwork.biz.webservice.controller;

import com.orange.signsatwork.biz.domain.*;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.security.AppSecurityAdmin;
import com.orange.signsatwork.biz.webservice.model.CommunityCreationViewApi;
import com.orange.signsatwork.biz.webservice.model.CommunityResponseApi;
import com.orange.signsatwork.biz.webservice.model.LabelCreationViewApi;
import com.orange.signsatwork.biz.webservice.model.LabelResponseApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
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
    labelResponseApi.labelId = label.id;
    /*String values = user.name() + ';' + messageByLocaleService.getMessage(CommunityType.Job.toString()) + ';' + communityCreationViewApi.getName();
    MessageServer messageServer = new MessageServer(new Date(), "CreateJobCommunityMessage", values, ActionType.NO);
    services.messageServerService().addMessageServer(messageServer);*/

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

    response.setStatus(HttpServletResponse.SC_OK);
    return labelResponseApi;
  }


}
