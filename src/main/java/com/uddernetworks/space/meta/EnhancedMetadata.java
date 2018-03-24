package com.uddernetworks.space.meta;

import java.util.HashMap;
import java.util.Map;

public class EnhancedMetadata {

    private Map<String, Object> data = new HashMap<>();

    public Object getData(String name) {
        return this.data.get(name);
    }

    public void setData(String name, Object data) {
        this.data.put(name, data);
    }

}
