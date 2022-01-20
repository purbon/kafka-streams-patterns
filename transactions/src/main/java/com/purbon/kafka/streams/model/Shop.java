package com.purbon.kafka.streams.model;

public class Shop {

    private String name;
    private String owner;
    private Long creationTime;
    private Long updateTime;

    public Shop(String name, String owner, Long creationTime, Long updateTime) {
        this.name = name;
        this.owner = owner;
        this.creationTime = creationTime;
        this.updateTime = updateTime;
    }

    public Shop() {
        this("", "", 0L, 0L);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "Shop{" +
                "name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                ", creationTime=" + creationTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
