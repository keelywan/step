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

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import com.google.sps.data.CommentRequest;
import java.util.Date;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String num = request.getParameter("num");
    int displayNum = Integer.MAX_VALUE;
    if(!num.equals("all")) {
      displayNum = Integer.parseInt(num);
    }

    Query query = new Query("Comment").addSort("date", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    int total = results.countEntities(FetchOptions.Builder.withLimit(Integer.MAX_VALUE));
    List<Entity> resultsLimited = results.asList(FetchOptions.Builder.withLimit(displayNum));

    ArrayList<Comment> comments = new ArrayList<>();
    for (Entity entity : resultsLimited) {
      long id = entity.getKey().getId();
      String username = (String) entity.getProperty("username");
      Date date = (Date) entity.getProperty("date");
      String content = (String) entity.getProperty("content");

      Comment comment = new Comment(username, id, date, content);
      comments.add(comment);
    }
    CommentRequest res = new CommentRequest(comments, total);
    String json = convertToJSON(res);

    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String comment = request.getParameter("user-comment");
    String name = request.getParameter("name").trim();
    if(name.equals("")) {
      name = "Anonymous";
    }

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("username", name);
    commentEntity.setProperty("date", new Date());
    commentEntity.setProperty("content", comment);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);

    // Redirect back to the HTML page.
    response.sendRedirect("/");
  }

  /**
   * Converts a CommentRequest into a JSON string using the Gson library.
   */
  public String convertToJSON(CommentRequest comments) {
    Gson gson = new Gson();
    String json = gson.toJson(comments);
    return json;
  }
}
