package com.savvato.skillsmatrix.controllers;

import com.savvato.skillsmatrix.controllers.dto.SkillsMatrixDTO;
import com.savvato.skillsmatrix.controllers.dto.SkillsMatrixSummaryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController 
public class SkillsMatrixAPIController {

    public SkillsMatrixAPIController() {

    }

    @RequestMapping(value = { "/api/v1/skills-matrix"}, method = RequestMethod.GET)
    public ResponseEntity<List<SkillsMatrixSummaryDTO>> getAll() {

        return null;
    }

    @RequestMapping(value = { "/api/v1/skills-matrix/{userId}"}, method = RequestMethod.GET)
    public ResponseEntity<List<SkillsMatrixDTO>> getById() {

        return null;
    }

}
