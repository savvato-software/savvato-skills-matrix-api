package com.savvato.skillsmatrix.services;

import com.savvato.skillsmatrix.entities.SkillsMatrix;
import com.savvato.skillsmatrix.entities.SkillsMatrixLineItem;
import com.savvato.skillsmatrix.entities.SkillsMatrixSkill;
import com.savvato.skillsmatrix.entities.SkillsMatrixTopic;
import com.savvato.skillsmatrix.repositories.SkillsMatrixLineItemRepository;
import com.savvato.skillsmatrix.repositories.SkillsMatrixRepository;
import com.savvato.skillsmatrix.repositories.SkillsMatrixSkillRepository;
import com.savvato.skillsmatrix.repositories.SkillsMatrixTopicRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigInteger;
import java.util.*;

@Service
public class SkillsMatrixServiceImpl implements SkillsMatrixService {

	private static final Log logger = LogFactory.getLog(SkillsMatrixService.class);
	
	@PersistenceContext
	EntityManager em;
	
	@Autowired
	SkillsMatrixRepository skillsMatrixRepository;
	
	@Autowired
	SkillsMatrixTopicRepository skillsMatrixTopicRepository;

	@Autowired
	SkillsMatrixLineItemRepository skillsMatrixLineItemRepository;
	@Autowired
	SkillsMatrixSkillRepository skillsMatrixSkillRepository;

	@Override
	public SkillsMatrix get(Long id) {
		Optional<SkillsMatrix> opt = skillsMatrixRepository.findById(id);
		
		List topicSequences = getTopicSequences(id);
		List lineItemSequences = getLineItemSequences(id);
		List skillSequences = getSkillSequences(id);
		
		SkillsMatrix sm = null;
		
		ArrayList<Long> idsArr = new ArrayList<>();
		
		if (opt.isPresent()) {
			
			sm = opt.get();
			
			// make sure we have unique instances of each skillsmatrixlineitem... because a topic can share a line item with another topic.
			// if it does, by default, both topics will have the same instance. This is a problem, because the sequence that the topic appears
			// within a topic, can be different for each topic. You need unique instances to represent that.
			
			// for each topic
			Set<SkillsMatrixTopic> smTopics = sm.getTopics();
			smTopics.forEach((t) -> {
				Set<SkillsMatrixLineItem> smTopicLineItems = t.getLineItems();
				Set<SkillsMatrixLineItem> set2 = new HashSet<>();

				// for each of its line items
				smTopicLineItems.forEach((li) -> {
					//   check if we've seen it before
					if (idsArr.contains(li.getId())) {

						//   if so, create a new instance, remove the old instance, replace with the new
						SkillsMatrixLineItem newLineItem = new SkillsMatrixLineItem(li.getName());
						newLineItem.setId(li.getId());
						set2.add(newLineItem);

					} else {
					
						set2.add(li);
						idsArr.add(li.getId());
					}
					
				});
				
				t.setLineItems(set2);
			});
			
			Set<SkillsMatrixTopic> topics = sm.getTopics();
			
			Iterator<SkillsMatrixTopic> smtIterator = topics.iterator();
			
			while (smtIterator.hasNext()) {
				SkillsMatrixTopic topic = smtIterator.next();
				
				setSequenceOnTopic(topic, topicSequences);
				
				Set<SkillsMatrixLineItem> lineItems = topic.getLineItems();
				
				Iterator<SkillsMatrixLineItem> smliIterator = lineItems.iterator();
				
				while (smliIterator.hasNext()) {
					SkillsMatrixLineItem smli = smliIterator.next();
					
					setSequenceOnLineItem(topic.getId(), smli, lineItemSequences);

					Set<SkillsMatrixSkill> skills = smli.getSkills();
					if (skills != null) {
						Iterator<SkillsMatrixSkill> skillsIterator = skills.iterator();

						while (skillsIterator.hasNext()) {
							SkillsMatrixSkill skill = skillsIterator.next();

							setSequenceAndLevelOnSkill(topic.getId(), smli.getId(), skill, skillSequences);
						}
					}
				}
			}
		}
		
		return sm;
	}
	
	@Override
	@Transactional
	public SkillsMatrixTopic addTopic(String topicName) {
		SkillsMatrixTopic rtn = skillsMatrixTopicRepository.save(new SkillsMatrixTopic(topicName));
		
		List resultList = em.createNativeQuery("SELECT max(sequence) FROM skills_matrix_topic_map where skills_matrix_id=1")
				.getResultList();
		
		Long currentMaxSequenceNum = 0L;
		
		if (resultList.size() > 0 && resultList.get(0) != null)
			currentMaxSequenceNum = Long.parseLong(resultList.get(0).toString());
		
		em.createNativeQuery("INSERT INTO skills_matrix_topic_map (skills_matrix_id, skills_matrix_topic_id, sequence) VALUES (1, :topicId, :sequence)")
			.setParameter("topicId",  rtn.getId())
			.setParameter("sequence", currentMaxSequenceNum + 1)
			.executeUpdate();
		
		return rtn;
	}
	
	@Override
	@Transactional
	public SkillsMatrixLineItem addLineItem(Long topicId, String lineItemName) {
		SkillsMatrixLineItem rtn = skillsMatrixLineItemRepository.save(new SkillsMatrixLineItem(lineItemName));

		List resultList =
			em.createNativeQuery("SELECT max(sequence) FROM skills_matrix_topic_line_item_map where skills_matrix_topic_id=:topicId")
				.setParameter("topicId", topicId)
				.getResultList();
		
		Long currentMaxSequenceNum = 0L;
		
		if (resultList.size() > 0 && resultList.get(0) != null)
			currentMaxSequenceNum = Long.parseLong(resultList.get(0).toString());
		
		if (topicId > 0) {
			em.createNativeQuery("INSERT INTO skills_matrix_topic_line_item_map (skills_matrix_topic_id, skills_matrix_line_item_id, sequence) VALUES (:topicId, :lineItemId, :sequence)")
				.setParameter("topicId", topicId)
				.setParameter("lineItemId", rtn.getId())
				.setParameter("sequence", currentMaxSequenceNum + 1)
				.executeUpdate();
		}

		return rtn;
	}

	@Override
	@Transactional
	public SkillsMatrixSkill addSkill(Long lineItemId, Long level, String skillDescription) {
		SkillsMatrixSkill rtn = skillsMatrixSkillRepository.save(new SkillsMatrixSkill(skillDescription));

		List resultList =
			em.createNativeQuery("SELECT max(sequence) FROM skills_matrix_line_item_skill_map where skills_matrix_line_item_id=:lineItemId and level=:level")
					.setParameter("lineItemId", lineItemId)
					.setParameter("level", level)
				.getResultList();

		Long currentMaxSequenceNum = 0L;

		if (resultList.size() > 0 && resultList.get(0) != null)
			currentMaxSequenceNum = Long.parseLong(resultList.get(0).toString());

		if (lineItemId > 0) {
			em.createNativeQuery("INSERT INTO skills_matrix_line_item_skill_map (skills_matrix_line_item_id, skills_matrix_skill_id, level, sequence) VALUES (:lineItemId, :skillId, :level, :sequence)")
					.setParameter("lineItemId", lineItemId)
					.setParameter("skillId", rtn.getId())
					.setParameter("level", level)
					.setParameter("sequence", currentMaxSequenceNum + 1)
					.executeUpdate();
		}

		rtn.setLevel(level);
		rtn.setSequence(currentMaxSequenceNum + 1);
		rtn.setLineItemId(lineItemId);

		return rtn;
	}

	@Override
	@Transactional
	public void deleteSkill(Long lineItemId, Long skillId) {
		em.createNativeQuery("DELETE FROM skills_matrix_line_item_skill_map m WHERE m.skills_matrix_line_item_id=:lineItemId AND m.skills_matrix_skill_id=:skillId")
				.setParameter("lineItemId", lineItemId)
				.setParameter("skillId", skillId)
				.executeUpdate();

		List resultList = em.createNativeQuery("SELECT count(*) FROM skills_matrix_line_item_skill_map m WHERE m.skills_matrix_skill_id=:skillId")
				.setParameter("skillId", skillId)
				.getResultList();

		if (resultList.size() > 0 && resultList.get(0) != null) {
			Long count = Long.parseLong(resultList.get(0).toString());

			if (count == 0)
				skillsMatrixSkillRepository.deleteById(skillId);
		}
	}

	@Override
	public SkillsMatrixSkill updateSkill(Long skillId, String desc) {
		SkillsMatrixSkill skill = new SkillsMatrixSkill();
		skill.setId(skillId);
		skill.setDescription(desc);

		return skillsMatrixSkillRepository.save(skill);
	}
	
	@Override
	public Optional<SkillsMatrixLineItem> getLineItem(Long lineItemId) {
		return skillsMatrixLineItemRepository.findById(lineItemId);
	}
	
	@Override
	public SkillsMatrixTopic updateTopic(Long topicId, String name) {
		Optional<SkillsMatrixTopic> opt = skillsMatrixTopicRepository.findById(topicId);
		SkillsMatrixTopic rtn = null;
		
		if (opt.isPresent()) {
			SkillsMatrixTopic smt = opt.get();
			smt.setName(name);
			
			rtn = skillsMatrixTopicRepository.save(smt);
		}

		return rtn;
	}
	
	@Override
	public SkillsMatrixLineItem updateLineItem(Long lineItemId, String lineItemName) {
		Optional<SkillsMatrixLineItem> opt = skillsMatrixLineItemRepository.findById(lineItemId);
		SkillsMatrixLineItem rtn = null;
		
		if (opt.isPresent()) {
			SkillsMatrixLineItem smli = opt.get();
			
			smli.setName(lineItemName);
			
			rtn = skillsMatrixLineItemRepository.save(smli);
		}

		return rtn;
	}
	
	@Override
	@Transactional
	public boolean updateSequencesRelatedToATopicAndItsLineItems(long[] arr) {
		Optional<SkillsMatrixTopic> opt = skillsMatrixTopicRepository.findById(arr[1]);

		// TODO: Pass JSON to the controller, and create a POJO model, instead of the array. @RequestBody
		
		if (opt.isPresent()) {
			this.setTopicSequence(arr[1], arr[2]);
			
			if (arr[3] > 0 && arr[4] > 0)
				this.setLineItemSequence(arr[1], arr[3], arr[4]);
		}
		
		return true;
	}
	
	private void setTopicSequence(Long topicId, Long sequence) {
		em.createNativeQuery("UPDATE skills_matrix_topic_map smtm SET smtm.sequence = :sequence WHERE smtm.skills_matrix_topic_id = :topicId")
			.setParameter("topicId", topicId)
			.setParameter("sequence", sequence)
			.executeUpdate();
	}
	
	private void setLineItemSequence(Long topicId, Long lineItemId, Long sequence) {
		em.createNativeQuery("UPDATE skills_matrix_topic_line_item_map smlitm SET smlitm.sequence=:sequence WHERE smlitm.skills_matrix_topic_id=:topicId AND smlitm.skills_matrix_line_item_id=:lineItemId")
		.setParameter("topicId", topicId)
		.setParameter("lineItemId", lineItemId)
		.setParameter("sequence", sequence)
		.executeUpdate();
	}
	
	/**
	 * Returns the sequence of the topics in a given tech profile.
	 * 
	 * @param skillsMatrixId
	 * @return
	 */
	private List getTopicSequences(Long skillsMatrixId) {
		List resultList = em.createNativeQuery("SELECT smt.id as topic_id, smtm.sequence FROM skills_matrix sm, skills_matrix_topic smt, skills_matrix_topic_map smtm WHERE sm.id=:skillsMatrixId AND smtm.skills_matrix_id=sm.id AND smtm.skills_matrix_topic_id=smt.id;")
				.setParameter("skillsMatrixId", skillsMatrixId).getResultList();
		
		return resultList;
	}
	
	private List getLineItemSequences(Long skillsMatrixId) {
		List resultList = em.createNativeQuery("select smt.id as topic_id, smli.id as skills_matrix_line_item_id, smtlim.sequence FROM skills_matrix sm, skills_matrix_topic smt, skills_matrix_topic_map smtm, skills_matrix_line_item smli, skills_matrix_topic_line_item_map smtlim WHERE sm.id=:skillsMatrixId and smtm.skills_matrix_id=sm.id and smtm.skills_matrix_topic_id=smt.id and smt.id=smtlim.skills_matrix_topic_id and smtlim.skills_matrix_line_item_id=smli.id;")
				.setParameter("skillsMatrixId", skillsMatrixId).getResultList();

		return resultList;
	}

	private List getSkillSequences(Long skillsMatrixId) {
		List resultList = em.createNativeQuery("select smt.id as topic_id, smli.id as skills_matrix_line_item_id, smsk.id as skills_matrix_skill_id, smliskm.level, smliskm.sequence FROM skills_matrix sm, skills_matrix_topic smt, skills_matrix_topic_map smtm, skills_matrix_line_item smli, skills_matrix_topic_line_item_map smtlim, skills_matrix_skill smsk, skills_matrix_line_item_skill_map smliskm WHERE sm.id=:skillsMatrixId and smtm.skills_matrix_id=sm.id and smtm.skills_matrix_topic_id=smt.id and smt.id=smtlim.skills_matrix_topic_id and smtlim.skills_matrix_line_item_id=smli.id and smli.id=smliskm.skills_matrix_line_item_id and smliskm.skills_matrix_skill_id=smsk.id;")
				.setParameter("skillsMatrixId", skillsMatrixId).getResultList();

		return resultList;
	}

	private SkillsMatrixTopic setSequenceOnTopic(SkillsMatrixTopic topic, List topicSequences) {
		int x = 0;
		SkillsMatrixTopic rtn = null;
		
		while (rtn == null && x < topicSequences.size()) {
			Object[] ts = (Object[])topicSequences.get(x++);
			
			if (((BigInteger)ts[0]).longValue() == topic.getId()) {
				topic.setSequence(((BigInteger)ts[1]).longValue());
				rtn = topic;
			}
		}
		
		return rtn;
	}
	
	private SkillsMatrixLineItem setSequenceOnLineItem(Long topicId, SkillsMatrixLineItem smli, List lineItemSequences) {
		int x = 0;
		SkillsMatrixLineItem rtn = null;

		while (rtn == null && x < lineItemSequences.size()) {
			Object[] lis = (Object[])lineItemSequences.get(x++);

			if (((BigInteger)lis[0]).longValue() == topicId && ((BigInteger)lis[1]).longValue() == smli.getId()) {
				 smli.setSequence(((BigInteger)lis[2]).longValue());
				rtn =  smli;
			}
		}

		return rtn;
	}

	private SkillsMatrixSkill setSequenceAndLevelOnSkill(Long topicId, Long lineItemId, SkillsMatrixSkill smsk, List skillsSequences) {
		int x = 0;
		SkillsMatrixSkill rtn = null;

		while (rtn == null && x < skillsSequences.size()) {
			Object[] sks = (Object[])skillsSequences.get(x++);

			if (((BigInteger)sks[0]).longValue() == topicId
					&& ((BigInteger)sks[1]).longValue() == lineItemId
					&& ((BigInteger)sks[2]).longValue() == smsk.getId()) {
				smsk.setLevel(((BigInteger)sks[3]).longValue());
				smsk.setSequence(((BigInteger)sks[4]).longValue());
				rtn = smsk;
			}
		}

		return rtn;
	}
}
