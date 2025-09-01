package com.orange.signsatwork.biz.view.controller;

import com.orange.signsatwork.biz.domain.CommunityType;
import com.orange.signsatwork.biz.domain.LabelType;
import com.orange.signsatwork.biz.domain.User;
import com.orange.signsatwork.biz.persistence.model.CommunityViewData;
import com.orange.signsatwork.biz.persistence.model.LabelViewData;
import com.orange.signsatwork.biz.persistence.service.MessageByLocaleService;
import com.orange.signsatwork.biz.persistence.service.Services;
import com.orange.signsatwork.biz.security.AppSecurityAdmin;
import com.orange.signsatwork.biz.view.model.AuthentModel;
import com.orange.signsatwork.biz.view.model.CommunityCreationView;
import com.orange.signsatwork.biz.view.model.LabelCreationView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.orange.signsatwork.biz.domain.CommunityType.Job;
@Controller
public class LabelController {

  @Autowired
  private Services services;
  @Autowired
  MessageByLocaleService messageByLocaleService;

  @Autowired
  private AppSecurityAdmin appSecurityAdmin;

  @Value("${app.name}")
  String appName;

  @Secured("ROLE_USER_A")
  @RequestMapping(value = "/sec/label/search")
  public String searchLabel(@ModelAttribute LabelCreationView labelCreationView, @RequestParam("id") Long signId, @RequestParam("type") Optional<LabelType> labelType) {
    if (signId == null) {
      signId = 0L;
    }
    String name = labelCreationView.getName();
    if (labelType.isPresent()) {
      return "redirect:/sec/labels-suggest?name=" + URLEncoder.encode(name) + "&id=" + signId + "&type=" + labelType.get();
    } else {
      return "redirect:/sec/labels-suggest?name=" + URLEncoder.encode(name) + "&id=" + signId;
    }
  }

  @Secured({"ROLE_USER","ROLE_ADMIN"})
  @RequestMapping(value = "/sec/labels-suggest")
  public String showlabelsSuggest(Model model, @RequestParam("name") String name, @RequestParam("id") Long signId, @RequestParam("type") Optional<LabelType> labelType, Principal principal) {
    boolean isAdmin = appSecurityAdmin.isAdmin(principal);
    User user = services.user().withUserName(principal.getName());
    String decodeName = URLDecoder.decode(name);
    model.addAttribute("title", messageByLocaleService.getMessage("label.new"));
    AuthentModel.addAuthenticatedModel(model, AuthentModel.isAuthenticated(principal));
    List<Object[]> queryLabels = services.label().searchBis(decodeName.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE").toUpperCase());

    List<LabelViewData> labelViewData = queryLabels.stream()
      .map(objectArray -> new LabelViewData(objectArray))
      .collect(Collectors.toList());

    model.addAttribute("labelName", decodeName.trim());
    model.addAttribute("isLabelAlreadyExist", false);
    List<LabelViewData> labelsWithSameName = new ArrayList<>();
    for (LabelViewData label:labelViewData) {
      if (label.name.trim().replace("œ", "oe").replace("æ", "ae").equalsIgnoreCase(decodeName.trim().replace("œ", "oe").replace("æ", "ae").replace("Œ","OE").replace("Æ'", "AE")) ) {
        model.addAttribute("isLabelAlreadyExist", true);
        model.addAttribute("labelMatche", label);
      } else {
        labelsWithSameName.add(label);
      }
    }

    /*List<Object[]> queryCommunitiesForFavorite = services.community().allForFavorite(user.id);
    List<CommunityViewData> communitiesViewData = queryCommunitiesForFavorite.stream()
      .map(objectArray -> new CommunityViewData(objectArray))
      .filter(c -> communitiesWithSameName.stream().map(co -> co.id).collect(Collectors.toList()).contains(c.id))
      .sorted((c1, c2) -> c1.name.compareTo(c2.name))
      .collect(Collectors.toList());*/

    model.addAttribute("labelsWithSameName", labelsWithSameName);

    LabelCreationView labelCreationView = new LabelCreationView();
    labelCreationView.setName(decodeName.trim());
    model.addAttribute("labelCreationView", labelCreationView);

    model.addAttribute("signId", signId);
    if (labelType.isPresent()) {
      model.addAttribute("labelType", labelType.get());
    }
    model.addAttribute("appName", appName);
    model.addAttribute("isAdmin", isAdmin);

    if (isAdmin && labelType.isPresent() && labelType.get().equals(LabelType.Admin)) {
      model.addAttribute("backUrl", "/sec/admin/manage_labels");
      return "admin/labels-suggest";
    } else {
      return "labels-suggest";
    }
  }
}
