/*
 * This file is part of Voxel
 * 
 * Copyright (C) 2016 Lux Vacuos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.luxvacuos.voxel.client.rendering.api.opengl.shaders;

import net.luxvacuos.igl.vector.Matrix4f;
import net.luxvacuos.voxel.client.core.ClientVariables;
import net.luxvacuos.voxel.client.rendering.api.opengl.shaders.data.Attribute;
import net.luxvacuos.voxel.client.rendering.api.opengl.shaders.data.UniformBoolean;
import net.luxvacuos.voxel.client.rendering.api.opengl.shaders.data.UniformFloat;
import net.luxvacuos.voxel.client.rendering.api.opengl.shaders.data.UniformMatrix;
import net.luxvacuos.voxel.client.rendering.api.opengl.shaders.data.UniformSampler;
import net.luxvacuos.voxel.client.resources.GameResources;
import net.luxvacuos.voxel.client.util.Maths;
import net.luxvacuos.voxel.client.world.entities.Camera;

/**
 * Entity Shader
 * 
 * @author Guerra24 <pablo230699@hotmail.com>
 * @category Rendering
 */
public class EntityShader extends ShaderProgram {

	private UniformMatrix transformationMatrix = new UniformMatrix("transformationMatrix");
	private UniformMatrix projectionMatrix = new UniformMatrix("projectionMatrix");
	private UniformMatrix viewMatrix = new UniformMatrix("viewMatrix");
	private UniformMatrix biasMatrix = new UniformMatrix("biasMatrix");
	private UniformMatrix projectionLightMatrix = new UniformMatrix("projectionLightMatrix");
	private UniformMatrix viewLightMatrix = new UniformMatrix("viewLightMatrix");
	private UniformFloat entityLight = new UniformFloat("entityLight");
	private UniformSampler texture0 = new UniformSampler("texture0");
	private UniformSampler depth = new UniformSampler("depth");
	private UniformBoolean useShadows = new UniformBoolean("useShadows");

	public EntityShader() {
		super(ClientVariables.VERTEX_FILE_ENTITY, ClientVariables.FRAGMENT_FILE_ENTITY, new Attribute(0, "position"),
				new Attribute(1, "textureCoords"), new Attribute(2, "normals"));
		super.storeAllUniformLocations(transformationMatrix, projectionMatrix, viewMatrix, biasMatrix,
				projectionLightMatrix, viewLightMatrix, entityLight, texture0, depth, useShadows);
		connectTextureUnits();
	}

	/**
	 * Loads Textures ID
	 * 
	 */
	private void connectTextureUnits() {
		super.start();
		texture0.loadTexUnit(0);
		depth.loadTexUnit(1);
		super.stop();
	}

	public void loadEntityLight(float light) {
		entityLight.loadFloat(light);
	}

	public void useShadows(boolean value) {
		useShadows.loadBoolean(value);
	}

	/**
	 * Loads Transformation Matrix to the shader
	 * 
	 * @param matrix
	 *            Transformation Matrix
	 */
	public void loadTransformationMatrix(Matrix4f matrix) {
		transformationMatrix.loadMatrix(matrix);
	}

	public void loadBiasMatrix(GameResources gm) {
		Matrix4f biasMatrix = new Matrix4f();
		biasMatrix.m00 = 0.5f;
		biasMatrix.m11 = 0.5f;
		biasMatrix.m22 = 0.5f;
		biasMatrix.m30 = 0.5f;
		biasMatrix.m31 = 0.5f;
		biasMatrix.m32 = 0.5f;
		this.biasMatrix.loadMatrix(biasMatrix);
		projectionLightMatrix.loadMatrix(gm.getMasterShadowRenderer().getProjectionMatrix());
	}

	public void loadLightMatrix(GameResources gm) {
		viewLightMatrix.loadMatrix(Maths.createViewMatrix(gm.getSun_Camera()));
	}

	/**
	 * Loads View Matrix to the shader
	 * 
	 * @param camera
	 *            Camera
	 */
	public void loadviewMatrix(Camera camera) {
		viewMatrix.loadMatrix(Maths.createViewMatrix(camera));
	}

	/**
	 * Loads Projection Matrix to the shader
	 * 
	 * @param projection
	 *            Projection Matrix
	 */
	public void loadProjectionMatrix(Matrix4f projection) {
		projectionMatrix.loadMatrix(projection);
	}
}