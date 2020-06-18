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

package com.google.sps.servlets;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;


@WebServlet("/personality-data")
public class PersonalityDataServlet extends HttpServlet {

  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json");

    Query query = new Query("Personality");
    PreparedQuery results = datastore.prepare(query);

    Map<String, Long> personalityTypeVotes = new HashMap<>();

    for(Entity personalityEntity: results.asIterable()) {
      String type = (String) personalityEntity.getProperty("type");
      Long numVotes = (Long) personalityEntity.getProperty("votes");
      personalityTypeVotes.put(type, numVotes);
    }

    Gson gson = new Gson();
    String json = gson.toJson(personalityTypeVotes);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String personalityType = request.getParameter("type");

    Long currentVotes = getNumberOfVotes(personalityType);

    Entity personalityEntity = new Entity("Personality", personalityType);
    personalityEntity.setProperty("type", personalityType);
    personalityEntity.setProperty("votes", currentVotes + 1);

    datastore.put(personalityEntity);

    response.sendRedirect("/");
  }

  private long getNumberOfVotes(String personalityType) {
    Query query = new Query("Personality")
        .setFilter(new Query.FilterPredicate("type", Query.FilterOperator.EQUAL, personalityType));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      return 0;
    }
    Long numVotes = (Long) entity.getProperty("votes");
    return numVotes;
  }
}
