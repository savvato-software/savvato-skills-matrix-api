package com.savvato.skillsmatrix.services;

public interface SkillsMatrixTopicService {
	boolean addExistingLineItemAsChild(String topicId, String existingLineItemId);
	boolean removeLineItemAsChild(String topicId, String existingLineItemId);
}
