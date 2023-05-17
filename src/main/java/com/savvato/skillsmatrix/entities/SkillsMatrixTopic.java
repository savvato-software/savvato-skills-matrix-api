package com.savvato.skillsmatrix.entities;

import javax.persistence.*;
import java.util.Set;

@Entity
public class SkillsMatrixTopic implements PermIdEntityBehavior {

	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
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
		name="skills_matrix_topic_line_item_map"
		, joinColumns={
			@JoinColumn(name="skillsMatrixTopicId")
			}
		, inverseJoinColumns={
			@JoinColumn(name="skillsMatrixLineItemId")
			}
		)
	private Set<SkillsMatrixLineItem> lineItems;
    
    public Set<SkillsMatrixLineItem> getLineItems() {
    	return lineItems;
    }
    
    public void setLineItems(Set<SkillsMatrixLineItem> lineItems) {
    	this.lineItems = lineItems;
    }
    
    @Transient
    private Long sequence;
    
    public Long getSequence() {
    	return sequence;
    }
    
    public void setSequence(Long seq) {
    	this.sequence = seq;
    }
    
	public SkillsMatrixTopic(String name) {
		this.name = name;
	}
	
	public SkillsMatrixTopic() {
		
	}
	
	public String toString() {
		return this.id + " " + this.name;
	}
	
}
