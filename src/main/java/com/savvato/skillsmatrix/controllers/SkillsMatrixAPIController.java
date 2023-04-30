package com.savvato.skillsmatrix.controllers;

import com.savvato.skillsmatrix.constants.Constants;
import com.savvato.skillsmatrix.controllers.dto.SkillRequest;
import com.savvato.skillsmatrix.dto.SkillsMatrixSummaryDTO;
import com.savvato.skillsmatrix.entities.SkillsMatrix;
import com.savvato.skillsmatrix.entities.SkillsMatrixLineItem;
import com.savvato.skillsmatrix.entities.SkillsMatrixSkill;
import com.savvato.skillsmatrix.entities.SkillsMatrixTopic;
import com.savvato.skillsmatrix.services.SkillsMatrixService;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController 
public class SkillsMatrixAPIController {

    @Autowired
    SkillsMatrixService skillsMatrixService;

    public SkillsMatrixAPIController() {

    }

    @RequestMapping(value = { "/api/v1/skills-matrix"}, method = RequestMethod.GET)
    public ResponseEntity<List<SkillsMatrixSummaryDTO>> getAll() {

        return null;
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/{id}" }, method=RequestMethod.GET)
    public SkillsMatrix get(@PathVariable Long id) {
        return skillsMatrixService.get(id);
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/topics/new" }, method=RequestMethod.POST)
    public SkillsMatrixTopic newTopic(HttpServletRequest request) {
        String name = request.getParameter("topicName");

        return skillsMatrixService.addTopic(name);
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/topic/{topicId}" }, method=RequestMethod.POST)
    public SkillsMatrixTopic updateTopic(HttpServletRequest request, @PathVariable Long topicId) {
        SkillsMatrixTopic rtn = null;

        try {
            String str = request.getReader().lines().collect(Collectors.joining());
            net.minidev.json.parser.JSONParser parser = new JSONParser();

            JSONObject obj = (JSONObject)parser.parse(str);

            String newName = ((JSONObject)obj.get("topic")).get("name").toString();

            rtn = skillsMatrixService.updateTopic(
                    topicId,
                    newName);

        } catch (IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return rtn;
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/topics/{id}/lineitem/new" }, method=RequestMethod.POST)
    public SkillsMatrixLineItem newLineItem(HttpServletRequest request, @PathVariable Long id) {
        String name = request.getParameter("lineItemName");
        String l0desc = request.getParameter(Constants.L0DESCRIPTION);
        String l1desc = request.getParameter(Constants.L1DESCRIPTION);
        String l2desc = request.getParameter(Constants.L2DESCRIPTION);
        String l3desc = request.getParameter(Constants.L3DESCRIPTION);

        if (l0desc == null) l0desc = "Level 1 skill.";
        if (l1desc == null) l1desc = "Level 2 skill.";
        if (l2desc == null) l2desc = "Level 3 skill.";
        if (l3desc == null) l3desc = "Level 4 skill.";

        return skillsMatrixService.addLineItem(id, name);
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/lineitem/{lineItemId}" }, method=RequestMethod.GET)
    public SkillsMatrixLineItem getLineItem(HttpServletRequest request, @PathVariable Long lineItemId) {
        Optional<SkillsMatrixLineItem> rtn = this.skillsMatrixService.getLineItem(lineItemId);

        if (rtn.isPresent())
            return rtn.get();
        else
            return null;
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/lineitem/{lineItemId}" }, method=RequestMethod.POST)
    public SkillsMatrixLineItem updateLineItem(HttpServletRequest request, @PathVariable Long lineItemId) {
        SkillsMatrixLineItem rtn = null;

        try {
            String str = request.getReader().lines().collect(Collectors.joining());
            net.minidev.json.parser.JSONParser parser = new JSONParser();

            JSONObject obj = (JSONObject)((JSONObject)parser.parse(str)).get("lineItem");

            rtn = skillsMatrixService.updateLineItem(
                    Long.parseLong(obj.getAsString("id")),
                    obj.getAsString("name"));

        } catch (IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return rtn;
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/sequences" }, method=RequestMethod.POST)
    public boolean updateLineItemSequences(HttpServletRequest request) {
        try {
            String str = request.getReader().lines().collect(Collectors.joining());

            net.minidev.json.parser.JSONParser parser = new JSONParser();

            JSONObject obj = (JSONObject)parser.parse(str);
            JSONArray arr = (JSONArray)obj.get("arr");

            long numOfTopics = arr.size();

            for (long x=0; x < numOfTopics; x++) {
                JSONArray topicArr = (JSONArray)arr.get((int)x);
                long numOfLineItems = topicArr.size();

                for (long y=0; y < numOfLineItems; y++) {
                    JSONArray liArr = (JSONArray)topicArr.get((int)y);
                    String str2 = liArr.toString();
                    str2 = str2.substring(1, str2.length() - 1);
                    List<String> list = Arrays.asList(str2.split("\\s*,\\s*"));

                    long[] larr = new long[5];

                    larr[0] = Long.parseLong(list.get(0));
                    larr[1] = Long.parseLong(list.get(1));
                    larr[2] = Long.parseLong(list.get(2));
                    larr[3] = Long.parseLong(list.get(3));
                    larr[4] = Long.parseLong(list.get(4));

                    skillsMatrixService.updateSequencesRelatedToATopicAndItsLineItems(larr);
                }
            }

        } catch (IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return true;
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/skill/new" }, method=RequestMethod.POST)
    public SkillsMatrixSkill newSkill(@RequestBody @Valid SkillRequest req) {
        return this.skillsMatrixService.addSkill(req.lineItemId, req.skillLevel, req.skillDescription);
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/skill/delete" }, method=RequestMethod.DELETE)
    public void deleteSkill(@RequestBody @Valid SkillRequest req) {
        this.skillsMatrixService.deleteSkill(req.lineItemId, req.skillId);
    }

}
