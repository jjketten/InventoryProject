package com.jamiekettenhofen.inventory_project_backend.model;

// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;
// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;
import lombok.Value;

/**
 * Non-entity/table model for quantities
 */
// @NoArgsConstructor
// @AllArgsConstructor
@Value
// @Entity
// @Table(name = "Item")
public class Quantity {
    private String unit;
    private Integer amount;
    private Item w;
}
