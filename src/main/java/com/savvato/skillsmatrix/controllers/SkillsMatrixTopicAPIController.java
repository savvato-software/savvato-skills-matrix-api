package com.savvato.skillsmatrix.controllers;

import com.savvato.skillsmatrix.controllers.dto.LineItemRequest;
import com.savvato.skillsmatrix.services.SkillsMatrixTopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class SkillsMatrixTopicAPIController {

    @Autowired
    SkillsMatrixTopicService skillsMatrixTopicService;

    SkillsMatrixTopicAPIController() {

    }

    @RequestMapping(value = { "/api/v1/skills-matrix/topic/{topicId}/addExistingLineItemAsChild" }, method=RequestMethod.POST)
    public boolean addExistingLineItemAsChild(@PathVariable String topicId, @RequestBody @Valid LineItemRequest request) {
        return this.skillsMatrixTopicService.addExistingLineItemAsChild(topicId, request.lineItemId);
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/topic/{topicId}/lineItem/{lineItemId}" }, method=RequestMethod.DELETE)
    public boolean deleteLineItemAsChild(@PathVariable String topicId, @PathVariable String lineItemId) {
        return this.skillsMatrixTopicService.removeLineItemAsChild(topicId, lineItemId);
    }
}
