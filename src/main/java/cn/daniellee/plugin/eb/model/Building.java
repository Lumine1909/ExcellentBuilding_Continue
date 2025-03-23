package cn.daniellee.plugin.eb.model;

import cn.daniellee.plugin.eb.menu.filter.TagHandler;

public class Building {

    private String id;
    private String name;
    private String player;
    private String server;
    private String location;
    private long createDate;
    private boolean reviewed;
    private int likes;
    private String liker;
    private String iconMaterial;
    private int iconDurability;
    private String introduction;
    private TagHandler tags;

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

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getLiker() {
        return liker;
    }

    public void setLiker(String liker) {
        this.liker = liker;
    }

    public String getIconMaterial() {
        return iconMaterial;
    }

    public void setIconMaterial(String iconMaterial) {
        this.iconMaterial = iconMaterial;
    }

    public int getIconDurability() {
        return iconDurability;
    }

    public void setIconDurability(int iconDurability) {
        this.iconDurability = iconDurability;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public void setTags(TagHandler tags) {
        this.tags = tags;
    }

    public TagHandler getTags() {
        if (this.tags == null) {
            return TagHandler.NULL_TAG;
        }
        return tags;
    }
}