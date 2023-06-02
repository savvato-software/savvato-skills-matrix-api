package com.savvato.skillsmatrix.services;

import com.savvato.skillsmatrix.dto.SkillsMatrixSummaryDTO;
import com.savvato.skillsmatrix.entities.SkillsMatrix;
import com.savvato.skillsmatrix.entities.SkillsMatrixLineItem;
import com.savvato.skillsmatrix.entities.SkillsMatrixSkill;
import com.savvato.skillsmatrix.entities.SkillsMatrixTopic;
import net.minidev.json.parser.ParseException;

import java.util.List;
import java.util.Optional;

public interface SkillsMatrixService {

	SkillsMatrix importSkillsMatrix(String str) throws ParseException;

	public Iterable<SkillsMatrixSummaryDTO> getAllMatrixSummaries();
	public SkillsMatrix get(String id);

	SkillsMatrix addSkillsMatrix(String name);
	SkillsMatrix addSkillsMatrix(String name, String skillsMatrixId);
	SkillsMatrixSummaryDTO updateSkillsMatrix(String skillsMatrixId, String name);

	public SkillsMatrixTopic addTopic(String skillsMatrixId, String topicName);
	public SkillsMatrixTopic addTopic(String skillsMatrixId, String topicName, String topicId, Long sequence);
	public SkillsMatrixTopic updateTopic(String topicId, String name);

	public SkillsMatrixLineItem addLineItem(String topicId, String lineItemName);
	public SkillsMatrixLineItem addLineItem(String topicId, String lineItemName, String lineItemId);
	public SkillsMatrixLineItem addLineItem(String topicId, String lineItemName, String lineItemId, Long sequence);
	public Optional<SkillsMatrixLineItem> getLineItem(String lineItemId);
	public SkillsMatrixLineItem updateLineItem(String lineItemId, String lineItemName);

	public SkillsMatrixSkill addSkill(String lineItemId, Long level, String skillDescription);
	public SkillsMatrixSkill addSkill(String lineItemId, Long level, String skillDescription, String skillId, String detailLineItemId, Long sequence);

	public void deleteSkill(String lineItemId, String skillId);

	public SkillsMatrixSkill updateSkill(String skillId, String desc, String detailLineItemId);

	public boolean updateSequencesRelatedToATopicAndItsLineItems(String[] arr);

	public boolean updateSequencesRelatedToALineItemAndItsSkills(String[] arr);

	public boolean updateSkillLevel(String skillsMatrixId, String lineItemId, String skillId, long level);
}
