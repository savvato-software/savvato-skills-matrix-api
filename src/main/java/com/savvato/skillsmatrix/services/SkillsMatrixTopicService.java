package com.savvato.skillsmatrix.services;

public interface SkillsMatrixTopicService {
	boolean addExistingLineItemAsChild(Long topicId, Long existingLineItemId);
	boolean removeLineItemAsChild(Long topicId, Long existingLineItemId);
}
