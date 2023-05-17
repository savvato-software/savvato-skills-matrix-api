package com.savvato.skillsmatrix.entities;

import javax.persistence.*;
import java.util.Set;

@Entity
public class SkillsMatrix implements PermIdEntityBehavior {

	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	private String name;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
    @ManyToMany
	@JoinTable(
		name="skills_matrix_topic_map"
		, joinColumns={
			@JoinColumn(name="skills_matrix_id")
			}
		, inverseJoinColumns={
			@JoinColumn(name="skills_matrix_topic_id")
			}
		)
	private Set<SkillsMatrixTopic> topics;
    
    public Set<SkillsMatrixTopic> getTopics() {
    	return topics;
    }
    
    public void setTopics(Set<SkillsMatrixTopic> topics) {
    	this.topics = topics;
    }
    
	public SkillsMatrix(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public SkillsMatrix() {
		
	}
}
