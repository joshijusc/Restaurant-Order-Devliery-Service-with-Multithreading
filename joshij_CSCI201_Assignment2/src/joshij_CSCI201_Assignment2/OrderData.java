package joshij_CSCI201_Assignment2;

import java.util.List;
import java.util.ArrayList;


public class OrderData {
    private List<Order> orderData_;
    
    public OrderData(List<Order> orderData) {
        this.orderData_ = orderData;
    }

    public List<Order> getOrderData() {
        return orderData_;
    }
}