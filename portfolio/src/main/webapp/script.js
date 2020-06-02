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

/**
 * Retrieves image and label information.
 */
function getImage(name) {
  return [document.getElementById(name), document.getElementById(name + '-Img')];
}

/**
 * Reduces opacity and shows name of location when image is moused over.
 */
function displayLocation(name) {
  var [label, img] = getImage(name);
  label.style.display = "block"; 
  img.style.opacity = 0.3; 
}

/**
 * Hides label and changes opacity back to 1 when mousing out of image. 
 */
function hideLocation(name) {
  var [label, img] = getImage(name); 
  label.style.display = "none"; 
  img.style.opacity = 1; 
}

/**
 * Async function to fetch server content and add it to DOM.
 */
async function getServerContent() {
  const response = await fetch('/data');
  const content = await response.text();
  var obj = JSON.parse(content); 

  const commentEl = document.getElementById('comment-container');
  obj.forEach((line) => {
    commentEl.appendChild(createCommentElement(line));
  });
}

/** 
 * Creates a <div> element containing comment information. 
 */
function createCommentElement(comment) {
  const divElement = document.createElement('div');
  divElement.setAttribute("class", "comment-div");
  
  var userP = document.createElement("span");
  userP.innerText = comment.user; 
  userP.setAttribute("class", "user-attr");

  var dateP = document.createElement("span");
  dateP.innerText = comment.commentDate; 
  dateP.setAttribute("class", "date-attr");

  var textP = document.createElement("p"); 
  textP.innerText = comment.content;
  textP.setAttribute("class", "content-attr");

  divElement.appendChild(userP);
  divElement.appendChild(dateP); 
  divElement.appendChild(textP); 
  return divElement;
}
