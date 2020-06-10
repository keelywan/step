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

/** Class containing user's authorization status. */
public class AuthStatus {

  /**
   * True if user is currently logged in, false otherwise.
   */
  private boolean loggedIn;

  /**
   * Email of current user if they are logged in, null otherwise.
   */
  private String email;

  /**
   * Login URL if user is currently logged out, null otherwise.
   */
  private String loginUrl;

  /**
   * Logout URL if user is currently logged in, null otherwise.
   */
  private String logoutUrl;

  public AuthStatus(boolean loginStatus, String userEmail, String loginLink, String logoutLink) {
    loggedIn = loginStatus;
    email = userEmail;
    loginUrl = loginLink;
    logoutUrl = logoutLink;
  }
}