package com.savvato.skillsmatrix.entities;

import javax.persistence.*;
import java.util.Set;

@Entity
public class SkillsMatrix {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	///
	private String name;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
    @ManyToMany
	@JoinTable(
		name="tech_profile_topic_map"
		, joinColumns={
			@JoinColumn(name="techProfileId")
			}
		, inverseJoinColumns={
			@JoinColumn(name="techProfileTopicId")
			}
		)
	private Set<SkillsMatrixTopic> topics;
    
    public Set<SkillsMatrixTopic> getTopics() {
    	return topics;
    }
    
    public void setTopics(Set<SkillsMatrixTopic> topics) {
    	this.topics = topics;
    }
    
	public SkillsMatrix(String name) {
		this.name = name;
	}
	
	public SkillsMatrix() {
		
	}
}
