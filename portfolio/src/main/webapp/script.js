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
  label.style.display = 'block';
  img.style.opacity = 0.3; 
}

/**
 * Hides label and changes opacity back to 1 when mousing out of image. 
 */
function hideLocation(name) {
  var [label, img] = getImage(name); 
  label.style.display = 'none';
  img.style.opacity = 1; 
}

/**
 * Async function to fetch server content and add it to DOM.
 */
async function getServerContent() {
  var num = "?num=" + document.getElementById('display-num').value;
  var order = "&order=" + document.getElementById('display-order').value;
  var user = "&user=" + document.getElementById('display-user').value;

  const response = await fetch('/data' + num + order + user);
  const content = await response.text();
  var obj = JSON.parse(content); 

  clearDiv();

  const commentEl = document.getElementById('comment-container');
  const descriptionSpan = document.createElement('p');
  descriptionSpan.innerText = 'Showing ' + obj.comments.length + ' of ' + obj.total + ' comments.';
  commentEl.append(descriptionSpan);
  obj.comments.forEach((line) => {
    commentEl.appendChild(createCommentElement(line));
  });
}

/** 
 * Creates a <div> element containing comment information. 
 */
function createCommentElement(comment) {
  const divElement = document.createElement('div');
  divElement.setAttribute('class', 'comment-div');
  
  const userP = document.createElement("span");
  userP.innerText = comment.user; 
  userP.setAttribute('class', 'user-attr');

  const dateP = document.createElement('span');
  dateP.innerText = comment.commentDate; 
  dateP.setAttribute('class', 'date-attr');

  const deleteButton = document.createElement('button');
  deleteButton.innerHTML = '<i class="fas fa-trash-alt"></i>';
  deleteButton.setAttribute('class', 'del-attr');
  deleteButton.addEventListener('click', () => {
    deleteComment(comment);
  });

  const textP = document.createElement('p');
  textP.innerText = comment.content;
  textP.setAttribute('class', 'content-attr');

  divElement.appendChild(userP);
  divElement.appendChild(deleteButton);
  divElement.appendChild(dateP); 
  divElement.appendChild(textP); 
  return divElement;
}

/**
 * Clears div element containing comments.
 */
function clearDiv() {
  const commentEl = document.getElementById('comment-container');
  while(commentEl.firstChild) {
    commentEl.removeChild(commentEl.firstChild);
  }
}

/**
 * Deletes all comments from Datastore.
 */
async function deleteAll() {
  const response = await fetch('/delete-data', {method: 'POST'});
  getServerContent();
}

/**
 * Deletes specified comment.
 */
async function deleteComment(comment) {
  const params = new URLSearchParams();
  params.append('id', comment.id);
  const response = await fetch('/delete-data', {method: 'POST', body: params});
  getServerContent();
}

/**
 * Changes text of selected number option then retrieves server content.
 */
function changeDisplayNum() {
  const selectEl = document.getElementById('display-num');
  var index = selectEl.selectedIndex;
  for(var i = 0; i < selectEl.options.length; i++) {
    if(index === i) {
      selectEl[i].innerHTML = 'Show: ' + selectEl[i].innerHTML;
    }
    else {
      selectEl[i].innerHTML = selectEl[i].innerHTML.replace('Show: ', '');
    }
  }
  getServerContent();
}

/**
 * Changes text of selected order option then retrieves server content.
 */
function changeDisplayOrder() {
  const selectEl = document.getElementById('display-order');
  var index = selectEl.selectedIndex;
  for(var i = 0; i < selectEl.options.length; i++) {
    if(index === i) {
      selectEl[i].innerHTML = 'Order: ' + selectEl[i].innerHTML;
    }
    else {
      selectEl[i].innerHTML = selectEl[i].innerHTML.replace('Order: ', '');
    }
  }
  getServerContent();
}
