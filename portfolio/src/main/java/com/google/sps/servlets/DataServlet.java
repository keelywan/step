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
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  enum Params { OLDEST, ALL }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Retrieve parameters from request
    String maxNumberOfComments = request.getParameter("num").toUpperCase();
    String orderOfComments = request.getParameter("order").toUpperCase();
    String user = request.getParameter("user").trim().toUpperCase();
    
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
      String imageUrl = (String) entity.getProperty("imageUrl");

      Comment comment = new Comment(username, id, date, content, imageUrl);
      comments.add(comment);
    }

    int totalNumberOfComments = results.countEntities(FetchOptions.Builder.withLimit(Integer.MAX_VALUE));
    CommentRequest res = new CommentRequest(comments, totalNumberOfComments);
    String json = convertToJSON(res);

    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String imageUrl = getUploadedFileUrl(request, "image");
    String comment = request.getParameter("user-comment");
    String name = request.getParameter("name").trim().toUpperCase();
    if(name.equals("")) {
      name = "ANONYMOUS";
    }

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("username", name);
    commentEntity.setProperty("date", new Date());
    commentEntity.setProperty("content", comment);
    commentEntity.setProperty("imageUrl", imageUrl);

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

  /** Returns a URL that points to the uploaded file, or null if the user didn't upload a file. */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get("image");

    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }

    // We could check the validity of the file here, e.g. to make sure it's an image file
    // https://stackoverflow.com/q/10779564/873165

    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

    // To support running in Google Cloud Shell with AppEngine's devserver, we must use the relative
    // path to the image, rather than the path returned by imagesService which contains a host.
    try {
      URL url = new URL(imagesService.getServingUrl(options));
      return url.getPath();
    } catch (MalformedURLException e) {
      return imagesService.getServingUrl(options);
    }
  }
}
