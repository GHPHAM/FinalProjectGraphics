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

        private int skyboxTexture;
        private static final float SKYBOX_SIZE = 200.0f;  // Size of the skybox

        private void initSkybox() {
            try {
                skyboxTexture = TextureLoader.getTexture("PNG",
                    ResourceLoader.getResourceAsStream("res/skybox.png")
                ).getTextureID();
            } catch(Exception e) {
                System.out.println("Error loading skybox texture: " + e.getMessage());
            }
        }

        private void renderSkybox() {
            glPushMatrix();
            glDisable(GL_LIGHTING);
            glDisable(GL_DEPTH_TEST);
            glEnable(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, skyboxTexture);

            // Center the skybox around the camera
            glTranslatef(0, 0, 0);

            // For a standard skybox texture in horizontal cross format:
            //    [TOP]
            // [L][F][R][B]
            //    [BOT]
            float ix = 1.0f/4.0f;  // Each face is 1/4 of the texture width
            float iy = 1.0f/4.0f;  // Each face is 1/3 of the texture height

            glBegin(GL_QUADS);

            // Front face (middle strip)
            glTexCoord2f(ix, 2*iy);      glVertex3f(-SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE);
            glTexCoord2f(2*ix, 2*iy);    glVertex3f(SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE);
            glTexCoord2f(2*ix, iy);      glVertex3f(SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE);
            glTexCoord2f(ix, iy);        glVertex3f(-SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE);

            // Back face (rightmost)
            glTexCoord2f(3*ix, 2*iy);    glVertex3f(SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE);
            glTexCoord2f(4*ix, 2*iy);    glVertex3f(-SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE);
            glTexCoord2f(4*ix, iy);      glVertex3f(-SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE);
            glTexCoord2f(3*ix, iy);      glVertex3f(SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE);

            // Top face (top strip)
            glTexCoord2f(ix, iy);         glVertex3f(-SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE);
            glTexCoord2f(2*ix, iy);        glVertex3f(SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE);
            glTexCoord2f(2*ix, 0);      glVertex3f(SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE);
            glTexCoord2f(ix, 0);       glVertex3f(-SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE);

            // Bottom face (bottom strip)
            glTexCoord2f(ix, 2*iy);      glVertex3f(-SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE);
            glTexCoord2f(2*ix, 2*iy);    glVertex3f(SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE);
            glTexCoord2f(2*ix, 3*iy);    glVertex3f(SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE);
            glTexCoord2f(ix, 3*iy);      glVertex3f(-SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE);

            // Right face (right of front)
            glTexCoord2f(2*ix, 2*iy);    glVertex3f(SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE);
            glTexCoord2f(3*ix, 2*iy);    glVertex3f(SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE);
            glTexCoord2f(3*ix, iy);      glVertex3f(SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE);
            glTexCoord2f(2*ix, iy);      glVertex3f(SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE);

            // Left face (left of front)
            glTexCoord2f(ix, 2*iy);      glVertex3f(-SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE);
            glTexCoord2f(0, 2*iy);       glVertex3f(-SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE);
            glTexCoord2f(0, iy);         glVertex3f(-SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE);
            glTexCoord2f(ix, iy);        glVertex3f(-SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE);


            glEnd();

            glEnable(GL_LIGHTING);
            glEnable(GL_DEPTH_TEST);
            glPopMatrix();
        }

        public void render() {
            renderSkybox();

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
                    float noiseVal = (float) noise.getNoise((int)(x * 0.2f), (int)(z * 0.2f));
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
                        NormalData.put(createCubeNormal());
                    }
                }
            }

            // NETHER PORTAL
            int portalBaseX = CHUNK_SIZE / 2;
            int portalBaseZ = CHUNK_SIZE / 2;
            int portalBaseY = (int) heightMap[portalBaseX][portalBaseZ] + 1;
            for (int y = 0; y < 5; y++) {
                for (int x = 0; x < 4; x++) {
                    int worldX = portalBaseX + x;
                    int worldY = portalBaseY + y;
                    int worldZ = portalBaseZ;
                    boolean isEdge = (x == 0 || x == 3 || y == 0 || y == 4);
                    Block.BlockType type = isEdge ? Block.BlockType.BlockType_Obsidian : Block.BlockType.BlockType_Portal;
                    Blocks[worldX][worldY][worldZ] = new Block(type);
                    VertexPositionData.put(createCube(
                            startX + worldX * CUBE_LENGTH,
                            startY + worldY * CUBE_LENGTH,
                            startZ + worldZ * CUBE_LENGTH));
                    VertexColorData.put(createCubeVertexCol(getCubeColor(Blocks[worldX][worldY][worldZ])));
                    VertexTextureData.put(createTexCube(0, 0, Blocks[worldX][worldY][worldZ]));
                    NormalData.put(createCubeNormal());
                }
            }

            NormalData.flip();

            int r1 = r.nextInt(2);

            for (int x = 0; x < CHUNK_SIZE; x++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    int maxY = (int) heightMap[x][z];
                    for (int y = 0; y <= maxY && y < CHUNK_SIZE; y++) {

                        Block.BlockType type;

                        switch (r1) {
                        case 0: // grass world
                            if (y == 0) {
                                type = Block.BlockType.BlockType_Bedrock;
                            } else if (y < maxY * 0.75f) {
                                type = Block.BlockType.BlockType_Stone;
                                /*
                            } else if (y < maxY * 0.5f) {
                                type = Block.BlockType.BlockType_Sand;
                            } else if (y < maxY * 0.75f) {
                                type = Block.BlockType.BlockType_Water;
                                */
                            } else if (y < maxY) {
                                type = Block.BlockType.BlockType_Dirt;
                            } else {
                                type = Block.BlockType.BlockType_Grass;
                            }
                            break;
                        default: // ocean world
                            if (y == 0) {
                                type = Block.BlockType.BlockType_Bedrock;
                            } else if (y < maxY * 0.5f) {
                                type = Block.BlockType.BlockType_Stone;
                            } else if (y < maxY * 0.75) {
                                type = Block.BlockType.BlockType_Sand;
                            } else {
                                type = Block.BlockType.BlockType_Water;
                            }
                            break;
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
                                case 6: // obsidian
                                    return new float[]{
                                        // BOTTOM QUAD(DOWN=+Y)
                                        x + offset * 6, y + offset * 3,
                                        x + offset * 5, y + offset * 3,
                                        x + offset * 5, y + offset * 2,
                                        x + offset * 6, y + offset * 2,
                                        // TOP!
                                        x + offset * 6, y + offset * 3,
                                        x + offset * 5, y + offset * 3,
                                        x + offset * 5, y + offset * 2,
                                        x + offset * 6, y + offset * 2,
                                        // FRONT QUAD
                                        x + offset * 6, y + offset * 3,
                                        x + offset * 5, y + offset * 3,
                                        x + offset * 5, y + offset * 2,
                                        x + offset * 6, y + offset * 2,
                                        // BACK QUAD
                                        x + offset * 6, y + offset * 3,
                                        x + offset * 5, y + offset * 3,
                                        x + offset * 5, y + offset * 2,
                                        x + offset * 6, y + offset * 2,
                                        // LEFT QUAD
                                        x + offset * 6, y + offset * 3,
                                        x + offset * 5, y + offset * 3,
                                        x + offset * 5, y + offset * 2,
                                        x + offset * 6, y + offset * 2,
                                        // RIGHT QUAD
                                        x + offset * 6, y + offset * 3,
                                        x + offset * 5, y + offset * 3,
                                        x + offset * 5, y + offset * 2,
                                        x + offset * 6, y + offset * 2,};
                                case 7: // portal
                                    return new float[]{
                                        // BOTTOM QUAD(DOWN=+Y)
                                        x + offset * 6, y + offset * 15,
                                        x + offset * 5, y + offset * 15,
                                        x + offset * 5, y + offset * 14,
                                        x + offset * 6, y + offset * 14,
                                        // TOP!
                                        x + offset * 6, y + offset * 15,
                                        x + offset * 5, y + offset * 15,
                                        x + offset * 5, y + offset * 14,
                                        x + offset * 6, y + offset * 14,
                                        // FRONT QUAD
                                        x + offset * 6, y + offset * 15,
                                        x + offset * 5, y + offset * 15,
                                        x + offset * 5, y + offset * 14,
                                        x + offset * 6, y + offset * 14,
                                        // BACK QUAD
                                        x + offset * 6, y + offset * 15,
                                        x + offset * 5, y + offset * 15,
                                        x + offset * 5, y + offset * 14,
                                        x + offset * 6, y + offset * 14,
                                        // LEFT QUAD
                                        x + offset * 6, y + offset * 15,
                                        x + offset * 5, y + offset * 15,
                                        x + offset * 5, y + offset * 14,
                                        x + offset * 6, y + offset * 14,
                                        // RIGHT QUAD
                                        x + offset * 6, y + offset * 15,
                                        x + offset * 5, y + offset * 15,
                                        x + offset * 5, y + offset * 14,
                                        x + offset * 6, y + offset * 14,};
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
            initSkybox();
            rebuildMesh(startX, startY, startZ);
        }

        public void newChunk() {
            rebuildMesh(StartX, StartY, StartZ); // Rebuild the mesh to reflect the deletion
        }
    }