package joshij_CSCI201_Assignment2;

import java.util.List;
import java.util.ArrayList;
import com.google.gson.annotations.SerializedName;


public class RestaurantData {
	@SerializedName("data")
    private List<Restaurant> restaurantData_;
    
    public RestaurantData(List<Restaurant> restaurantData) {
        this.restaurantData_ = restaurantData;
    }

    public List<Restaurant> getRestaurantData() {
        return restaurantData_;
    }
}