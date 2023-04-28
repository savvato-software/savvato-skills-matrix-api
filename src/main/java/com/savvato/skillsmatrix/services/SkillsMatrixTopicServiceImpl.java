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
	public boolean addExistingLineItemAsChild(Long topicId, Long existingLineItemId) {
		Optional<SkillsMatrixTopic> topic = skillsMatrixTopicRepository.findById(topicId);

		if (topic.isPresent()) {
			SkillsMatrixTopic _topic = topic.get();

			Optional<SkillsMatrixLineItem> existingLineItemIdOptional = skillsMatrixLineItemRepository.findById(existingLineItemId);

			if (existingLineItemIdOptional.isPresent()) {
				SkillsMatrixLineItem _existingLineItem = existingLineItemIdOptional.get();

				List resultList = em.createNativeQuery("SELECT max(sequence) FROM skills_matrix_topic_line_item_map where skills_matrix_topic_id=:topicId")
						.setParameter("topicId", _topic.getId())
						.getResultList();

				Long currentMaxSequenceNum = 0L;
						
				if (resultList.size() > 0 && resultList.get(0) != null)
					currentMaxSequenceNum = Long.parseLong(resultList.get(0).toString());
				
				em.createNativeQuery("INSERT INTO skills_matrix_topic_line_item_map (skills_matrix_topic_id, skills_matrix_line_item_id, sequence) VALUES (:topicId, :lineItemId, :sequence)")
					.setParameter("topicId", _topic.getId())
					.setParameter("lineItemId", _existingLineItem.getId())
					.setParameter("sequence", currentMaxSequenceNum + 1)
					.executeUpdate();

				return true;
			}
		}

		return false;
	}
	
	@Override
	public boolean removeLineItemAsChild(Long topicId, Long existingLineItemId) { 
		Optional<SkillsMatrixTopic> topic = skillsMatrixTopicRepository.findById(topicId);

		if (topic.isPresent()) {
			SkillsMatrixTopic _topic = topic.get();
			Set<SkillsMatrixLineItem> topicLineItems = _topic.getLineItems();
			
			topicLineItems.removeIf(li -> li.getId() == existingLineItemId);
			
			// necessary?
			_topic.setLineItems(topicLineItems);
			
			skillsMatrixTopicRepository.save(_topic);
			return true;
		}

		return false;
	}
}
