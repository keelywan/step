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

import java.util.Date;

/** Class containing comment data. */ 
public class Comment {

  /** Username of commenter. */
  private String user; 

  /** ID of comment in Datastore. */
  private long id;

  /** Time at which comment was posted. */
  private Date commentDate; 

  /** Comment text. */
  private String content; 

  /** Sentiment score of comment. */
  private Double score;

  public Comment(String username, long commentID, Date postTime, String comment, Double sentimentScore) {
    user = username; 
    id = commentID;
    commentDate = postTime; 
    content = comment;
    score = sentimentScore;
  }
}