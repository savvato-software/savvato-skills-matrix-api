package com.savvato.skillsmatrix.services;

import com.savvato.skillsmatrix.entities.SkillsMatrix;
import com.savvato.skillsmatrix.entities.SkillsMatrixLineItem;
import com.savvato.skillsmatrix.entities.SkillsMatrixSkill;
import com.savvato.skillsmatrix.entities.SkillsMatrixTopic;

import java.util.List;
import java.util.Optional;

public interface SkillsMatrixService {

	public Iterable<SkillsMatrix> getAll();
	public SkillsMatrix get(Long id);

	SkillsMatrix addSkillsMatrix(String name);
	SkillsMatrix updateSkillsMatrix(Long skillsMatrixId, String name);

	public SkillsMatrixTopic addTopic(Long skillsMatrixId, String topicName);
	public SkillsMatrixTopic updateTopic(Long topicId, String name);

	public SkillsMatrixLineItem addLineItem(Long topicId, String lineItemName);
	public Optional<SkillsMatrixLineItem> getLineItem(Long lineItemId);
	public SkillsMatrixLineItem updateLineItem(Long lineItemId, String lineItemName);

	public SkillsMatrixSkill addSkill(Long lineItemId, Long level, String skillDescription);

	public void deleteSkill(Long lineItemId, Long skillId);

	public SkillsMatrixSkill updateSkill(Long skillId, String desc);

	public boolean updateSequencesRelatedToATopicAndItsLineItems(long[] arr);

	public boolean updateSequencesRelatedToALineItemAndItsSkills(long[] arr);

	public boolean updateSkillLevel(long lineItemId, long skillId, long level);
}
