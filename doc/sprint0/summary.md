# Krusty Krab : Planit - Summary

Planning a trip shouldn't have to be stressful and time consuming. The less time you are spending planning where to go, means that you can spend more time visiting more places. 

At this time there are many tools that can help you plan specific parts of your trips but this can easily make planning hectic as you will have to go back and forth from one platform to another. 

If you were able to have all these planning tools in one app, it would greatly organize your planning and in turn speed the process.

## Objectives

The main objective of this project is to provide travelers with a way to quickly create a custom itinerary for their trips. The Planit app will provide the users with a platform that will have all the planning tools they may need to organize their trip. 

Below are the tools that the Planit team presented, with focus more towards the first five features in this list as they are the most important.

- Filters to help plan user's ideal trips
  - Location
  - Date and Time
  - Choice of Transportation
  - Travel Proximity/Radius
  - Daily Budget
  - Attraction Preferences
 - Sync Itinerary to Phone Calendar
 - In-app Maps
 - Book Reservations Through the App
 - Ability to Rate/Review Locations
 - Share Itinerary With Others

The core component of this app is the **personal itinerary creator**. Once the user enters the filters above, the Planit app will create a personal itinerary based on the selected filters. Once the itinerary is created, the user will then have the ability to make changes to their itinerary as they wish. This automatic planner will provide the users with a quick way to decide where they should visit during their trip.

## Key Users

Below are the key demographics that the Planit team presented.

- **Travelers**
- Families
- Couples
- Business People

Ideally, this app would be useful for anyone who needs to plan a trip, whether it be for a day or a couple weeks. One of the noticeable key users of the app would be travelers. Since the main objective of this app is to provide people with a quick way to plan their trips, it seems trivial that the key users of the app would be travelers. It would allow them to cut down on their planning time so that they can go enjoy and explore their location.

## Scenarios

- As a non-registered user, I want to be able to register an account
  - Create a new user account with username, password, email and store in our database
- As a user, I want to create an itinerary for a day
  - Select a day you want to plan
  - Select filters
  - Based on selected filters find activities to add to the itinerary
  - Generate itinerary for the day
- As a user, I want to change activities in my itinerary
  - Select an activity you want to switch
  - Select an activity available at that given time
  - Save the new itinerary
- As a user, I want to sync my itinerary to my phone calendar
  - Using the Google Calendar API to edit the user's calendar
  - Perhaps edit the default calendar on smart phones (iOS/Android)

- As a user, I want to be able to see the location of activities on a map
  - Select an activity or location you would like to visit then use Google Maps API to view it on a map
- Book Reservations Through the App
  - After deciding a place they would like to visit, they can either click on a phone number provided or click a link to be redirected to the website
- Ability to Rate/Review Locations
  - The user can rate the location out of 5 and write a review which will be stored in our database
  - Or possibly use another 3rd party platform such as Google Reviews or TripAdvisor
- Share Itinerary With Others
  - Create a link for the unique itinerary created
  - Share via text, messenger, Instagram, etc.

## Principles

While developing the product, our team has came up with the following principles that we thought were important.

- **Intuitive Design**: The app should be simple and easy to use. The design of the app should make navigation and usage of the app intuitive.
- **Itinerary Priority**: The main priority of the app is the personal itinerary generation tool. Since it is unique to Planit, this feature should be the most polished
- **"Why build things that are already made"**: The app will have many features that will require large databases. But there are already many large databases with the information we need. So when building the app we will look for APIs that we can call instead in order to simplify the work that needs to be done (Examples Google Map, Google Reviews)