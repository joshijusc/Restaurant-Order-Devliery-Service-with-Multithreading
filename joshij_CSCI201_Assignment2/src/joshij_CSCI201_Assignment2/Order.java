package joshij_CSCI201_Assignment2;

import java.util.*;


public class Order {
	private int readyTime_;
	private String restaurantName_;
	private String food_;
	
    public Order(int readyTime, String location, String food){
    	readyTime_ = readyTime;
    	restaurantName_ = location;
    	food_ = food;
    }
    
    public int getReadyTime() {
    	return readyTime_;
    }
    
    public String getRestaurantName() {
    	return restaurantName_;
    }
    
    public String getFood() {
    	return food_;
    }
}