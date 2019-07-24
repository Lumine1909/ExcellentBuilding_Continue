package cn.daniellee.plugin.eb.menu.filter;

import cn.daniellee.plugin.eb.model.Building;
import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TagHandler {

    public static final TagHandler NULL_TAG = new TagHandler();

    private Set<BuildingTag> tags = new HashSet<>();

    public void addTag(BuildingTag tag) {
        tags.add(tag);
    }

    public void removeTag(BuildingTag tag) {
        tags.remove(tag);
    }

    public boolean hasTag(BuildingTag tag) {
        return tags.contains(tag);
    }


    public boolean filterBuilding(Building building) {
        for (BuildingTag tag : tags) {
            if (!building.getTags().hasTag(tag)) {
                return false;
            }
        }
        return true;
    }

    public Set<BuildingTag> getTags() {
        return tags;
    }

    public void setTagsOverride(Collection<BuildingTag> tags) {
        this.tags.clear();
        this.tags.addAll(tags);
    }

    public List<String> serializeTags() {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (BuildingTag tag : tags) {
            builder.add(tag.toString());
        }
        return builder.build();
    }

    public static TagHandler deserializeTags(List<String> tags) {
        TagHandler tagHandler = new TagHandler();
        for (String tag : tags) {
            BuildingTag btag = BuildingTag.of(tag);
            if (btag != null) {
                tagHandler.addTag(btag);
            }
        }
        return tagHandler;
    }

    public String toString() {
        if (tags.isEmpty()) {
            return "无";
        }
        StringBuilder builder = new StringBuilder();
        for (BuildingTag tag : tags) {
            builder.append(tag.toString()).append(", ");
        }
        return builder.substring(0, builder.length() - 2);
    }
}
