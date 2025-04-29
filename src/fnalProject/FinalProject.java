package fnalProject;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;

import java.util.Random;
import java.nio.FloatBuffer;

import org.lwjgl.util.glu.GLU;

public class FinalProject {
    // Windows
    // Apparently static final is the equivalence of const

    private FPCameraController fp = new FPCameraController(0f,0f,0f);
    private DisplayMode displayMode;

    private FloatBuffer lightPosition;
    private FloatBuffer whiteLight;


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
            "I AM STEVE!",
            "hey does anyone think about the end of the universe sometimes"
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
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glEnable(GL_NORMALIZE);
        initLightArrays();
        glLight(GL_LIGHT0, GL_POSITION, lightPosition); //sets our light’s position
        glLight(GL_LIGHT0, GL_SPECULAR, whiteLight);//sets our specular light
        glLight(GL_LIGHT0, GL_DIFFUSE, whiteLight);//sets our diffuse light
        glLight(GL_LIGHT0, GL_AMBIENT, whiteLight);//sets our ambient light
        glEnable(GL_LIGHTING);//enables our lighting
        glEnable(GL_LIGHT0);//enables light0
        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
    }

    private void initLightArrays(){
        lightPosition = BufferUtils.createFloatBuffer(4);
        lightPosition.put(100.0f).put(100.0f).put(100.0f).put(1.0f).flip();

        whiteLight = BufferUtils.createFloatBuffer(4);
        whiteLight.put(1.0f).put(1.0f).put(1.0f).put(0.0f).flip();
    }

    public static void main(String[] args) {
        FinalProject basic = new FinalProject();
        basic.start();
    }
}
