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

package io.github.guerra24.voxel.client.kernel.core;

import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.opengl.GL11.GL_RENDERER;
import static org.lwjgl.opengl.GL11.GL_VENDOR;
import static org.lwjgl.opengl.GL11.glGetString;

import io.github.guerra24.voxel.client.kernel.api.VAPI;
import io.github.guerra24.voxel.client.kernel.bootstrap.Bootstrap;
import io.github.guerra24.voxel.client.kernel.graphics.opengl.Display;
import io.github.guerra24.voxel.client.kernel.input.Keyboard;
import io.github.guerra24.voxel.client.kernel.resources.GameResources;
import io.github.guerra24.voxel.client.kernel.resources.GuiResources;
import io.github.guerra24.voxel.client.kernel.util.Logger;
import io.github.guerra24.voxel.client.kernel.world.WorldHandler;
import io.github.guerra24.voxel.client.kernel.world.block.BlocksResources;

/**
 * The Kernel, Game Engine Core
 * 
 * @author Guerra24 <pablo230699@hotmail.com>
 * @category Kernel
 */
public class Kernel implements IKernel {

	public static float renderCalls = 0;
	public static float renderCallsPerFrame = 0;
	public static float totalRenderCalls = 0;
	public static int errorTime = 0;

	/**
	 * Game Threads
	 */
	public static WorldThread worldThread;
	public static WorldThread1 worldThread2;

	/**
	 * Game Data
	 */
	private GameResources gameResources;
	private GuiResources guiResources;
	private WorldHandler worlds;
	private Display display;
	private VAPI api;

	/**
	 * Constructor of the Kernel, Initializes the Game and starts the loop
	 * 
	 * @param errorTest
	 *            If running JUnit Test
	 */
	public Kernel() {
		mainLoop();
	}

	@Override
	public void init() {

		display = new Display();
		display.initDsiplay(1280, 720);
		display.startUp();
		Logger.log(Thread.currentThread(), "Loading");
		Logger.log(Thread.currentThread(), "Voxel Game Version: " + KernelConstants.version);
		Logger.log(Thread.currentThread(), "Build: " + KernelConstants.build);
		Logger.log(Thread.currentThread(), "Running on: " + Bootstrap.getPlatform());
		Logger.log(Thread.currentThread(), "Vendor: " + glGetString(GL_VENDOR));
		Logger.log(Thread.currentThread(), "Renderer: " + glGetString(GL_RENDERER));

		gameResources = new GameResources();
		api = new VAPI();
		api.preInit();
		gameResources.init();
		guiResources = new GuiResources(gameResources);
		BlocksResources.createBlocks(gameResources.getLoader());
		gameResources.addRes();
		gameResources.music();
		worlds = new WorldHandler();
		Logger.log(Thread.currentThread(), "Initializing Threads");
		worldThread = new WorldThread();
		worldThread.setName("Voxel World 1");
		worldThread.setApi(api);
		worldThread.setWorldHandler(worlds);
		worldThread.setGm(gameResources);
		worldThread.start();
		worldThread2 = new WorldThread1();
		worldThread2.setName("Voxel World 2");
		worldThread2.setApi(api);
		worldThread2.setWorldHandler(worlds);
		worldThread2.setGameResources(gameResources);
		worldThread2.setKernel(this);
		worldThread2.setGuiResources(guiResources);
		worldThread2.start();
		api.init();
		api.postInit();
		glfwShowWindow(Display.getWindow());
		gameResources.getSoundSystem().play("menu1");
	}

	@Override
	public void mainLoop() {
		init();
		float delta = 0;
		while (gameResources.getGameStates().loop) {
			if (Display.timeCountRender > 1f) {
				Logger.log(Thread.currentThread(), "RCPS: " + Kernel.renderCallsPerFrame);
				Logger.log(Thread.currentThread(), "FPS: " + Display.fps);
				Logger.log(Thread.currentThread(), "UPS: " + Display.ups);
				Display.fps = Display.fpsCount;
				Display.fpsCount = 0;
				Display.ups = Display.upsCount;
				Display.upsCount = 0;
				Display.timeCountRender -= 1f;
			}
			delta = Display.getDeltaRender();
			render(gameResources, delta);
			totalRenderCalls += renderCalls;
			renderCallsPerFrame = renderCalls;
			renderCalls = 0;
		}
		dispose();
	}

	@Override
	public void render(GameResources gm, float delta) {
		Display.fpsCount++;
		switch (gm.getGameStates().state) {
		case MAINMENU:
			gm.getFrustum().calculateFrustum(gm);
			gm.getRenderer().prepare();
			gm.getRenderer().renderEntity(gm.mainMenuModels, gm.mainMenuLights, gm);
			gm.getGuiRenderer().renderGui(gm.guis2);
			display.updateDisplay(30, gm);
			break;
		case IN_PAUSE:
			gm.getRenderer().prepare();
			worlds.getWorld(worlds.getActiveWorld()).updateChunksRender(gm);
			gm.getRenderer().renderEntity(gm.getPhysics().getMobManager().getMobs(), gm.lights, gm);
			gm.getSkyboxRenderer().render(KernelConstants.RED, KernelConstants.GREEN, KernelConstants.BLUE, delta, gm);
			gm.getParticleController().render(gm);
			gm.getGuiRenderer().renderGui(gm.guis4);
			display.updateDisplay(KernelConstants.FPS, gm);
			break;
		case GAME:// THIS NEEDS OPTIMIZATION...
			gm.getCamera().update(delta, gameResources, guiResources, worlds.getWorld(worlds.getActiveWorld()), api);
			gm.getPhysics().getMobManager().getPlayer().update(delta, gm, guiResources,
					worlds.getWorld(worlds.getActiveWorld()), api);
			gm.getCamera().updatePicker(worlds.getWorld(worlds.getActiveWorld()));
			gm.getFrustum().calculateFrustum(gm);

			gm.getWaterFBO().begin(512, 512);
			gm.getCamera().invertPitch();
			gm.getRenderer().prepare();
			gm.getSkyboxRenderer().render(KernelConstants.RED, KernelConstants.GREEN, KernelConstants.BLUE, delta, gm);
			gm.getWaterFBO().end();
			gm.getCamera().invertPitch();

			gm.getPostProcessing().getPost_fbo().begin(Display.getWidth(), Display.getHeight());
			gm.getRenderer().prepare();
			worlds.getWorld(worlds.getActiveWorld()).updateChunksRender(gm);
			gm.getSkyboxRenderer().render(KernelConstants.RED, KernelConstants.GREEN, KernelConstants.BLUE, delta, gm);
			gm.getRenderer().renderEntity(gm.getPhysics().getMobManager().getMobs(), gm.lights, gm);
			gm.getParticleController().render(gm);
			gm.getPostProcessing().getPost_fbo().end();

			gm.getRenderer().prepare();
			gm.getPostProcessing().render();
			gm.getGuiRenderer().renderGui(gm.guis);
			display.updateDisplay(KernelConstants.FPS, gm);
			break;
		case LOADING_WORLD:
			gm.getRenderer().prepare();
			gm.getGuiRenderer().renderGui(gm.guis3);
			display.updateDisplay(30, gm);
			break;
		}
	}

	public void update(GameResources gm, GuiResources gi, WorldHandler world, float delta) {
		Display.upsCount++;
		switch (gm.getGameStates().state) {
		case MAINMENU:
			if (Keyboard.isKeyDown(Keyboard.KEY_O))
				Bootstrap.config.setVisible(true);
			gm.mainMenuModels.get(0).getEntity().increaseRotation(0, 0.1f, 0);
			break;
		case IN_PAUSE:
			if (Keyboard.isKeyDown(Keyboard.KEY_O))
				Bootstrap.config.setVisible(true);
			break;
		case GAME:
			gm.getPhysics().getMobManager().update(delta, gm, gi, worlds.getWorld(worlds.getActiveWorld()), api);
			gm.getParticleController().update(delta, gm, gi, worlds.getWorld(worlds.getActiveWorld()));
			gm.getWaterRenderer().update(delta);
			gm.getSkyboxRenderer().update(delta);
			gm.getParticleController().update(delta, gm, gi, worlds.getWorld(worlds.getActiveWorld()));
			break;
		case LOADING_WORLD:
			break;
		}
		gm.getGameStates().switchStates(gm, world, api, display);
	}

	@Override
	public void dispose() {
		Logger.log(Thread.currentThread(), "Closing Game");
		gameResources.cleanUp();
		api.dispose();
		Bootstrap.config.dispose();
		display.closeDisplay();
	}

	public GameResources getGameResources() {
		return gameResources;
	}

	public GuiResources getGuiResources() {
		return guiResources;
	}

	public WorldHandler getWorldHadler() {
		return worlds;
	}

	public VAPI getApi() {
		return api;
	}

}
