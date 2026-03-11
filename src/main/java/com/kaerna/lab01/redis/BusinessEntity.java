package com.kaerna.lab01.redis;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class BusinessEntity {

    private String id;
    private String name;
    private String type;

    public BusinessEntity() {
    }

    public BusinessEntity(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        if (id != null) map.put("id", id);
        if (name != null) map.put("name", name);
        if (type != null) map.put("type", type);
        return map;
    }

    public static BusinessEntity fromMap(Map<String, String> map) {
        BusinessEntity e = new BusinessEntity();
        e.setId(map.get("id"));
        e.setName(map.get("name"));
        e.setType(map.get("type"));
        return e;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusinessEntity that = (BusinessEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type);
    }
}
