package joshij_CSCI201_Assignment2;

import java.io.*;
import java.util.*;
import com.google.gson.*;
import java.util.concurrent.Semaphore;

public class OrderProgram {
    private static List<Restaurant> restaurants = new ArrayList<>();
    private static List<Order> orders = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);
    private static HashMap<String, Semaphore> driverSemaphores = new HashMap<String, Semaphore>(); // Set to <RestaurantName, new Semaphore(restaurant.getNumDrivers())>
    private static long startTime = 0;
    private static double userLatitude = 0;
    private static double userLongitude = 0;

    
    
    public static void main(String[] args) {
        String jsonFileName = null;
        String csvFileName = null;

        // Get JSON File and validate
        while (true) {
            System.out.print("What is the name of the file containing the restaurant information? ");
            jsonFileName = scanner.nextLine().trim();

            if (isValidFileName(jsonFileName)) {
                if (loadAndValidateData(jsonFileName)) {
                	break;
                } else continue;
            } else {
                System.out.println("Invalid filename. Please try again.\n");
            }
        }
        
        // Get CSV File and validate
        while (true) {
            System.out.print("What is the name of the file containing the schedule information? ");
            csvFileName = scanner.nextLine().trim();

            if (isValidFileName(csvFileName) && loadAndValidateCSVData(csvFileName)) {
            	// Don't need to check if valid CSV File as per directions on second to last page
            	System.out.println("Read the file properly.\n");
        		break;
            } else {
                System.out.println("Invalid file. Please try again.\n");
            }
        }
        
        // Initialize semaphores for each restaurant
        for (Restaurant restaurant : restaurants) {
            driverSemaphores.put(restaurant.getName(), new Semaphore(restaurant.getNumDrivers()));
        }
        
        // Prompt user for coordinates
        userLatitude = getUserLatitude();
        userLongitude = getUserLongitude();
        
        //  ================== Core Operations ====================
        System.out.println("\nStarting execution of program...");
        
        // Start the timer when the first driver is released
        startTimer();
        
        // “Write a method to help me finish the driverThreads for this program."  (20 lines). ChatGPT, 4 Sep. version, OpenAI, 28 Sep. 2023, chat.openai.com/chat.
        // Create and start driver threads for each order
        List<DriverThread> driverThreads = new ArrayList<>();
        for (Order order : orders) {
        	DriverThread driverThread = new DriverThread(order);
            driverThreads.add(driverThread);
            driverThread.start();
        }

        // Wait for all driver threads to finish
        for (DriverThread driverThread : driverThreads) {
            try {
                driverThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("All orders complete!");
    }
    
 // Define the DriverThread class
    static class DriverThread extends Thread {
        private Order order;

        public DriverThread(Order order) {
            this.order = order;
        }

        @Override
        public void run() {
            try {
                // Wait until the order's ready time
                long elapsedTime = getElapsedTime();
                long timeToWait = order.getReadyTime() * 1000 - elapsedTime;
                if (timeToWait > 0) {
                    Thread.sleep(timeToWait);
                }
                
                // Get the semaphore for the restaurant
                Semaphore restaurantSemaphore = driverSemaphores.get(order.getRestaurantName());

                // Acquire the semaphore (driver) if available
                while (!restaurantSemaphore.tryAcquire()) {
                	Thread.sleep(100); // Arbitrary 100 milliseconds chosen to ensure orders aren't deployed too early if waiting
                }
                
                System.out.println(formatElapsedTime() + " Starting delivery of " + order.getFood() + " from " + order.getRestaurantName() + "!");

                // Simulate delivery time
                double distance = calculateDistance(userLatitude, userLongitude,
                        getRestaurantLatitude(order.getRestaurantName()), getRestaurantLongitude(order.getRestaurantName()));
                double deliveryTime = distance * 2.0; // 1 mile per second, drive to user and back
                Thread.sleep((int) (deliveryTime * 1000.0)); // Convert from seconds to milliseconds
                
                // Release the semaphore (driver)
                restaurantSemaphore.release();
                System.out.println(formatElapsedTime() + " Finished delivery of " + order.getFood() + " from " + order.getRestaurantName() + "!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    
  // Start the timer when the first driver is released
  private static void startTimer() {
      startTime = System.currentTimeMillis();   
  }

  private static long getElapsedTime() {
  	return System.currentTimeMillis() - startTime; 
  }
  
  // Get the current timer time in the format "[HH:MM:ss.SSS]"
  private static String formatElapsedTime() {
	 	
      long elapsedTime = getElapsedTime();
      long seconds = elapsedTime / 1000;
      long minutes = seconds / 60;
      long hours = minutes / 60;

      seconds %= 60;
      minutes %= 60;
      hours %= 24;

      double secondsWithMilliseconds = elapsedTime / 1000.0; // Convert milliseconds to seconds
      return String.format("[%02d:%02d:%02.3f]", hours, minutes, secondsWithMilliseconds);
  }

  private static double getRestaurantLatitude(String restaurantName) {
	  double thisRestaurantLatitude = 0.0;
	  for (Restaurant restaurant : restaurants) {
		  if (restaurantName.equals(restaurant.getName())) { // Cannot use "==" for String value comparison
			  thisRestaurantLatitude = restaurant.getLatitude();
		  }
	  }
	  return thisRestaurantLatitude;
  }

  private static double getRestaurantLongitude(String restaurantName) {
	  double thisRestaurantLongitude = 0.0;
	  for (Restaurant restaurant : restaurants) {
		  if (restaurantName.equals(restaurant.getName())) thisRestaurantLongitude = restaurant.getLongitude();
	  }
	  return thisRestaurantLongitude;
  }

    
    // ============================================= Previous code, still works and using ========================================================
    
    
    // “Write a method similar to loadAndValidateData but instead for loading data from a CSV file such as this one"  (35 lines). ChatGPT, 4 Sep. version, OpenAI, 7 Sep. 2023, chat.openai.com/chat.
    private static boolean loadAndValidateCSVData(String csvFilePath) {
    	String line;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            while ((line = br.readLine()) != null) {
                // Split the CSV line into fields
                String[] fields = line.split(",");
                
                // Assuming the CSV has three fields: readyTime, location, foodItem
              try {
	                if (fields.length == 3) {
	                    int readyTime = Integer.parseInt(fields[0].trim());
	                    String location = fields[1].trim();
	                    String foodItem = fields[2].trim();	                    
	                  // Validate the loaded data
	                  if (isValidOrderData(readyTime, location, foodItem)) {
	                      // Create and add a new Order object to the list
	                      Order order = new Order(readyTime, location, foodItem);
	                      orders.add(order);
	                  } else {
	                      System.out.println("Error: Invalid order data found.");
	                  } 
	                }

	          } catch (NumberFormatException e) {
	              System.out.println("Error: Invalid data format in CSV file.");
	              return false;
	          }
            }
        } catch (IOException e) {
            System.out.println("Error: Could not read the CSV file.");
            return false;
        }    	
        return true;
    }
   
    private static boolean isValidOrderData(int readyTime, String location, String food) {
        // Check if readyTime is non-negative, and location and food are not empty.
        return readyTime >= 0 && !location.isEmpty() && !food.isEmpty();
    }
    
    
    
    private static boolean isValidFileName(String fileName) {
        File file = new File(fileName);
        return file.exists() && !file.isDirectory();
    }
    
    private static boolean loadAndValidateData(String fileName) {
        try (FileReader reader = new FileReader(fileName)) {
            Gson gson = new Gson();
            StringBuilder jsonData = new StringBuilder();
            String line;

            // Read the JSON data from the file line by line
            try (BufferedReader br = new BufferedReader(reader)) {
                while ((line = br.readLine()) != null) {
                    jsonData.append(line);
                }
            }

            RestaurantData rest = gson.fromJson(jsonData.toString(), RestaurantData.class);

            if (rest != null && rest.getRestaurantData() != null) {
                restaurants = rest.getRestaurantData();
                // Validate the loaded data
                if (isValidRestaurantData(restaurants)) System.out.println("Read the file properly.\n");
                 else {
                    System.out.println("The file " + fileName + " does not contain valid restaurant data.");
                    return false;
                }
            } else {
                System.out.println("The file " + fileName + " does not contain valid restaurant data.");
                return false;
            }

        } catch (FileNotFoundException fnfe) {
            System.out.println("The file " + fileName + " could not be found.");
            return false;
        } catch (NumberFormatException nfe) {
            System.out.println("The file " + fileName + " is not formatted properly.");
            return false;
        } catch (JsonSyntaxException jse) {
            System.out.println("The file " + fileName + " is not formatted properly.");
            return false;
        } catch (NullPointerException npe) {
            System.out.println("The file " + fileName + " is not formatted properly.");
            return false;
        } catch (IOException ioe) {
            System.out.println("The file" + fileName + " is not formatted properly.");
            return false;
        }
        return true;
    }
    
    private static boolean isValidRestaurantData(List<Restaurant> restaurantList) {
        for (Restaurant restaurant : restaurantList) {
            if (!isValidRestaurant(restaurant)) return false;
        }
        return true;
    }

    private static boolean isValidRestaurant(Restaurant restaurant) {
    	// Check that name, address, and menu items are Strings
        if (!(restaurant.getName() instanceof String) ||
        			!(restaurant.getAddress() instanceof String) ||
        			!areMenuItemsStrings(restaurant.getMenu())) {
            return false;
        }

        // Validate latitude and longitude as doubles
        Double restLatitudeDouble = restaurant.getLatitude();
        Double restLongitudeDouble = restaurant.getLongitude();
        if (!(restLongitudeDouble instanceof Double) || !(restLatitudeDouble instanceof Double)) {
            return false; // Invalid data type
        }
        
        // Validate drivers as integers
        Integer driversInteger = restaurant.getNumDrivers();
        if (!(driversInteger instanceof Integer)) {
        	return false;
        }
        
        // Check that there are no missing parameters
        if (restaurant.getLatitude() == 0.0 || restaurant.getLongitude() == 0.0 ||
        		restaurant.getName() == null || restaurant.getAddress() == null
        		|| restaurant.getNumDrivers() == 0 || restaurant.getMenu() == null ||restaurant.getMenu().size() == 0) return false;
        
        return true;
    }

    
    private static boolean isString(Object obj) { return obj instanceof String;}

    private static boolean areMenuItemsStrings(List<String> menu) {
        for (String menuItem : menu) {
            if (!isString(menuItem)) return false;
        }
        return true;
    }


    private static double getUserLatitude() {
        double userLatitude = 34.021160;
        boolean validInput = false;
        
        while (!validInput) {
            try {
                System.out.print("What is the latitude? ");
                userLatitude = Double.parseDouble(scanner.nextLine());
                validInput = true;
            } catch (NumberFormatException e) {System.out.println("Invalid input. Latitude must be a valid number.");}
        }
        return userLatitude;
    }

    private static double getUserLongitude() {
        double userLongitude = -118.287132;
        boolean validInput = false;

        while (!validInput) {
            try {
                System.out.print("What is the longitude? ");
                userLongitude = Double.parseDouble(scanner.nextLine());
                validInput = true;
            } catch (NumberFormatException e) {System.out.println("Invalid input. Longitude must be a valid number.");}
        }
        return userLongitude;
    }
    
    private static double calculateDistance(double userLatitude, double userLongitude, double restaurantLatitude, double restaurantLongitude) {
        double userLatitudeRadians = Math.toRadians(userLatitude);
        double userLongitudeRadians = Math.toRadians(userLongitude);
        double restaurantLatitudeRadians = Math.toRadians(restaurantLatitude);
        double restaurantLongitudeRadians = Math.toRadians(restaurantLongitude);

        // Latitude/Longitude 1 is Restaurant and Latitude/Longitude 2 is User (from formula)
        double distance = 3963.0 * Math.acos((Math.sin(restaurantLatitudeRadians) 
        		* Math.sin(userLatitudeRadians)) 
        		+ Math.cos(restaurantLatitudeRadians) * Math.cos(userLatitudeRadians)
                * Math.cos(userLongitudeRadians - restaurantLongitudeRadians));
        
        return Math.round(distance * 10.0) / 10.0;
    }    
}
















