package com.kitcheninventory.inventory_project_backend.model;

import java.io.Serializable;
import java.util.Objects;

public class ItemCategoryId implements Serializable {
    private Long itemID;
    private Long categoryID;

    public ItemCategoryId() {}

    public ItemCategoryId(Long itemID, Long categoryID) {
        this.itemID = itemID;
        this.categoryID = categoryID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemCategoryId that)) return false;
        return Objects.equals(itemID, that.itemID) && Objects.equals(categoryID, that.categoryID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemID, categoryID);
    }
}
