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
  const [label, img] = getImage(name);
  label.style.display = 'block';
  img.style.opacity = 0.3; 
}

/**
 * Hides label and changes opacity back to 1 when mousing out of image. 
 */
function hideLocation(name) {
  const [label, img] = getImage(name);
  label.style.display = 'none';
  img.style.opacity = 1; 
}

/**
 * Add server content to DOM.
 */
async function displayServerContent() {
  const { comments, totalComments } = await getServerContent();

  removeAllCommentsFromPage();

  const commentEl = document.getElementById('comment-container');
  const descriptionParagraph = document.createElement('p');
  descriptionParagraph.innerText = 
      'Showing ' + comments.length + ' of ' + totalComments + ' comments.';
  commentEl.append(descriptionParagraph);
  comments.forEach((line) => {
    commentEl.appendChild(createCommentElement(line));
  });
}

/**
 * Async function to fetch and return server content.
 */
async function getServerContent() {
  const num = "?num=" + document.getElementById('display-num').value;
  const order = "&order=" + document.getElementById('display-order').value;
  const user = "&user=" + document.getElementById('display-user').value;
  const langCode = "&lang=" + document.getElementById('display-language').value;

  const response = await fetch('/data' + num + order + user + langCode);
  const content = await response.text();
  return JSON.parse(content);
}

/** 
 * Creates a <div> element containing comment information. 
 */
function createCommentElement(comment) {
  const divElement = document.createElement('div');
  divElement.setAttribute('class', 'comment-div');
  
  const userSpan = document.createElement("span");
  userSpan.innerText = comment.user;
  userSpan.setAttribute('class', 'user-attr');

  const dateSpan = document.createElement('span');
  dateSpan.innerText = comment.commentDate;
  dateSpan.setAttribute('class', 'date-attr');

  const deleteButton = document.createElement('button');
  deleteButton.innerHTML = '<i class="fas fa-trash-alt"></i>';
  deleteButton.setAttribute('class', 'del-attr');
  deleteButton.addEventListener('click', () => {
    deleteComment(comment);
  });

  const textParagraph = document.createElement('p');
  textParagraph.innerText = comment.content;
  textParagraph.setAttribute('class', 'content-attr');

  divElement.appendChild(userSpan);
  divElement.appendChild(deleteButton);
  divElement.appendChild(dateSpan);
  divElement.appendChild(textParagraph);
  return divElement;
}

/**
 * Clears div element containing comments.
 */
function removeAllCommentsFromPage() {
  const commentEl = document.getElementById('comment-container');
  while(commentEl.firstChild) {
    commentEl.removeChild(commentEl.firstChild);
  }
}

/**
 * Deletes all comments from Datastore.
 */
async function deleteAllComments() {
  await fetch('/delete-data', {method: 'POST'});
  displayServerContent();
}

/**
 * Deletes specified comment.
 */
async function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  await fetch('/delete-data', {method: 'POST', body: params});
  displayServerContent();
}
