package krusty_krab.krusty_krab.domain;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.maps.*;
import com.google.maps.errors.ApiException;
import com.google.maps.model.*;
import com.mongodb.MongoClient;
import krusty_krab.krusty_krab.mongo.MongoDBEventDAO;
import java.time.*;
import java.util.Date;
import java.lang.Math.*;
import java.util.Currency;
import java.util.*;
import java.lang.Thread;

public class GoogleMaps {

  private final float WALK_DISTANCE = 3, BIKE_DISTANCE = 7, TRANSIT_DISTANCE = 15;

  private static GeoApiContext KEY = null;
  public static Map<String, ArrayList<PlaceType>> act_map = null;

  public GoogleMaps() {
    if (KEY == null) {
      // KrustyKrab: AIzaSyDxwdE3kLIG6GehK-6h4DnLENeiayH2FYc
      // Hemant: AIzaSyDT2fV_yz3DWPcKbwiyxNZUxHdf373Yal8
      KEY = new GeoApiContext.Builder().apiKey("AIzaSyDxwdE3kLIG6GehK-6h4DnLENeiayH2FYc").build();
    }
    if (act_map == null) {
      act_map = new HashMap<String, ArrayList<PlaceType>>();
      fillActMap();
    }
  }

  private static void fillActMap() {  
    ArrayList<PlaceType> obj = new ArrayList<PlaceType>();
    obj.add(PlaceType.AMUSEMENT_PARK);
    obj.add(PlaceType.CAMPGROUND);
    obj.add(PlaceType.PARK);
    obj.add(PlaceType.RV_PARK);
    act_map.put("Parks/Amusement Parks",obj);

    ArrayList<PlaceType> obj2 = new ArrayList<PlaceType>();
    obj2.clear();
    obj2.add(PlaceType.BAKERY);
    obj2.add(PlaceType.MEAL_DELIVERY);
    obj2.add(PlaceType.MEAL_TAKEAWAY);
    obj2.add(PlaceType.RESTAURANT);
    obj2.add(PlaceType.CAFE);
    act_map.put("Food/Restaurants", obj2);

    ArrayList<PlaceType> obj3 = new ArrayList<PlaceType>();
    obj3.add(PlaceType.AQUARIUM);
    obj3.add(PlaceType.CITY_HALL);
    obj3.add(PlaceType.LIBRARY);
    obj3.add(PlaceType.LOCAL_GOVERNMENT_OFFICE);
    obj3.add(PlaceType.ZOO);
    obj3.add(PlaceType.UNIVERSITY);
    obj3.add(PlaceType.SHOPPING_MALL);
    obj3.add(PlaceType.ART_GALLERY);
    obj3.add(PlaceType.PAINTER);
    obj3.add(PlaceType.MUSEUM);
    act_map.put("Tourist Attractions", obj3);
    
    ArrayList<PlaceType> obj4 = new ArrayList<PlaceType>();
    obj4.clear();
    obj4.add(PlaceType.CASINO);
    obj4.add(PlaceType.BOWLING_ALLEY);
    act_map.put("Entertainment", obj4);
    
    ArrayList<PlaceType> obj5 = new ArrayList<PlaceType>();
    obj5.add(PlaceType.BOOK_STORE);
    obj5.add(PlaceType.DEPARTMENT_STORE);
    obj5.add(PlaceType.CLOTHING_STORE);
    obj5.add(PlaceType.CONVENIENCE_STORE);
    act_map.put("Shopping", obj5);
    
    ArrayList<PlaceType> obj6 = new ArrayList<PlaceType>();
    obj6.clear();
    obj6.add(PlaceType.NIGHT_CLUB);
    obj6.add(PlaceType.BAR);
    obj6.add(PlaceType.LIQUOR_STORE);
    act_map.put("Bars/Clubs", obj6);
    
    ArrayList<PlaceType> obj7 = new ArrayList<PlaceType>();
    obj7.add(PlaceType.SYNAGOGUE);
    obj7.add(PlaceType.CHURCH);
    act_map.put("Historical Sites", obj7);
  }

  public static Instant getInstant(Time start) {

    long seconds_start = start.toMinutes() * 60;

    Date date = new Date();
    Time curr = new Time(date.getYear(), date.getMonth(), date.getDay(), date.getHours(),
        date.getMinutes(), true);
    long seconds_curr = curr.toMinutes() * 60;

    Instant now = Instant.now();
    long offset = Math.abs(seconds_curr - seconds_start);
    now.plusSeconds(offset);

    return now;
  }

  public static Transportation getTransObject(DirectionsRoute[] rou, Time startTime, String i) {
    Transportation e = null;

    for (DirectionsRoute j : rou) {
      long tot_dis = 0;
      long tot_tim = 0;
      for (DirectionsLeg k : j.legs) {
        tot_dis += k.distance.inMeters;
        tot_tim += k.duration.inSeconds;
      }
      float min = tot_tim / 60;
      String subtitle = "";
      Time expected = null;
      Time endtime = null;
      if (min < 60) {
        subtitle += Integer.toString(Math.round(min)) + " minutes";
        expected = new Time(0, 0, 0, 0, Math.round(min), true);
      } else {
        float hours = min / 60;
        float minutes = min % 60;
        expected = new Time(0, 0, 0, Math.round(hours), Math.round(minutes), true);
        subtitle += Integer.toString(Math.round(hours)) + " hours "
            + Integer.toString(Math.round(minutes)) + " minutes";
      }

      if ((startTime.getMinute() + expected.getMinute()) < 60) {
        endtime = new Time(startTime.getYear(), startTime.getMonth(), startTime.getDay(),
            startTime.getHour() + expected.getHour(), startTime.getMinute() + expected.getMinute(),
            true);
      } else {
        endtime = new Time(startTime.getYear(), startTime.getMonth(), startTime.getDay(),
            startTime.getHour() + expected.getHour()
                + (startTime.getMinute() + expected.getMinute()) / 60,
            (startTime.getMinute() + expected.getMinute()) % 60, true);
      }
      e = new Transportation(((float) tot_dis / (float) 1000), i, 0.0f, startTime, endtime,
          expected, "icon", subtitle);
      break;
    }
    return e;
  }

  public Transportation getTransportation(String loc1, String loc2, Time startTime,
      List<String> methods) {
    List<Transportation> obj = new ArrayList<Transportation>();

     for (String i : methods) {
       if (i.equals("Bike")) {
         DirectionsApiRequest req = DirectionsApi.getDirections(KEY, "", "").originPlaceId(loc1).destinationPlaceId(loc2).mode(TravelMode.BICYCLING);
         DirectionsResult res = null;
         try {
           res = req.await();
         } catch (Exception e) {
           // TODO Auto-generated catch blocke.printStackTrace();
         }

         DirectionsRoute[] rou = res.routes;
         obj.add(getTransObject(rou, startTime, i));
       }
       if (i.equals("Drive")) {
         DirectionsApiRequest req = DirectionsApi.getDirections(KEY, "",
                 "").originPlaceId(loc1).destinationPlaceId(loc2).mode(TravelMode.DRIVING);
         DirectionsResult res = null;
         try {
           res = req.await();
         } catch (Exception e) {
           // TODO Auto-generated catch block e.printStackTrace();
         }

         DirectionsRoute[] rou = res.routes;
         obj.add(getTransObject(rou, startTime, i));
       }
       if (i.equals("Transit")) {
         DirectionsApiRequest req = DirectionsApi.getDirections(KEY, "", "").originPlaceId(loc1)
        .destinationPlaceId(loc2).mode(TravelMode.TRANSIT).departureTime(getInstant(startTime))
        .transitMode(TransitMode.BUS);
         DirectionsResult res = null;
         try { res = req.await(); }
         catch (Exception e) { // TODO Auto-generated catch block e.printStackTrace();
         }
         DirectionsRoute[] rou = res.routes;
         obj.add(getTransObject(rou, startTime, i));
       }
       if (i.equals("Walk")) {
        DirectionsApiRequest req = DirectionsApi.getDirections(KEY, "", "").originPlaceId(loc1)
        .destinationPlaceId(loc2).mode(TravelMode.WALKING);
        DirectionsResult res = null;
        try { res = req.await(); }
        catch (Exception e) { // TODO Auto-generated catch block e.printStackTrace();
        }

        DirectionsRoute[] rou = res.routes;
        obj.add(getTransObject(rou, startTime, i));
       }
    }
  return chooseTransportation(obj);

  //  return new Transportation(5, "walk", 0, startTime, startTime.add(new Time(0, 0, 0, 0, 5, true)),
    //    new Time(0, 0, 0, 0, 5, true), "flight-takeoff", "5 minutes");
  }

  public static int filterByPrice(float budget, float price) {
    if (budget < price) {
      return 100;
    }
    return 0;
  }

  public static int filterByTime(Time currentTime, Time expectedLength, Time[] timeArray) {
    // close time at index 1, open time at index 0
    // return 0 if good, return >0 for bad

    // Time eventOpenTime = timeArray[0];
    Time eventCloseTime = timeArray[1];
    return eventCloseTime.getDifference(currentTime).isLessThan(expectedLength) ? 0 : 1;
  }

  public static int filterByVisited(String id, List<String> visitedEvents,
      List<String> itinEvents) {
    if (visitedEvents.contains(id) || itinEvents.contains(id)) {
      return 100;
    }
    return 0;
  }

  public static int filterByActivity(String[] eventActivities, List<String> userActivities) {
    for(String userActivityCat:userActivities){
      List<PlaceType> category_entries = act_map.get(userActivityCat);
      for (String eventActivity : eventActivities) {
        for(PlaceType userActivity:category_entries){
          if (userActivity.toString().equals(eventActivity)) {
            return 0;
          }
        }
      }
    }
    return 100;
  }

  public static float getPriceLevelUnknown(PriceLevel obj, AddressType[] r, String user) {
    float price = -1;

    if (user.equals("Parks/Amusement Parks")) {
      price = 3;
    } else if (user.equals("Food/Restaurants")) {
      price = 2;
    } else if (user.equals("Tourist Attractions")) {
      price = 3;
    } else if (user.equals("Entertainment")) {
      price = 2;
    } else if (user.equals("Shopping")) {
      price = 4;
    } else if (user.equals("Bars/Clubs")) {
      price = 4;
    } else if (user.equals("Historical Sites")) {
      price = 0;
    }
    return price;
  }

  public static float getPriceLevel(PriceLevel obj, AddressType[] r, String user) {
    float price = -1;

    if (obj == PriceLevel.FREE) {
      price = 0;
    } else if (obj == PriceLevel.INEXPENSIVE) {
      price = 1;
    } else if (obj == PriceLevel.MODERATE) {
      price = 2;
    } else if (obj == PriceLevel.EXPENSIVE) {
      price = 3;
    } else if (obj == PriceLevel.VERY_EXPENSIVE) {
      price = 4;
    } else if (obj == PriceLevel.UNKNOWN) {
      price = getPriceLevelUnknown(obj, r, user);
    }


    return price;
  }

  // index 0 - closing time
  // index 1 - start time
  public static Time[] getTime(OpeningHours hours, Time time) {

    Date date = new Date();
    date.setYear(time.getYear());
    date.setMonth(time.getMonth() - 1);
    date.setDate(time.getDay());
    date.setHours(time.getHour());
    date.setMinutes(time.getMinute());
    date.setSeconds(0);
    int curDayIndex = -1;
    for (int i = 0; i < hours.periods.length; i++) {
      if (date.getDay() == 0 && hours.periods[i].open.day.getName() == "Sunday") {
        curDayIndex = i;
      } else if (date.getDay() == 1 && hours.periods[i].open.day.getName() == "Monday") {
        curDayIndex = i;
      } else if (date.getDay() == 2 && hours.periods[i].open.day.getName() == "Tuesday") {
        curDayIndex = i;
      } else if (date.getDay() == 3 && hours.periods[i].open.day.getName() == "Wednesday") {
        curDayIndex = i;
      } else if (date.getDay() == 4 && hours.periods[i].open.day.getName() == "Thursday") {
        curDayIndex = i;
      } else if (date.getDay() == 5 && hours.periods[i].open.day.getName() == "Friday") {
        curDayIndex = i;
      } else if (date.getDay() == 6 && hours.periods[i].open.day.getName() == "Saturday") {
        curDayIndex = i;
      }
    }
    if (curDayIndex != -1) {
      int str_year = time.getYear();
      int str_month = time.getMonth();
      int str_day = time.getDay();
      int str_hour = hours.periods[curDayIndex].open.time.getHour();
      int str_minute = hours.periods[curDayIndex].open.time.getMinute();
      boolean str_positive = true;

      int clo_year = time.getYear();
      int clo_month = time.getMonth();
      int clo_day = time.getDay();
      int clo_hour = hours.periods[curDayIndex].close.time.getHour();
      int clo_minute = hours.periods[curDayIndex].close.time.getMinute();
      boolean clo_positive = true;

      // if event closes after 12pm, just sets it to close at 12pm
      if (clo_hour < str_hour) {
        clo_hour = 24;
        clo_minute = 0;
      }

      Time open = new Time(str_year, str_month, str_day, str_hour, str_minute, str_positive);
      Time close = new Time(clo_year, clo_month, clo_day, clo_hour, clo_minute, clo_positive);
      Time[] times = {open, close};

      times[0] = open;
      times[1] = close;

      return times;
    } else {
      return null;
    }

  }

  public static String getPhoto(Photo[] photos) {
    String result = "";

    if (photos == null) {
      return result;
    }
    if (photos.length == 0) {
      return result;
    }

    return result;
  }
  /*
   * Event e1 = new Event("ripley's aquarium", "ripley's aquarium", 43.2, 43.2, "aquarium", 5, 2,
   * new Time(2019, 10, 25, 8, 0, true), new Time(2019, 10, 25, 22, 0, true), new Time(0, 0, 0, 2,
   * 0, true), "toronto", "There be fish", "1"); Event e2 = new Event("cn tower", "cn tower", 43.2,
   * 43.2,"lookout", 4, 4, new Time(2019, 10, 25, 8, 0, true), new Time(2019, 10, 25, 22, 0, true),
   * new Time(0, 0, 0, 2, 0, true), "../images/toronto.jpg", "If Quebec is Canada's ass...", "2");
   * Event e3 = new Event("Canadian National Exhibition", "Canadian National Exhibition", 43.2,
   * 43.2,"Festival", 4, 4, new Time(2019, 10, 25, 8, 0, true), new Time(2019, 10, 25, 22, 0, true),
   * new Time(0, 0, 0, 2, 0, true),
   * "https://www.dailydot.com/wp-content/uploads/2018/10/pikachu_surprised_meme-e1540570767482.png",
   * "If Quebec is Canada's ass...", "3"); Event e4 = new Event("Eaton Centre", "Eaton Centre",
   * "Mall", 4, 4, new Time(2019, 10, 25, 8, 0, true), new Time(2019, 10, 25, 22, 0, true), new
   * Time(0, 0, 0, 2, 0, true),
   * "https://www.dailydot.com/wp-content/uploads/2018/10/pikachu_surprised_meme-e1540570767482.png",
   * "If Quebec is Canada's ass...", "4");
   */

  public static String getHomeLocation(double lat, double ltd) {
    String id = "";
    LatLng cur_loc = new LatLng((double) lat, (double) ltd);

    id = "ChIJf9Wrt2_a1IkRrHuIaQFuZbs";
    return id;
  }


  public void initializeDatabase() throws ApiException, InterruptedException, IOException {

    //Data filters
     Time startTime = new Time(2019, 11, 30, 9, 00, true);
     Time endTime = new Time(2019, 11, 30, 24, 00, true);
     double lat = 43.645474;
     double ltd = -79.380922;
     float budget = 150f;
     float maxDist = 10f;
    
     List<String> activities = new ArrayList<String>();
     activities.add("Parks/Amusement Parks");
     activities.add("Tourist Attractions");
    activities.add("Entertainment");
    activities.add("Historical Sites");
    //
    
    // initialize variables
    List<Event> events = new ArrayList<Event>();
    ArrayList<String> place_ids = null;
    NearbySearchRequest all_events = null;
    PlacesSearchResult results[];
    PlacesSearchResponse obj;
    String nextToken = null;

    LatLng cur_loc = new LatLng((double) lat, (double) ltd);

    for (String userAct : activities) {
      for (PlaceType diff : act_map.get(userAct)) {
        System.out.println("place mapping: "+diff);
        all_events =
            PlacesApi.nearbySearchQuery(KEY, cur_loc).radius((int) (maxDist * 1000)).type(diff);

        Thread.sleep(6000);


        obj = all_events.awaitIgnoreError();
        nextToken = obj.nextPageToken;

        System.out.println("extra result token (null if not): "+nextToken);
        results = obj.results;
        place_ids = new ArrayList<String>();
        for (PlacesSearchResult i : results) {
          place_ids.add(i.placeId);
        }
        Thread.sleep(6000);

        while (nextToken != null) {
          all_events = PlacesApi.nearbySearchNextPage(KEY, nextToken);
          Thread.sleep(6000);

          obj = all_events.await();
          results = obj.results;
          for (PlacesSearchResult i : results) {
            place_ids.add(i.placeId);
          }
          System.out.println("Loop iteration over page tokens");
          nextToken = obj.nextPageToken;
        }

        for (String i : place_ids) {
          PlaceDetailsRequest req = PlacesApi.placeDetails(KEY, i).fields();
          PlaceDetails r = null;
          try {
            r = req.await();
          } catch (Exception e) {
            e.printStackTrace();
          }
          //System.out.println(r.name + "" + Arrays.toString(r.types) + "" + r.priceLevel);
          if (r.name != null && r.formattedAddress != null && r.types != null
              && r.priceLevel != null && r.openingHours != null) {

            Time[] times = getTime(r.openingHours, startTime);
            if (times != null) {
              int first = filterByPrice(budget, getPriceLevel(r.priceLevel, r.types, userAct));
              int second = filterByTime(startTime, endTime, times);


              if ((first + second) == 0) {
                Event e = new Event(r.name, r.formattedAddress, r.geometry.location.lat, r.geometry.location.lng,
                    Arrays.toString(r.types).replace("[", "").replace("]", ""), (int) r.rating,
                    getPriceLevel(r.priceLevel, r.types, userAct), times[0], times[1],
                    getExpectedLength(userAct), getPhoto(r.photos), r.url.toString(), i);
                // System.out.println(e.getLocation());

                // Check if event already exists
                boolean check = true;
                for (Event temp : events) {
                  if (temp.getId().equals(e.getId())) {
                    check = false;
                  }
                }
                if (check) {
                  events.add(e);
                  GoogleMaps.storeEventInMongo(e);
                }

              }
            }
          }
        }
      }
    }

  }


  public static Time getExpectedLength(String user) {
    Time length = null;

    if (user.equals("Parks/Amusement Parks")) {
      length = new Time(0, 0, 0, 6, 0, true);
    } else if (user.equals("Food/Restaurants")) {
      length = new Time(0, 0, 0, 1, 0, true);
    } else if (user.equals("Tourist Attractions")) {
      length = new Time(0, 0, 0, 2, 0, true);
    } else if (user.equals("Entertainment")) {
      length = new Time(0, 0, 0, 1, 0, true);
    } else if (user.equals("Shopping")) {
      length = new Time(0, 0, 0, 2, 0, true);
    } else if (user.equals("Bars/Clubs")) {
      length = new Time(0, 0, 0, 2, 0, true);
    } else if (user.equals("Historical Sites")) {
      length = new Time(0, 0, 0, 0, 30, true);
    }
    return length;
  }

  public List<Event> getEvents1(Time startTime, Time endTime, double lat, double ltd, float maxDist,
      List<String> activities, float budget, List<String> visitedEvents, List<String> itinEvents)
      throws ApiException, InterruptedException, IOException {
    List<Event> events = new ArrayList<Event>();

    // initialize variables
    ArrayList<String> place_ids = null;
    NearbySearchRequest all_events = null;
    PlacesSearchResult results[];
    PlacesSearchResponse obj;
    String nextToken = null;

    LatLng cur_loc = new LatLng((double) lat, (double) ltd);

    for (String userAct : activities) {
      for (PlaceType diff : act_map.get(userAct)) {
        System.out.println("place mapping: "+diff);
        all_events =
            PlacesApi.nearbySearchQuery(KEY, cur_loc).radius((int) (maxDist * 1000)).type(diff);

        Thread.sleep(6000);


        obj = all_events.awaitIgnoreError();
        nextToken = obj.nextPageToken;

        System.out.println("extra result token (null if not): "+nextToken);
        results = obj.results;
        place_ids = new ArrayList<String>();
        for (PlacesSearchResult i : results) {
          place_ids.add(i.placeId);
        }
        Thread.sleep(6000);

        while (nextToken != null) {
          all_events = PlacesApi.nearbySearchNextPage(KEY, nextToken);
          Thread.sleep(6000);

          obj = all_events.await();
          results = obj.results;
          for (PlacesSearchResult i : results) {
            place_ids.add(i.placeId);
          }
          System.out.println("Loop iteration over page tokens");
          nextToken = obj.nextPageToken;
        }

        for (String i : place_ids) {
          PlaceDetailsRequest req = PlacesApi.placeDetails(KEY, i).fields();
          PlaceDetails r = null;
          try {
            r = req.await();
          } catch (Exception e) {
            e.printStackTrace();
          }
          //System.out.println(r.name + "" + Arrays.toString(r.types) + "" + r.priceLevel);
          if (r.name != null && r.formattedAddress != null && r.types != null
              && r.priceLevel != null && r.openingHours != null) {

            Time[] times = getTime(r.openingHours, startTime);
            if (times != null) {
              int first = filterByPrice(budget, getPriceLevel(r.priceLevel, r.types, userAct));
              int second = filterByTime(startTime, endTime, times);

              if ((first + second) == 0) {
                Event e = new Event(r.name, r.formattedAddress, r.geometry.location.lat, r.geometry.location.lng,
                    Arrays.toString(r.types).replace("[", "").replace("]", ""), (int) r.rating,
                    getPriceLevel(r.priceLevel, r.types, userAct), times[0], times[1],
                    getExpectedLength(userAct), getPhoto(r.photos), r.url.toString(), i);
                // System.out.println(e.getLocation());

                // Check if event already exists
                boolean check = true;
                for (Event temp : events) {
                  if (temp.getId().equals(e.getId())) {
                    check = false;
                  }
                }
                if (check) {
                  events.add(e);
                }

              }
            }
          }
        }
      }
    }
    /*
     *
     * Event e1 = new Event("ripley's aquarium", "ripley's aquarium", 43.2, 43.2, "aquarium", 5, 2,
     * new Time(2019, 10, 25, 8, 0, true), new Time(2019, 10, 25, 22, 0, true), new Time(0, 0, 0, 2,
     * 0, true), "toronto", "There be fish", "1"); Event e2 = new Event("cn tower", "cn tower",
     * 43.2, 43.2,"lookout", 4, 4, new Time(2019, 10, 25, 8, 0, true), new Time(2019, 10, 25, 22, 0,
     * true), new Time(0, 0, 0, 2, 0, true), "../images/toronto.jpg",
     * "If Quebec is Canada's ass...", "2"); Event e3 = new Event("Canadian National Exhibition",
     * "Canadian National Exhibition", 43.2, 43.2,"Festival", 4, 4, new Time(2019, 10, 25, 8, 0,
     * true), new Time(2019, 10, 25, 22, 0, true), new Time(0, 0, 0, 2, 0, true),
     * "https://www.dailydot.com/wp-content/uploads/2018/10/pikachu_surprised_meme-e1540570767482.png",
     * "If Quebec is Canada's ass...", "3"); Event e4 = new Event("Eaton Centre", "Eaton Centre",
     * 47.2, 47.2, "Mall", 4, 4, new Time(2019, 10, 25, 8, 0, true), new Time(2019, 10, 25, 22, 0,
     * true), new Time(0, 0, 0, 2, 0, true),
     * "https://www.dailydot.com/wp-content/uploads/2018/10/pikachu_surprised_meme-e1540570767482.png",
     * "If Quebec is Canada's ass...", "4");
     *
     * events.add(e1); events.add(e2); events.add(e3); events.add(e4);
     */
    return events;
  }

  public List<Event> getEvents(Time startTime, Time endTime, double lat, double ltd, float maxDist,
      List<String> activities, float budget, List<String> visitedEvents, List<String> itinEvents) {
    List<Event> events = new ArrayList<Event>();
    events = GoogleMaps.getEventsFromMongo();
    List<Event> filteredEvents = new ArrayList<>();
    for (Event e : events) {
      int third = filterByVisited(e.getId(), visitedEvents, itinEvents);
      int first = filterByPrice(budget, e.getPrice());
      int second = 0;
      int fourth = filterByActivity(e.getActivity().split(","), activities);

      if (first == 0 && second == 0 && third == 0 && fourth == 0) {
        filteredEvents.add(e);
      }
    }

    return filteredEvents;
  }

  public Transportation chooseTransportation(List<Transportation> trans) {

    // Checks if walking is an option, and the distance is underneath the walk distance
    for (Transportation t : trans) {
      if (t.getTitle().equals(Transportation.WALK) && t.getDistance() < WALK_DISTANCE) {
        return t;
      }
    }


    // otherwise, gets transportation object with least time
    Transportation min_trans = trans.get(0);
    for (Transportation t : trans) {
      if (t.getExpectedLength().toMinutes() < min_trans.getExpectedLength().toMinutes()) {
        min_trans = t;
      }
    }

    // float distance = trans.get(0).getDistance();
    // Transportation transportation;
    /*
     * // Choose the transportation based on the distance you need to travel if (distance <
     * WALK_DISTANCE) { transportation = getTransportationWithTitle(trans, Transportation.WALK); }
     * else if (WALK_DISTANCE <= distance && distance < BIKE_DISTANCE) { transportation =
     * getTransportationWithTitle(trans, Transportation.BIKE); } else if (BIKE_DISTANCE <= distance
     * && distance < TRANSIT_DISTANCE) { transportation = getTransportationWithTitle(trans,
     * Transportation.TRANSIT); } else { transportation = getTransportationWithTitle(trans,
     * Transportation.DRIVE); }
     * 
     * return transportation;
     */
    return min_trans;

  }

  private Transportation getTransportationWithTitle(List<Transportation> trans, String title) {
    for (Transportation transportation : trans) {
      if (transportation.getTitle().equals(title)) {
        return transportation;
      }
    }

    return null;
  }

  // Gets event from google maps api using name of event
  public Event getEvent(String name) {
    return new Event();
  }

  // Gets event from google maps api using the event id
  public static Event getEventByID(String id) {
    if (id.equals("1")) {
      return new Event("ripley's aquarium", "ripley's aquarium", 43.2, 43.2, "aquarium", 5, 2,
          new Time(2019, 10, 25, 8, 0, true), new Time(2019, 10, 25, 22, 0, true),
          new Time(0, 0, 0, 2, 0, true), "toronto", "There be fish", "1");
    } else if (id.equals("2")) {
      return new Event("cn tower", "cn tower", 43.2, 43.2, "aquarium", 4, 4,
          new Time(2019, 10, 25, 8, 0, true), new Time(2019, 10, 25, 22, 0, true),
          new Time(0, 0, 0, 2, 0, true), "../images/toronto.jpg", "If Quebec is Canada's ass...",
          "2");
    } else if (id.equals("3")) {
      return new Event("Canadian National Exhibition", "Canadian National Exhibition", 43.2, 43.2,
          "aquarium", 4, 4, new Time(2019, 10, 25, 8, 0, true), new Time(2019, 10, 25, 22, 0, true),
          new Time(0, 0, 0, 2, 0, true),
          "https://www.dailydot.com/wp-content/uploads/2018/10/pikachu_surprised_meme-e1540570767482.png",
          "If Quebec is Canada's ass...", "3");
    } else if (id.equals("4")) {
      return new Event("Eaton Centre", "Eaton Centre", 43.2, 43.2, "Mall", 4, 4,
          new Time(2019, 10, 25, 8, 0, true), new Time(2019, 10, 25, 22, 0, true),
          new Time(0, 0, 0, 2, 0, true),
          "https://www.dailydot.com/wp-content/uploads/2018/10/pikachu_surprised_meme-e1540570767482.png",
          "If Quebec is Canada's ass...", "4");
    } else {
      MongoDBEventDAO mpd = new MongoDBEventDAO(new MongoClient());
      return mpd.readEvent(id);
    }
  }

  public static List<Event> getExploreEvents() {
    List<Event> events = new ArrayList<>();
    events.add(getEventByID("4"));
    events.add(getEventByID("4"));
    events.add(getEventByID("4"));
    events.add(getEventByID("4"));
    events.add(getEventByID("4"));
    events.add(getEventByID("4"));
    events.add(getEventByID("4"));
    events.add(getEventByID("4"));
    events.add(getEventByID("4"));
    events.add(getEventByID("4"));
    return events;
  }

  public static float budgetToRange(float budget) {
    if (budget < 10) {
      return 0;
    } else if (budget < 20) {
      return 1;
    } else if (budget < 30) {
      return 2;
    } else if (budget < 40) {
      return 3;
    } else {
      return 4;
    }
  }

  public static float rangeToBudget(float range) {
    if (range == 0) {
      return 0;
    } else if (range == 1) {
      return 10;
    } else if (range == 2) {
      return 20;
    } else if (range == 3) {
      return 30;
    } else {
      return 40;
    }
  }

  public GeoApiContext getKEY() {
    return KEY;
  }

  public void setKEY(GeoApiContext kEY) {
    KEY = kEY;
  }

  public static void storeEventInMongo(Event e) {
    MongoDBEventDAO mpd = new MongoDBEventDAO(new MongoClient());
    mpd.createEvent(e);
  }

  public static List<Event> getEventsFromMongo() {
    MongoDBEventDAO mpd = new MongoDBEventDAO(new MongoClient());
    return mpd.readAllEvents();
  }
}
