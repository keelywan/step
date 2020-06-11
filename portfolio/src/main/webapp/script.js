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

let showScore = false;

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
  const [ authInfo, commentInfo, statusCode ] = await getServerContent();
  console.log(authInfo);
  console.log(commentInfo);
  console.log(statusCode);
  displayLoginInfo(authInfo);
  if(statusCode === 200) {
    const { comments, totalComments } = commentInfo;

    removeAllCommentsFromPage();
    addComments(comments, totalComments, authInfo.email);
    displayCommentSection();
  }
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
  let parsedContent = JSON.parse(content);
  parsedContent.push(response.status);
  return parsedContent;
}

/**
 * Add all comments from server to HTML page.
 */
function addComments(comments, totalComments, email) {
  const commentEl = document.getElementById('comment-container');
  const descriptionParagraph = document.createElement('p');
  descriptionParagraph.innerText =
      'Showing ' + comments.length + ' of ' + totalComments + ' comments.';
  commentEl.append(descriptionParagraph);
  comments.forEach((line) => {
    commentEl.appendChild(createCommentElement(line, email));
  });
}

/** 
 * Creates a <div> element containing comment information. 
 */
function createCommentElement(comment, email) {
  const divElement = document.createElement('div');
  divElement.setAttribute('class', 'comment-div');
  
  const userSpan = document.createElement("span");
  userSpan.innerText = comment.user;
  userSpan.setAttribute('class', 'user-attr');

  const dateSpan = document.createElement('span');
  dateSpan.innerText = comment.commentDate;
  dateSpan.setAttribute('class', 'date-attr');

  const textParagraph = document.createElement('p');
  textParagraph.innerText = comment.content;
  textParagraph.setAttribute('class', 'content-attr');

  divElement.appendChild(userSpan);
  if(email === comment.email) {
    const deleteButton = document.createElement('button');
    deleteButton.innerHTML = '<i class="fas fa-trash-alt"></i>';
    deleteButton.setAttribute('class', 'del-attr');
    deleteButton.addEventListener('click', () => {
      deleteComment(comment);
    });
    divElement.appendChild(deleteButton);
  }
  divElement.appendChild(dateSpan);
  divElement.appendChild(textParagraph);

  if(showScore) {
    const scoreParagraph = document.createElement('p');
    scoreParagraph.innerText = 'Sentiment Score: ' + comment.score;
    scoreParagraph.setAttribute('class', 'score-attr');
    divElement.appendChild(scoreParagraph);
  }
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
      center: {lat: 38.8761607, lng: -77.481413}, zoom: 9});

  const billyGoatString = '<h3>Billy Goat Trail</h3>' +
      '<p>The Billy Goat Trail is located in Great Falls, Maryland. It\'s one of my favorite trails ' +
      'because it requires climbing over boulders and traversing a rocky terrain - just like a goat! ' +
      'It also has spectacular views of the Potomac River. For more information, check ' +
      '<a href="https://www.nps.gov/choh/planyourvisit/billy-goat-trail.htm">here</a>.</p>' +
      '<img src="/images/billygoat.jpg" class="info-window-img"/>';
  const bearsDenString = '<h3>Bears Den Overlook</h3>' +
      '<p>Bear\'s Den is just off the Appalachian Trail. It\'s a fairly short and easy hike that leads ' +
      'to a scenic overlook. If you want, there are also a network of trails that you can take to lengthen ' +
      'your hike.</p>' +
      '<img src="/images/bearsden.jpg" class="info-window-img"/>';
  const skylineDriveString = '<h3>Stony Man</h3>' +
      '<p>Stony Man is a short and easy hike in Shenandoah National Park. This trail will take you to ' +
      'the summit of Stony Man Mountain where you can capture amazing views of the Shenandoah Valley. ' +
      'There are also tons of other trails in Shenandoah National Park that are worth checking out!' +
      '<img src="/images/skyline.jpeg" class="info-window-img"/>';
  const sugarloafString = '<h3>Sugarloaf Mountain</h3>' +
      '<p>Sugarloaf Mountain has a variety of trails to suit everyone\'s hiking level. The trails have ' +
      'lots of inclines, so get ready. Also dog friendly! Check out more information ' +
      '<a href="https://www.alltrails.com/trail/us/maryland/sugarloaf-mountain-and-northern-peaks-trail">' +
      'here</a>.</p>' +
      '<img src="/images/sugarloaf.jpg" class="info-window-img"/>';
  const restonString = '<h3>Reston Trails</h3>' +
      '<p>Reston has miles of paved and natural pathways that connect neighborhoods, recreation areas, ' +
      'and shopping centers. My family loves to come here to walk because of the shaded paths and close ' +
      'proximity to our home. Here\'s a link to all the ' +
      '<a href="https://www.reston.org/Parks,RecreationEvents/Pathways/tabid/418/Default.aspx">trail maps</a>.</p>';

  createMarkerAndInfoWindow(map, 38.9931697, -77.3153832, 'Billy Goat Trail', billyGoatString);
  createMarkerAndInfoWindow(map, 38.9963263, -77.4272852, 'Bears Den', bearsDenString);
  createMarkerAndInfoWindow(map, 38.6106997, -78.365843, 'Stony Man', skylineDriveString);
  createMarkerAndInfoWindow(map, 39.2648323, -77.4040018, 'Sugarloaf Mountain', sugarloafString);
  createMarkerAndInfoWindow(map, 38.92485, -77.3716722, 'Reston Trails', restonString);
}

/**
 * Adds a marker and corresponding info window to map.
 */
function createMarkerAndInfoWindow(map, latitude, longitude, titleDesc, contentDesc) {
  const icon = {
    url: 'https://maps.google.com/mapfiles/kml/paddle/blu-stars.png',
    scaledSize: new google.maps.Size(35, 35),
    origin: new google.maps.Point(0,0),
    anchor: new google.maps.Point(0, 0)
  };

  const marker = new google.maps.Marker({
    position: {lat: latitude, lng: longitude},
    map: map,
    animation: google.maps.Animation.DROP,
    icon: icon,
    title: titleDesc
  });

  const infoWindow = new google.maps.InfoWindow({content: contentDesc});
  marker.addListener('click', function() {
    infoWindow.open(map, marker);
  })
}

/**
 * Initializes elements on page on load.
 */
function initializePage() {
  createMap();
  displayServerContent();
}

/**
 * Handles display of sentiment scores.
 */
function toggleSentimentScore() {
  showScore = !showScore;
  displayServerContent();
}

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);

/**
 * Fetches personality data and uses it to create a chart.
 */
function drawChart() {
  fetch('/personality-data').then(response => response.json())
  .then((personalityVotes) => {
    const data = new google.visualization.DataTable();
    data.addColumn('string', 'Personality Type');
    data.addColumn('number', 'Votes');
    data.addColumn({type:'string', role:'style'});
    Object.keys(personalityVotes).forEach((type) => {
      data.addRow([type, personalityVotes[type], 'color:#5c7fe0']);
    });

    const options = {
      'title': 'Personality Types',
      'width':600,
      'height':500,
      legend: { position: "none" }
    };

    const chart = new google.visualization.ColumnChart(
        document.getElementById('chart-container'));
    chart.draw(data, options);
  });
}

/**
 * Display comment section and its navbar link.
 */
function displayCommentSection() {
  document.getElementById('comments').style.display = 'block';
  document.getElementById('comments-link').style.display = 'block';
}

/** 
 * Sets login or logout link in nav bar.
 */
function displayLoginInfo(authStatus) {
  const output = authStatus.loggedIn 
      ? '<a href="' + authStatus.logoutUrl + '">Logout</a>'
      : '<a href="' + authStatus.loginUrl + '">Login</a>';
  document.getElementById('login').innerHTML = output;
} 