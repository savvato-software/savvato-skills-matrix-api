package com.savvato.skillsmatrix.entities;

import javax.persistence.*;

@Entity
public class SkillsMatrixSkill implements PermIdEntityBehavior {

	@Id
	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	///
	private String description;

	public String getName() { // to comply with PermIdEntityBehavior
		return description;
	}

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

	private String detailLineItemId;

	public String getDetailLineItemId() {
		return detailLineItemId;
	}

	public void setDetailLineItemId(String detailLineItemId) {
		this.detailLineItemId = detailLineItemId;
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
