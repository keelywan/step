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

  /** 
   * List containing a strict subset of the comments in Datastore that satisfy the query
   * if fewer than the total were requested, else all of the comments with a maximum of 
   * MAX_INTEGER.
   */
  private List<Comment> comments;

  /**
   * Total number of comments existing in Datastore that satisfy the query. 
   */
  private int totalComments;

  public CommentRequest(List<Comment> commentsToReturn, int maxNumberofComments) {
    comments = commentsToReturn;
    totalComments = maxNumberofComments;
  }
}