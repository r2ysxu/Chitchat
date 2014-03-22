var typing = false;

function talkBubble(message, name) {
	var comp = document.getElementById('overlayText');

	comp.appendChild(createSpeechBubble(message, name));
}

function populateChatHistory(message, usr) {
	var chatLog = document.getElementById('chatHist');

	var speechTr = document.createElement('div');

	var nameDiv = document.createElement('div');
	nameDiv.innerText = usr + ': ';
	nameDiv.className = 'chatBubbleUsr';

	var textDiv = document.createElement('div');
	textDiv.innerText = message;
	textDiv.className = 'chatBubbleText';

	speechTr.appendChild(nameDiv);
	speechTr.appendChild(textDiv);

	chatLog.appendChild(speechTr);
}

function createSpeechBubble(message, name) {
	var speechBubble = document.createElement('div');

	var nameDiv = document.createElement('div');
	nameDiv.innerText = name + ':';
	nameDiv.className = 'chatBubbleUsr';

	var textDiv = document.createElement('div');
	textDiv.innerText = message;
	textDiv.className = 'chatBubbleText';

	speechBubble.appendChild(nameDiv);
	speechBubble.appendChild(textDiv);

	speechBubble.className = 'chatBubble';
	return speechBubble;
}

var fadePercent = 80;
function popBubble() {
	var comp = document.getElementById('overlayText');
	var firstText = comp.firstChild;
	if (firstText) {
		firstText.style.opacity = '.' + fadePercent;
		fadePercent -= 10;

		if (fadePercent == 0) {
			fadePercent = 80;
			comp.removeChild(comp.firstChild);
		}
	}
}

function forcePop(num) {
	var comp = document.getElementById('overlayText');
	while (comp.childElementCount >= num) {
		comp.removeChild(comp.firstChild);
	}
}

function get(name) {
	if (name = (new RegExp('[?&]' + encodeURIComponent(name) + '=([^&]*)'))
			.exec(location.search))
		return decodeURIComponent(name[1]);
}