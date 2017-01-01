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

package net.luxvacuos.voxel.client.world.chunks;

import net.luxvacuos.voxel.client.core.ClientWorldSimulation;
import net.luxvacuos.voxel.client.rendering.api.opengl.Tessellator;
import net.luxvacuos.voxel.client.rendering.world.block.ICustomRenderBlock;
import net.luxvacuos.voxel.client.rendering.world.block.IRenderBlock;
import net.luxvacuos.voxel.client.world.block.BlocksResources;
import net.luxvacuos.voxel.client.world.block.RenderBlock;
import net.luxvacuos.voxel.client.world.entities.Camera;
import net.luxvacuos.voxel.universal.world.chunk.Chunk;
import net.luxvacuos.voxel.universal.world.chunk.IChunk;
import net.luxvacuos.voxel.universal.world.dimension.IDimension;
import net.luxvacuos.voxel.universal.world.utils.ChunkNode;

public class RenderChunk {

	private Tessellator tess;
	private IDimension dim;
	private IChunk chunk;

	public RenderChunk(IDimension dim) {
		this.dim = dim;
		tess = new Tessellator(BlocksResources.getMaterial());
	}

	public void render(Camera camera, Camera sunCamera, ClientWorldSimulation clientWorldSimulation, int shadowMap) {
		if (chunk.needsMeshRebuild()) {
			tess.begin();
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					for (int y = 0; y < 256; y++) {
						RenderBlock block = (RenderBlock) chunk.getChunkData().getBlockAt(x, y, z);
						if (!block.isTransparent())
							if (!block.hasCustomModel()) {
								tess.generateCube(chunk.getNode().getX() * 16 + x, y, chunk.getNode().getZ() * 16 + z,
										1, cullFaceUp(block, x, y, z), cullFaceDown(block, x, y, z),
										cullFaceEast(block, x, y, z), cullFaceWest(block, x, y, z),
										cullFaceNorth(block, x, y, z), cullFaceSouth(block, x, y, z), block);
							} else {
								((ICustomRenderBlock) block).generateCustomModel(tess, chunk.getNode().getX() * 16 + x,
										y, chunk.getNode().getZ() * 16 + z, 1, cullFaceUp(block, x, y, z),
										cullFaceDown(block, x, y, z), cullFaceEast(block, x, y, z),
										cullFaceWest(block, x, y, z), cullFaceNorth(block, x, y, z),
										cullFaceSouth(block, x, y, z));
							}
					}
				}

			}
			tess.end();
			((Chunk) chunk).completedMeshRebuild();
		}
		tess.draw(camera, sunCamera, clientWorldSimulation, shadowMap);
	}

	public void renderShadow(Camera sunCamera) {
		tess.drawShadow(sunCamera);
	}

	public void renderOcclusion(Camera camera) {
		tess.drawOcclusion(camera);
	}

	private boolean cullFaceWest(RenderBlock block, int x, int y, int z) {
		if (x > 1 && x < 16) {
			RenderBlock b = (RenderBlock) chunk.getChunkData().getBlockAt(x - 1, y, z);
			if (b.getID() == block.getID())
				return false;
			if (b.isTransparent() || b.hasCustomModel() || b.isFluid())
				return true;
		}
		if (dim.getBlockAt(chunk.getNode().getX() * 16 + x - 1, y, chunk.getNode().getZ() * 16 + z).getID() == block
				.getID())
			return false;
		if (((IRenderBlock) dim.getBlockAt(chunk.getNode().getX() * 16 + x - 1, y, chunk.getNode().getZ() * 16 + z))
				.isTransparent())
			return true;
		return false;
	}

	private boolean cullFaceEast(RenderBlock block, int x, int y, int z) {
		if (x > 0 && x < 15) {
			RenderBlock b = (RenderBlock) chunk.getChunkData().getBlockAt(x + 1, y, z);
			if (b.getID() == block.getID())
				return false;
			if (b.isTransparent() || b.hasCustomModel() || b.isFluid())
				return true;
		}
		if (dim.getBlockAt(chunk.getNode().getX() * 16 + x + 1, y, chunk.getNode().getZ() * 16 + z).getID() == block
				.getID())
			return false;
		if (((IRenderBlock) dim.getBlockAt(chunk.getNode().getX() * 16 + x + 1, y, chunk.getNode().getZ() * 16 + z))
				.isTransparent())
			return true;
		return false;
	}

	private boolean cullFaceDown(RenderBlock block, int x, int y, int z) {
		if (y > 1 && y < 256) {
			RenderBlock b = (RenderBlock) chunk.getChunkData().getBlockAt(x, y - 1, z);
			if (b.getID() == block.getID())
				return false;
			if (b.isTransparent() || b.hasCustomModel() || b.isFluid())
				return true;
		}
		// if (dim.getBlockAt(x, y - 1, z).getID() == block.getID())
		// return false;
		// if (((IRenderBlock) dim.getBlockAt(x, y - 1, z)).isTransparent())
		// return true;
		return false;
	}

	private boolean cullFaceUp(RenderBlock block, int x, int y, int z) {
		if (y > 0 && y < 255) {
			RenderBlock b = (RenderBlock) chunk.getChunkData().getBlockAt(x, y + 1, z);
			if (b.getID() == block.getID())
				return false;
			if (b.isTransparent() || b.hasCustomModel() || b.isFluid())
				return true;
		}
		// if (dim.getBlockAt(x, y + 1, z).getID() == block.getID())
		// return false;
		// if (((IRenderBlock) dim.getBlockAt(x, y + 1, z)).isTransparent())
		// return true;
		return false;
	}

	private boolean cullFaceNorth(RenderBlock block, int x, int y, int z) {
		if (z > 1 && z < 16) {
			RenderBlock b = (RenderBlock) chunk.getChunkData().getBlockAt(x, y, z - 1);
			if (b.getID() == block.getID())
				return false;
			if (b.isTransparent() || b.hasCustomModel() || b.isFluid())
				return true;
		}
		if (dim.getBlockAt(chunk.getNode().getX() * 16 + x, y, chunk.getNode().getZ() * 16 + z - 1).getID() == block
				.getID())
			return false;
		if (((IRenderBlock) dim.getBlockAt(chunk.getNode().getX() * 16 + x, y, chunk.getNode().getZ() * 16 + z - 1))
				.isTransparent())
			return true;
		return false;
	}

	private boolean cullFaceSouth(RenderBlock block, int x, int y, int z) {
		if (z > 0 && z < 15) {
			RenderBlock b = (RenderBlock) chunk.getChunkData().getBlockAt(x, y, z + 1);
			if (b.getID() == block.getID())
				return false;
			if (b.isTransparent() || b.hasCustomModel() || b.isFluid())
				return true;
		}
		if (dim.getBlockAt(chunk.getNode().getX() * 16 + x, y, chunk.getNode().getZ() * 16 + z + 1).getID() == block
				.getID())
			return false;
		if (((IRenderBlock) dim.getBlockAt(chunk.getNode().getX() * 16 + x, y, chunk.getNode().getZ() * 16 + z + 1))
				.isTransparent())
			return true;
		return false;
	}

	public ChunkNode getNode() {
		if (chunk == null)
			return null;
		return chunk.getNode();
	}

	public void setChunk(IChunk chunk) {
		this.chunk = chunk;
	}

	public void dispose() {
		tess.cleanUp();
	}

}
