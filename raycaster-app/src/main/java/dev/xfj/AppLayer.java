package dev.xfj;

import org.lwjgl.opengl.GL45;

import static org.lwjgl.opengl.GL11.*;

public class AppLayer implements Layer {

    private static final int MAP_X = 8;
    private static final int MAP_Y = 8;
    private static final int MAP_S = 64;
    private static final int[] MAP = new int[]{
            1, 1, 1, 1, 1, 1, 1, 1,
            1, 0, 1, 0, 0, 0, 0, 1,
            1, 0, 1, 0, 0, 0, 0, 1,
            1, 0, 1, 0, 0, 0, 0, 1,
            1, 0, 0, 0, 0, 0, 0, 1,
            1, 0, 0, 0, 0, 1, 0, 1,
            1, 0, 0, 0, 0, 0, 0, 1,
            1, 1, 1, 1, 1, 1, 1, 1};

    @Override
    public void onAttach() {
        GL45.glClearColor(0.3f,0.3f,0.3f,0);
        GL45.glOrtho(0, 1024, 510, 0, -1, 1);
    }

    @Override
    public void onDetach() {
        System.out.println("Not implemented!");
    }

    @Override
    public void onUpdate(float ts) {
        GL45.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        drawMap2D();
    }

    @Override
    public void onUIRender() {

    }
    private void drawMap2D() {
        int x, y, xo, yo;
        for (y = 0; y < MAP_Y; y++) {
            for (x = 0; x < MAP_X; x++) {
                if (MAP[y * MAP_X + x] == 1) {
                    glColor3f(1, 1, 1);
                } else {
                    glColor3f(0, 0, 0);
                }
                xo = x * MAP_S;
                yo = y * MAP_S;
                glBegin(GL_QUADS);
                glVertex2i(0 + xo + 1, 0 + yo + 1);
                glVertex2i(0 + xo + 1, MAP_S + yo - 1);
                glVertex2i(MAP_S + xo - 1, MAP_S + yo - 1);
                glVertex2i(MAP_S + xo - 1, 0 + yo + 1);
                glEnd();
            }
        }
    }
}
