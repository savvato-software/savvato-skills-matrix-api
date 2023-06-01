package com.savvato.skillsmatrix.controllers;

import com.savvato.skillsmatrix.constants.Constants;
import com.savvato.skillsmatrix.controllers.dto.LineItemRequest;
import com.savvato.skillsmatrix.controllers.dto.SkillRequest;
import com.savvato.skillsmatrix.controllers.dto.SkillsMatrixRequest;
import com.savvato.skillsmatrix.controllers.dto.TopicRequest;
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
import org.springframework.http.HttpStatus;
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

    @RequestMapping(value = {"/api/v1/skills-matrix/import"}, method = RequestMethod.POST)
    public ResponseEntity importSkillsMatrix(HttpServletRequest request) throws IOException, ParseException {
        String str = request.getReader().lines().collect(Collectors.joining());

        skillsMatrixService.importSkillsMatrix(str);

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @RequestMapping(value = { "/api/v1/skills-matrix"}, method = RequestMethod.GET)
    public ResponseEntity<Iterable<SkillsMatrixSummaryDTO>> getAll() {
        return ResponseEntity.status(HttpStatus.OK).body(skillsMatrixService.getAllMatrixSummaries());
    }
    @RequestMapping(value = { "/api/v1/skills-matrix"}, method = RequestMethod.PUT)
    public ResponseEntity<SkillsMatrixSummaryDTO> updateSkillsMatrixName(@RequestBody @Valid SkillsMatrixRequest req) {
        SkillsMatrixSummaryDTO dto = skillsMatrixService.updateSkillsMatrix(req.skillsMatrixId, req.name);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/{id}" }, method=RequestMethod.GET)
    public SkillsMatrix get(@PathVariable String id) {
        return skillsMatrixService.get(id);
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/new" }, method=RequestMethod.POST)
    public SkillsMatrix newSkillsMatrix(@RequestBody @Valid SkillsMatrixRequest req) {
        return skillsMatrixService.addSkillsMatrix(req.name);
    }

    @RequestMapping(value = {"/api/v1/skills-matrix/{skillsMatrixId}" }, method=RequestMethod.POST)
    public SkillsMatrix updateSkillsMatrix(HttpServletRequest request, @PathVariable Long skillsMatrixId){

        return null;
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/topics/new" }, method=RequestMethod.POST)
    public SkillsMatrixTopic newTopic(@RequestBody @Valid TopicRequest req) {
        return skillsMatrixService.addTopic(req.skillsMatrixId, req.topicName);
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/topic/{topicId}" }, method=RequestMethod.POST)
    public SkillsMatrixTopic updateTopic(HttpServletRequest request, @PathVariable String topicId) {
        SkillsMatrixTopic rtn = null;

        try {
            String str = request.getReader().lines().collect(Collectors.joining());
            net.minidev.json.parser.JSONParser parser = new JSONParser();

            JSONObject obj = (JSONObject)parser.parse(str);

            String newName = ((JSONObject)obj.get("topic")).get("name").toString();

            rtn = skillsMatrixService.updateTopic(topicId, newName);

        } catch (IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return rtn;
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/topics/{id}/lineitem/new" }, method=RequestMethod.POST)
    public SkillsMatrixLineItem newLineItem(@RequestBody @Valid LineItemRequest req) {
        return skillsMatrixService.addLineItem(req.topicId, req.lineItemName);
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/lineitem/{lineItemId}" }, method=RequestMethod.GET)
    public SkillsMatrixLineItem getLineItem(HttpServletRequest request, @PathVariable Long lineItemId) {
        Optional<SkillsMatrixLineItem> rtn = this.skillsMatrixService.getLineItem(lineItemId+"");

        if (rtn.isPresent())
            return rtn.get();
        else
            return null;
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/lineitem/{lineItemId}" }, method=RequestMethod.POST)
    public SkillsMatrixLineItem updateLineItem(@RequestBody @Valid LineItemRequest req) {
        SkillsMatrixLineItem rtn = null;

        rtn = skillsMatrixService.updateLineItem(req.lineItemId, req.lineItemName);

        return rtn;
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/sequences/skills"}, method=RequestMethod.PUT)
    public boolean updateSkillSequences(HttpServletRequest request) {
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

                        String[] larr = new String[liArr.size()];

                        for(int z=0; z < liArr.size(); z++) {
                            larr[z] = liArr.get(z).toString();
                        }

                        skillsMatrixService.updateSequencesRelatedToALineItemAndItsSkills(larr);
                    }
                }
        } catch (IOException | ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return true;
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

                    String[] larr = new String[5];

                    larr[0] = list.get(0); // skills matrix id
                    larr[1] = list.get(1); // topic id
                    larr[2] = list.get(2); // its sequence
                    larr[3] = list.get(3); // line item id
                    larr[4] = list.get(4); // line item sequence

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

        if (req.lineItemId.isEmpty() || req.skillLevel == null || req.skillDescription.isEmpty())
            throw new IllegalArgumentException("lineItemId, skillLevel and skillDescription must have values. lineItemId = " + req.lineItemId + ", skillLevel = " + req.skillLevel + ", skillDescription = " + req.skillDescription);

        return this.skillsMatrixService.addSkill(req.lineItemId, req.skillLevel, req.skillDescription);
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/skill/delete" }, method=RequestMethod.DELETE)
    public void deleteSkill(@RequestBody @Valid SkillRequest req) {

        if (req.lineItemId.isEmpty() || req.skillId.isEmpty())
            throw new IllegalArgumentException("lineItemId and skillId must have values. lineItemId = " + req.lineItemId + ", skillId = " + req.skillId);

        this.skillsMatrixService.deleteSkill(req.lineItemId, req.skillId);
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/skill" }, method=RequestMethod.PUT)
    public void updateSkill(@RequestBody @Valid SkillRequest req) {
        this.skillsMatrixService.updateSkill(req.skillId, req.skillDescription, req.detailLineItemId);
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/skill/update-level" }, method=RequestMethod.PUT)
    public void updateSkillLevel(@RequestBody @Valid SkillRequest req) {
        this.skillsMatrixService.updateSkillLevel(req.skillsMatrixId, req.lineItemId, req.skillId, req.skillLevel);
    }
}
