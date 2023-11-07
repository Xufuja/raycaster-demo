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
    private static final double PI_DIVIDED_2 = PI / 2;
    private static final double PI_DIVIDED_2_x3 = 3 * PI / 2;
    private static final float DEGREE_IN_RADIANS = (float) Math.toRadians(1);
    private static final int[] MAP = new int[]{
            1, 1, 1, 1, 1, 1, 1, 1,
            1, 0, 1, 0, 0, 0, 0, 1,
            1, 0, 1, 0, 0, 0, 0, 1,
            1, 0, 1, 0, 0, 0, 0, 1,
            1, 0, 0, 0, 0, 0, 0, 1,
            1, 0, 0, 0, 0, 1, 0, 1,
            1, 0, 0, 0, 0, 0, 0, 1,
            1, 1, 1, 1, 1, 1, 1, 1};
    private float playerX;
    private float playerY;
    private float playerDeltaX;
    private float playerDeltaY;
    private float playerAngle;

    @Override
    public void onAttach() {
        init();
    }

    private void init() {
        glClearColor(0.3f, 0.3f, 0.3f, 0.0f);
        glOrtho(0, Application.getInstance().getSpecification().width, Application.getInstance().getSpecification().height, 0, -1, 1);

        playerX = 300.0f;
        playerY = 300.0f;
        playerDeltaX = cos(playerAngle) * 5;
        playerDeltaY = sin(playerAngle) * 5;
    }

    @Override
    public void onDetach() {
        System.out.println("Not implemented!");
    }

    @Override
    public void onUpdate(float ts) {
        drawMap2D();
        drawPlayer();
        drawRays3D();

        if (Input.isKeyDown(KeyCodes.A)) {
            playerAngle -= 0.1f;

            if (playerAngle < 0) {
                playerAngle += 2 * PI;
            }

            playerDeltaX = cos(playerAngle) * 5;
            playerDeltaY = sin(playerAngle) * 5;
        }

        if (Input.isKeyDown(KeyCodes.D)) {
            playerAngle -= 0.1f;

            if (playerAngle > 2 * PI) {
                playerAngle -= 2 * PI;
            }

            playerDeltaX = cos(playerAngle) * 5;
            playerDeltaY = sin(playerAngle) * 5;
        }

        if (Input.isKeyDown(KeyCodes.W)) {
            playerX += playerDeltaX;
            playerY += playerDeltaY;
        }

        if (Input.isKeyDown(KeyCodes.S)) {
            playerX -= playerDeltaX;
            playerY -= playerDeltaY;
        }
    }

    @Override
    public void onUIRender() {

    }

    private void drawPlayer() {
        glColor3f(1.0f, 1.0f, 0.0f);
        glPointSize(8.0f);
        glBegin(GL_POINT);
        glVertex2i((int) playerX, (int) playerY);
        glEnd();

        glLineWidth(3.0f);
        glBegin(GL_LINES);
        glVertex2i((int) playerX, (int) playerY);
        glVertex2i((int) (playerX + playerDeltaX), (int) (playerY + playerDeltaY));
        glEnd();

    }

    private void drawMap2D() {
        int xOffset = 0;
        int yOffset = 0;

        for (int y = 0; y < MAP_Y; y++) {
            for (int x = 0; x < MAP_X; x++) {
                if (MAP[y * MAP_X + x] == 1) {
                    glColor3f(1.0f, 1.0f, 1.0f);
                } else {
                    glColor3f(0.0f, 0.0f, 0.0f);
                }

                xOffset = x * MAP_SIZE;
                yOffset = y * MAP_SIZE;

                glBegin(GL_QUADS);
                //The + 1 and -1 add the outlines around the cubes
                glVertex2i(xOffset + 1, yOffset + 1);
                glVertex2i(xOffset + 1, yOffset + MAP_SIZE - 1);
                glVertex2i(xOffset + MAP_SIZE - 1, yOffset + MAP_SIZE - 1);
                glVertex2i(xOffset + MAP_SIZE - 1, yOffset + 1);
                glEnd();
            }
        }
    }

    private void drawRays3D() {
        int rays = 0;
        int mapX = 0;
        int mapY = 0;
        int mapPosition = 0;
        int depthOfField = 0;

        float rayX = 0.0f;
        float rayY = 0.0f;
        float rayAngle = playerAngle - DEGREE_IN_RADIANS * 30;
        float xOffset = 0.0f;
        float yOffset = 0.0f;
        float distance = 0.0f;

        if (rayAngle < 0) {
            rayAngle += 2 * PI;
        }

        if (rayAngle > 2 * PI) {
            rayAngle -= 2 * PI;
        }

        for (rays = 0; rays < 60; rays++) {
            //Check horizontal grid line
            float distanceHorizontal = 1000000.0f;
            float horizontalX = playerX;
            float horizontalY = playerY;
            float aTan = -1 / tan(rayAngle); //Negative inverse of tangent

            if (rayAngle > PI) { //Looking up
                rayY = (((int) playerY >> 6) << 6) - 0.0001f;
                rayX = (playerY - rayY) * aTan + playerX;
                yOffset = -64;
                xOffset = -yOffset * aTan;
            }

            if (rayAngle < PI) { //Looking down
                rayY = (((int) playerY >> 6) << 6) + 64;
                rayX = (playerY - rayY) * aTan + playerX;
                yOffset = 64;
                xOffset = -yOffset * aTan;
            }

            if (rayAngle == 0 || rayAngle == PI) { //Looking left or right
                rayX = playerX;
                rayY = playerY;
                depthOfField = 8;
            }

            while (depthOfField < 8) {
                mapX = (int) rayX >> 6;
                mapY = (int) rayY >> 6;
                mapPosition = mapY * MAP_X + mapX;

                if (mapPosition > 0 && mapPosition < MAP_X * MAP_Y && MAP[mapPosition] > 0) { //Hit a wall
                    horizontalX = rayX;
                    horizontalY = rayY;
                    distanceHorizontal = distance(playerX, playerY, horizontalX, horizontalY, rayAngle);
                    depthOfField = 8;
                } else { //Next line
                    rayX += xOffset;
                    rayY += yOffset;
                    depthOfField += 1;
                }
            }

            //Check vertical grid line
            float distanceVertical = 1000000.0f;
            float verticalX = playerX;
            float verticalY = playerY;
            depthOfField = 0;

            float nTan = -tan(rayAngle); //Negative tangent

            if (rayAngle > PI_DIVIDED_2 && rayAngle < PI_DIVIDED_2_x3) { //Looking left
                rayX = (((int) playerX >> 6) << 6) - 0.0001f;
                rayY = (playerX - rayX) * nTan + playerY;
                xOffset = -64;
                yOffset = -xOffset * nTan;
            }

            if (rayAngle < PI_DIVIDED_2 || rayAngle > PI_DIVIDED_2_x3) { //Looking right
                rayX = (((int) playerX >> 6) << 6) + 64;
                rayY = (playerX - rayX) * nTan + playerY;
                xOffset = 64;
                yOffset = -xOffset * nTan;
            }

            if (rayAngle == 0 || rayAngle == PI) { //Looking up or down
                rayX = playerX;
                rayY = playerY;
                depthOfField = 8;
            }

            while (depthOfField < 8) {
                mapX = (int) rayX >> 6;
                mapY = (int) rayY >> 6;
                mapPosition = mapY * MAP_X + mapX;

                if (mapPosition > 0 && mapPosition < MAP_X * MAP_Y && MAP[mapPosition] > 0) { //Hit a wall
                    verticalX = rayX;
                    verticalY = rayY;
                    distanceVertical = distance(playerX, playerY, verticalX, verticalY, rayAngle);
                    depthOfField = 8;
                } else { //Next line
                    rayX += xOffset;
                    rayY += yOffset;
                    depthOfField += 1;
                }
            }

            if (distanceVertical < distanceHorizontal) { //Vertical wall hit
                rayX = verticalX;
                rayY = verticalY;
                distance = distanceVertical;
                glColor3f(0.9f, 0.0f, 0.0f);
            }

            if (distanceHorizontal < distanceVertical) { //Horizontal wall hit
                rayX = horizontalX;
                rayY = horizontalY;
                distance = distanceHorizontal;
                glColor3f(0.7f, 0.0f, 0.0f);
            }

            glLineWidth(1.0f);
            glBegin(GL_LINES);
            glVertex2i((int) playerX, (int) playerY);
            glVertex2i((int) rayX, (int) rayY);
            glEnd();

            //Draw walls
            float angleDifference = playerAngle - rayAngle;

            if (angleDifference < 0) {
                angleDifference += 2 * PI;
            }

            if (angleDifference > 2 * PI) {
                angleDifference -= 2 * PI;
            }

            distance = distance * cos(angleDifference); //Fixes fisheye effect

            float lineHeight = (MAP_SIZE * 320) / distance;

            if (lineHeight > 320) {
                lineHeight = 320;
            }

            float lineOffset = 160 - lineHeight / 2;

            glLineWidth(8.0f);
            glBegin(GL_LINES);
            glVertex2i(rays * 8 + 530, (int) lineOffset);
            glVertex2i(rays * 8 + 530, (int) (lineHeight + lineOffset));
            glEnd();

            rayAngle += DEGREE_IN_RADIANS;

            if (rayAngle < 0) {
                rayAngle += 2 * PI;
            }

            if (rayAngle > 2 * PI) {
                rayAngle -= 2 * PI;
            }
        }
    }

    float distance(float ax, float ay, float bx, float by, float angle) {
        return sqrt((bx - ax) * (bx - ax) + (by - ay) * (by - ay));
    }

    private static int[] loadArray(String path) {
        try {
            return Files.readAllLines(Path.of(path)).stream().mapToInt(Integer::parseInt).toArray();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

}
