package com.uddernetworks.space.meta;

import java.util.HashMap;
import java.util.Map;

public class EnhancedMetadata {

    public Map<String, Object> data = new HashMap<>();

    public boolean containsData(String name) {
        return this.data.containsKey(name);
    }

    public Object getData(String name) {
        return this.data.get(name);
    }

    public Object getData(String name, Object def) {
        return this.data.getOrDefault(name, def);
    }

    public void setData(String name, Object data) {
        this.data.put(name, data);
    }

}
