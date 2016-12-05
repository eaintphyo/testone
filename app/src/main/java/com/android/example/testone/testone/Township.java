package com.android.example.testone.testone;

import io.realm.RealmObject;

/**
 * Created by Aspire on 12/5/2016.
 */

public class Township extends RealmObject {

    private String id;
    private String unique_id;
    private String name;

    public Township() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUnique_id() {
        return unique_id;
    }

    public void setUnique_id(String unique_id) {
        this.unique_id = unique_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
