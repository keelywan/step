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
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  enum Params { OLDEST, ALL, ANONYMOUS }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    response.setContentType("application/json");
    response.getWriter().print("[");
    // Retrieve authentication status
    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/auth");
    dispatcher.include(request, response);
    response.setStatus(response.getStatus());

    if(response.getStatus() == HttpServletResponse.SC_FORBIDDEN) {
      response.getWriter().print(", {}");
    }
    else {
      // Retrieve parameters from request
      String maxNumberOfComments = request.getParameter("num").toUpperCase();
      String orderOfComments = request.getParameter("order").toUpperCase();
      String user = request.getParameter("user").trim().toUpperCase();
      String languageCode = request.getParameter("lang");
      
      int numberOfCommentsDisplayed = setCommentLimit(maxNumberOfComments);
      SortDirection sortOrder = setSortStyle(orderOfComments);
      Query query = setQuery(sortOrder, user);

      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      PreparedQuery results = datastore.prepare(query);

      List<Entity> resultsLimited = results.asList(FetchOptions.Builder.withLimit(numberOfCommentsDisplayed));

      // Add retrieved comments to ArrayList
      ArrayList<Comment> comments = new ArrayList<>();
      for (Entity entity : resultsLimited) {
        long id = entity.getKey().getId();
        String username = (String) entity.getProperty("username");
        Date date = (Date) entity.getProperty("date");
        String content = (String) entity.getProperty("content");
        Double score = (Double) entity.getProperty("score");

        // content = translateComment(content, languageCode);

        Comment comment = new Comment(username, id, date, content, score);
        comments.add(comment);
      }

      int totalNumberOfComments = results.countEntities(FetchOptions.Builder.withLimit(Integer.MAX_VALUE));
      CommentRequest res = new CommentRequest(comments, totalNumberOfComments);
      String json = convertToJSON(res);

      response.getWriter().print(",");
      response.getWriter().println(json);
    }
    response.getWriter().print("]");
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String comment = request.getParameter("user-comment");
    String name = request.getParameter("name").trim().toUpperCase();
    if(name.equals("")) {
      name = Params.ANONYMOUS.toString(); 
    }

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("username", name);
    commentEntity.setProperty("date", new Date());
    commentEntity.setProperty("content", comment);
    commentEntity.setProperty("score", getSentimentScore(comment));

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

  /**
   * Returns an int representing the maximum number of comments being requested.
   */
  public int setCommentLimit(String numberOfComments) {
    return numberOfComments.equals(Params.ALL.toString()) ? Integer.MAX_VALUE :
        Integer.parseInt(numberOfComments);
  }

  /**
   * Returns a SortDirection for the order the comments in the query are sorted.
   */
  public SortDirection setSortStyle(String orderOfComments) {
    return orderOfComments.equals(Params.OLDEST.toString()) ? SortDirection.ASCENDING :
        SortDirection.DESCENDING;
  }

  /**
   * Returns a query for Datastore given the sort order and the user's name.
   */
  public Query setQuery(SortDirection orderOfComments, String username) {
    Query query = new Query("Comment").addSort("date", orderOfComments);
    if(username != null && !username.equals("")) {
      return query.setFilter(new FilterPredicate("username", FilterOperator.EQUAL, username));
    }
    return query;
  }

  /**
   * Translates a comment given the language code.
   */
  public String translateComment(String originalComment, String languageCode) {
    Translate translate = TranslateOptions.getDefaultInstance().getService();
    Translation translation =
        translate.translate(originalComment, Translate.TranslateOption.targetLanguage(languageCode));
    return translation.getTranslatedText();
  }

  /**
   * Computes sentiment score of comment.
   */
  public float getSentimentScore(String comment) throws IOException {
    Document doc =
        Document.newBuilder().setContent(comment).setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    float score = sentiment.getScore();
    languageService.close();
    return score;
  }
}
