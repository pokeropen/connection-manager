'use strict';

var nameInput = $('#name');
var roomInput = $('#room-id');
var userroomOuterContainer = document.querySelector('#user-room-outer-container');
var eventsOuterContainer = document.querySelector('#events-outer-container');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');
var roomIdDisplay = document.querySelector('#room-id-display');
var leaveButton = document.querySelector('#leave-room-button');

var stompClient = null;
var currentSubscription;
var username = null;
var roomId = null;
var topic = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function connect(event) {
  username = nameInput.val().trim();
  Cookies.set('name', username);
  if (username) {
    toggleRoomJoinForm(false);

    var socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);
  }
  event.preventDefault();
}

function toggleRoomJoinForm(showRoomJoinForm) {
  if (showRoomJoinForm) {
      userroomOuterContainer.classList.remove('hidden');
      eventsOuterContainer.classList.add('hidden');
  } else {
      userroomOuterContainer.classList.add('hidden');
      eventsOuterContainer.classList.remove('hidden');
  }
}

function enterRoom(newRoomId) {
  // Clear messages
  while (messageArea.firstChild) {
      messageArea.removeChild(messageArea.firstChild);
  }

  roomId = newRoomId;
  Cookies.set('roomId', roomId);
  roomIdDisplay.textContent = roomId;
  topic = `/app/messaging/${newRoomId}`;

  if (currentSubscription) {
    currentSubscription.unsubscribe();
  }
  currentSubscription = stompClient.subscribe(`/channel/${roomId}`, onMessageReceived);

  stompClient.send(`${topic}/addUser`,
    {},
    JSON.stringify({sender: username, type: 'JOIN'})
  );
}

function leaveRoom() {
    roomId = null;
    if (currentSubscription) {
        currentSubscription.unsubscribe();
    }

    var roomEvent = {
        sender: username,
        content: messageInput.value,
        type: 'LEAVE'
    };
    stompClient.send(`${topic}/leave`, {}, JSON.stringify(roomEvent));

    toggleRoomJoinForm(true);
}

function onConnected() {
  enterRoom(roomInput.val());
  connectingElement.classList.add('hidden');
}

function onError(error) {
  connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
  connectingElement.style.color = 'red';
}

function sendMessage(event) {
  var messageContent = messageInput.value.trim();
  if (messageContent && stompClient) {
    var roomEvent = {
      sender: username,
      content: messageInput.value,
      type: 'GAME_PLAY'
    };
    stompClient.send(`${topic}/sendMessage`, {}, JSON.stringify(roomEvent));
  }
  messageInput.value = '';
  event.preventDefault();
}

function onMessageReceived(payload) {
  var message = JSON.parse(payload.body);

  var messageElement = document.createElement('li');

  if (message.type == 'JOIN') {
    messageElement.classList.add('event-message');
    message.content = message.sender + ' joined!';
  } else if (message.type == 'LEAVE') {
    messageElement.classList.add('event-message');
    message.content = message.sender + ' left!';
  } else {
    messageElement.classList.add('gameplay-event');

    var avatarElement = document.createElement('i');
    var avatarText = document.createTextNode(message.sender[0]);
    avatarElement.appendChild(avatarText);
    avatarElement.style['background-color'] = getAvatarColor(message.sender);

    messageElement.appendChild(avatarElement);

    var usernameElement = document.createElement('span');
    var usernameText = document.createTextNode(message.sender);
    usernameElement.appendChild(usernameText);
    messageElement.appendChild(usernameElement);
  }

  var textElement = document.createElement('p');
  var messageText = document.createTextNode(message.content);
  textElement.appendChild(messageText);

  messageElement.appendChild(textElement);

  messageArea.appendChild(messageElement);
  messageArea.scrollTop = messageArea.scrollHeight;
}

function getAvatarColor(messageSender) {
  var hash = 0;
  for (var i = 0; i < messageSender.length; i++) {
      hash = 31 * hash + messageSender.charCodeAt(i);
  }
  var index = Math.abs(hash % colors.length);
  return colors[index];
}

$(document).ready(function() {
  var savedName = Cookies.get('name');
  if (savedName) {
    nameInput.val(savedName);
  }

  var savedRoom = Cookies.get('roomId');
  if (savedRoom) {
    roomInput.val(savedRoom);
  }

  userroomOuterContainer.classList.remove('hidden');
  usernameForm.addEventListener('submit', connect, true);
  messageForm.addEventListener('submit', sendMessage, true);

  leaveButton.addEventListener('click', leaveRoom);
});
