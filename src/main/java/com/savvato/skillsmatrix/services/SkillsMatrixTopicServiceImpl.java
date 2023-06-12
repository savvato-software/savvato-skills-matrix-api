package com.savvato.skillsmatrix.services;

import com.savvato.skillsmatrix.entities.SkillsMatrixLineItem;
import com.savvato.skillsmatrix.entities.SkillsMatrixTopic;
import com.savvato.skillsmatrix.repositories.SkillsMatrixLineItemRepository;
import com.savvato.skillsmatrix.repositories.SkillsMatrixTopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class SkillsMatrixTopicServiceImpl implements SkillsMatrixTopicService {

	@PersistenceContext
	EntityManager em;
	
	@Autowired
	SkillsMatrixTopicRepository skillsMatrixTopicRepository;
	
	@Autowired
	SkillsMatrixLineItemRepository skillsMatrixLineItemRepository;
	
	@Override
	@Transactional
	public boolean addExistingLineItemAsChild(String topicId, String existingLineItemId) {
		Optional<SkillsMatrixTopic> topic = skillsMatrixTopicRepository.findById(topicId);

		if (topic.isPresent()) {
			SkillsMatrixTopic _newTopic = topic.get();

			Optional<SkillsMatrixLineItem> existingLineItem = skillsMatrixLineItemRepository.findById(existingLineItemId);

			if (existingLineItem.isPresent()) {
				SkillsMatrixLineItem _existingLineItem = existingLineItem.get();

				String existingLineItemParentTopicId = null;
				// query for the existingLineItem's parent topic id
				List resultList = em.createNativeQuery("SELECT skills_matrix_topic_id FROM skills_matrix_topic_line_item_map WHERE skills_matrix_line_item_id=:existingLineItemId")
					.setParameter("existingLineItemId", existingLineItemId)
					.getResultList();

				if (resultList.size() > 0 && resultList.get(0) != null) {
					existingLineItemParentTopicId = resultList.get(0).toString();
				}

				// query for the existingLineItem's sequence number
				resultList = em.createNativeQuery("SELECT sequence FROM skills_matrix_topic_line_item_map WHERE skills_matrix_topic_id=:existingLineItemParentTopicId AND skills_matrix_line_item_id=:existingLineItemId")
					.setParameter("existingLineItemParentTopicId", existingLineItemParentTopicId)
					.setParameter("existingLineItemId", existingLineItemId)
					.getResultList();

				Long originalSequenceNum = 0L;
				if (resultList.size() > 0 && resultList.get(0) != null) {
					originalSequenceNum = Long.parseLong(resultList.get(0).toString());
				}

				em.createNativeQuery("DELETE FROM skills_matrix_topic_line_item_map WHERE skills_matrix_topic_id=:existingLineItemParentTopicId AND skills_matrix_line_item_id=:existingLineItemId")
					.setParameter("existingLineItemParentTopicId", existingLineItemParentTopicId)
					.setParameter("existingLineItemId", existingLineItemId)
					.executeUpdate();

				// update the sequence numbers of all the other line items
				em.createNativeQuery("UPDATE skills_matrix_topic_line_item_map SET sequence=sequence-1 WHERE skills_matrix_topic_id=:existingLineItemParentTopicId AND sequence>:originalSequenceNum")
					.setParameter("existingLineItemParentTopicId", existingLineItemParentTopicId)
					.setParameter("originalSequenceNum", originalSequenceNum)
					.executeUpdate();

				resultList = em.createNativeQuery("SELECT max(sequence) FROM skills_matrix_topic_line_item_map where skills_matrix_topic_id=:newTopicId")
						.setParameter("newTopicId", _newTopic.getId())
						.getResultList();

				Long currentMaxSequenceNum = 0L;
						
				if (resultList.size() > 0 && resultList.get(0) != null)
					currentMaxSequenceNum = Long.parseLong(resultList.get(0).toString());
				
				em.createNativeQuery("INSERT INTO skills_matrix_topic_line_item_map (skills_matrix_topic_id, skills_matrix_line_item_id, sequence) VALUES (:newTopicId, :existingLineItemId, :sequence)")
					.setParameter("newTopicId", _newTopic.getId())
					.setParameter("existingLineItemId", existingLineItemId)
					.setParameter("sequence", currentMaxSequenceNum + 1)
					.executeUpdate();

				return true;
			}
		}

		return false;
	}
	
	@Override
	@Transactional
	public boolean removeLineItemAsChild(String topicId, String existingLineItemId) {
		Optional<SkillsMatrixTopic> topic = skillsMatrixTopicRepository.findById(topicId);

		if (topic.isPresent()) {
			Optional<SkillsMatrixLineItem> existingLineItem = skillsMatrixLineItemRepository.findById(existingLineItemId);

			if (existingLineItem.isPresent()) {

				// query for the ids for all the skills related to this line item
				List resultList = em.createNativeQuery("SELECT skills_matrix_skill_id FROM skills_matrix_line_item_skill_map WHERE skills_matrix_line_item_id=:existingLineItemId")
					.setParameter("existingLineItemId", existingLineItemId)
					.getResultList();

				// save the list in a variable. ids are strings.
				List<String> skillIds = new ArrayList<String>();
				for (Object o : resultList) {
					skillIds.add(o.toString());
				}

				// delete each skill, first from the map and then from the database
				for (String skillId : skillIds) {
					em.createNativeQuery("DELETE FROM skills_matrix_line_item_skill_map WHERE skills_matrix_line_item_id=:existingLineItemId AND skills_matrix_skill_id=:skillId")
						.setParameter("existingLineItemId", existingLineItemId)
						.setParameter("skillId", skillId)
						.executeUpdate();

					em.createNativeQuery("DELETE FROM skills_matrix_skill WHERE id=:skillId")
						.setParameter("skillId", skillId)
						.executeUpdate();
				}

				Long originalSequenceNum = 0L;
				// get the sequence number of the line item
				resultList = em.createNativeQuery("SELECT sequence FROM skills_matrix_topic_line_item_map WHERE skills_matrix_topic_id=:topicId AND skills_matrix_line_item_id=:existingLineItemId")
					.setParameter("topicId", topicId)
					.setParameter("existingLineItemId", existingLineItemId)
					.getResultList();

				if (resultList.size() > 0 && resultList.get(0) != null) {
					originalSequenceNum = Long.parseLong(resultList.get(0).toString());
				}

				// remove the line item from the topic map, and then from the database
				em.createNativeQuery("DELETE FROM skills_matrix_topic_line_item_map WHERE skills_matrix_topic_id=:topicId AND skills_matrix_line_item_id=:existingLineItemId")
					.setParameter("topicId", topicId)
					.setParameter("existingLineItemId", existingLineItemId)
					.executeUpdate();

				// update the sequence numbers of all the other line items
				em.createNativeQuery("UPDATE skills_matrix_topic_line_item_map SET sequence=sequence-1 WHERE skills_matrix_topic_id=:topicId AND sequence>:originalSequenceNum")
					.setParameter("topicId", topicId)
					.setParameter("originalSequenceNum", originalSequenceNum)
					.executeUpdate();

				em.createNativeQuery("DELETE FROM skills_matrix_line_item WHERE id=:existingLineItemId")
					.setParameter("existingLineItemId", existingLineItemId)
					.executeUpdate();

				return true;
			}
		}

		return false;

	}
}
