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
import java.util.ArrayList;
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

    Comparator<Event> orderByStartTime =
        (Event e1, Event e2) -> Long.compare(e1.getWhen().start(), e2.getWhen().start());
    Collections.sort(eventsList, orderByStartTime);
    return filterEventsAndGetAvailableTimes(eventsList, attendees, duration);
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

  /**
   * Filter out events that don't have meeting request attendees in it and return list
   * of all available TimeRanges longer than duration.
   */
  private ArrayList<TimeRange> filterEventsAndGetAvailableTimes(ArrayList<Event> events,
      ArrayList<String> attendees, long duration) {
    ArrayList<TimeRange> availableTimes = new ArrayList<TimeRange>();
    int curTime = 0;
    for(int i = 0; i < events.size(); i++) {
      Event event = events.get(i);
      ArrayList<String> eventAttendees = new ArrayList<String>(event.getAttendees());
      if(hasIntersection(eventAttendees, attendees)) {
        if(curTime + duration > TimeRange.END_OF_DAY) {
          return availableTimes;
        }
        TimeRange timeInterval = event.getWhen();
        int startTime = timeInterval.start();
        int endTime = timeInterval.end();
        int startCurDiff = startTime - curTime;
        if(startCurDiff >= duration) {
          availableTimes.add(TimeRange.fromStartDuration(curTime, startCurDiff));
          curTime = endTime;
        } else {
          curTime = Math.max(curTime, endTime);
        }
      }
    }
    if(TimeRange.END_OF_DAY - curTime >= duration) {
      availableTimes.add(TimeRange.fromStartEnd(curTime, TimeRange.END_OF_DAY, true));
    }
    return availableTimes;
  }
}
