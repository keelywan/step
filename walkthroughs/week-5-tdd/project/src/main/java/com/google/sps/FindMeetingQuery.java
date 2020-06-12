// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    ArrayList<TimeRange> meetingTimes = new ArrayList<>();
    long duration = request.getDuration();
    if(duration > TimeRange.WHOLE_DAY.duration() || duration < 0) {
      return meetingTimes;
    }

    ArrayList<String> attendees = new ArrayList<String>(request.getAttendees());
    ArrayList<Event> eventsList = new ArrayList<Event>(events);
    ArrayList<TimeRange> timesList = filterEventsAndGetTimes(eventsList, attendees);

    Collections.sort(timesList, TimeRange.ORDER_BY_START);

    int curTime = 0;
    int listIndex = 0;
    while(listIndex < timesList.size()) {
      if(curTime + duration > TimeRange.END_OF_DAY) {
        return meetingTimes;
      }
      TimeRange timeInterval = timesList.get(listIndex);
      int startTime = timeInterval.start();
      int endTime = timeInterval.end();
      int diff = startTime - curTime; 
      if(diff >= duration) {
        meetingTimes.add(TimeRange.fromStartDuration(curTime, diff));
        curTime = endTime;
      } else {
        curTime = Math.max(curTime, endTime);
      }
      listIndex++;
    }
    if(TimeRange.END_OF_DAY - curTime + 1 >= duration) {
      meetingTimes.add(TimeRange.fromStartDuration(curTime, TimeRange.END_OF_DAY - curTime + 1));
    }
    /**
     * go through list of events
     * if the current time + duration is past the end of the day, return the list of times 
     * if we reach the end of the list, check to see if there is time after htat  
     */

    return meetingTimes;
  }

  /**
   * Filter out events that don't have meeting attendees in it and return list of TimeRanges.
   */
  private ArrayList<TimeRange> filterEventsAndGetTimes(ArrayList<Event> events, ArrayList<String> attendees) {
    ArrayList<TimeRange> filteredTimes = new ArrayList<TimeRange>();
    for(int i = 0; i < events.size(); i++) {
      Event event = events.get(i);
      ArrayList<String> eventAttendees = new ArrayList<String>(event.getAttendees());
      if(hasIntersection(eventAttendees, attendees)) {
        filteredTimes.add(event.getWhen());
      }
    }
    return filteredTimes; 
  }

  /**
   * Determine if there is an intersection between event attendees and meeting attendees.
   */
  private boolean hasIntersection(ArrayList<String> eventAttendees, ArrayList<String> meetingAttendees) {
    for(int i = 0; i < meetingAttendees.size(); i++) {
      if(eventAttendees.contains(meetingAttendees.get(i))) {
        return true;
      }
    }
    return false;
  }

  // Debugging purposes  
  public void printEventList(ArrayList<Event> events) {
    for(int i = 0; i < events.size(); i++) {
      System.out.println(events.get(i).getWhen().start());
    }
    System.out.println("-------BREAK-------");
  }
}
