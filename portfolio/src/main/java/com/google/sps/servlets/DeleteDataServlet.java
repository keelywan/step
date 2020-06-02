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
import java.util.ArrayList;
import com.google.gson.Gson;
import com.google.sps.data.Comment;
import java.util.Date;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/** Servlet that returns deletes comments. */
@WebServlet("/delete-data")
public class DeleteDataServlet extends HttpServlet {

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String requestID = request.getParameter("id");

    if(requestID != null) {
      // Delete specific comment 
      long id = Long.parseLong(requestID);
      Key commentEntityKey = KeyFactory.createKey("Comment", id);
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.delete(commentEntityKey);
    }
    else {
      // Delete all comments 
      Query query = new Query("Comment");

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery results = datastore.prepare(query);

      for (Entity entity : results.asIterable()) {
        Key commentEntityKey = entity.getKey();
        datastore.delete(commentEntityKey);
      }
    }

    response.setContentType("application/json");
    response.getWriter().println("");
  }
}
