
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class program1 {
    // global params
    int numSegments = 500;

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
        Display.setDisplayMode(new DisplayMode(640, 480));
        Display.setTitle("Program 1");
        Display.create();
    }

    private void initGL() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, 640, 0, 480, 1, -1);
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
    }
                            
    private void drawLine(int x, int y, int xf, int yf)
    {
        glColor3f(1.0f, 0.0f, 0.0f);
        int dx, dy, d, incrementR, incrementUR, incrementDR;
        
        dx = Math.abs(xf - x);
        dy = Math.abs(yf - y);
        d = 2 * dy - dx;
        incrementR = 2 * dy;
        incrementUR = 2 * (dy - dx);
        incrementDR = 2 * (dy + dx);
        
        while (x < xf)
        { 
            //System.out.println("x: " + x + ", y: " + y);
            glVertex2f(x, y);
            
            if (d > 0) // positive then up right or down right
            {
                x += (x < xf ? 1 : -1);
                y += (y < yf ? 1 : -1);
                d += (y < yf ? incrementUR : incrementDR); // up right or down right
            }
            else // otherwise only move by x
            {
                x += (x < xf ? 1 : -1);
                d += incrementR;
            }
        }
    }    
    
    private void drawCircle(int x, int y, int r)
    {
        glColor3f(0.0f, 0.0f, 1.0f);
        //int numSegments = 1000; // maybe 1000 is enough?
        double theta = 2 * Math.PI / numSegments;

        for (int i = 0; i < numSegments; i++) {
            double newX = r * Math.cos(i * theta);
            double newY = r * Math.sin(i * theta);
            glVertex2f(x + (int) newX, y + (int) newY);
        }
    }

    private void drawElipse(int x, int y, int xrad, int yrad)
    {
        glColor3f(0.0f, 1.0f, 0.0f);
        //int numSegments = 1000; // maybe 1000 is enough?
        double theta = 2 * Math.PI / numSegments;

        for (int i = 0; i < numSegments; i++) {
            double newX = xrad * Math.cos(i * theta);
            double newY = yrad * Math.sin(i * theta);
            glVertex2f(x + (int) newX, y + (int) newY);
        }
    }
    
    private void render() {
        while (!Display.isCloseRequested()) {
            
                glClear(GL_COLOR_BUFFER_BIT
                        | GL_DEPTH_BUFFER_BIT);
                glLoadIdentity();
                glPointSize(1);
                glBegin(GL_POINTS);

            try {
                FileInputStream fstream = new FileInputStream("coordinates.txt");
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                String strLine;
                String[] coord1Str, coord2Str, coordStr;
                int[] coord1, coord2, coord;
                
                while ((strLine = br.readLine()) != null) { // check if line still available
                    
                    String[] tokens = strLine.split(" ");
                    switch (tokens[0]) {
                        case "l":
                            coord1Str = tokens[1].split(",");
                            coord2Str = tokens[2].split(",");
                            coord1 = new int[] { Integer.parseInt(coord1Str[0]), Integer.parseInt(coord1Str[1]) };
                            coord2 = new int[] { Integer.parseInt(coord2Str[0]), Integer.parseInt(coord2Str[1]) };
                            drawLine(coord1[0], coord1[1], coord2[0], coord2[1]);
                            break;
                        case "c":
                            coordStr = tokens[1].split(",");
                            coord = new int[] { Integer.parseInt(coordStr[0]), Integer.parseInt(coordStr[1]) };
                            drawCircle(coord[0], coord[1], Integer.parseInt(tokens[2]));
                            break;
                        case "e":
                            coord1Str = tokens[1].split(",");
                            coord2Str = tokens[2].split(",");
                            coord1 = new int[] { Integer.parseInt(coord1Str[0]), Integer.parseInt(coord1Str[1]) };
                            coord2 = new int[] { Integer.parseInt(coord2Str[0]), Integer.parseInt(coord2Str[1]) };
                            drawElipse(coord1[0], coord1[1], coord2[0], coord2[1]);
                            break;
                        default:
                            break;
                    }
                }
                br.close();
                in.close();
                fstream.close();
                
                Display.update();
                Display.sync(60);
                //return; // DO NOT RETURN HERE OR THE WINDOWS WILL DISAPPEAR
            } catch (Exception e) {
            }
            glEnd();
        }
        Display.destroy();
    }

    public static void main(String[] args) {
        program1 basic = new program1();
        basic.start();
    }
}
