package krusty_krab.krusty_krab.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Itinerary {

    private Time startTime;
    private Time endTime;
    private String home;
    private String location;
    private float maxDist;
    private List<String> activities;
    private float budget;
    private List<ItineraryItem> itin = new ArrayList();
    private List<String> visitedEvents = new ArrayList();

    public float minScore = 0.0f;
    public GoogleMaps gm = new GoogleMaps();

    public Itinerary() {}

    public Event getNextBestEvent(Time curTime, String curLoc){

        List<Event> events = this.gm.getEvents(curTime, this.getEndTime(), curLoc, this.getLocation(), this.getMaxDist(), this.getActivities(), this.getBudget());

        // If no events left that satisfy filters
        if(events.isEmpty()) {
            throw new NoSuchElementException();
        }
        Event bestEvent = events.get(0);

        for(Event e: events){
            //if((!this.getVisitedEvents().contains(e.getLocation())) && (e.getScore(curTime, curLoc, this.gm, this.getMaxDist(), this.getBudget()) > bestEvent.getScore(curTime, curLoc, this.gm, this.getMaxDist(), this.getBudget()))){
            if(e.getScore(curTime, curLoc, this.gm, this.getMaxDist(), this.getBudget()) > bestEvent.getScore(curTime, curLoc, this.gm, this.getMaxDist(), this.getBudget())){
                bestEvent = e;
            }
        }

        //this.visitedEvents.add(bestEvent.getLocation());
        return bestEvent;
    }

    public void createItinerary(){
        Time curTime = getStartTime();
        String curLoc = getHome();
        try{
            Event nextEvent = getNextBestEvent(curTime, curLoc);
            //this.itin.add(nextEvent);

            while(nextEvent.getScore(curTime, curLoc, this.gm, this.getMaxDist(), this.getBudget()) > minScore){
                Transportation transp = this.gm.getTransportation(curLoc, nextEvent.getLocation(), curTime);
                this.itin.add(transp);
                this.itin.add(nextEvent);
                curLoc = nextEvent.getLocation();
                curTime = curTime.add(transp.getExpectedLength()).add(nextEvent.getExpectedLength());
                nextEvent = getNextBestEvent(curTime, curLoc);
                break;
            }
        }
        catch(NoSuchElementException e){}
    }

    public List<ItineraryItem> getItin() {
        return itin;
    }

    public Time getStartTime() {
        return startTime;
    }

    public Time getEndTime() {
        return endTime;
    }

    public String getHome() {
        return home;
    }

    public String getLocation() {
        return location;
    }

    public float getMaxDist() {
        return maxDist;
    }

    public List<String> getActivities() {
        return activities;
    }

    public float getBudget() {
        return budget;
    }

    public void setStartTime(Time startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Time endTime) {
        this.endTime = endTime;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setMaxDist(float maxDist) {
        this.maxDist = maxDist;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }

    public void setBudget(float budget) {
        this.budget = budget;
    }

    public void setItin(List<ItineraryItem> itin) {
        this.itin = itin;
    }

    public List<String> getVisitedEvents() {
        return visitedEvents;
    }
}