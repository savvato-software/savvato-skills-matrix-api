package com.savvato.skillsmatrix.entities;

import javax.persistence.*;
import java.util.Set;

@Entity
public class SkillsMatrix extends PermIdEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
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
    
	public SkillsMatrix(String name) {
		this.name = name;
	}
	
	public SkillsMatrix() {
		
	}
}
