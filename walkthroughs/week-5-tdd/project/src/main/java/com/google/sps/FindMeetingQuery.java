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
import java.util.Comparator;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    HashSet<TimeRange> meetingTimes = new HashSet<>();
    long duration = request.getDuration();
    if(duration > TimeRange.WHOLE_DAY.duration() || duration < 0) {
      return meetingTimes;
    }

    Collection<String> attendees = request.getAttendees();
    ArrayList<Event> eventsList = new ArrayList<Event>(events);
    printEventList(eventsList);

    Comparator<Event> compareByStartTime = (Event e1, Event e2) -> e1.getWhen().start() - e2.getWhen().start();
    Collections.sort(eventsList, compareByStartTime);
    printEventList(eventsList);

    int curTime = 0;
    int listIndex = 0;
    while(curTime + duration < TimeRange.END_OF_DAY && listIndex < eventsList.size()) {

    }

    return meetingTimes;
    // throw new UnsupportedOperationException("TODO: Implement this method.");
    // Check time request.getDuration() should be greater than 0, less than 24
    // Sort events in order of start times -- DONE (does this need to be done?)
    // Find events with all attendees? Does this need to be done? -- for now, assume events have at least one of the attendees
    // Merge together adjacent or overlapping events
    // Find empty blocks 
    // while time + duration < TimeRange.END_OF_DAY
  }

  // Merge overlapping events -- only pass if list is greater than 1
  public ArrayList<Event> mergeEvents(ArrayList<Event> events) {
    int startTime = events.get(0).getWhen().start();
    int endTime = events.get(0).getWhen().end();
    for(int i = 1; i < events.size(); i++) {

    }
    return null;
  }

  // Debugging purposes  
  public void printEventList(ArrayList<Event> events) {
    for(int i = 0; i < events.size(); i++) {
      System.out.println(events.get(i).getWhen().start());
    }
    System.out.println("-------BREAK-------");
  }
}
