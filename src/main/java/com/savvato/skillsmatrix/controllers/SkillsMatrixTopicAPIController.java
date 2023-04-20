package com.savvato.skillsmatrix.controllers;

import com.savvato.skillsmatrix.entities.SkillsMatrix;
import com.savvato.skillsmatrix.services.SkillsMatrixTopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class SkillsMatrixTopicAPIController {

    @Autowired
    SkillsMatrixTopicService skillsMatrixTopicService;

    SkillsMatrixTopicAPIController() {

    }

    @RequestMapping(value = { "/api/techprofile/topic/{topicId}/addExistingLineItemAsChild" }, method= RequestMethod.POST)
    public boolean addExistingLineItemAsChild(HttpServletRequest request, @PathVariable Long topicId) {
        Long existingLineItemId = Long.parseLong(request.getParameter("existingLineItemId"));
        return this.skillsMatrixTopicService.addExistingLineItemAsChild(topicId, existingLineItemId);
    }

    @RequestMapping(value = { "/api/techprofile/topic/{topicId}/lineItem/{lineItemId}" }, method=RequestMethod.DELETE)
    public boolean deleteLineItemAsChild(HttpServletRequest request, @PathVariable Long topicId, @PathVariable Long lineItemId) {
        return this.skillsMatrixTopicService.removeLineItemAsChild(topicId, lineItemId);
    }

}
