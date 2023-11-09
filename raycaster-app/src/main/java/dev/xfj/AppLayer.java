package dev.xfj;

import dev.xfj.application.Application;
import dev.xfj.input.Input;
import dev.xfj.input.KeyCodes;

import java.nio.file.Files;

import static java.lang.Math.PI;

import java.nio.file.Path;

import static org.joml.Math.*;
import static org.lwjgl.opengl.GL41.*;

public class AppLayer implements Layer {
    private static final int MAP_X = 8;
    private static final int MAP_Y = 8;
    private static final int MAP_SIZE = 64;
    private static final int[] MAP_W = new int[]{
            1, 1, 1, 1, 2, 2, 2, 2,
            6, 0, 0, 1, 0, 0, 0, 2,
            1, 0, 0, 4, 0, 2, 0, 2,
            1, 5, 4, 5, 0, 0, 0, 2,
            2, 0, 0, 0, 0, 0, 0, 1,
            2, 0, 0, 0, 0, 1, 0, 1,
            2, 0, 0, 0, 0, 0, 0, 1,
            1, 1, 1, 1, 1, 1, 1, 1};
    private static final int[] MAP_F = new int[]{
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 2, 2, 2, 0,
            0, 0, 0, 0, 6, 0, 2, 0,
            0, 0, 8, 0, 2, 7, 6, 0,
            0, 0, 2, 0, 0, 0, 0, 0,
            0, 0, 2, 0, 8, 0, 0, 0,
            0, 1, 1, 1, 1, 0, 8, 0,
            0, 0, 0, 0, 0, 0, 0, 0};
    private static final int[] MAP_C = new int[]{
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 4, 2, 4, 0, 0, 0, 0,
            0, 0, 2, 0, 0, 0, 0, 0,
            0, 0, 2, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0};

    private static final int[] allTextures = loadArray("textures\\allTextures.txt");
    private static final int[] lost = loadArray("textures\\lost.txt");
    private static final int[] sky = loadArray("textures\\sky.txt");
    private static final int[] sprites = loadArray("textures\\sprites.txt");
    private static final int[] title = loadArray("textures\\title.txt");
    private static final int[] won = loadArray("textures\\won.txt");

    private float playerX;
    private float playerY;
    private float playerDeltaX;
    private float playerDeltaY;
    private float playerAngle;
    private float fps;
    private int gameState;
    private int timer;
    private float fade;
    private final Sprite[] spriteArray = {new Sprite(), new Sprite(), new Sprite(), new Sprite()};
    private final int[] depth = new int[120];

    @Override
    public void onAttach() {
        glOrtho(0, Application.getInstance().getSpecification().width, Application.getInstance().getSpecification().height, 0, -1, 1);
        glClearColor(0.3f, 0.3f, 0.3f, 0.0f);
    }

    private void init() {
        playerX = 150.0f;
        playerY = 400.0f;
        playerAngle = 90.0f;
        playerDeltaX = cos(degreesToRadians(playerAngle));
        playerDeltaY = -sin(degreesToRadians(playerAngle));
        MAP_W[19] = 4;
        MAP_W[26] = 4;

        //Key
        spriteArray[0].type = 1;
        spriteArray[0].state = 1;
        spriteArray[0].map = 0;
        spriteArray[0].x = 1.5f * 64;
        spriteArray[0].y = 5.0f * 64;
        spriteArray[0].z = 20;

        //Light 1
        spriteArray[1].type = 2;
        spriteArray[1].state = 1;
        spriteArray[1].map = 1;
        spriteArray[1].x = 1.5f * 64;
        spriteArray[1].y = 4.5f * 64;
        spriteArray[1].z = 0;

        //Light 2
        spriteArray[2].type = 2;
        spriteArray[2].state = 1;
        spriteArray[2].map = 1;
        spriteArray[2].x = 3.5f * 64;
        spriteArray[2].y = 4.5f * 64;
        spriteArray[2].z = 0;

        //Enemy
        spriteArray[3].type = 3;
        spriteArray[3].state = 1;
        spriteArray[3].map = 2;
        spriteArray[3].x = 2.5f * 64;
        spriteArray[3].y = 2 * 64;
        spriteArray[3].z = 20;
    }

    @Override
    public void onDetach() {
        System.out.println("Not implemented!");
    }

    @Override
    public void onUpdate(float ts) {
        fps = ts * 1000;

        //Init
        if (gameState == 0) {
            init();
            fade = 0;
            timer = 0;
            gameState = 1;
        }

        //Start screen
        if (gameState == 1) {
            screen(1);
            timer += 1 * fps;
            if (timer > 2000) {
                fade = 0;
                timer = 0;
                gameState = 2;
            }
        }

        //Main loop
        if (gameState == 2) {
            if (Input.isKeyDown(KeyCodes.A)) {
                playerAngle += 0.2 * fps;
                playerAngle = fixAngle(playerAngle);
                playerDeltaX = cos(degreesToRadians(playerAngle));
                playerDeltaY = -sin(degreesToRadians(playerAngle));
            }

            if (Input.isKeyDown(KeyCodes.D)) {
                playerAngle -= 0.2 * fps;
                playerAngle = fixAngle(playerAngle);
                playerDeltaX = cos(degreesToRadians(playerAngle));
                playerDeltaY = -sin(degreesToRadians(playerAngle));
            }

            int xOffset;
            if (playerDeltaX < 0) {
                xOffset = -20;
            } else {
                xOffset = 20;
            }

            int yOffset;
            if (playerDeltaY < 0) {
                yOffset = -20;
            } else {
                yOffset = 20;
            }

            int ipx = (int) (playerX / 64.0);
            int ipx_add_xo = (int) ((playerX + xOffset) / 64.0);
            int ipx_sub_xo = (int) ((playerX - xOffset) / 64.0);
            int ipy = (int) (playerY / 64.0);
            int ipy_add_yo = (int) ((playerY + yOffset) / 64.0);
            int ipy_sub_yo = (int) ((playerY - yOffset) / 64.0);

            if (Input.isKeyDown(KeyCodes.W))                                                                  //move forward
            {
                if (MAP_W[ipy * MAP_X + ipx_add_xo] == 0) {
                    playerX += playerDeltaX * 0.2 * fps;
                }
                if (MAP_W[ipy_add_yo * MAP_X + ipx] == 0) {
                    playerY += playerDeltaY * 0.2 * fps;
                }
            }

            if (Input.isKeyDown(KeyCodes.S))                                                                  //move backward
            {
                if (MAP_W[ipy * MAP_X + ipx_sub_xo] == 0) {
                    playerX -= playerDeltaX * 0.2 * fps;
                }
                if (MAP_W[ipy_sub_yo * MAP_X + ipx] == 0) {
                    playerY -= playerDeltaY * 0.2 * fps;
                }
            }

            if (Input.isKeyDown(KeyCodes.E) && spriteArray[0].state == 0)             //open doors
            {
                if (playerDeltaX < 0) {
                    xOffset = -25;
                } else {
                    xOffset = 25;
                }
                if (playerDeltaY < 0) {
                    yOffset = -25;
                } else {
                    yOffset = 25;
                }
                ipx_add_xo = (int) ((playerX + xOffset) / 64.0);
                ipy_add_yo = (int) ((playerY + yOffset) / 64.0);
                if (MAP_W[ipy_add_yo * MAP_X + ipx_add_xo] == 4) {
                    MAP_W[ipy_add_yo * MAP_X + ipx_add_xo] = 0;
                }
            }
            drawSky();
            drawRays2D();
            drawSprite();

            if ((int) playerX >> 6 == 1 && (int) playerY >> 6 == 1) {
                fade = 0;
                timer = 0;
                gameState = 3;
            }
        }

        //Victory
        if (gameState == 3) {
            screen(2);
            timer += 1 * fps;
            if (timer > 2000) {
                fade = 0;
                timer = 0;
                gameState = 0;
            }
        }

        //Loss
        if (gameState == 4) {
            screen(3);
            timer += 1 * fps;
            if (timer > 2000) {
                fade = 0;
                timer = 0;
                gameState = 0;
            }
        }
    }

    @Override
    public void onUIRender() {

    }

    private void drawSprite() {
        int x;
        int y;
        int s;

        //Get key
        if (playerX < spriteArray[0].x + 30 && playerX > spriteArray[0].x - 30 && playerY < spriteArray[0].y + 30 && playerY > spriteArray[0].y - 30) {
            spriteArray[0].state = 0;
        }

        //Lose game
        if (playerX < spriteArray[3].x + 30 && playerX > spriteArray[3].x - 30 && playerY < spriteArray[3].y + 30 && playerY > spriteArray[3].y - 30) {
            gameState = 4;
        }

        //Enemy attack
        int spritesX = (int) spriteArray[3].x >> 6, spritesY = (int) spriteArray[3].y >> 6;
        int spritesX_add = ((int) spriteArray[3].x + 15) >> 6, spritesY_add = ((int) spriteArray[3].y + 15) >> 6;
        int spritesX_sub = ((int) spriteArray[3].x - 15) >> 6, spritesY_sub = ((int) spriteArray[3].y - 15) >> 6;

        if (spriteArray[3].x > playerX && MAP_W[spritesY * 8 + spritesX_sub] == 0) {
            spriteArray[3].x -= 0.04 * fps;
        }

        if (spriteArray[3].x < playerX && MAP_W[spritesY * 8 + spritesX_add] == 0) {
            spriteArray[3].x += 0.04 * fps;
        }

        if (spriteArray[3].y > playerY && MAP_W[spritesY_sub * 8 + spritesX] == 0) {
            spriteArray[3].y -= 0.04 * fps;
        }

        if (spriteArray[3].y < playerY && MAP_W[spritesY_add * 8 + spritesX] == 0) {
            spriteArray[3].y += 0.04 * fps;
        }

        for (s = 0; s < 4; s++) {
            float sx = spriteArray[s].x - playerX;
            float sy = spriteArray[s].y - playerY;
            float sz = spriteArray[s].z;

            float cosAngle = cos(degreesToRadians(playerAngle));
            float sinAngle = sin(degreesToRadians(playerAngle));
            float a = sy * cosAngle + sx * sinAngle;
            float b = sx * cosAngle - sy * sinAngle;
            sx = a;
            sy = b;

            sx = (sx * 108.0f / sy) + (120 / 2); //convert to screen x,y
            sy = (sz * 108.0f / sy) + (80 / 2);

            int scale = (int) (32 * 80 / b);   //scale sprite based on distance

            if (scale < 0) {
                scale = 0;
            }
            if (scale > 120) {
                scale = 120;
            }

            //Texture
            float t_x = 0;
            float t_y = 31;
            float t_x_step = 31.5f / (float) scale;
            float t_y_step = 32.0f / (float) scale;

            for (x = (int) (sx - scale / 2); x < sx + scale / 2.0f; x++) {
                t_y = 31;
                for (y = 0; y < scale; y++) {
                    if (spriteArray[s].state == 1 && x > 0 && x < 120 && b < depth[x]) {
                        int pixel = ((int) t_y * 32 + (int) t_x) * 3 + (spriteArray[s].map * 32 * 32 * 3);
                        int red = sprites[pixel + 0];
                        int green = sprites[pixel + 1];
                        int blue = sprites[pixel + 2];

                        if (red != 255 && green != 0 && blue != 255) //Skip transparency (purple)
                        {
                            glPointSize(8);
                            glColor3ub((byte) red, (byte) green, (byte) blue);
                            glBegin(GL_POINTS);
                            glVertex2i(x * 8, (int) (sy * 8 - y * 8));
                            glEnd();
                        }

                        t_y -= t_y_step;

                        if (t_y < 0) {
                            t_y = 0;
                        }
                    }
                }
                t_x += t_x_step;
            }
        }
    }

    private void drawRays2D() {
        int ray;
        int mapX;
        int mapY;
        int mapPosition;
        int depthOfField;
        float vx;
        float vy;
        float rayX;
        float rayY;
        float rayAngle;
        float xOffset = 0;
        float yOffset = 0;
        float distanceVertical;
        float distanceHorizontal;

        rayAngle = fixAngle(playerAngle + 30);

        for (ray = 0; ray < 120; ray++) {
            int verticalMapTexture = 0;
            int horizontalMapTexture = 0;

            //Vertical
            depthOfField = 0;
            distanceVertical = 100000;
            float tan = tan(degreesToRadians(rayAngle));

            if (cos(degreesToRadians(rayAngle)) > 0.001) {
                //Look left
                rayX = (((int) playerX >> 6) << 6) + 64;
                rayY = (playerX - rayX) * tan + playerY;
                xOffset = 64;
                yOffset = -xOffset * tan;
            } else if (cos(degreesToRadians(rayAngle)) < -0.001) {
                //Look right
                rayX = (((int) playerX >> 6) << 6) - 0.0001f;
                rayY = (playerX - rayX) * tan + playerY;
                xOffset = -64;
                yOffset = -xOffset * tan;
            } else {
                //Look up or down
                rayX = playerX;
                rayY = playerY;
                depthOfField = 8;
            }

            while (depthOfField < 8) {
                mapX = (int) (rayX) >> 6;
                mapY = (int) (rayY) >> 6;
                mapPosition = mapY * MAP_X + mapX;

                if (mapPosition > 0 && mapPosition < MAP_X * MAP_Y && MAP_W[mapPosition] > 0) {
                    verticalMapTexture = MAP_W[mapPosition] - 1;
                    depthOfField = 8;
                    distanceVertical = cos(degreesToRadians(rayAngle)) * (rayX - playerX) - sin(degreesToRadians(rayAngle)) * (rayY - playerY);
                } else {
                    rayX += xOffset;
                    rayY += yOffset;
                    depthOfField += 1;
                }
            }

            vx = rayX;
            vy = rayY;

            //Horizontal
            depthOfField = 0;
            distanceHorizontal = 100000;
            tan = 1.0f / tan;

            if (sin(degreesToRadians(rayAngle)) > 0.001) {
                //Look up
                rayY = (((int) playerY >> 6) << 6) - 0.0001f;
                rayX = (playerY - rayY) * tan + playerX;
                yOffset = -64;
                xOffset = -yOffset * tan;
            } else if (sin(degreesToRadians(rayAngle)) < -0.001) {
                //Look down
                rayY = (((int) playerY >> 6) << 6) + 64;
                rayX = (playerY - rayY) * tan + playerX;
                yOffset = 64;
                xOffset = -yOffset * tan;
            } else {
                //Look left or right
                rayX = playerX;
                rayY = playerY;
                depthOfField = 8;
            }

            while (depthOfField < 8) {
                mapX = (int) (rayX) >> 6;
                mapY = (int) (rayY) >> 6;
                mapPosition = mapY * MAP_X + mapX;

                if (mapPosition > 0 && mapPosition < MAP_X * MAP_Y && MAP_W[mapPosition] > 0) {
                    horizontalMapTexture = MAP_W[mapPosition] - 1;
                    depthOfField = 8;
                    distanceHorizontal = cos(degreesToRadians(rayAngle)) * (rayX - playerX) - sin(degreesToRadians(rayAngle)) * (rayY - playerY);
                } else {
                    rayX += xOffset;
                    rayY += yOffset;
                    depthOfField += 1;
                }
            }

            float shade = 1;
            glColor3f(0, 0.8f, 0);

            if (distanceVertical < distanceHorizontal) {
                horizontalMapTexture = verticalMapTexture;
                shade = 0.5f;
                rayX = vx;
                rayY = vy;
                distanceHorizontal = distanceVertical;
                glColor3f(0, 0.6f, 0);
            }

            int ca = (int) fixAngle(playerAngle - rayAngle);

            //Fix fisheye
            distanceHorizontal = distanceHorizontal * cos(degreesToRadians(ca));
            int lineHeight = (int) ((MAP_SIZE * 640) / (distanceHorizontal));
            float ty_step = 32.0f / (float) lineHeight;
            float ty_off = 0;

            if (lineHeight > 640) {
                ty_off = (lineHeight - 640) / 2.0f;
                lineHeight = 640;
            }

            int lineOffset = 320 - (lineHeight >> 1);

            depth[ray] = (int) distanceHorizontal;

            //Walls
            int y;
            float ty = ty_off * ty_step;
            float tx;

            if (shade == 1) {
                tx = (int) (rayX / 2.0) % 32;
                if (rayAngle > 180) {
                    tx = 31 - tx;
                }
            } else {
                tx = (int) (rayY / 2.0) % 32;
                if (rayAngle > 90 && rayAngle < 270) {
                    tx = 31 - tx;
                }
            }

            for (y = 0; y < lineHeight; y++) {
                int pixel = ((int) ty * 32 + (int) tx) * 3 + (horizontalMapTexture * 32 * 32 * 3);
                int red = (int) (allTextures[pixel + 0] * shade);
                int green = (int) (allTextures[pixel + 1] * shade);
                int blue = (int) (allTextures[pixel + 2] * shade);
                glPointSize(8);
                glColor3ub((byte) red, (byte) green, (byte) blue);
                glBegin(GL_POINTS);
                glVertex2i(ray * 8, y + lineOffset);
                glEnd();
                ty += ty_step;
            }

            //Floor
            for (y = lineOffset + lineHeight; y < 640; y++) {
                float dy = y - (640 / 2.0f), deg = degreesToRadians(rayAngle), raFix = cos(degreesToRadians(fixAngle(playerAngle - rayAngle)));
                tx = playerX / 2 + cos(deg) * 158 * 2 * 32 / dy / raFix;
                ty = playerY / 2 - sin(deg) * 158 * 2 * 32 / dy / raFix;
                int mp = MAP_F[(int) (ty / 32.0) * MAP_X + (int) (tx / 32.0)] * 32 * 32;
                int pixel = (((int) (ty) & 31) * 32 + ((int) (tx) & 31)) * 3 + mp * 3;
                int red = (int) (allTextures[pixel + 0] * 0.7);
                int green = (int) (allTextures[pixel + 1] * 0.7);
                int blue = (int) (allTextures[pixel + 2] * 0.7);
                glPointSize(8);
                glColor3ub((byte) red, (byte) green, (byte) blue);
                glBegin(GL_POINTS);
                glVertex2i(ray * 8, y);
                glEnd();

                //Ceiling
                mp = MAP_C[(int) (ty / 32.0) * MAP_X + (int) (tx / 32.0)] * 32 * 32;
                pixel = (((int) (ty) & 31) * 32 + ((int) (tx) & 31)) * 3 + mp * 3;
                red = allTextures[pixel + 0];
                green = allTextures[pixel + 1];
                blue = allTextures[pixel + 2];

                if (mp > 0) {
                    glPointSize(8);
                    glColor3ub((byte) red, (byte) green, (byte) blue);
                    glBegin(GL_POINTS);
                    glVertex2i(ray * 8, 640 - y);
                    glEnd();
                }
            }

            rayAngle = fixAngle(rayAngle - 0.5f);
        }
    }

    private void drawSky()
    {
        int x, y;
        for (y = 0; y < 40; y++) {
            for (x = 0; x < 120; x++) {
                int xo = (int) playerAngle * 2 - x;

                if (xo < 0) {
                    xo += 120;
                }

                xo = xo % 120; //return 0-120 based on player angle
                int pixel = (y * 120 + xo) * 3;
                int red = sky[pixel + 0];
                int green = sky[pixel + 1];
                int blue = sky[pixel + 2];

                glPointSize(8);
                glColor3ub((byte) red, (byte) green, (byte) blue);
                glBegin(GL_POINTS);
                glVertex2i(x * 8, y * 8);
                glEnd();
            }
        }
    }

    void screen(int v)
    {
        int x;
        int y;
        int[] t = {};

        if (v == 1) {
            t = title;
        }

        if (v == 2) {
            t = won;
        }

        if (v == 3) {
            t = lost;
        }

        for (y = 0; y < 80; y++) {
            for (x = 0; x < 120; x++) {
                int pixel = (y * 120 + x) * 3;
                int red = (int) (t[pixel + 0] * fade);
                int green = (int) (t[pixel + 1] * fade);
                int blue = (int) (t[pixel + 2] * fade);

                glPointSize(8);
                glColor3ub((byte) red, (byte) green, (byte) blue);
                glBegin(GL_POINTS);
                glVertex2i(x * 8, y * 8);
                glEnd();
            }
        }

        if (fade < 1) {
            fade += 0.001 * fps;
        }

        if (fade > 1) {
            fade = 1;
        }
    }

    private float degreesToRadians(float degree) {
        return (float) (degree * PI / 180.0f);
    }

    private float fixAngle(float angle) {
        if (angle > 359) {
            angle -= 360;
        }

        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    private static int[] loadArray(String path) {
        try {
            return Files.readAllLines(Path.of(path)).stream().mapToInt(Integer::parseInt).toArray();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
