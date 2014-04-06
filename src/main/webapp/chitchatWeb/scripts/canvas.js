var gl; // A global variable for the WebGL context
var canvas;
var mvMatrix;
var shaderProgram;
var vertexPositionAttribute;
var textureCoordAttribute;
var perspectiveMatrix;

var horizAspect = 480.0 / 640.0;

var players = Array();
var pc = 0;
var maxPlayers = 4;

// Init
function start() {
	canvas = document.getElementById("glcanvas");

	gl = initWebGL(canvas); // Initialize the GL context

	// Only continue if WebGL is available and working

	if (gl) {
		gl.clearColor(1.0, 1.0, 1.0, 1.0); // Set clear color to white
		gl.enable(gl.DEPTH_TEST); // Enable depth testing
		gl.depthFunc(gl.LEQUAL); // Near things obscure far things
		gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
		gl.viewport(0, 0, canvas.width, canvas.height);
		gl.blendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA);
		gl.enable(gl.BLEND);

		// Add Key Events
		document.onkeydown = handleKeyDown;
		document.onkeyup = handleKeyUp;

		initShaders();
		initBuffers();
		initAllTexture();

		setInterval(repaint, 15); // Repaint Interval
		setInterval(popBubble, 500); // Pop Bubble
	}
}

function initWebGL(canvas) {
	gl = null;

	try {
		// Try to grab the standard context. If it fails, fallback to
		// experimental.
		gl = canvas.getContext("webgl")
				|| canvas.getContext("experimental-webgl");
	} catch (e) {
	}

	// If we don't have a GL context, give up now
	if (!gl) {
		alert("Unable to initialize WebGL. Your browser may not support it.");
		gl = null;
	}

	return gl;
}

// Shader
function initShaders() {
	var fragmentShader = getShader(gl, "shader-fs");
	var vertexShader = getShader(gl, "shader-vs");

	// Create the shader program

	shaderProgram = gl.createProgram();
	gl.attachShader(shaderProgram, vertexShader);
	gl.attachShader(shaderProgram, fragmentShader);
	gl.linkProgram(shaderProgram);

	// If creating the shader program failed, alert

	if (!gl.getProgramParameter(shaderProgram, gl.LINK_STATUS)) {
		alert("Unable to initialize the shader program.");
	}

	gl.useProgram(shaderProgram);

	vertexPositionAttribute = gl.getAttribLocation(shaderProgram,
			"aVertexPosition");
	gl.enableVertexAttribArray(vertexPositionAttribute);
	textureCoordAttribute = gl
			.getAttribLocation(shaderProgram, "aTextureCoord");
	gl.enableVertexAttribArray(textureCoordAttribute);

}

function getShader(gl, id) {
	var shaderScript, theSource, currentChild, shader;

	shaderScript = document.getElementById(id);

	if (!shaderScript) {
		return null;
	}

	theSource = "";
	currentChild = shaderScript.firstChild;

	while (currentChild) {
		if (currentChild.nodeType == currentChild.TEXT_NODE) {
			theSource += currentChild.textContent;
		}

		currentChild = currentChild.nextSibling;
	}
	if (shaderScript.type == "x-shader/x-fragment") {
		shader = gl.createShader(gl.FRAGMENT_SHADER);
	} else if (shaderScript.type == "x-shader/x-vertex") {
		shader = gl.createShader(gl.VERTEX_SHADER);
	} else {
		// Unknown shader type
		return null;
	}
	gl.shaderSource(shader, theSource);

	// Compile the shader program
	gl.compileShader(shader);

	// See if it compiled successfully
	if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
		alert("An error occurred compiling the shaders: "
				+ gl.getShaderInfoLog(shader));
		return null;
	}

	return shader;
}

// Buffers

var bgVerticesBuffer;
var bgVerticesTextureCoordBuffer;
var bgVerticesIndexBuffer;

function initBgBuffer() {
	bgVerticesBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, bgVerticesBuffer);
	var maxSize = 3.4;
	var vertices = [ -maxSize, maxSize * horizAspect, 0, -maxSize,
			-maxSize * horizAspect, 0, maxSize, maxSize * horizAspect, 0,
			maxSize, -maxSize * horizAspect, 0 ];
	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices), gl.STATIC_DRAW);

	bgVerticesTextureCoordBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ARRAY_BUFFER, bgVerticesTextureCoordBuffer);
	var textureCoords = [ 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 1.0, 1.0 ];
	gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(textureCoords),
			gl.STATIC_DRAW);

	bgVerticesIndexBuffer = gl.createBuffer();
	gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, bgVerticesIndexBuffer);
	var vertexIndices = [ 0, 1, 2, 0, 2, 3 ];
	gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, new Uint16Array(vertexIndices),
			gl.STATIC_DRAW);
}

function initBuffers() {
	initBgBuffer();
	for ( var i = 0; i < maxPlayers; i++) {
		players[i] = new Player('Player ' + i);
		players[i].initPlayerBuffer();
	}
}

function repaint() {
	drawScene();
	handleCollision();
	handleKeyDownEvents();
}

// Texturing

function initTexture(imagePath) {
	var texture = gl.createTexture();
	var image = new Image();
	image.onload = function() {
		handleTextureLoaded(image, texture);
	};
	image.src = imagePath;
	return texture;
}

var textures = Array();
function initAllTexture() {
	textures[4] = initTexture("chitchatWeb/images/jrpepeJumpRight.png");
	textures[3] = initTexture("chitchatWeb/images/jrpepeJumpLeft.png");
	textures[2] = initTexture("chitchatWeb/images/jrpepeLeft.png");
	textures[1] = initTexture("chitchatWeb/images/jrpepeRight.png");
	textures[0] = initTexture("chitchatWeb/images/msbg.png");
}

function handleTextureLoaded(image, texture) {
	gl.bindTexture(gl.TEXTURE_2D, texture);
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE);
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE);
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST);
	gl.texParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST);
	gl.texImage2D(gl.TEXTURE_2D, 0, gl.RGBA, gl.RGBA, gl.UNSIGNED_BYTE, image);
}

// Drawing

function drawBackground() {
	// Initial Translation
	loadIdentity();
	mvTranslate([ -0.0, 0.0, -6.0 ]);

	// Save Matrix Location
	mvPushMatrix();

	// Binding matrix to buffer
	gl.bindBuffer(gl.ARRAY_BUFFER, bgVerticesBuffer);
	gl.vertexAttribPointer(vertexPositionAttribute, 3, gl.FLOAT, false, 0, 0);

	// Bind Texture to Buffer
	gl.bindBuffer(gl.ARRAY_BUFFER, bgVerticesTextureCoordBuffer);
	gl.vertexAttribPointer(textureCoordAttribute, 2, gl.FLOAT, false, 0, 0);

	// Bind Textures
	gl.activeTexture(gl.TEXTURE0);
	gl.bindTexture(gl.TEXTURE_2D, textures[0]);
	gl.uniform1i(gl.getUniformLocation(shaderProgram, "uSampler"), 0);

	gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, bgVerticesIndexBuffer);

	// Draw Object
	setMatrixUniforms();
	gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);

	// Restore the original matrix
	mvPopMatrix();
}

function drawScene() {
	gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);

	perspectiveMatrix = makePerspective(45, 640.0 / 480.0, 0.1, 100.0);

	drawBackground();
	for ( var i = 0; i < maxPlayers; i++) {
		if (players[i].online)
			players[i].drawPlayer();
	}
}

function handleCollision() {
	if (airDelay > maxAirDelay) {
		jumping = false;
		airDelay = 0;
	} else {
		airDelay++;
	}
}
