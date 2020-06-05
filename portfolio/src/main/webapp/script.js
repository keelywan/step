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

/**
 * Creates a map and adds it to the page.
 */
function createMap() {
  const map = new google.maps.Map(
      document.getElementById('hiking-map'), {
      center: {lat: 38.8991477, lng: -77.2767716}, zoom: 9});

  const billyGoatString = '<h3>Billy Goat Trail</h3>' +
      '<p>The Billy Goat Trail is located in Great Falls, Maryland. It\'s one of my favorite trails because it ' +
      'requires climbing over boulders and traversing a rocky terrain - just like a goat! It also has ' +
      'spectacular views of the Potomac River. For more information, check ' +
      '<a href="https://www.nps.gov/choh/planyourvisit/billy-goat-trail.htm">here</a>.</p>' +
      '<img src="/images/billygoat.jpg" class="info-window-img"/>';
  const bearsDenString = '<h3>Bears Den Overlook</h3>' +
        '<p>Bear\'s Den is just off the Appalachian Trail. It\'s a fairly short and easy hike that leads to a scenic ' +
        'overlook. If you want, there are also a network of trails that you can take to lengthen your hike.</p>' +
        '<img src="/images/bearsden.jpg" class="info-window-img"/>';
  const skylineDriveString = '<h3>Skyline Drive</h3>' +
        '<p>Bear\'s Den is just off the Appalachian Trail. It\'s a fairly short and easy hike that leads to a scenic ' +
        'overlook. If you want, there are also a network of trails that you can take to lengthen your hike.</p>' +
        '<img src="/images/skyline.jpeg" class="info-window-img"/>';
  const sugarloafString = '<h3>Sugarloaf Mountain</h3>' +
        '<p>Sugarloaf Mountain has a variety of trails to suit everyone\'s hiking level. The trails have lots of ' +
        'inclines, so get ready. Also dog friendly! Check out more information ' +
        '<a href="https://www.alltrails.com/trail/us/maryland/sugarloaf-mountain-and-northern-peaks-trail">here</a>.</p>' +
        '<img src="/images/sugarloaf.jpg" class="info-window-img"/>';

  createMarkerAndInfoWindow(map, 38.9931697, -77.3153832, 'Billy Goat Trail', billyGoatString);
  createMarkerAndInfoWindow(map, 38.9963263, -77.4272852, 'Bears Den', bearsDenString);
  createMarkerAndInfoWindow(map, 38.6692618, -78.1812651, 'Skyline Drive', skylineDriveString);
  createMarkerAndInfoWindow(map, 39.2648323, -77.4040018, 'Sugarloaf Mountain', sugarloafString);
}

function createMarkerAndInfoWindow(map, latitude, longitude, titleDesc, contentDesc) {
  const marker = new google.maps.Marker({
    position: {lat: latitude, lng: longitude},
    map: map,
    title: titleDesc
  });
  const infoWindow = new google.maps.InfoWindow({content: contentDesc});
  marker.addListener('click', function() {
    infoWindow.open(map, marker);
  })
}