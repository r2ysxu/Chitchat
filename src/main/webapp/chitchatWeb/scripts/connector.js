var URL = "ws://localhost:8080/Chitchat/echo";
var websocket;
var playerIndex = -1;

function connect() {
	websocket = new WebSocket(URL);
	websocket.onopen = function(event) {
		onOpen(event);
	};
	websocket.onmessage = function(event) {
		onMessage(event);
	};
	websocket.onerror = function(event) {
		onError(event);
	};

	window.onbeforeunload = function() {
		websocket.onclose = function() {
		}; // disable onclose handler first
		websocket.close();
	};
}

function onOpen(event) {
	var request = '{ "type" : "join", "name" : "' + get('usr') + '" }';
	websocket.send(request);
}

function onMessage(event) {

	if (typeof event.data == "string") {

		var response = JSON.parse(event.data);

		if (response.type == 'conn') {
			connResponse(response);
		} else if (response.type == 'join') {
			joinResponse(response);
		} else if (response.type == 'chat') {
			chatResponse(response);
		} else if (response.type == 'move') {
			moveResponse(response);
		} else if (response.type == 'stop') {
			stopResponse(response);
		} else if (response.type == 'quit') {
			pc = response.pc;
			document.getElementById('currentChatters').innerText = 'Currently: '
					+ pc + ' in chat';
		}

	}
}

function onError(event) {
}

function sendMsg() {
	event = window.event;
	if (event.keyCode == 13) {
		var val = document.getElementById("chatTextField").value;
		if (val != '') {
			var request = '{ "type" : "chat", "message" : "' + val + '" }';
			websocket.send(request);
			document.getElementById("chatTextField").value = '';
			document.getElementById('glcanvas').focus();
		}
	}
}

function connResponse(response) {
	playerIndex = response.index;
	players[playerIndex].online = true;
}

function joinResponse(response) {
	players[response.index].online = true;
	players[response.index].name = response.name;
	pc = response.pc;
	document.getElementById('currentChatters').innerText = 'Currently: ' + pc
			+ ' in chat';
}

function chatResponse(response) {
	talkBubble(response.message, response.name);
	forcePop(7);
	populateChatHistory(response.message, response.name);
}

function stopResponse(response) {
	players[response.index].stopMoving();
}

function moveResponse(response) {
	if (response.pos == 'left') {
		players[response.index].moveLeft();
	} else if (response.pos == 'right') {
		players[response.index].moveRight();
	} else if (response.pos == 'jump') {
		players[response.index].jumpUp();
	}
}

// Event Handler
var keyUp = true;
var currentlyPressedKeys = {};
function handleKeyDown(event) {
	keyUp = false;
	currentlyPressedKeys[event.keyCode] = true;
}

function handleKeyUp(event) {
	keyUp = true;
	if (currentlyPressedKeys[13]) { // Enter key
		document.getElementById('chatTextField').focus();
	}
	if (currentlyPressedKeys[33]) { // Page Up
	}
	if (currentlyPressedKeys[34]) { // Page Down
	}
	if (currentlyPressedKeys[37]) { // Left cursor key
		handleWalkKeys('left');
	}
	if (currentlyPressedKeys[39]) { // Right cursor key
		handleWalkKeys('right');
	}
	if (currentlyPressedKeys[38]) { // Up cursor key
		var request = '{ "type" : "stop", "pos" : "jump" }';
		websocket.send(request);
	}
	if (currentlyPressedKeys[40]) { // Down cursor key
	}
	currentlyPressedKeys[event.keyCode] = false;
}

var jumping = false;
var airDelay = 0;
var maxAirDelay = 15;

var startTime;
function handleWalkKeys(pos) {
	if (players[playerIndex].walking) {
		var endTime = (new Date).getTime();
		var delta = endTime - startTime;
		var request = '{ "type" : "stop", "pos" : "' + pos + '", "eta" : "'
				+ delta + '" }';
		websocket.send(request);
		startTime = endTime;
	}
}

function handleKeyDownEvents() {
	if (currentlyPressedKeys[33]) { // Page Up
	}
	if (currentlyPressedKeys[34]) { // Page Down
	}
	if (currentlyPressedKeys[37]) { // Left cursor key
		if (!players[playerIndex].walking) {
			var request = '{ "type" : "move", "pos" : "left" }';
			websocket.send(request);
			players[playerIndex].walking = true;
			startTime = (new Date).getTime();
		}
	}
	if (currentlyPressedKeys[39]) { // Right cursor key
		if (!players[playerIndex].walking) {
			var request = '{ "type" : "move", "pos" : "right" }';
			websocket.send(request);
			players[playerIndex].walking = true;
			startTime = (new Date).getTime();
		}
	}
	if (currentlyPressedKeys[38]) { // Up cursor key
		if (!jumping) {
			var request = '{ "type" : "move", "pos" : "jump" }';
			websocket.send(request);
			jumping = true;
		}
	}
	if (currentlyPressedKeys[40]) { // Down cursor key
	}
}