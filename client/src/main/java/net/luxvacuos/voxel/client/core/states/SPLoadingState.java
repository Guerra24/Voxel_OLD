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

package net.luxvacuos.voxel.client.core.states;

import org.lwjgl.nanovg.NanoVG;

import net.luxvacuos.voxel.client.rendering.api.opengl.MasterRenderer;
import net.luxvacuos.voxel.client.resources.GameResources;
import net.luxvacuos.voxel.client.ui.Text;
import net.luxvacuos.voxel.client.ui.Window;
import net.luxvacuos.voxel.client.world.entities.PlayerCamera;
import net.luxvacuos.voxel.universal.core.AbstractVoxel;
import net.luxvacuos.voxel.universal.core.states.AbstractState;
import net.luxvacuos.voxel.universal.core.states.StateMachine;

/**
 * Singleplayer World Loading State, this is runned when loading a world.
 * 
 * @author danirod
 */
public class SPLoadingState extends AbstractState {

	private Window window;

	public SPLoadingState() {
		super(StateNames.SP_LOADING);
		window = new Window(20, GameResources.getInstance().getDisplay().getDisplayHeight() - 20,
				GameResources.getInstance().getDisplay().getDisplayWidth() - 40,
				GameResources.getInstance().getDisplay().getDisplayHeight() - 40, "Loading World");

		Text text = new Text("Loading World", window.getWidth() / 2, window.getHeight());
		text.setAlign(NanoVG.NVG_ALIGN_CENTER);
		window.addChildren(text);
	}

	@Override
	public void start() {
		new Thread(() -> {
			GameResources.getInstance().getWorldsHandler().getActiveWorld().init();
			GameResources.getInstance().getWorldsHandler().getActiveWorld().getActiveDimension().getPhysicsEngine()
					.addEntity(GameResources.getInstance().getCamera());
			((PlayerCamera) GameResources.getInstance().getCamera()).setMouse();
			//GameResources.getInstance().getGlobalStates().setState(GameState.SP);
			StateMachine.setCurrentState(StateNames.SINGLEPLAYER);
		}).start();
	}

	@Override
	public void update(AbstractVoxel voxel, float delta) {
		window.update(delta);
		((PlayerCamera) GameResources.getInstance().getCamera()).setMouse();
	}

	@Override
	public void render(AbstractVoxel voxel, float delta) {
		GameResources gm = (GameResources) voxel.getGameResources();
		MasterRenderer.prepare();
		gm.getDisplay().beingNVGFrame();
		window.render();
		gm.getDisplay().endNVGFrame();
	}

}
