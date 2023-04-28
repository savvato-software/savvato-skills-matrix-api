package com.savvato.skillsmatrix.entities;

import javax.persistence.*;
import java.util.Set;

@Entity
public class SkillsMatrixLineItem {

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
			name="skills_matrix_line_item_skill_map"
			, joinColumns={
			@JoinColumn(name="skillsMatrixLineItemId")
	}
			, inverseJoinColumns={
			@JoinColumn(name="skillsMatrixSkillId")
	}
	)
	private Set<SkillsMatrixSkill> skills;

	public Set<SkillsMatrixSkill> getSkills() {
		return skills;
	}

	public void setSkills(Set<SkillsMatrixSkill> skills) {
		this.skills = skills;
	}

    @Transient
    private Long sequence;
    
    public Long getSequence() {
    	return sequence;
    }
    
    public void setSequence(Long seq) {
    	this.sequence = seq;
    }

    public SkillsMatrixLineItem(String name) {
		this.name = name;
	}
	
	public SkillsMatrixLineItem() {
		
	}
	
	public String toString() {
		return this.id + " " + this.name;
	}
}
