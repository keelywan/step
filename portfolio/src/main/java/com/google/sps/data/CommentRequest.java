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

package com.google.sps.data;

import com.google.sps.data.Comment;
import java.util.List;


/** Class containing requested comment data. */ 
public class CommentRequest {

  /** List of requested comments. */
  private List<Comment> comments;

  /**
   * Total number of comments found from the query. If no limit is set and all comments asked for
   * are returned, total should be equal to the size of List<Comment> comments. Alternatively, if
   * the limit is greater than the number of comments returned, then total should also be equal to
   * the size of List<Comment> comments. Otherwise, only a subset of all the comments found will
   * be returned to the client.
   */
  private int totalComments;

  public CommentRequest(List<Comment> commentsToReturn, int maxNumberofComments) {
    comments = commentsToReturn;
    totalComments = maxNumberofComments;
  }
}