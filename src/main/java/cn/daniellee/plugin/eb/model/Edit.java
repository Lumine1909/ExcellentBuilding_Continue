package cn.daniellee.plugin.eb.model;

import java.util.List;

public class Edit {

    private String id;
    private String operation;
    private List<String> introduction; // 编辑介绍时的缓存

    public Edit(String id, String operation) {
        this.id = id;
        this.operation = operation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public List<String> getIntroduction() {
        return introduction;
    }

    public void setIntroduction(List<String> introduction) {
        this.introduction = introduction;
    }
}
