//Snowball Object

function Snowball() {
	this.verticesBuffer;
	this.verticesTextureCoordBuffer;
	this.verticesIndexBuffer;

	this.startX = 0.0;
	this.startY = 0.0;

	this.xPos = 0.0;
	this.yPos = 0.0;

	var vAcc = 0.0005;
	var baseVVel = 0.005;
	var baseHVel = 0.04;
	var updown = true;

	var vVel = baseVVel;

	this.inflight = false;
	this.leftright = false;

	this.initSnowballBuffer = function initSnowballBuffer() {
		this.verticesBuffer = gl.createBuffer();
		gl.bindBuffer(gl.ARRAY_BUFFER, this.verticesBuffer);
		var vertices = [ -0.1, 0.1, 0.0, -0.1, -0.1, 0.0, 0.1, 0.1, 0.0, 0.1,
				-0.1, 0.0 ];
		gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(vertices),
				gl.STATIC_DRAW);

		this.verticesTextureCoordBuffer = gl.createBuffer();
		gl.bindBuffer(gl.ARRAY_BUFFER, this.verticesTextureCoordBuffer);
		var textureCoords = [ 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 1.0, 1.0 ];
		gl.bufferData(gl.ARRAY_BUFFER, new Float32Array(textureCoords),
				gl.STATIC_DRAW);

		this.verticesIndexBuffer = gl.createBuffer();
		gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, this.verticesIndexBuffer);
		var plVertexIndices = [ 0, 1, 2, 0, 2, 3 ];
		gl.bufferData(gl.ELEMENT_ARRAY_BUFFER,
				new Uint16Array(plVertexIndices), gl.STATIC_DRAW);
	};

	this.updatePosition = function updatePosition(x, y, leftright) {
		this.startX = x;
		this.startY = y;
		this.xPos = this.startX;
		this.yPos = this.startY;
		this.leftright = leftright;
	};

	this.animateSnowball = function animateSnowball() {
		this.drawSnowball();
		if (this.leftright)
			this.xPos -= baseHVel;
		else
			this.xPos += baseHVel;
		if (vVel <= 0.0)
			updown = false;
		if (updown) {
			this.yPos += vVel;
			vVel -= vAcc;
		} else {
			this.yPos -= vVel;
			vVel += vAcc;
		}

		if (this.yPos < -1.82 || this.xPos < -3 || this.yPos > 3) {
			this.xPos = this.startX;
			this.yPos = this.startY;
			this.inflight = false;
			vVel = baseVVel;
			projHeight = 0.0;
			updown = true;
		}
	};

	this.drawSnowball = function drawSnowball() {
		// Initial Translation
		loadIdentity();
		mvTranslate([ -0.0, 0.0, -5.9 ]);

		// Save Matrix Location
		mvPushMatrix();

		// Translate
		mvTranslate([ this.xPos, this.yPos, 0.0 ]);

		// Binding matrix to buffer
		gl.bindBuffer(gl.ARRAY_BUFFER, this.verticesBuffer);
		gl.vertexAttribPointer(vertexPositionAttribute, 3, gl.FLOAT, false, 0,
				0);

		// Bind Texture to Buffer
		gl.bindBuffer(gl.ARRAY_BUFFER, this.verticesTextureCoordBuffer);
		gl.vertexAttribPointer(textureCoordAttribute, 2, gl.FLOAT, false, 0, 0);

		// Bind Textures
		gl.activeTexture(gl.TEXTURE0);
		gl.bindTexture(gl.TEXTURE_2D, textures[4]);
		gl.uniform1i(gl.getUniformLocation(shaderProgram, "uSampler"), 0);

		gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, this.verticesIndexBuffer);

		// Draw Object
		setMatrixUniforms();
		gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);

		// Restore the original matrix
		mvPopMatrix();
	};
}