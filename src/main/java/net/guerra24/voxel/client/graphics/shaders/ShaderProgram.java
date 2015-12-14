/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Guerra24
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.guerra24.voxel.client.graphics.shaders;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import net.guerra24.voxel.client.core.VoxelVariables;
import net.guerra24.voxel.client.util.Logger;
import net.guerra24.voxel.universal.util.vector.Matrix4f;
import net.guerra24.voxel.universal.util.vector.Vector2f;
import net.guerra24.voxel.universal.util.vector.Vector3f;
import net.guerra24.voxel.universal.util.vector.Vector4f;

/**
 * Shader Program, Use to create shaders
 * 
 * @author Guerra24 <pablo230699@hotmail.com>
 * @category Rendering
 */
public abstract class ShaderProgram {
	/**
	 * Program ID
	 */
	private int programID;
	/**
	 * Vertex Shader ID
	 */
	private int vertexShaderID;
	/**
	 * Fragment Shader ID
	 */
	private int fragmentShaderID;
	/**
	 * Used to Load Matrix to shader
	 */
	private static FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

	/**
	 * Constructor, Creates a Shader Program Using a Vertex Shader and a
	 * Fragment Shader
	 * 
	 * @param vertexFile
	 *            Vertex Shader Path
	 * @param fragmentFile
	 *            Fragment Shader Path
	 */
	public ShaderProgram(String vertexFile, String fragmentFile) {
		vertexShaderID = loadShader(vertexFile, GL_VERTEX_SHADER);
		fragmentShaderID = loadShader(fragmentFile, GL_FRAGMENT_SHADER);
		programID = glCreateProgram();
		glAttachShader(programID, vertexShaderID);
		glAttachShader(programID, fragmentShaderID);
		bindAttributes();
		glLinkProgram(programID);
		glValidateProgram(programID);
		getAllUniformLocations();
	}

	/**
	 * Loads all Uniform Locations
	 */
	protected abstract void getAllUniformLocations();

	/**
	 * Gets The Uniform Location
	 * 
	 * @param uniformName
	 *            Uniform Namer
	 * @return ID of the Uniform Location
	 */
	protected int getUniformLocation(String uniformName) {
		return glGetUniformLocation(programID, uniformName);
	}

	/**
	 * Starts the Shader
	 * 
	 */
	public void start() {
		try {
			if (VoxelVariables.binded)
				throw new RuntimeException("One Shader is already binded");
		} catch (RuntimeException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		glUseProgram(programID);
		VoxelVariables.binded = true;
	}

	/**
	 * Stops the Shader
	 * 
	 */
	public void stop() {
		glUseProgram(0);
		VoxelVariables.binded = false;
	}

	/**
	 * Clear all the shader loaded data
	 * 
	 */
	public void cleanUp() {
		stop();
		glDetachShader(programID, vertexShaderID);
		glDetachShader(programID, fragmentShaderID);
		glDeleteShader(vertexShaderID);
		glDeleteShader(fragmentShaderID);
		glDeleteProgram(programID);
	}

	/**
	 * Binds the Shader Attributes
	 */
	protected abstract void bindAttributes();

	/**
	 * Binds the variable to an uniform id
	 * 
	 * @param attribute
	 *            Uniform ID
	 * @param variableName
	 *            Variable Name
	 */
	protected void bindAttribute(int attribute, String variableName) {
		glBindAttribLocation(programID, attribute, variableName);
	}

	/**
	 * Load a float
	 * 
	 * @param location
	 *            Uniform ID
	 * @param value
	 *            Value
	 */
	protected void loadFloat(int location, float value) {
		glUniform1f(location, value);
	}

	/**
	 * Load an int
	 * 
	 * @param location
	 *            Uniform ID
	 * @param value
	 *            Value
	 */
	protected void loadInt(int location, int value) {
		glUniform1i(location, value);
	}

	/**
	 * Load a Vector4f
	 * 
	 * @param location
	 *            Uniform ID
	 * @param vector
	 *            Vector4f
	 */
	protected void loadVector(int location, Vector4f vector) {
		glUniform4f(location, vector.x, vector.y, vector.z, vector.w);
	}

	/**
	 * Load a Vector3f
	 * 
	 * @param location
	 *            Uniform ID
	 * @param vector
	 *            Vector3f
	 */
	protected void loadVector(int location, Vector3f vector) {
		glUniform3f(location, vector.x, vector.y, vector.z);
	}

	/**
	 * Load a Vector2f
	 * 
	 * @param location
	 *            Uniform ID
	 * @param vector
	 *            Vector2f
	 */
	protected void load2DVector(int location, Vector2f vector) {
		glUniform2f(location, vector.x, vector.y);
	}

	/**
	 * Load a Boolean
	 * 
	 * @param location
	 *            Uniform ID
	 * @param value
	 *            Value
	 */
	protected void loadBoolean(int location, boolean value) {
		int toLoad = 0;
		if (value) {
			toLoad = 1;
		}
		glUniform1i(location, toLoad);
	}

	/**
	 * Load a Matrix4f
	 * 
	 * @param location
	 *            Uniform ID
	 * @param matrix
	 *            Matrix4f
	 */
	protected void loadMatrix(int location, Matrix4f matrix) {
		matrix.store(matrixBuffer);
		matrixBuffer.flip();
		glUniformMatrix4fv(location, false, matrixBuffer);
	}

	/**
	 * Load a Shader File
	 * 
	 * @param file
	 *            Shader Path
	 * @param type
	 *            Type of Shader
	 * @return Shader ID
	 * 
	 * @throws IllegalStateException
	 *             in case of compilation error
	 */
	private int loadShader(String file, int type) throws IllegalStateException {
		StringBuilder shaderSource = new StringBuilder();
		String sfile = "assets/shaders/" + file;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(sfile));
			Logger.log("Loading Shader: " + file);
			String line;
			while ((line = reader.readLine()) != null) {
				shaderSource.append(line).append("//\n");
			}
			reader.close();
		} catch (IOException e) {
			Logger.warn("Shader file not found: " + file);
			e.printStackTrace();
			System.exit(0);
		}
		int shaderID = glCreateShader(type);
		glShaderSource(shaderID, shaderSource);
		glCompileShader(shaderID);
		if (glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
			System.err.println(glGetShaderInfoLog(shaderID, 500));
			throw new IllegalStateException("Syntax Error in " + file);
		}
		return shaderID;
	}

}