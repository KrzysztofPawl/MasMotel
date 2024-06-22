package com.motel.DTO;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RoomView {
    private int roomNumber;
    private Integer floor;
    private BigDecimal priceForNight;
    private String standard;
    private int beds;
    private BooleanProperty selected = new SimpleBooleanProperty(false);

    public RoomView(int roomNumber, Integer floor, BigDecimal priceForNight, String standard, int beds) {
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.priceForNight = priceForNight;
        this.standard = standard;
        this.beds = beds;
    }

    public BooleanProperty selectedProperty() {
        return this.selected;
    }

}
