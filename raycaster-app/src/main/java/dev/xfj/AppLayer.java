package dev.xfj;

import dev.xfj.events.Event;
import dev.xfj.events.EventDispatcher;
import dev.xfj.events.key.KeyPressedEvent;
import dev.xfj.input.KeyCodes;
import org.joml.Math;
import org.lwjgl.opengl.GL41;

import static org.joml.Math.*;

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

    private float px;
    private float py;
    private float pdx;
    private float pdy;
    private float pa;

    @Override
    public void onAttach() {
        GL41.glClearColor(0.3f, 0.3f, 0.3f, 0);
        GL41.glOrtho(0, 1024, 510, 0, -1, 1);

        px = 150;
        py = 400;
        pa = 90;
        pdx = cos(degToRad((int) pa));
        pdy = -sin(degToRad((int) pa));
    }

    @Override
    public void onDetach() {
        System.out.println("Not implemented!");
    }

    @Override
    public void onUpdate(float ts) {
        drawMap2D();
        drawPlayer2D();
        drawRays2D();
    }

    @Override
    public void onUIRender() {

    }

    @Override
    public void onEvent(Event event) {
        EventDispatcher eventDispatcher = new EventDispatcher(event);
        eventDispatcher.dispatch(KeyPressedEvent.class, this::onKeyPressed);
    }

    private boolean onKeyPressed(KeyPressedEvent event) {
        switch (event.getKeyCode()) {
            case KeyCodes.A -> {
                pa += 5;
                pa = fixAng((int) pa);
                pdx = cos(degToRad((int) pa));
                pdy = -sin(degToRad((int) pa));
            }
            case KeyCodes.D -> {
                pa -= 5;
                pa = fixAng((int) pa);
                pdx = cos(degToRad((int) pa));
                pdy = -sin(degToRad((int) pa));
            }
            case KeyCodes.W -> {
                px += pdx * 5;
                py += pdy * 5;
            }
            case KeyCodes.S -> {
                px -= pdx * 5;
                py -= pdy * 5;
            }
        }
        return false;
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

    private float degToRad(int a) {
        return Math.toRadians(a);
    }

    private int fixAng(int a) {
        if (a > 359) {
            a -= 360;
        }
        if (a < 0) {
            a += 360;
        }
        return a;
    }

    private void drawPlayer2D() {
        GL41.glColor3f(1, 1, 0);
        GL41.glPointSize(8);
        GL41.glLineWidth(4);
        GL41.glBegin(GL41.GL_POINTS);
        GL41.glVertex2i((int) px, (int) py);
        GL41.glEnd();

        GL41.glBegin(GL41.GL_LINES);
        GL41.glVertex2i((int) px, (int) py);
        GL41.glVertex2i((int) (px + pdx * 20), (int) (py + pdy * 20));
        GL41.glEnd();
    }

    private void drawRays2D() {
        GL41.glColor3f(0, 1, 1);
        GL41.glBegin(GL41.GL_QUADS);
        GL41.glVertex2i(526, 0);
        GL41.glVertex2i(1006, 0);
        GL41.glVertex2i(1006, 160);
        GL41.glVertex2i(526, 160);
        GL41.glEnd();

        GL41.glColor3f(0, 0, 1);
        GL41.glBegin(GL41.GL_QUADS);
        GL41.glVertex2i(526, 160);
        GL41.glVertex2i(1006, 160);
        GL41.glVertex2i(1006, 320);
        GL41.glVertex2i(526, 320);
        GL41.glEnd();

        int r = 0;
        int mx = 0;
        int my = 0;
        int mp = 0;
        int dof = 0;
        int side = 0;

        float vx = 0.0f;
        float vy = 0.0f;
        float rx = 0.0f;
        float ry = 0.0f;
        float ra = 0.0f;
        float xo = 0.0f;
        float yo = 0.0f;
        float disV = 0.0f;
        float disH = 0.0f;

        ra = fixAng((int) (pa + 30));                                                              //ray set back 30 degrees

        for (r = 0; r < 60; r++) {
            //---Vertical---
            dof = 0;
            side = 0;
            disV = 100000;
            float Tan = tan(degToRad((int) ra));

            if (cos(degToRad((int) ra)) > 0.001) {
                rx = (((int) px >> 6) << 6) + 64;
                ry = (px - rx) * Tan + py;
                xo = 64;
                yo = -xo * Tan;
            }//looking left
            else if (cos(degToRad((int) ra)) < -0.001) {
                rx = (float) ((((int) px >> 6) << 6) - 0.0001);
                ry = (px - rx) * Tan + py;
                xo = -64;
                yo = -xo * Tan;
            }//looking right
            else {
                rx = px;
                ry = py;
                dof = 8;
            }                                                  //looking up or down. no hit

            while (dof < 8) {
                mx = (int) (rx) >> 6;
                my = (int) (ry) >> 6;
                mp = my * MAP_X + mx;

                if (mp > 0 && mp < MAP_X * MAP_Y && MAP[mp] == 1) {
                    dof = 8;
                    disV = cos(degToRad((int) ra)) * (rx - px) - sin(degToRad((int) ra)) * (ry - py);
                }//hit
                else {
                    rx += xo;
                    ry += yo;
                    dof += 1;
                }                                               //check next horizontal
            }

            vx = rx;
            vy = ry;

            //---Horizontal---
            dof = 0;
            disH = 100000;
            Tan = (float) (1.0 / Tan);

            if (sin(degToRad((int) ra)) > 0.001) {
                ry = (float) ((((int) py >> 6) << 6) - 0.0001);
                rx = (py - ry) * Tan + px;
                yo = -64;
                xo = -yo * Tan;
            }//looking up
            else if (sin(degToRad((int) ra)) < -0.001) {
                ry = (((int) py >> 6) << 6) + 64;
                rx = (py - ry) * Tan + px;
                yo = 64;
                xo = -yo * Tan;
            }//looking down
            else {
                rx = px;
                ry = py;
                dof = 8;
            }                                                   //looking straight left or right

            while (dof < 8) {
                mx = (int) (rx) >> 6;
                my = (int) (ry) >> 6;
                mp = my * MAP_X + mx;
                if (mp > 0 && mp < MAP_X * MAP_Y && MAP[mp] == 1) {
                    dof = 8;
                    disH = cos(degToRad((int) ra)) * (rx - px) - sin(degToRad((int) ra)) * (ry - py);
                }//hit
                else {
                    rx += xo;
                    ry += yo;
                    dof += 1;
                }                                               //check next horizontal
            }

            GL41.glColor3f(0.0f, 0.8f, 0);

            if (disV < disH) {
                rx = vx;
                ry = vy;
                disH = disV;
                GL41.glColor3f(0.0f, 0.6f, 0);
            }                  //horizontal hit first

            GL41.glLineWidth(2);
            GL41.glBegin(GL41.GL_LINES);
            GL41.glVertex2i((int) px, (int) py);
            GL41.glVertex2i((int) rx, (int) ry);
            GL41.glEnd();//draw 2D ray

            int ca = fixAng((int) (pa - ra));
            disH = disH * cos(degToRad(ca));                            //fix fisheye
            int lineH = (int) ((MAP_S * 320) / (disH));

            if (lineH > 320) {
                lineH = 320;
            }                     //line height and limit

            int lineOff = 160 - (lineH >> 1);                                               //line offset

            GL41.glLineWidth(8);
            GL41.glBegin(GL41.GL_LINES);
            GL41.glVertex2i(r * 8 + 530, lineOff);
            GL41.glVertex2i(r * 8 + 530, lineOff + lineH);
            GL41.glEnd();//draw vertical wall

            ra = fixAng((int) (ra - 1));                                                              //go to next ray
        }
    }
}
