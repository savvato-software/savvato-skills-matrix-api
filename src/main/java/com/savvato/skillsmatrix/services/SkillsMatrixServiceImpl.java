package com.savvato.skillsmatrix.services;

import com.savvato.skillsmatrix.entities.SkillsMatrix;
import com.savvato.skillsmatrix.entities.SkillsMatrixLineItem;
import com.savvato.skillsmatrix.entities.SkillsMatrixTopic;
import com.savvato.skillsmatrix.repositories.SkillsMatrixLineItemRepository;
import com.savvato.skillsmatrix.repositories.SkillsMatrixRepository;
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
	SkillsMatrixRepository techProfileRepository;
	
	@Autowired
	SkillsMatrixTopicRepository techProfileTopicRepository;
	
	@Autowired
	SkillsMatrixLineItemRepository techProfileLineItemRepository;
	
	@Override
	public SkillsMatrix get(Long id) {
		Optional<SkillsMatrix> opt = techProfileRepository.findById(id);
		
		List topicSequences = getTopicSequences(id);
		List lineItemSequences = getLineItemSequences(id);
		
		SkillsMatrix tp = null;
		
		ArrayList<Long> idsArr = new ArrayList<>();
		
		if (opt.isPresent()) {
			
			tp = opt.get();
			
			// make sure we have unique instances of each techprofilelineitem... because a topic can share a line item with another topic.
			// if it does, by default, both topics will have the same instance. This is a problem, because the sequence that the topic appears
			// within a topic, can be different for each topic. You need unique instances to represent that.
			
			// for each topic
			Set<SkillsMatrixTopic> tpTopics = tp.getTopics();
			tpTopics.forEach((t) -> {
				Set<SkillsMatrixLineItem> tpTopicLineItems = t.getLineItems();
				Set<SkillsMatrixLineItem> set2 = new HashSet<>();

				// for each of its line items
				tpTopicLineItems.forEach((li) -> {
					//   check if we've seen it before
					if (idsArr.contains(li.getId())) {

						//   if so, create a new instance, remove the old instance, replace with the new
						SkillsMatrixLineItem newLineItem = new SkillsMatrixLineItem(li.getName(), li.getL0Description(), li.getL1Description(), li.getL2Description(), li.getL3Description());
						newLineItem.setId(li.getId());
						set2.add(newLineItem);

					} else {
					
						set2.add(li);
						idsArr.add(li.getId());
					}
					
				});
				
				t.setLineItems(set2);
			});
			
			Set<SkillsMatrixTopic> topics = tp.getTopics();
			
			Iterator<SkillsMatrixTopic> tptIterator = topics.iterator();
			
			while (tptIterator.hasNext()) {
				SkillsMatrixTopic topic = tptIterator.next();
				
				setSequenceOnTopic(topic, topicSequences);
				
				Set<SkillsMatrixLineItem> lineItems = topic.getLineItems();
				
				Iterator<SkillsMatrixLineItem> tpliIterator = lineItems.iterator();
				
				while (tpliIterator.hasNext()) {
					SkillsMatrixLineItem tpli = tpliIterator.next();
					
					setSequenceOnLineItem(topic.getId(), tpli, lineItemSequences);
				}
			}
		}
		
		return tp;
	}
	
	@Override
	@Transactional
	public SkillsMatrixTopic addTopic(String topicName) {
		SkillsMatrixTopic rtn = techProfileTopicRepository.save(new SkillsMatrixTopic(topicName));
		
		List resultList = em.createNativeQuery("SELECT max(sequence) FROM tech_profile_topic_map where tech_profile_id=1")
				.getResultList();
		
		Long currentMaxSequenceNum = 0L;
		
		if (resultList.size() > 0 && resultList.get(0) != null)
			currentMaxSequenceNum = Long.parseLong(resultList.get(0).toString());
		
		em.createNativeQuery("INSERT INTO tech_profile_topic_map (tech_profile_id, tech_profile_topic_id, sequence) VALUES (1, :topicId, :sequence)")
			.setParameter("topicId",  rtn.getId())
			.setParameter("sequence", currentMaxSequenceNum + 1)
			.executeUpdate();
		
		return rtn;
	}
	
	@Override
	@Transactional
	public SkillsMatrixLineItem addLineItem(Long topicId, String lineItemName, String l0desc, String l1desc, String l2desc, String l3desc) {
		SkillsMatrixLineItem rtn = techProfileLineItemRepository.save(new SkillsMatrixLineItem(lineItemName, l0desc, l1desc, l2desc, l3desc));

		List resultList = em.createNativeQuery("SELECT max(sequence) FROM tech_profile_topic_line_item_map where tech_profile_topic_id=:topicId")
		.setParameter("topicId", topicId)
		.getResultList();
		
		Long currentMaxSequenceNum = 0L;
		
		if (resultList.size() > 0 && resultList.get(0) != null)
			currentMaxSequenceNum = Long.parseLong(resultList.get(0).toString());
		
		if (topicId > 0) {
			em.createNativeQuery("INSERT INTO tech_profile_topic_line_item_map (tech_profile_topic_id, tech_profile_line_item_id, sequence) VALUES (:topicId, :lineItemId, :sequence)")
				.setParameter("topicId", topicId)
				.setParameter("lineItemId", rtn.getId())
				.setParameter("sequence", currentMaxSequenceNum + 1)
				.executeUpdate();
		}

		return rtn;
	}
	
	@Override
	public Optional<SkillsMatrixLineItem> getLineItem(Long lineItemId) {
		return techProfileLineItemRepository.findById(lineItemId);
	}
	
	@Override
	public SkillsMatrixTopic updateTopic(Long topicId, String name) {
		Optional<SkillsMatrixTopic> opt = techProfileTopicRepository.findById(topicId);
		SkillsMatrixTopic rtn = null;
		
		if (opt.isPresent()) {
			SkillsMatrixTopic tpt = opt.get();
			tpt.setName(name);
			
			rtn = techProfileTopicRepository.save(tpt);
		}

		return rtn;
	}
	
	@Override
	public SkillsMatrixLineItem updateLineItem(Long lineItemId, String lineItemName, String l0desc, String l1desc, String l2desc, String l3desc) {
		Optional<SkillsMatrixLineItem> opt = techProfileLineItemRepository.findById(lineItemId);
		SkillsMatrixLineItem rtn = null;
		
		if (opt.isPresent()) {
			SkillsMatrixLineItem tpli = opt.get();
			
			tpli.setL0Description(l0desc);
			tpli.setL1Description(l1desc);
			tpli.setL2Description(l2desc);
			tpli.setL3Description(l3desc);
			
			tpli.setName(lineItemName);
			
			rtn = techProfileLineItemRepository.save(tpli);
		}

		return rtn;
	}
	
	@Override
	@Transactional
	public boolean updateSequencesRelatedToATopicAndItsLineItems(long[] arr) {
		Optional<SkillsMatrixTopic> opt = techProfileTopicRepository.findById(arr[1]);

		// TODO: Pass JSON to the controller, and create a POJO model, instead of the array. @RequestBody
		
		if (opt.isPresent()) {
			this.setTopicSequence(arr[1], arr[2]);
			
			if (arr[3] > 0 && arr[4] > 0)
				this.setLineItemSequence(arr[1], arr[3], arr[4]);
		}
		
		return true;
	}
	
	private void setTopicSequence(Long topicId, Long sequence) {
		em.createNativeQuery("UPDATE tech_profile_topic_map tptm SET tptm.sequence = :sequence WHERE tptm.tech_profile_topic_id = :topicId")
			.setParameter("topicId", topicId)
			.setParameter("sequence", sequence)
			.executeUpdate();
	}
	
	private void setLineItemSequence(Long topicId, Long lineItemId, Long sequence) {
		em.createNativeQuery("UPDATE tech_profile_topic_line_item_map tplitm SET tplitm.sequence=:sequence WHERE tplitm.tech_profile_topic_id=:topicId AND tplitm.tech_profile_line_item_id=:lineItemId")
		.setParameter("topicId", topicId)
		.setParameter("lineItemId", lineItemId)
		.setParameter("sequence", sequence)
		.executeUpdate();
	}
	
	/**
	 * Returns the sequence of the topics in a given tech profile.
	 * 
	 * @param techProfileId
	 * @return
	 */
	private List getTopicSequences(Long techProfileId) {
		List resultList = em.createNativeQuery("SELECT tpt.id as topic_id, tptm.sequence FROM tech_profile tp, tech_profile_topic tpt, tech_profile_topic_map tptm WHERE tp.id=:techProfileId AND tptm.tech_profile_id=tp.id AND tptm.tech_profile_topic_id=tpt.id;")
				.setParameter("techProfileId", techProfileId).getResultList();
		
		return resultList;
	}
	
	private List getLineItemSequences(Long techProfileId) {
		List resultList = em.createNativeQuery("select tpt.id as topic_id, tpli.id as tech_profile_line_item_id, tptlim.sequence FROM tech_profile tp, tech_profile_topic tpt, tech_profile_topic_map tptm, tech_profile_line_item tpli, tech_profile_topic_line_item_map tptlim WHERE tp.id=:techProfileId and tptm.tech_profile_id=tp.id and tptm.tech_profile_topic_id=tpt.id and tpt.id=tptlim.tech_profile_topic_id and tptlim.tech_profile_line_item_id=tpli.id;")
				.setParameter("techProfileId", techProfileId).getResultList();

		return resultList;
	}

	private SkillsMatrixTopic setSequenceOnTopic(SkillsMatrixTopic topic, List topicSequences) {
		int x = 0;
		SkillsMatrixTopic rtn = null;
		
		while (rtn == null && x < topicSequences.size()) {
			Object[] ts = (Object[])topicSequences.get(x++);
			
			if (((BigInteger)ts[0]).longValue() == (Long)topic.getId()) {
				topic.setSequence(((BigInteger)ts[1]).longValue());
				rtn = topic;
			}
		}
		
		return rtn;
	}
	
	private SkillsMatrixLineItem setSequenceOnLineItem(Long topicId, SkillsMatrixLineItem tpli, List lineItemSequences) {
		int x = 0;
		SkillsMatrixLineItem rtn = null;

		while (rtn == null && x < lineItemSequences.size()) {
			Object[] lis = (Object[])lineItemSequences.get(x++);

			if (((BigInteger)lis[0]).longValue() == topicId && ((BigInteger)lis[1]).longValue() == (Long)tpli.getId()) {
				tpli.setSequence(((BigInteger)lis[2]).longValue());
				rtn = tpli;
			}
		}

		return rtn;
	}
}
