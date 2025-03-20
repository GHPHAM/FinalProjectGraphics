package FinalProject;


import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.Sys;

public class FinalProject {
    // Windows
    // Apparently static final is the equivalence of const
    private static final int WINDOW_WIDTH = 640;
    private static final int WINDOW_HEIGHT = 480;
    private static final int NEAR_PLANE = 100;
    private static final int FAR_PLANE = 100;


    public void start() {
        try {
            createWindow();
            initGL();
            render();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createWindow() throws Exception {
        Display.setFullscreen(false);
        Display.setDisplayMode(new DisplayMode(WINDOW_WIDTH, WINDOW_HEIGHT));
        Display.setTitle("Final Project");
        Display.create();
    }

    private void initGL() {
        int width =  WINDOW_WIDTH/2;
        int height =  WINDOW_HEIGHT/2;

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(-width, width, -height, height, -NEAR_PLANE, FAR_PLANE);
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        glEnable(GL_DEPTH_TEST);
    }

    private void drawCube() {
        float size = 20.0f; // Cube size

        // Front face (RED)
        glColor3f(1.0f, 0.0f, 0.0f);
        glBegin(GL_POLYGON);
        glVertex3f(-size, -size, size);
        glVertex3f(size, -size, size);
        glVertex3f(size, size, size);
        glVertex3f(-size, size, size);
        glEnd();

        // Back face (GREEN)
        glColor3f(0.0f, 1.0f, 0.0f);
        glBegin(GL_POLYGON);
        glVertex3f(-size, -size, -size);
        glVertex3f(-size, size, -size);
        glVertex3f(size, size, -size);
        glVertex3f(size, -size, -size);
        glEnd();

        // Top face (BLUE)
        glColor3f(0.0f, 0.0f, 1.0f);
        glBegin(GL_POLYGON);
        glVertex3f(-size, size, -size);
        glVertex3f(-size, size, size);
        glVertex3f(size, size, size);
        glVertex3f(size, size, -size);
        glEnd();

        // Bottom face (YELLOW)
        glColor3f(1.0f, 1.0f, 0.0f);
        glBegin(GL_POLYGON);
        glVertex3f(-size, -size, -size);
        glVertex3f(size, -size, -size);
        glVertex3f(size, -size, size);
        glVertex3f(-size, -size, size);
        glEnd();

        // Left face (CYAN)
        glColor3f(0.0f, 1.0f, 1.0f);
        glBegin(GL_POLYGON);
        glVertex3f(-size, -size, -size);
        glVertex3f(-size, -size, size);
        glVertex3f(-size, size, size);
        glVertex3f(-size, size, -size);
        glEnd();

        // Right face (MAGENTA)
        glColor3f(1.0f, 0.0f, 1.0f);
        glBegin(GL_POLYGON);
        glVertex3f(size, -size, -size);
        glVertex3f(size, size, -size);
        glVertex3f(size, size, size);
        glVertex3f(size, -size, size);
        glEnd();
    }

    private void render() {

        while (!Display.isCloseRequested())
        {
            try {
                glClear(GL_COLOR_BUFFER_BIT
                        | GL_DEPTH_BUFFER_BIT);
                glLoadIdentity();
                glPointSize(1);

                drawCube();

                Display.update();
                Display.sync(60);
                //return; // DO NOT RETURN HERE OR THE WINDOWS WILL DISAPPEAR
            } catch (Exception e) {
            }
        }
        Display.destroy();
    }

    public static void main(String[] args) {
        FinalProject basic = new FinalProject();
        basic.start();
    }
}
