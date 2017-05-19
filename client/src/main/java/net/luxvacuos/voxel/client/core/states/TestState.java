/*
 * This file is part of Voxel
 * 
 * Copyright (C) 2016-2017 Lux Vacuos
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

import static net.luxvacuos.voxel.universal.core.GlobalVariables.REGISTRY;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;

import org.lwjgl.glfw.GLFW;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import net.luxvacuos.igl.vector.Matrix4d;
import net.luxvacuos.igl.vector.Vector3d;
import net.luxvacuos.igl.vector.Vector3f;
import net.luxvacuos.voxel.client.core.ClientVariables;
import net.luxvacuos.voxel.client.core.ClientWorldSimulation;
import net.luxvacuos.voxel.client.core.subsystems.GraphicalSubsystem;
import net.luxvacuos.voxel.client.ecs.EntityResources;
import net.luxvacuos.voxel.client.ecs.entities.CameraEntity;
import net.luxvacuos.voxel.client.ecs.entities.PlayerCamera;
import net.luxvacuos.voxel.client.ecs.entities.RenderEntity;
import net.luxvacuos.voxel.client.ecs.entities.Sun;
import net.luxvacuos.voxel.client.input.KeyboardHandler;
import net.luxvacuos.voxel.client.input.Mouse;
import net.luxvacuos.voxel.client.rendering.api.glfw.Window;
import net.luxvacuos.voxel.client.rendering.api.opengl.ParticleDomain;
import net.luxvacuos.voxel.client.rendering.api.opengl.Renderer;
import net.luxvacuos.voxel.client.rendering.api.opengl.Tessellator;
import net.luxvacuos.voxel.client.rendering.api.opengl.objects.Light;
import net.luxvacuos.voxel.client.rendering.api.opengl.objects.Model;
import net.luxvacuos.voxel.client.rendering.api.opengl.objects.ParticleTexture;
import net.luxvacuos.voxel.client.rendering.utils.BlockFaceAtlas;
import net.luxvacuos.voxel.client.resources.AssimpResourceLoader;
import net.luxvacuos.voxel.client.resources.ResourceLoader;
import net.luxvacuos.voxel.client.ui.menus.GameWindow;
import net.luxvacuos.voxel.client.ui.menus.PauseWindow;
import net.luxvacuos.voxel.client.util.Maths;
import net.luxvacuos.voxel.client.world.block.BlocksResources;
import net.luxvacuos.voxel.client.world.block.RenderBlock;
import net.luxvacuos.voxel.client.world.particles.ParticleSystem;
import net.luxvacuos.voxel.universal.core.AbstractVoxel;
import net.luxvacuos.voxel.universal.core.GlobalVariables;
import net.luxvacuos.voxel.universal.core.states.AbstractState;
import net.luxvacuos.voxel.universal.core.states.StateMachine;
import net.luxvacuos.voxel.universal.ecs.components.Position;
import net.luxvacuos.voxel.universal.ecs.components.Scale;
import net.luxvacuos.voxel.universal.material.BlockMaterial;
import net.luxvacuos.voxel.universal.world.dimension.PhysicsSystem;

/**
 * Test State
 * 
 * @author danirod
 */
public class TestState extends AbstractState {

	private PhysicsSystem physicsSystem;
	private Engine engine;
	private Sun sun;
	private ClientWorldSimulation worldSimulation;
	private CameraEntity camera;
	private Tessellator tess;
	private ParticleSystem particleSystem;
	private Vector3d particlesPoint;
	private GameWindow gameWindow;
	private PauseWindow pauseWindow;

	private RenderEntity mat1, mat2, mat3, mat4, mat5, rocket, plane, character, cerberus;

	private Model sphere, dragon, rocketM, planeM, characterM, cerberusM;
	private ParticleTexture fire;

	public TestState() {
		super(StateNames.TEST);
	}

	@Override
	public void init() {
		Window window = GraphicalSubsystem.getMainWindow();
		ResourceLoader loader = window.getResourceLoader();

		Matrix4d[] shadowProjectionMatrix = new Matrix4d[4];

		int shadowDrawDistance = (int) GlobalVariables.REGISTRY
				.getRegistryItem("/Voxel/Settings/Graphics/shadowsDrawDistance");

		shadowProjectionMatrix[0] = Maths.orthographic(-shadowDrawDistance / 32, shadowDrawDistance / 32,
				-shadowDrawDistance / 32, shadowDrawDistance / 32, -shadowDrawDistance, shadowDrawDistance, false);
		shadowProjectionMatrix[1] = Maths.orthographic(-shadowDrawDistance / 10, shadowDrawDistance / 10,
				-shadowDrawDistance / 10, shadowDrawDistance / 10, -shadowDrawDistance, shadowDrawDistance, false);
		shadowProjectionMatrix[2] = Maths.orthographic(-shadowDrawDistance / 4, shadowDrawDistance / 4,
				-shadowDrawDistance / 4, shadowDrawDistance / 4, -shadowDrawDistance, shadowDrawDistance, false);
		shadowProjectionMatrix[3] = Maths.orthographic(-shadowDrawDistance, shadowDrawDistance, -shadowDrawDistance,
				shadowDrawDistance, -shadowDrawDistance, shadowDrawDistance, false);
		Matrix4d projectionMatrix = Renderer.createProjectionMatrix(window.getWidth(), window.getHeight(),
				(int) REGISTRY.getRegistryItem("/Voxel/Settings/Core/fov"), ClientVariables.NEAR_PLANE,
				ClientVariables.FAR_PLANE);

		camera = new PlayerCamera(projectionMatrix, ClientVariables.user.getUsername(),
				ClientVariables.user.getUUID().toString());
		camera.setPosition(new Vector3d(0, 2, 0));
		sun = new Sun(shadowProjectionMatrix);

		EntityResources.load(loader);
		AssimpResourceLoader aLoader = window.getAssimpResourceLoader();

		worldSimulation = new ClientWorldSimulation(10000);
		engine = new Engine();
		physicsSystem = new PhysicsSystem(null);
		physicsSystem.addBox(new BoundingBox(new Vector3(-50, -1, -50), new Vector3(50, 0, 50)));
		engine.addSystem(physicsSystem);

		sphere = aLoader.loadModel("levels/test_state/models/sphere.blend");

		tess = new Tessellator(BlocksResources.getMaterial());

		// Renderer.getLightRenderer().addLight(new Light(new Vector3f(-8, 5,
		// -8), new Vector3f(1, 1, 1)));
		// Renderer.getLightRenderer().addLight(new Light(new Vector3f(-8, 5,
		// 8), new Vector3f(1, 1, 1)));
		// Renderer.getLightRenderer().addLight(new Light(new Vector3f(8, 5,
		// -8), new Vector3f(1, 1, 1)));
		// Renderer.getLightRenderer().addLight(new Light(new Vector3f(8, 5, 8),
		// new Vector3f(1, 1, 1)));
		// Renderer.getLightRenderer().addLight(new Light(new Vector3f(0, 5, 0),
		// new Vector3f(1, 1, 1)));
		Renderer.getLightRenderer().addLight(new Light(new Vector3f(5, 5, 12), new Vector3f(100, 100, 100),
				new Vector3f(-0.5f, -0.5f, -1f), 20, 15));
		Renderer.getLightRenderer().addLight(new Light(new Vector3f(-5, 5, 12), new Vector3f(100, 100, 100),
				new Vector3f(0.5f, -0.5f, -1f), 20, 15));

		mat1 = new RenderEntity("", sphere);
		mat1.getComponent(Position.class).set(0, 1, 0);

		mat2 = new RenderEntity("", sphere);
		mat2.getComponent(Position.class).set(3, 1, 0);

		mat3 = new RenderEntity("", sphere);
		mat3.getComponent(Position.class).set(6, 1, 0);

		mat4 = new RenderEntity("", sphere);
		mat4.getComponent(Position.class).set(9, 1, 0);

		dragon = aLoader.loadModel("levels/test_state/models/dragon.blend");

		mat5 = new RenderEntity("", dragon);

		mat5.getComponent(Position.class).set(-7, 0, 0);
		mat5.getComponent(Scale.class).setScale(0.5f);

		rocketM = aLoader.loadModel("levels/test_state/models/Rocket.obj");

		rocket = new RenderEntity("", rocketM);
		rocket.getComponent(Position.class).set(0, 0, -5);

		planeM = aLoader.loadModel("levels/test_state/models/plane.blend");

		plane = new RenderEntity("", planeM);
		plane.getComponent(Scale.class).setScale(1f);

		characterM = aLoader.loadModel("levels/test_state/models/monkey.blend");

		character = new RenderEntity("", characterM);
		character.getComponent(Position.class).set(0, 0, 5);
		// character.getComponent(Scale.class).setScale(0.21f);

		cerberusM = aLoader.loadModel("levels/test_state/models/cerberus.blend");

		cerberus = new RenderEntity("", cerberusM);
		cerberus.getComponent(Position.class).set(5, 1.25f, 5);
		cerberus.getComponent(Scale.class).setScale(0.5f);

		fire = new ParticleTexture(loader.loadTexture("textures/particles/fire0.png").getID(), 4);

		particleSystem = new ParticleSystem(fire, 1000, 1, -1f, 3f, 6f);
		particleSystem.setDirection(new Vector3d(0, -1, 0), 0.4f);
		particlesPoint = new Vector3d(0, 1.7f, -5);

		// worldSimulation.setTime(22000);
	}

	@Override
	public void dispose() {
		tess.cleanUp();
		sphere.dispose();
		dragon.dispose();
		characterM.dispose();
		fire.dispose();
		planeM.dispose();
		rocketM.dispose();
		cerberusM.dispose();
		planeM.dispose();
	}

	@Override
	public void start() {
		Renderer.setShadowPass((camera, sunCamera, frustum, shadowMap) -> {
			tess.drawShadow(sunCamera);
		});

		Renderer.setDeferredPass((camera, sunCamera, frustum, shadowMap) -> {
			tess.draw(camera, worldSimulation);
		});

		RenderBlock t = new RenderBlock(new BlockMaterial("test"), new BlockFaceAtlas("leaves"));
		t.setID(1);
		tess.begin();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				tess.generateCube(20 + x, 0, z, 1, true, true, true, true, true, true, t);
			}
		}
		tess.end();
		camera.setPosition(new Vector3d(0, 2, 0));
		physicsSystem.getEngine().addEntity(camera);
		physicsSystem.getEngine().addEntity(plane);
		physicsSystem.getEngine().addEntity(mat1);
		physicsSystem.getEngine().addEntity(mat2);
		physicsSystem.getEngine().addEntity(mat3);
		physicsSystem.getEngine().addEntity(mat4);
		physicsSystem.getEngine().addEntity(mat5);
		physicsSystem.getEngine().addEntity(rocket);
		physicsSystem.getEngine().addEntity(character);
		physicsSystem.getEngine().addEntity(cerberus);
		((PlayerCamera) camera).setMouse();
		Renderer.render(engine.getEntities(), ParticleDomain.getParticles(), camera, worldSimulation, sun, 0);
		gameWindow = new GameWindow(0, (int) REGISTRY.getRegistryItem("/Voxel/Display/height"),
				(int) REGISTRY.getRegistryItem("/Voxel/Display/width"),
				(int) REGISTRY.getRegistryItem("/Voxel/Display/height"));
		GraphicalSubsystem.getWindowManager().addWindow(0, gameWindow);
	}

	@Override
	public void end() {
		physicsSystem.getEngine().removeAllEntities();
	}

	@Override
	public void update(AbstractVoxel voxel, float delta) {
		GraphicalSubsystem.getWindowManager().update(delta);
		Window window = GraphicalSubsystem.getMainWindow();
		KeyboardHandler kbh = window.getKeyboardHandler();
		if (!ClientVariables.paused) {

			engine.update(delta);
			sun.update(camera.getPosition(), worldSimulation.update(delta), delta);
			// particleSystem.generateParticles(particlesPoint, delta);
			ParticleDomain.update(delta, camera);

			if (kbh.isKeyPressed(GLFW.GLFW_KEY_R))
				ClientVariables.raining = !ClientVariables.raining;
			if (kbh.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
				kbh.ignoreKeyUntilRelease(GLFW.GLFW_KEY_ESCAPE);
				((PlayerCamera) camera).unlockMouse();
				ClientVariables.paused = true;
				float borderSize = (float) REGISTRY.getRegistryItem("/Voxel/Settings/WindowManager/borderSize");
				float titleBarHeight = (float) REGISTRY.getRegistryItem("/Voxel/Settings/WindowManager/titleBarHeight");
				int height = (int) REGISTRY.getRegistryItem("/Voxel/Display/height");
				pauseWindow = new PauseWindow(borderSize + 10, height - titleBarHeight - 10,
						(int) REGISTRY.getRegistryItem("/Voxel/Display/width") - borderSize * 2f - 20,
						height - titleBarHeight - borderSize - 50);
				GraphicalSubsystem.getWindowManager().addWindow(pauseWindow);
				GraphicalSubsystem.getWindowManager().toggleShell();
			}
		} else if (ClientVariables.exitWorld) {
			gameWindow.closeWindow();
			pauseWindow.closeWindow();
			ClientVariables.exitWorld = false;
			ClientVariables.paused = false;
			StateMachine.setCurrentState(StateNames.MAIN_MENU);
		} else {
			if (kbh.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
				kbh.ignoreKeyUntilRelease(GLFW.GLFW_KEY_ESCAPE);
				Mouse.setGrabbed(true);
				ClientVariables.paused = false;
				pauseWindow.closeWindow();
				GraphicalSubsystem.getWindowManager().toggleShell();
			}
		}

	}

	@Override
	public void render(AbstractVoxel voxel, float alpha) {
		Renderer.render(engine.getEntities(), ParticleDomain.getParticles(), camera, worldSimulation, sun, alpha);
		Renderer.clearBuffer(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		Renderer.clearColors(1, 1, 1, 1);
		GraphicalSubsystem.getWindowManager().render();
	}

}
