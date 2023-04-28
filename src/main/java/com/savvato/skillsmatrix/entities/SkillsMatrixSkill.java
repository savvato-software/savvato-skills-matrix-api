package com.savvato.skillsmatrix.entities;

import javax.persistence.*;

@Entity
public class SkillsMatrixSkill {

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
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Transient
	private Long level;

	public Long getLevel() { return level; }

	public void setLevel(Long level) { this.level = level; }

    @Transient
    private Long sequence;

    public Long getSequence() {
    	return sequence;
    }

    public void setSequence(Long seq) {
    	this.sequence = seq;
    }

    public SkillsMatrixSkill(String name) {
		this.description = name;
	}

	public SkillsMatrixSkill() {
		
	}
	
	public String toString() {
		return this.id + " " + this.description;
	}
}
