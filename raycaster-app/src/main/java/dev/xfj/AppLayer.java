package dev.xfj;

import org.lwjgl.opengl.GL41;

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
        GL41.glClearColor(0.3f, 0.3f, 0.3f, 0);
        GL41.glOrtho(0, 1024, 510, 0, -1, 1);
    }

    @Override
    public void onDetach() {
        System.out.println("Not implemented!");
    }

    @Override
    public void onUpdate(float ts) {
        GL41.glClear(GL41.GL_COLOR_BUFFER_BIT | GL41.GL_DEPTH_BUFFER_BIT);
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
                    GL41.glColor3f(1, 1, 1);
                } else {
                    GL41.glColor3f(0, 0, 0);
                }
                xo = x * MAP_S;
                yo = y * MAP_S;
                GL41.glBegin(GL41.GL_QUADS);
                GL41.glVertex2i(0 + xo + 1, 0 + yo + 1);
                GL41.glVertex2i(0 + xo + 1, MAP_S + yo - 1);
                GL41.glVertex2i(MAP_S + xo - 1, MAP_S + yo - 1);
                GL41.glVertex2i(MAP_S + xo - 1, 0 + yo + 1);
                GL41.glEnd();
            }
        }
    }
}
