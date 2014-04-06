//Player Object

function Player(name) {
	this.name = name;
	this.online = false;
	this.verticesBuffer;
	this.verticesTextureCoordBuffer;
	this.verticesIndexBuffer;
	this.xPos = 0.0;
	this.yPos = 0.0;

	this.initPlayerBuffer = initPlayerBuffer;
	function initPlayerBuffer() {
		this.verticesBuffer = gl.createBuffer();
		gl.bindBuffer(gl.ARRAY_BUFFER, this.verticesBuffer);
		var vertices = [ -0.5, 0.5, 0.0, -0.5, -0.5, 0.0, 0.5, 0.5, 0.0, 0.5,
				-0.5, 0.0 ];
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
	}

	this.drawPlayer = drawPlayer;
	function drawPlayer() {
		// Initial Translation
		loadIdentity();
		mvTranslate([ -0.0, 0.0, -6.0 ]);

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
		gl.bindTexture(gl.TEXTURE_2D, this.correctTexture());
		gl.uniform1i(gl.getUniformLocation(shaderProgram, "uSampler"), 0);

		gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, this.verticesIndexBuffer);

		// Draw Object
		setMatrixUniforms();
		gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4);

		// Restore the original matrix
		mvPopMatrix();
	}

	this.landed = false;
	this.walking = false;
	this.leftright = true;

	this.correctTexture = correctTexture;
	function correctTexture() {
		if (this.jumping && this.leftright)
			return textures[3];
		else if (this.jumping && !this.leftright)
			return textures[4];
		else if (this.leftright)
			return textures[2];
		else
			return textures[1];
	}
}