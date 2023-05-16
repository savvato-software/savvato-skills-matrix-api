package com.savvato.skillsmatrix.entities;

public abstract class PermIdEntity {
    private String permId;

    public String getPermId() {
        return permId;
    }

    public void setPermId(String permId) {
        this.permId = permId;
    }

    abstract public String getName();
}
