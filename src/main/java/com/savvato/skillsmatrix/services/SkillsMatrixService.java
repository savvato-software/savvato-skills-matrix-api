package com.savvato.skillsmatrix.services;

import com.savvato.skillsmatrix.entities.SkillsMatrix;
import com.savvato.skillsmatrix.entities.SkillsMatrixLineItem;
import com.savvato.skillsmatrix.entities.SkillsMatrixTopic;

import java.util.List;
import java.util.Optional;

public interface SkillsMatrixService {

	public SkillsMatrix get(Long id);

	public SkillsMatrixTopic addTopic(String topicName);
	public SkillsMatrixTopic updateTopic(Long topicId, String name);

	public SkillsMatrixLineItem addLineItem(Long topicId, String lineItemName, String l0desc, String l1desc, String l2desc, String l3desc);
	public Optional<SkillsMatrixLineItem> getLineItem(Long lineItemId);
	public SkillsMatrixLineItem updateLineItem(Long lineItemId, String lineItemName, String l0desc, String l1desc, String l2desc, String l3desc);
	
	public boolean updateSequencesRelatedToATopicAndItsLineItems(long[] arr);
}