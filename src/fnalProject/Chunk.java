package fnalProject;

import java.nio.FloatBuffer;
import java.util.Random;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;


public class Chunk {
    static final int CHUNK_SIZE = 30;
    static final int CUBE_LENGTH = 2;
    private Block[][][] Blocks;
    private int VBOVertexHandle;
    private int VBOColorHandle;
    private int VBOTextureHandle;
    private int VBONormalHandle;
    private Texture texture;
    private int StartX, StartY, StartZ;
    private Random r;

    public void render() {
        glPushMatrix();

        glEnableClientState(GL_NORMAL_ARRAY);

        glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
        glVertexPointer(3, GL_FLOAT, 0, 0L);

        glBindBuffer(GL_ARRAY_BUFFER, VBONormalHandle);
        glNormalPointer(GL_FLOAT, 0, 0L);

        glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
        glColorPointer(3, GL_FLOAT, 0, 0L);

        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBindTexture(GL_TEXTURE_2D, 1);
        glTexCoordPointer(2, GL_FLOAT, 0, 0L);

        glDrawArrays(GL_QUADS, 0, CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 24);

        glDisableClientState(GL_NORMAL_ARRAY);
        glPopMatrix();
    }

    public void rebuildMesh(float startX, float startY, float startZ) {
        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        VBOTextureHandle = glGenBuffers();
        VBONormalHandle = glGenBuffers();

        SimplexNoise noise = new SimplexNoise(10, 0.25, r.nextInt());
        float[][] heightMap = new float[CHUNK_SIZE][CHUNK_SIZE];

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                float noiseVal = (float) noise.getNoise((int)(x * 0.1f), (int)(z * 0.1f));
                float smoothedHeight = CHUNK_SIZE / 2f + noiseVal * (CHUNK_SIZE / 2f);
                heightMap[x][z] = smoothedHeight;
            }
        }

        FloatBuffer VertexPositionData = BufferUtils.createFloatBuffer(
                CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 6 * 12);
        FloatBuffer VertexColorData = BufferUtils.createFloatBuffer(
                CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 6 * 12);
        FloatBuffer VertexTextureData = BufferUtils.createFloatBuffer(
                CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 6 * 12);
        FloatBuffer NormalData = BufferUtils.createFloatBuffer(
                CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE * 6 * 12);

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int maxY = (int) heightMap[x][z];
                for (int y = 0; y <= maxY && y < CHUNK_SIZE; y++) {
                    // ...existing block creation code...
                    NormalData.put(createCubeNormal());
                }
            }
        }

        NormalData.flip();

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                int maxY = (int) heightMap[x][z];
                for (int y = 0; y <= maxY && y < CHUNK_SIZE; y++) {

                    Block.BlockType type;
                    if (y == 0) {
                        type = Block.BlockType.BlockType_Bedrock;
                    } else if (y < maxY * 0.25f) {
                        type = Block.BlockType.BlockType_Stone;
                    } else if (y < maxY * 0.5f) {
                        type = Block.BlockType.BlockType_Sand;
                    } else if (y < maxY * 0.75f) {
                        type = Block.BlockType.BlockType_Water;
                    } else if (y < maxY) {
                        type = Block.BlockType.BlockType_Dirt;
                    } else {
                        type = Block.BlockType.BlockType_Grass;
                    }

                    Blocks[x][y][z] = new Block(type);

                    VertexPositionData.put(createCube(
                            startX + x * CUBE_LENGTH,
                            startY + y * CUBE_LENGTH,
                            startZ + z * CUBE_LENGTH));

                    VertexColorData.put(createCubeVertexCol(getCubeColor(Blocks[x][y][z])));
                    VertexTextureData.put(createTexCube(0, 0, Blocks[x][y][z]));
                }
            }
        }

        VertexColorData.flip();
        VertexTextureData.flip();
        VertexPositionData.flip();

        glBindBuffer(GL_ARRAY_BUFFER, VBOVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexPositionData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glBindBuffer(GL_ARRAY_BUFFER, VBOColorHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexColorData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glBindBuffer(GL_ARRAY_BUFFER, VBOTextureHandle);
        glBufferData(GL_ARRAY_BUFFER, VertexTextureData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glBindBuffer(GL_ARRAY_BUFFER, VBONormalHandle);
        glBufferData(GL_ARRAY_BUFFER, NormalData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private float[] createCubeVertexCol(float[] CubeColorArray) {
        float[] cubeColors = new float[CubeColorArray.length * 4 * 6];
        for (int i = 0; i < cubeColors.length; i++) {
            cubeColors[i] = CubeColorArray[i %
                    CubeColorArray.length];
        }
        return cubeColors;
    }

    private float[] createCubeNormal() {
        return new float[] {
            // TOP
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            // BOTTOM
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            // FRONT
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            // BACK
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            // LEFT
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            // RIGHT
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f
        };
    }

    public static float[] createCube(float x, float y,
            float z) {
        int offset = CUBE_LENGTH / 2;
        return new float[] {
                // TOP QUAD
                x + offset, y + offset, z,
                x - offset, y + offset, z,
                x - offset, y + offset, z - CUBE_LENGTH,
                x + offset, y + offset, z - CUBE_LENGTH,
                // BOTTOM QUAD
                x + offset, y - offset, z - CUBE_LENGTH,
                x - offset, y - offset, z - CUBE_LENGTH,
                x - offset, y - offset, z,
                x + offset, y - offset, z,
                // FRONT QUAD
                x + offset, y + offset, z - CUBE_LENGTH,
                x - offset, y + offset, z - CUBE_LENGTH,
                x - offset, y - offset, z - CUBE_LENGTH,
                x + offset, y - offset, z - CUBE_LENGTH,
                // BACK QUAD
                x + offset, y - offset, z,
                x - offset, y - offset, z,
                x - offset, y + offset, z,
                x + offset, y + offset, z,
                // LEFT QUAD
                x - offset, y + offset, z - CUBE_LENGTH,
                x - offset, y + offset, z,
                x - offset, y - offset, z,
                x - offset, y - offset, z - CUBE_LENGTH,
                // RIGHT QUAD
                x + offset, y + offset, z,
                x + offset, y + offset, z - CUBE_LENGTH,
                x + offset, y - offset, z - CUBE_LENGTH,
                x + offset, y - offset, z };
    }

    private float[] getCubeColor(Block block) {
        return new float[] { 1, 1, 1 };
    }

    public static float[] createTexCube(float x, float y, Block block ){
        float offset = (1024f/16)/1024f;
        switch (block.GetID()){
            case 0:
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset*3, y + offset*10,
                    x + offset*2, y + offset*10,
                    x + offset*2, y + offset*9,
                    x + offset*3, y + offset*9,
                    // TOP!
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                    // FRONT QUAD
                    x + offset*3, y + offset*0,
                    x + offset*4, y + offset*0,
                    x + offset*4, y + offset*1,
                    x + offset*3, y + offset*1,
                    // BACK QUAD
                    x + offset*4, y + offset*1,
                    x + offset*3, y + offset*1,
                    x + offset*3, y + offset*0,
                    x + offset*4, y + offset*0,
                    // LEFT QUAD
                    x + offset*3, y + offset*0,
                    x + offset*4, y + offset*0,
                    x + offset*4, y + offset*1,
                    x + offset*3, y + offset*1,
                    // RIGHT QUAD
                    x + offset*3, y + offset*0,
                    x + offset*4, y + offset*0,
                    x + offset*4, y + offset*1,
                    x + offset*3, y + offset*1
                };

            case 1:
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset*3, y + offset*2,
                    x + offset*2, y + offset*2,
                    x + offset*2, y + offset*1,
                    x + offset*3, y + offset*1,
                    // TOP!
                    x + offset*3, y + offset*2,
                    x + offset*2, y + offset*2,
                    x + offset*2, y + offset*1,
                    x + offset*3, y + offset*1,
                    // FRONT QUAD
                    x + offset*3, y + offset*2,
                    x + offset*2, y + offset*2,
                    x + offset*2, y + offset*1,
                    x + offset*3, y + offset*1,
                    // BACK QUAD
                    x + offset*3, y + offset*2,
                    x + offset*2, y + offset*2,
                    x + offset*2, y + offset*1,
                    x + offset*3, y + offset*1,
                    // LEFT QUAD
                    x + offset*3, y + offset*2,
                    x + offset*2, y + offset*2,
                    x + offset*2, y + offset*1,
                    x + offset*3, y + offset*1,
                    // RIGHT QUAD
                    x + offset*3, y + offset*2,
                    x + offset*2, y + offset*2,
                    x + offset*2, y + offset*1,
                    x + offset*3, y + offset*1,
                };
            case 2:
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset*15, y + offset*13,
                    x + offset*14, y + offset*13,
                    x + offset*14, y + offset*12,
                    x + offset*15, y + offset*12,
                    // TOP!
                    x + offset*15, y + offset*13,
                    x + offset*14, y + offset*13,
                    x + offset*14, y + offset*12,
                    x + offset*15, y + offset*12,
                    // FRONT QUAD
                    x + offset*15, y + offset*13,
                    x + offset*14, y + offset*13,
                    x + offset*14, y + offset*12,
                    x + offset*15, y + offset*12,
                    // BACK QUAD
                    x + offset*15, y + offset*13,
                    x + offset*14, y + offset*13,
                    x + offset*14, y + offset*12,
                    x + offset*15, y + offset*12,
                    // LEFT QUAD
                    x + offset*15, y + offset*13,
                    x + offset*14, y + offset*13,
                    x + offset*14, y + offset*12,
                    x + offset*15, y + offset*12,
                    // RIGHT QUAD
                    x + offset*15, y + offset*13,
                    x + offset*14, y + offset*13,
                    x + offset*14, y + offset*12,
                    x + offset*15, y + offset*12,
                };
            case 3: //
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                    // TOP!
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                    // FRONT QUAD
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                    // BACK QUAD
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                    // LEFT QUAD
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                    // RIGHT QUAD
                    x + offset*3, y + offset*1,
                    x + offset*2, y + offset*1,
                    x + offset*2, y + offset*0,
                    x + offset*3, y + offset*0,
                };
            case 4: // stone
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset*2, y + offset*1,
                    x + offset*1, y + offset*1,
                    x + offset*1, y + offset*0,
                    x + offset*2, y + offset*0,
                    // TOP!
                    x + offset*2, y + offset*1,
                    x + offset*1, y + offset*1,
                    x + offset*1, y + offset*0,
                    x + offset*2, y + offset*0,
                    // FRONT QUAD
                    x + offset*2, y + offset*1,
                    x + offset*1, y + offset*1,
                    x + offset*1, y + offset*0,
                    x + offset*2, y + offset*0,
                    // BACK QUAD
                    x + offset*2, y + offset*1,
                    x + offset*1, y + offset*1,
                    x + offset*1, y + offset*0,
                    x + offset*2, y + offset*0,
                    // LEFT QUAD
                    x + offset*2, y + offset*1,
                    x + offset*1, y + offset*1,
                    x + offset*1, y + offset*0,
                    x + offset*2, y + offset*0,
                    // RIGHT QUAD
                    x + offset*2, y + offset*1,
                    x + offset*1, y + offset*1,
                    x + offset*1, y + offset*0,
                    x + offset*2, y + offset*0,
                };
            case 5: // bedrock
                return new float[]{
                    // BOTTOM QUAD(DOWN=+Y)
                    x + offset*2, y + offset*2,
                    x + offset*1, y + offset*2,
                    x + offset*1, y + offset*1,
                    x + offset*2, y + offset*1,
                    // TOP!
                    x + offset*2, y + offset*2,
                    x + offset*1, y + offset*2,
                    x + offset*1, y + offset*1,
                    x + offset*2, y + offset*1,
                    // FRONT QUAD
                    x + offset*2, y + offset*2,
                    x + offset*1, y + offset*2,
                    x + offset*1, y + offset*1,
                    x + offset*2, y + offset*1,
                    // BACK QUAD
                    x + offset*2, y + offset*2,
                    x + offset*1, y + offset*2,
                    x + offset*1, y + offset*1,
                    x + offset*2, y + offset*1,
                    // LEFT QUAD
                    x + offset*2, y + offset*2,
                    x + offset*1, y + offset*2,
                    x + offset*1, y + offset*1,
                    x + offset*2, y + offset*1,
                    // RIGHT QUAD
                    x + offset*2, y + offset*2,
                    x + offset*1, y + offset*2,
                    x + offset*1, y + offset*1,
                    x + offset*2, y + offset*1,
                };
        }

        // default block is grass block pog
        return new float[]{
            // BOTTOM QUAD(DOWN=+Y)
            x + offset*3, y + offset*10,
            x + offset*2, y + offset*10,
            x + offset*2, y + offset*9,
            x + offset*3, y + offset*9,
            // TOP!
            x + offset*3, y + offset*1,
            x + offset*2, y + offset*1,
            x + offset*2, y + offset*0,
            x + offset*3, y + offset*0,
            // FRONT QUAD
            x + offset*3, y + offset*0,
            x + offset*4, y + offset*0,
            x + offset*4, y + offset*1,
            x + offset*3, y + offset*1,
            // BACK QUAD
            x + offset*4, y + offset*1,
            x + offset*3, y + offset*1,
            x + offset*3, y + offset*0,
            x + offset*4, y + offset*0,
            // LEFT QUAD
            x + offset*3, y + offset*0,
            x + offset*4, y + offset*0,
            x + offset*4, y + offset*1,
            x + offset*3, y + offset*1,
            // RIGHT QUAD
            x + offset*3, y + offset*0,
            x + offset*4, y + offset*0,
            x + offset*4, y + offset*1,
            x + offset*3, y + offset*1
            };
    }

    public Chunk(int startX, int startY, int startZ) {
        // checks for the terrain file texture in the res folder
        try { texture = TextureLoader.getTexture("PNG",
                ResourceLoader.getResourceAsStream("res/terrain.png")
                );
        } catch(Exception e) {
            System.out.println("ERROR HELP HELP HELP");
        }

        r = new Random();
        Blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    if (r.nextFloat() > 0.833f) {
                        Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Grass);
                    } else if (r.nextFloat() > 0.666f) {
                        Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Dirt);
                    } else if (r.nextFloat() > 0.50f) {
                        Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Water);
                    } else if (r.nextFloat() > 0.333f) {
                        Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Sand);
                    } else if (r.nextFloat() > 0.166f) {
                        Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Stone);
                    } else {
                        Blocks[x][y][z] = new Block(Block.BlockType.BlockType_Bedrock);
                    }
                }
            }
        }
        VBOColorHandle = glGenBuffers();
        VBOVertexHandle = glGenBuffers();
        VBOTextureHandle = glGenBuffers();
        StartX = startX;
        StartY = startY;
        StartZ = startZ;
        rebuildMesh(startX, startY, startZ);
    }
}