package dev.xfj.application;

import dev.xfj.Layer;
import dev.xfj.LayerStack;
import imgui.ImGui;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Application {
    private static Application instance;
    private ApplicationSpecification specification;
    private long windowHandle;
    private boolean running;
    private float timeStep;
    private float frameTime;
    private float lastFrameTime;
    private LayerStack layerStack;

    public Application(ApplicationSpecification specification) {
        this.specification = specification;
        this.timeStep = 0.0f;
        this.frameTime = 0.0f;
        this.lastFrameTime = 0.0f;
        this.layerStack = new LayerStack();
        instance = this;
        init();
    }

    private void init() {
        boolean success = glfwInit();

        if (!success) {
            throw new RuntimeException("Could not initialize GLFW!");
        } else {
            glfwSetErrorCallback(new GLFWErrorCallback() {
                @Override
                public void invoke(int error, long description) {
                    System.err.println(String.format("GLFW error (%1$d): %2$d", error, description));
                }
            });
        }
        windowHandle = glfwCreateWindow(specification.width, specification.height, specification.name, NULL, NULL);

        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 5);

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();

        ImGui.createContext();
    }

    public void run() {
        running = true;

        while (running) {
            glfwPollEvents();

            for (Layer layer : layerStack.getLayers()) {
                layer.onUpdate(timeStep);
            }

            glfwSwapBuffers(windowHandle);

            float time = getTime();
            frameTime = time - lastFrameTime;
            timeStep = Math.min(frameTime, 0.0333f);
            lastFrameTime = time;

        }
    }

    public void pushLayer(Layer layer) {
        layerStack.pushLayer(layer);
    }

    public void pushOverlay(Layer layer) {
        layerStack.pushOverlay(layer);
    }

    public float getTime() {
        return (float) glfwGetTime();
    }

    public static Application getInstance() {
        return instance;
    }

    public static void close(Application instance) {
        instance.close();
    }

    private void close() {
        this.running = false;
    }

    public long getWindowHandle() {
        return windowHandle;
    }
}
