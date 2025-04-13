package fnalProject;


import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;

import java.util.Random;

import org.lwjgl.util.glu.GLU;

public class FinalProject {
    // Windows
    // Apparently static final is the equivalence of const

    private FPCameraController fp = new FPCameraController(0f,0f,0f);
    private DisplayMode displayMode;


    public void start() {
        try {
            createWindow();
            initGL();
            fp.gameLoop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createWindow() throws Exception {
        Display.setFullscreen(false);
        DisplayMode d[] =
        Display.getAvailableDisplayModes();
        for (int i = 0; i < d.length; i++) {
            if (d[i].getWidth() == 640 && d[i].getHeight() == 480
                && d[i].getBitsPerPixel() == 32)
            {
                displayMode = d[i];
                break;
            }
        }
        Display.setDisplayMode(displayMode);
        String[] titles = {
            "CHICKEN JOCKEY!!!",
            "FLINT N STEEL!",
            "WATER BUCKET RELEASE!",
            "DA NETHER!",
            "OVERWORLD!",
            "L-L-L-LAVA CH-CH-CH-CHICKEN!",
            "I AM STEVE!"
        };

        Random random = new Random();
        String randomTitle = titles[random.nextInt(titles.length)];
        Display.setTitle(randomTitle);

        Display.create();
    }

    private void initGL()
    {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        GLU.gluPerspective(100.0f, (float)displayMode.getWidth()/(float)
            displayMode.getHeight(), 0.1f, 300.0f);

        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_TEXTURE_2D);
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
    }

    public static void main(String[] args) {
        FinalProject basic = new FinalProject();
        basic.start();
    }
}
