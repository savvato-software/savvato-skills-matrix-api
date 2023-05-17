package com.savvato.skillsmatrix.services;

import com.savvato.skillsmatrix.entities.SkillsMatrix;
import com.savvato.skillsmatrix.entities.SkillsMatrixLineItem;
import com.savvato.skillsmatrix.entities.SkillsMatrixSkill;
import com.savvato.skillsmatrix.entities.SkillsMatrixTopic;
import com.savvato.skillsmatrix.repositories.SkillsMatrixLineItemRepository;
import com.savvato.skillsmatrix.repositories.SkillsMatrixRepository;
import com.savvato.skillsmatrix.repositories.SkillsMatrixSkillRepository;
import com.savvato.skillsmatrix.repositories.SkillsMatrixTopicRepository;
import com.savvato.skillsmatrix.utils.PermIdEntityUtils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
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

	public SkillsMatrix importSkillsMatrix(String json) throws ParseException {
		net.minidev.json.parser.JSONParser parser = new JSONParser();

		JSONObject obj = (JSONObject)parser.parse(json);

		JSONArray topics = (JSONArray)obj.get("topics");

		JSONArray skills = (JSONArray)obj.get("skills");

		JSONArray lineItems = (JSONArray)obj.get("lineItems");

		JSONArray skillsMatrix = (JSONArray)obj.get("skillsMatrix");

		return null;
	}

	public Iterable<SkillsMatrix> getAll() {
		return skillsMatrixRepository.findAll();
	}

	@Override
	public SkillsMatrix get(String skillsMatrixId) {
		Optional<SkillsMatrix> opt = skillsMatrixRepository.findById(skillsMatrixId);
		
		List topicSequences = getTopicSequences(skillsMatrixId);
		List lineItemSequences = getLineItemSequences(skillsMatrixId);
		List skillSequences = getSkillSequences(skillsMatrixId);

		SkillsMatrix sm = null;
		
		ArrayList<String> idsArr = new ArrayList<>();
		
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
	public SkillsMatrix addSkillsMatrix(String name) {
		return this.addSkillsMatrix(name, PermIdEntityUtils.getId(name));
	}

	private SkillsMatrix addSkillsMatrix(String name, String id) {
		SkillsMatrix sm = new SkillsMatrix();

		sm.setName(name);
		sm.setId(id);

		return skillsMatrixRepository.save(sm);
	}

	@Override
	public SkillsMatrix updateSkillsMatrix(String id, String name) {
		Optional<SkillsMatrix> opt = skillsMatrixRepository.findById(id);
		SkillsMatrix sm = null;

		if (opt.isPresent()) {
			sm = opt.get();

			sm.setName(name);

			skillsMatrixRepository.save(sm);
		}

		return sm;
	}
	
	@Override
	@Transactional
	public SkillsMatrixTopic addTopic(String skillsMatrixId, String topicName) {
		SkillsMatrixTopic st = new SkillsMatrixTopic(topicName);
		PermIdEntityUtils.setId(st);

		SkillsMatrixTopic rtn = skillsMatrixTopicRepository.save(st);
		
		List resultList = em.createNativeQuery("SELECT max(sequence) FROM skills_matrix_topic_map where skills_matrix_id=:skillsMatrixId")
				.setParameter("skillsMatrixId", skillsMatrixId)
				.getResultList();
		
		Long currentMaxSequenceNum = 0L;
		
		if (resultList.size() > 0 && resultList.get(0) != null)
			currentMaxSequenceNum = Long.parseLong(resultList.get(0).toString());
		
		em.createNativeQuery("INSERT INTO skills_matrix_topic_map (skills_matrix_id, skills_matrix_topic_id, sequence) VALUES (:skillsMatrixId, :topicId, :sequence)")
			.setParameter("skillsMatrixId", skillsMatrixId)
			.setParameter("topicId",  rtn.getId())
			.setParameter("sequence", currentMaxSequenceNum + 1)
			.executeUpdate();
		
		return rtn;
	}

	private SkillsMatrixTopic addTopic(String skillsMatrixId, String topicName, String topicId) {
		return null;
	}
	
	@Override
	@Transactional
	public SkillsMatrixLineItem addLineItem(String topicId, String lineItemName) {
		SkillsMatrixLineItem li = new SkillsMatrixLineItem(lineItemName);
		PermIdEntityUtils.setId(li);

		SkillsMatrixLineItem rtn = skillsMatrixLineItemRepository.save(li);

		List resultList =
			em.createNativeQuery("SELECT max(sequence) FROM skills_matrix_topic_line_item_map where skills_matrix_topic_id=:topicId")
				.setParameter("topicId", topicId)
				.getResultList();
		
		Long currentMaxSequenceNum = 0L;
		
		if (resultList.size() > 0 && resultList.get(0) != null)
			currentMaxSequenceNum = Long.parseLong(resultList.get(0).toString());
		
//		if (topicId > 0) {
			em.createNativeQuery("INSERT INTO skills_matrix_topic_line_item_map (skills_matrix_topic_id, skills_matrix_line_item_id, sequence) VALUES (:topicId, :lineItemId, :sequence)")
				.setParameter("topicId", topicId)
				.setParameter("lineItemId", rtn.getId())
				.setParameter("sequence", currentMaxSequenceNum + 1)
				.executeUpdate();
//		}

		return rtn;
	}

	@Override
	@Transactional
	public SkillsMatrixSkill addSkill(String parentLineItemId, Long level, String skillDescription) {
		SkillsMatrixSkill sms = new SkillsMatrixSkill(skillDescription);
		PermIdEntityUtils.setId(sms);
		sms.setDetailLineItemId(null);

		SkillsMatrixSkill rtn = skillsMatrixSkillRepository.save(sms);

		Long currentMaxSequenceNum = getMaxSequence(parentLineItemId, level);

		em.createNativeQuery("INSERT INTO skills_matrix_line_item_skill_map (skills_matrix_line_item_id, skills_matrix_skill_id, level, sequence) VALUES (:lineItemId, :skillId, :level, :sequence)")
				.setParameter("lineItemId", parentLineItemId)
				.setParameter("skillId", rtn.getId())
				.setParameter("level", level)
				.setParameter("sequence", currentMaxSequenceNum + 1)
				.executeUpdate();

		rtn.setLevel(level);
		rtn.setSequence(currentMaxSequenceNum + 1);

		return rtn;
	}

	@Override
	@Transactional
	public void deleteSkill(String lineItemId, String skillId) {
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
	public SkillsMatrixSkill updateSkill(String skillId, String desc, String detailLineItemId) {
		Optional<SkillsMatrixSkill> opt = skillsMatrixSkillRepository.findById(skillId);
		SkillsMatrixSkill rtn = null;

		if (opt.isPresent()) {
			SkillsMatrixSkill skill = opt.get();

			skill.setDescription(desc);

			skill.setDetailLineItemId(detailLineItemId);

			rtn = skillsMatrixSkillRepository.save(skill);
		}

		return rtn;
	}
	
	@Override
	public Optional<SkillsMatrixLineItem> getLineItem(String lineItemId) {
		return skillsMatrixLineItemRepository.findById(lineItemId);
	}
	
	@Override
	public SkillsMatrixTopic updateTopic(String topicId, String name) {
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
	public SkillsMatrixLineItem updateLineItem(String lineItemId, String lineItemName) {
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
	public boolean updateSequencesRelatedToATopicAndItsLineItems(String[] arr) {
		Optional<SkillsMatrixTopic> opt = skillsMatrixTopicRepository.findById(arr[1] + ""); 		// TODO: Fix this hack.

		// TODO: Pass JSON to the controller, and create a POJO model, instead of the array. @RequestBody
		
		if (opt.isPresent()) {
			this.setTopicSequence(arr[1], Long.valueOf(arr[2]));
			
			if (arr[3] != null && arr[4] != null)
				this.setLineItemSequence(arr[1], arr[3], Long.valueOf(arr[4]));
		}
		
		return true;
	}

	@Override
	@Transactional
	public boolean updateSequencesRelatedToALineItemAndItsSkills(String[] arr) {
		String lineItemId = arr[2];
		int pos = 3;

		while (pos < arr.length) {
			String skillId = arr[pos++];
			long sequence = Long.valueOf(arr[pos++]);

			setSkillSequence(lineItemId, skillId, sequence);
		}

		return true;
	}

	private long getMaxSequence( String lineItemId, long level) {
		List resultList =
				em.createNativeQuery("SELECT max(sequence) FROM skills_matrix_line_item_skill_map where skills_matrix_line_item_id=:lineItemId and level=:level")
						.setParameter("lineItemId", lineItemId)
						.setParameter("level", level)
						.getResultList();

		long rtn = 0;
		if (resultList.size() > 0 && resultList.get(0) != null)
			rtn = Long.parseLong(resultList.get(0).toString());

		return rtn;
	}

	private long getSkillLevel(String skillsMatrixId, String skillId) {
		long rtn = -1;
		List skillSequences = getSkillSequences(skillsMatrixId);

		boolean found = false;
		int i = 0;
		while (!found) {
			Object[] row = (Object[]) skillSequences.get(i++);

			if (row[2].equals(skillId)) {
				found = true;
				rtn = Long.parseLong(row[3]+"");
			}
		}

		return rtn;
	}



	@Override
	@Transactional
	public boolean updateSkillLevel(String skillsMatrixId,  String lineItemId,  String skillId, long destinationLevel) {
		// 	get the max sequence of the destination level
		long maxSequence = getMaxSequence(lineItemId, destinationLevel);

		//  get the level the skill is currently at
		long originalSkillLevel = getSkillLevel(skillsMatrixId, skillId);

		//  get the max sequence for the original level
		long origLevelMaxSequence = getMaxSequence(lineItemId, originalSkillLevel);

		//  insert a row for the lineItem, skill, destinationLevel and new sequence
		em.createNativeQuery("INSERT INTO skills_matrix_line_item_skill_map (skills_matrix_line_item_id, skills_matrix_skill_id, level, sequence) VALUES (:lineItemId, :skillId, :level, :sequence)")
				.setParameter("lineItemId", lineItemId)
				.setParameter("skillId", skillId)
				.setParameter("level", destinationLevel)
				.setParameter("sequence", maxSequence + 1)
				.executeUpdate();

		// get skills_matrix_line_item_skill_map for orginal level
		List<Long[]> arr = getSkillLineItemMapInfoPerLevel(lineItemId, originalSkillLevel);

		// find the index of the row we're moving
		int index = 0;
		boolean found = false;
		Object[] row = null;

		while (!found) {
			row = arr.get(index++);
			found = row[1].equals(skillId);
		}

		// while there is a next row
		while (index < arr.size()) {
			row = arr.get(index++);
			// set the sequence to its value -1
			em.createNativeQuery("UPDATE skills_matrix_line_item_skill_map smliskm SET smliskm.sequence=:sequence WHERE smliskm.skills_matrix_line_item_id=:lineItemId AND smliskm.skills_matrix_skill_id=:skillId")
					.setParameter("lineItemId", lineItemId)
					.setParameter("skillId", row[1])
					.setParameter("sequence", Long.parseLong(row[3]+"") - 1)
					.executeUpdate();
		}

		em.createNativeQuery("DELETE FROM skills_matrix_line_item_skill_map smliskm WHERE smliskm.skills_matrix_line_item_id=:lineItemId AND smliskm.skills_matrix_skill_id=:skillId AND smliskm.level=:level AND smliskm.sequence=:sequence")
				.setParameter("lineItemId", lineItemId)
				.setParameter("skillId", skillId)
				.setParameter("level", originalSkillLevel)
				.setParameter("sequence", origLevelMaxSequence)
				.executeUpdate();

		return true;
	}

	private void setSkillSequence(String lineItemId, String skillId, Long sequence) {
		em.createNativeQuery("UPDATE skills_matrix_line_item_skill_map smliskm SET smliskm.sequence=:sequence WHERE smliskm.skills_matrix_line_item_id=:lineItemId AND smliskm.skills_matrix_skill_id=:skillId")
				.setParameter("lineItemId", lineItemId)
				.setParameter("skillId", skillId)
				.setParameter("sequence", sequence)
				.executeUpdate();
	}

	private void setTopicSequence(String topicId, Long sequence) {
		em.createNativeQuery("UPDATE skills_matrix_topic_map smtm SET smtm.sequence = :sequence WHERE smtm.skills_matrix_topic_id = :topicId")
			.setParameter("topicId", topicId)
			.setParameter("sequence", sequence)
			.executeUpdate();
	}
	
	private void setLineItemSequence(String topicId, String lineItemId, Long sequence) {
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
	private List getTopicSequences(String skillsMatrixId) {
		List resultList = em.createNativeQuery("SELECT smt.id as topic_id, smtm.sequence FROM skills_matrix sm, skills_matrix_topic smt, skills_matrix_topic_map smtm WHERE sm.id=:skillsMatrixId AND smtm.skills_matrix_id=sm.id AND smtm.skills_matrix_topic_id=smt.id;")
				.setParameter("skillsMatrixId", skillsMatrixId).getResultList();
		
		return resultList;
	}
	
	private List getLineItemSequences(String skillsMatrixId) {
		List resultList = em.createNativeQuery("select smt.id as topic_id, smli.id as skills_matrix_line_item_id, smtlim.sequence FROM skills_matrix sm, skills_matrix_topic smt, skills_matrix_topic_map smtm, skills_matrix_line_item smli, skills_matrix_topic_line_item_map smtlim WHERE sm.id=:skillsMatrixId and smtm.skills_matrix_id=sm.id and smtm.skills_matrix_topic_id=smt.id and smt.id=smtlim.skills_matrix_topic_id and smtlim.skills_matrix_line_item_id=smli.id;")
				.setParameter("skillsMatrixId", skillsMatrixId).getResultList();

		return resultList;
	}

	private List getSkillSequences(String skillsMatrixId) {
		List resultList = em.createNativeQuery("select smt.id as topic_id, smli.id as skills_matrix_line_item_id, smsk.id as skills_matrix_skill_id, smliskm.level, smliskm.sequence FROM skills_matrix sm, skills_matrix_topic smt, skills_matrix_topic_map smtm, skills_matrix_line_item smli, skills_matrix_topic_line_item_map smtlim, skills_matrix_skill smsk, skills_matrix_line_item_skill_map smliskm WHERE sm.id=:skillsMatrixId and smtm.skills_matrix_id=sm.id and smtm.skills_matrix_topic_id=smt.id and smt.id=smtlim.skills_matrix_topic_id and smtlim.skills_matrix_line_item_id=smli.id and smli.id=smliskm.skills_matrix_line_item_id and smliskm.skills_matrix_skill_id=smsk.id ORDER BY topic_id, skills_matrix_line_item_id, level, sequence;")
				.setParameter("skillsMatrixId", skillsMatrixId).getResultList();

		return resultList;
	}

	private List getSkillLineItemMapInfoPerLevel(String lineItemId, Long level) {
		List resultList = em.createNativeQuery("select * from skills_matrix_line_item_skill_map m where m.skills_matrix_line_item_id=:lineItemId AND m.level=:level ORDER BY skills_matrix_line_item_id, level, sequence;")
				.setParameter("lineItemId", lineItemId)
				.setParameter("level", level)
				.getResultList();

		return resultList;
	}



	private SkillsMatrixTopic setSequenceOnTopic(SkillsMatrixTopic topic, List topicSequences) {
		int x = 0;
		SkillsMatrixTopic rtn = null;
		
		while (rtn == null && x < topicSequences.size()) {
			Object[] ts = (Object[])topicSequences.get(x++);
			
			if (ts[0].equals(topic.getId())) {
				topic.setSequence(((BigInteger)ts[1]).longValue());
				rtn = topic;
			}
		}
		
		return rtn;
	}
	
	private SkillsMatrixLineItem setSequenceOnLineItem(String topicId, SkillsMatrixLineItem smli, List lineItemSequences) {
		int x = 0;
		SkillsMatrixLineItem rtn = null;

		while (rtn == null && x < lineItemSequences.size()) {
			Object[] lis = (Object[])lineItemSequences.get(x++);

			if (lis[0].equals(topicId) && lis[1].equals(smli.getId())) {
				 smli.setSequence(((BigInteger)lis[2]).longValue());
				rtn =  smli;
			}
		}

		return rtn;
	}

	private SkillsMatrixSkill setSequenceAndLevelOnSkill(String topicId, String lineItemId, SkillsMatrixSkill smsk, List skillsSequences) {
		int x = 0;
		SkillsMatrixSkill rtn = null;

		while (rtn == null && x < skillsSequences.size()) {
			Object[] sks = (Object[])skillsSequences.get(x++);

			if (sks[0].equals(topicId)
					&& sks[1].equals(lineItemId)
					&& sks[2].equals(smsk.getId())) {
				smsk.setLevel(((BigInteger)sks[3]).longValue());
				smsk.setSequence(((BigInteger)sks[4]).longValue());
				rtn = smsk;
			}
		}

		return rtn;
	}


}
