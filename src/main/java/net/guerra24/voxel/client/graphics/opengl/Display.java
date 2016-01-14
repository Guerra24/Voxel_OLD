/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2016 Guerra24
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

package net.guerra24.voxel.client.graphics.opengl;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_VERSION_UNAVAILABLE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCharCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursorEnterCallback;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowFocusCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowIconifyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowRefreshCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.nanovg.NanoVG.nvgBeginFrame;
import static org.lwjgl.nanovg.NanoVG.nvgEndFrame;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_ANTIALIAS;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_DEBUG;
import static org.lwjgl.nanovg.NanoVGGL3.NVG_STENCIL_STROKES;
import static org.lwjgl.nanovg.NanoVGGL3.nvgCreateGL3;
import static org.lwjgl.nanovg.NanoVGGL3.nvgDeleteGL3;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_INVALID_ENUM;
import static org.lwjgl.opengl.GL11.GL_INVALID_OPERATION;
import static org.lwjgl.opengl.GL11.GL_INVALID_VALUE;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_OUT_OF_MEMORY;
import static org.lwjgl.opengl.GL11.GL_STACK_OVERFLOW;
import static org.lwjgl.opengl.GL11.GL_STACK_UNDERFLOW;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.GL_VENDOR;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glGetString;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL30.GL_INVALID_FRAMEBUFFER_OPERATION;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowIconifyCallback;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.glfw.GLFWWindowRefreshCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.NVXGPUMemoryInfo;
import org.lwjgl.opengl.WGLAMDGPUAssociation;

import de.matthiasmann.twl.utils.PNGDecoder;
import net.guerra24.voxel.client.core.VoxelVariables;
import net.guerra24.voxel.client.input.Keyboard;
import net.guerra24.voxel.client.input.Mouse;
import net.guerra24.voxel.client.util.Logger;

/**
 * Display Manager
 * 
 * @author Guerra24 <pablo230699@hotmail.com>
 * @category OpenGL
 */
public class Display {

	/**
	 * LWJGL Window
	 */
	private static long window;
	private static long vg;

	/**
	 * Display VidMode
	 */
	private GLFWVidMode vidmode;
	/**
	 * Display Data
	 */
	private static double lastLoopTimeUpdate;
	public static float timeCountUpdate;
	private static double lastLoopTimeRender;
	public static float timeCountRender;
	public static int ups;
	public static int upsCount;

	/**
	 * LWJGL Callback
	 */
	private static GLFWErrorCallback errorCallback;
	public GLFWKeyCallback keyCallback;
	public GLFWCharCallback charCallback;
	public GLFWCursorEnterCallback cursorEnterCallback;
	public GLFWCursorPosCallback cursorPosCallback;
	public GLFWMouseButtonCallback mouseButtonCallback;
	public GLFWWindowFocusCallback windowFocusCallback;
	public GLFWWindowIconifyCallback windowIconifyCallback;
	public GLFWWindowSizeCallback windowSizeCallback;
	public GLFWWindowPosCallback windowPosCallback;
	public GLFWWindowRefreshCallback windowRefreshCallback;
	public GLFWFramebufferSizeCallback framebufferSizeCallback;
	public GLFWScrollCallback scrollCallback;

	/**
	 * Window variables
	 */
	private boolean displayCreated = false;
	private boolean displayFocused = false;
	private boolean displayVisible = true;
	private boolean displayDirty = false;
	private boolean displayResizable = false;
	private int latestEventKey = 0;
	private int displayX = 0;
	private int displayY = 0;
	private boolean displayResized = false;
	private static int displayWidth = 0;
	private static int displayHeight = 0;
	private int displayFramebufferWidth = 0;
	private int displayFramebufferHeight = 0;
	private boolean latestResized = false;
	private int latestWidth = 0;
	private int latestHeight = 0;
	public static float pixelRatio;

	private static IntBuffer maxVram = BufferUtils.createIntBuffer(1);
	private static IntBuffer usedVram = BufferUtils.createIntBuffer(1);
	private static boolean nvidia = false;
	private static boolean amd = false;

	private static long variableYieldTime, lastTime;

	public void initDsiplay(int width, int height) {
		glfwSetErrorCallback(errorCallback = new GLFWErrorCallback() {
			GLFWErrorCallback delegate = GLFWErrorCallback.createPrint(System.err);

			@Override
			public void invoke(int error, long description) {
				switch (error) {
				case GLFW_VERSION_UNAVAILABLE:
					Logger.error("Voxel requires OpenGL 3.3 or higher");
					break;
				}
				delegate.invoke(error, description);
			}

			@Override
			public void release() {
				delegate.release();
				super.release();
			}
		});
		displayWidth = width;
		displayHeight = height;
		if (glfwInit() != GL_TRUE)
			throw new IllegalStateException("Unable to initialize GLFW");

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GL_TRUE);
		displayResizable = false;
		glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);

		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		window = glfwCreateWindow(displayWidth, displayHeight, VoxelVariables.Title, NULL, NULL);
		if (window == NULL) {
			throw new RuntimeException("Failed to create the GLFW window");
		}
		createCallBacks();
		setCallbacks();
		vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
		glfwSetWindowPos(window, (vidmode.width() - displayWidth) / 2, (vidmode.height() - displayHeight) / 2);
		glfwMakeContextCurrent(window);
		glfwSwapInterval(VoxelVariables.VSYNC ? 1 : 0);
	}

	/**
	 * Create the LWJGL CallBacks
	 * 
	 */
	public void createCallBacks() {
		keyCallback = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				latestEventKey = key;
				if (action == GLFW_RELEASE || action == GLFW.GLFW_PRESS) {
					Keyboard.addKeyEvent(key, action == GLFW.GLFW_PRESS ? true : false);
				}
			}
		};

		charCallback = new GLFWCharCallback() {
			@Override
			public void invoke(long window, int codepoint) {
				Keyboard.addCharEvent(latestEventKey, (char) codepoint);
			}
		};

		cursorEnterCallback = new GLFWCursorEnterCallback() {
			@Override
			public void invoke(long window, int entered) {
				Mouse.setMouseInsideWindow(entered == GL_TRUE);
			}
		};

		cursorPosCallback = new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double xpos, double ypos) {
				Mouse.addMoveEvent(xpos, ypos);
			}
		};

		mouseButtonCallback = new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				Mouse.addButtonEvent(button, action == GLFW.GLFW_PRESS ? true : false);
			}
		};

		windowFocusCallback = new GLFWWindowFocusCallback() {
			@Override
			public void invoke(long window, int focused) {
				displayFocused = focused == GL_TRUE;
			}
		};

		windowIconifyCallback = new GLFWWindowIconifyCallback() {
			@Override
			public void invoke(long window, int iconified) {
				displayVisible = iconified == GL_FALSE;
			}
		};

		windowSizeCallback = new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				latestResized = true;
				latestWidth = width;
				latestHeight = height;
			}
		};

		windowPosCallback = new GLFWWindowPosCallback() {
			@Override
			public void invoke(long window, int xpos, int ypos) {
				displayX = xpos;
				displayY = ypos;
			}
		};

		windowRefreshCallback = new GLFWWindowRefreshCallback() {
			@Override
			public void invoke(long window) {
				displayDirty = true;
			}
		};

		framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				displayFramebufferWidth = width;
				displayFramebufferHeight = height;
			}
		};

		scrollCallback = new GLFWScrollCallback() {
			@Override
			public void invoke(long window, double xoffset, double yoffset) {
				Mouse.addWheelEvent((int) (yoffset * 120));
			}
		};
	}

	/**
	 * Set the LWJGL CallBacks
	 * 
	 */
	public void setCallbacks() {
		glfwSetKeyCallback(window, keyCallback);
		glfwSetCharCallback(window, charCallback);
		glfwSetCursorEnterCallback(window, cursorEnterCallback);
		glfwSetCursorPosCallback(window, cursorPosCallback);
		glfwSetMouseButtonCallback(window, mouseButtonCallback);
		glfwSetWindowFocusCallback(window, windowFocusCallback);
		glfwSetWindowIconifyCallback(window, windowIconifyCallback);
		glfwSetWindowSizeCallback(window, windowSizeCallback);
		glfwSetWindowPosCallback(window, windowPosCallback);
		glfwSetWindowRefreshCallback(window, windowRefreshCallback);
		glfwSetFramebufferSizeCallback(window, framebufferSizeCallback);
		glfwSetScrollCallback(window, scrollCallback);
	}

	/**
	 * Creates and Sets the Display
	 * 
	 */
	public void startUp() {
		Logger.log("Creating Display");
		try {
			String[] IconPath = new String[2];
			IconPath[0] = "assets/icon/icon32.png";
			IconPath[1] = "assets/icon/icon64.png";
			ByteBuffer[] icon_array = new ByteBuffer[IconPath.length];
			for (int i = 0; i < IconPath.length; i++) {
				icon_array[i] = ByteBuffer.allocateDirect(1);
				String path = IconPath[i];
				icon_array[i] = loadIcon(path);
			}
		} catch (IOException e) {
			Logger.error("Failed to load icon");
			e.printStackTrace();
		}
		createCapabilities();
		vg = nvgCreateGL3(NVG_ANTIALIAS | NVG_STENCIL_STROKES | NVG_DEBUG);
		if (vg == NULL)
			throw new RuntimeException("Fail to create NanoVG");
		lastLoopTimeUpdate = getTime();
		lastLoopTimeRender = getTime();
		ByteBuffer w = BufferUtils.createByteBuffer(4);
		ByteBuffer h = BufferUtils.createByteBuffer(4);
		glfwGetFramebufferSize(window, w, h);
		displayFramebufferWidth = w.getInt(0);
		displayFramebufferHeight = h.getInt(0);

		glfwGetWindowSize(window, w, h);
		displayWidth = w.getInt(0);
		displayHeight = h.getInt(0);
		pixelRatio = (float) displayFramebufferWidth / (float) displayWidth;
		glViewport(0, 0, (int) (displayWidth * pixelRatio), (int) (displayHeight * pixelRatio));

		if (glGetString(GL_VENDOR).contains("NVIDIA"))
			nvidia = true;
		else if (glGetString(GL_VENDOR).contains("AMD"))
			amd = true;
		if (nvidia)
			glGetIntegerv(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_DEDICATED_VIDMEM_NVX, maxVram);
		else if (amd)
			glGetIntegerv(WGLAMDGPUAssociation.WGL_GPU_RAM_AMD, maxVram);
		if (nvidia)
			Logger.log("Max VRam: " + maxVram.get(0) + "KB");
		else if (amd)
			Logger.log("Max VRam: " + maxVram.get(0) + "MB");
		displayCreated = true;
	}

	/**
	 * Updates the Display
	 * 
	 * @param fps
	 *            Game Max FPS
	 */
	public void updateDisplay(int fps) {
		glfwSwapBuffers(window);
		glfwPollEvents();
		Mouse.poll();
		sync(fps);
		// checkErrors();
	}

	/**
	 * Call this before any NanoVG call
	 * 
	 */
	public static void beingNVGFrame() {
		nvgBeginFrame(vg, displayWidth, displayHeight, pixelRatio);
	}

	/**
	 * Ends the actual NVGFrame
	 */
	public static void endNVGFrame() {
		nvgEndFrame(vg);
	}

	private void checkErrors() {
		switch (glGetError()) {
		case GL_NO_ERROR:
			break;
		case GL_INVALID_ENUM:
			throw new RuntimeException("OpenGL: Invalid Enum");
		case GL_INVALID_VALUE:
			throw new RuntimeException("OpenGL: Invalid Value");
		case GL_INVALID_OPERATION:
			throw new RuntimeException("OpenGL: Invalid Operation");
		case GL_INVALID_FRAMEBUFFER_OPERATION:
			throw new RuntimeException("OpenGL: Invalid FrameBuffer Operation");
		case GL_OUT_OF_MEMORY:
			throw new RuntimeException("OpenGL: Out of Memory");
		case GL_STACK_UNDERFLOW:
			throw new RuntimeException("OpenGL: Underflow");
		case GL_STACK_OVERFLOW:
			throw new RuntimeException("OpenGL: Overflow");
		}
	}

	/**
	 * Destroy the display
	 * 
	 */
	public void closeDisplay() {
		nvgDeleteGL3(vg);

		glfwDestroyWindow(window);
		glfwTerminate();
		errorCallback.release();
		keyCallback.release();
		charCallback.release();
		cursorEnterCallback.release();
		cursorPosCallback.release();
		mouseButtonCallback.release();
		windowFocusCallback.release();
		windowIconifyCallback.release();
		windowSizeCallback.release();
		windowPosCallback.release();
		windowRefreshCallback.release();
		framebufferSizeCallback.release();
		scrollCallback.release();
	}

	/**
	 * Loads the Icon
	 * 
	 * @param path
	 *            Icon Path
	 * @return ByteBuffer
	 * @throws IOException
	 */
	private static ByteBuffer loadIcon(String path) throws IOException {
		InputStream inputStream = new FileInputStream(path);
		try {
			PNGDecoder decoder = new PNGDecoder(inputStream);
			ByteBuffer bytebuf = ByteBuffer.allocateDirect(decoder.getWidth() * decoder.getHeight() * 4);
			decoder.decode(bytebuf, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);
			bytebuf.flip();
			return bytebuf;
		} finally {
			inputStream.close();
		}
	}

	/**
	 * Get the time
	 * 
	 * @return time
	 */
	public static double getTime() {
		return glfwGetTime();
	}

	public static long getNanoTime() {
		return (long) (glfwGetTime() * (1000L * 1000L * 1000L));
	}

	public static int checkVRAM() {
		if (nvidia)
			glGetIntegerv(NVXGPUMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX, usedVram);
		return maxVram.get(0) - usedVram.get(0);
	}

	/**
	 * Calculates the Delta
	 * 
	 * @return Delta
	 */
	public static float getDeltaUpdate() {
		double time = getTime();
		float delta = (float) (time - lastLoopTimeUpdate);
		lastLoopTimeUpdate = time;
		timeCountUpdate += delta;
		return delta;
	}

	/**
	 * Calculates the Delta
	 * 
	 * @return Delta
	 */
	public static float getDeltaRender() {
		double time = getTime();
		float delta = (float) (time - lastLoopTimeRender);
		lastLoopTimeRender = time;
		timeCountRender += delta;
		return delta;
	}

	/**
	 * Get the Window
	 * 
	 * @return window
	 */
	public static long getWindow() {
		return window;
	}

	/**
	 * Get the NanoVG
	 * 
	 * @return vg
	 */
	public static long getVg() {
		return vg;
	}

	/**
	 * If a close is requested
	 * 
	 * @return Boolean
	 */
	public static boolean isCloseRequested() {
		return glfwWindowShouldClose(window) == GL_TRUE;
	}

	public static int getWidth() {
		return displayWidth;
	}

	public static void setWidth(int width) {
		displayWidth = width;
	}

	public boolean isDisplayCreated() {
		return displayCreated;
	}

	public boolean isDisplayFocused() {
		return displayFocused;
	}

	public boolean isDisplayVisible() {
		return displayVisible;
	}

	public boolean isDisplayDirty() {
		return displayDirty;
	}

	public boolean isDisplayResizable() {
		return displayResizable;
	}

	public int getLatestEventKey() {
		return latestEventKey;
	}

	public int getDisplayX() {
		return displayX;
	}

	public int getDisplayY() {
		return displayY;
	}

	public boolean isDisplayResized() {
		return displayResized;
	}

	public int getDisplayFramebufferWidth() {
		return displayFramebufferWidth;
	}

	public int getDisplayFramebufferHeight() {
		return displayFramebufferHeight;
	}

	public boolean isLatestResized() {
		return latestResized;
	}

	public int getLatestWidth() {
		return latestWidth;
	}

	public int getLatestHeight() {
		return latestHeight;
	}

	public static int getHeight() {
		return displayHeight;
	}

	public static void setHeight(int height) {
		displayHeight = height;
	}

	/**
	 * Limits the fps to a fixed value
	 * 
	 * @param fps
	 *            FPS Limit
	 */
	private void sync(int fps) {
		if (fps <= 0)
			return;
		long sleepTime = 1000000000 / fps;
		long yieldTime = Math.min(sleepTime, variableYieldTime + sleepTime % (1000 * 1000));
		long overSleep = 0;

		try {
			while (true) {
				long t = System.nanoTime() - lastTime;

				if (t < sleepTime - yieldTime) {
					Thread.sleep(1);
				} else if (t < sleepTime) {
					Thread.yield();
				} else {
					overSleep = t - sleepTime;
					break;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lastTime = System.nanoTime() - Math.min(overSleep, sleepTime);
			if (overSleep > variableYieldTime) {
				variableYieldTime = Math.min(variableYieldTime + 200 * 1000, sleepTime);
			} else if (overSleep < variableYieldTime - 200 * 1000) {
				variableYieldTime = Math.max(variableYieldTime - 2 * 1000, 0);
			}
		}
	}

}
