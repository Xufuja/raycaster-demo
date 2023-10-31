package dev.xfj;

import dev.xfj.events.Event;
import dev.xfj.events.EventDispatcher;
import dev.xfj.events.key.KeyPressedEvent;
import dev.xfj.input.KeyCodes;
import org.joml.Math;
import org.lwjgl.opengl.GL41;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.joml.Math.*;

public class AppLayer implements Layer {

    private static final int MAP_X = 8;
    private static final int MAP_Y = 8;
    private static final int MAP_S = 64;

    private static final int[] mapW = {      //walls
            1, 1, 1, 1, 2, 2, 2, 2,
            6, 0, 0, 1, 0, 0, 0, 2,
            1, 0, 0, 4, 0, 2, 0, 2,
            1, 5, 4, 5, 0, 0, 0, 2,
            2, 0, 0, 0, 0, 0, 0, 1,
            2, 0, 0, 0, 0, 1, 0, 1,
            2, 0, 0, 0, 0, 0, 0, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
    };

    private static final int[] mapF = {     //floors
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 2, 2, 2, 0,
            0, 0, 0, 0, 6, 0, 2, 0,
            0, 0, 8, 0, 2, 7, 6, 0,
            0, 0, 2, 0, 0, 0, 0, 0,
            0, 0, 2, 0, 8, 0, 0, 0,
            0, 1, 1, 1, 1, 0, 8, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
    };

    private static final int[] mapC = {        //ceiling
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 4, 2, 4, 0, 0, 0, 0,
            0, 0, 2, 0, 0, 0, 0, 0,
            0, 0, 2, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0,
    };

    private static final int[] allTextures = loadArray("textures\\allTextures.txt");
    private static final int[] lost = loadArray("textures\\lost.txt");
    private static final int[] sky = loadArray("textures\\sky.txt");
    private static final int[] sprites = loadArray("textures\\sprites.txt");
    private static final int[] title = loadArray("textures\\title.txt");
    private static final int[] won = loadArray("textures\\won.txt");

    private float px;
    private float py;
    private float pdx;
    private float pdy;
    private float pa;
    private float fps;
    private int gameState = 0;
    private int timer = 0; //game state. init, start screen, game loop, win/lose
    private float fade = 0;             //the 3 screens can fade up from black
    private Sprite[] sp = {new Sprite(), new Sprite(), new Sprite(), new Sprite()};
    private int[] depth = new int[120];

    @Override
    public void onAttach() {
        init();
    }

    private void init() {
        GL41.glClearColor(0.3f, 0.3f, 0.3f, 0);
        GL41.glOrtho(0, 960, 640, 0, -1, 1);

        px = 150;
        py = 400;
        pa = 90;
        pdx = cos(degToRad((int) pa));
        pdy = -sin(degToRad((int) pa));                                 //init player
        mapW[19] = 4;
        mapW[26] = 4; //close doors

        sp[0].type = 1;
        sp[0].state = 1;
        sp[0].map = 0;
        sp[0].x = 1.5f * 64;
        sp[0].y = 5.0f * 64;
        sp[0].z = 20; //key
        sp[1].type = 2;
        sp[1].state = 1;
        sp[1].map = 1;
        sp[1].x = 1.5f * 64;
        sp[1].y = 4.5f * 64;
        sp[1].z = 0; //light 1
        sp[2].type = 2;
        sp[2].state = 1;
        sp[2].map = 1;
        sp[2].x = 3.5f * 64;
        sp[2].y = 4.5f * 64;
        sp[2].z = 0; //light 2
        sp[3].type = 3;
        sp[3].state = 1;
        sp[3].map = 2;
        sp[3].x = 2.5f * 64;
        sp[3].y = 2 * 64;
        sp[3].z = 20; //enemy
    }

    @Override
    public void onDetach() {
        System.out.println("Not implemented!");
    }

    @Override
    public void onUpdate(float ts) {
        fps = ts;
        gameState = 2;
        drawSky();
        drawRays2D();
        drawSprite();
        if ((int) px >> 6 == 1 && (int) py >> 6 == 1) {
            fade = 0;
            timer = 0;
            gameState = 3;
        } //Entered block 1, Win game!!
        /*if (gameState == 0) {
            init();
            fade = 0;
            timer = 0;
            gameState = 1;
        } //init game
        if (gameState == 1) {
            screen(1);
            timer += 1 * fps;
            if (timer > 2000) {
                fade = 0;
                timer = 0;
                gameState = 2;
            }
        } //start screen
        if (gameState == 2) //The main game loop
        {
            drawSky();
            drawRays2D();
            drawSprite();
            if ((int) px >> 6 == 1 && (int) py >> 6 == 1) {
                fade = 0;
                timer = 0;
                gameState = 3;
            } //Entered block 1, Win game!!
        }

        if (gameState == 3) {
            screen(2);
            timer += 1 * fps;
            if (timer > 2000) {
                fade = 0;
                timer = 0;
                gameState = 0;
            }
        } //won screen
        if (gameState == 4) {
            screen(3);
            timer += 1 * fps;
            if (timer > 2000) {
                fade = 0;
                timer = 0;
                gameState = 0;
            }
        } //lost screen*/
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
        if (gameState == 2) //The main game loop
        {
            //buttons
            if (event.getKeyCode() == KeyCodes.A) {
                pa += 0.2 * fps;
                pa = fixAng((int) pa);
                pdx = cos(degToRad((int) pa));
                pdy = -sin(degToRad((int) pa));
            }
            if (event.getKeyCode() == KeyCodes.D) {
                pa -= 0.2 * fps;
                pa = fixAng((int) pa);
                pdx = cos(degToRad((int) pa));
                pdy = -sin(degToRad((int) pa));
            }

            int xo = 0;
            if (pdx < 0) {
                xo = -20;
            } else {
                xo = 20;
            }                                    //x offset to check map
            int yo = 0;
            if (pdy < 0) {
                yo = -20;
            } else {
                yo = 20;
            }                                    //y offset to check map
            int ipx = (int) (px / 64.0f);
            int ipx_add_xo = (int) ((px + xo) / 64.0f);
            int ipx_sub_xo = (int) ((px - xo) / 64.0f);             //x position and offset
            int ipy = (int) (py / 64.0f);
            int ipy_add_yo = (int) ((py + yo) / 64.0f);
            int ipy_sub_yo = (int) ((py - yo) / 64.0f);             //y position and offset
            if (event.getKeyCode() == KeyCodes.W)                                                                  //move forward
            {
                if (mapW[ipy * MAP_X + ipx_add_xo] == 0) {
                    px += pdx * 0.2 * fps;
                }
                if (mapW[ipy_add_yo * MAP_X + ipx] == 0) {
                    py += pdy * 0.2 * fps;
                }
            }
            if (event.getKeyCode() == KeyCodes.S)                                                                  //move backward
            {
                if (mapW[ipy * MAP_X + ipx_sub_xo] == 0) {
                    px -= pdx * 0.2 * fps;
                }
                if (mapW[ipy_sub_yo * MAP_X + ipx] == 0) {
                    py -= pdy * 0.2 * fps;
                }
            }
            if (event.getKeyCode() == KeyCodes.E)                                                                  //move backward
            {
                xo = 0;
                if (pdx < 0) {
                    xo = -25;
                } else {
                    xo = 25;
                }
                yo = 0;
                if (pdy < 0) {
                    yo = -25;
                } else {
                    yo = 25;
                }
                ipx = (int) (px / 64.0f);
                ipx_add_xo = (int) ((px + xo) / 64.0f);
                ipy = (int) (py / 64.0f);
                ipy_add_yo = (int) ((py + yo) / 64.0f);
                if (mapW[ipy_add_yo * MAP_X + ipx_add_xo] == 4) {
                    mapW[ipy_add_yo * MAP_X + ipx_add_xo] = 0;
                }
            }

        }


        return false;
    }

    private static int[] loadArray(String path) {
        try {
            return Files.readAllLines(Path.of(path)).stream().mapToInt(Integer::parseInt).toArray();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void drawSprite() {
        int x, y, s;
        if (px < sp[0].x + 30 && px > sp[0].x - 30 && py < sp[0].y + 30 && py > sp[0].y - 30) {
            sp[0].state = 0;
        } //pick up key
        if (px < sp[3].x + 30 && px > sp[3].x - 30 && py < sp[3].y + 30 && py > sp[3].y - 30) {
            gameState = 4;
        } //enemy kills

        //enemy attack
        int spx = (int) sp[3].x >> 6, spy = (int) sp[3].y >> 6;          //normal grid position
        int spx_add = ((int) sp[3].x + 15) >> 6, spy_add = ((int) sp[3].y + 15) >> 6; //normal grid position plus     offset
        int spx_sub = ((int) sp[3].x - 15) >> 6, spy_sub = ((int) sp[3].y - 15) >> 6; //normal grid position subtract offset
        if (sp[3].x > px && mapW[spy * 8 + spx_sub] == 0) {
            sp[3].x -= 0.04 * fps;
        }
        if (sp[3].x < px && mapW[spy * 8 + spx_add] == 0) {
            sp[3].x += 0.04 * fps;
        }
        if (sp[3].y > py && mapW[spy_sub * 8 + spx] == 0) {
            sp[3].y -= 0.04 * fps;
        }
        if (sp[3].y < py && mapW[spy_add * 8 + spx] == 0) {
            sp[3].y += 0.04 * fps;
        }

        for (s = 0; s < 4; s++) {
            float sx = sp[s].x - px; //temp float variables
            float sy = sp[s].y - py;
            float sz = sp[s].z;

            float CS = cos(degToRad((int) pa)), SN = sin(degToRad((int) pa)); //rotate around origin
            float a = sy * CS + sx * SN;
            float b = sx * CS - sy * SN;
            sx = a;
            sy = b;

            sx = (sx * 108.0f / sy) + (120.0f / 2); //convert to screen x,y
            sy = (sz * 108.0f / sy) + (80.0f / 2);

            int scale = (int) (32 * 80 / b);   //scale sprite based on distance
            if (scale < 0) {
                scale = 0;
            }
            if (scale > 120) {
                scale = 120;
            }

            //texture
            float t_x = 0, t_y = 31, t_x_step = 31.5f / (float) scale, t_y_step = 32.0f / (float) scale;

            for (x = (int) (sx - scale / 2); x < sx + (float) scale / 2; x++) {
                t_y = 31;
                for (y = 0; y < scale; y++) {
                    if (sp[s].state == 1 && x > 0 && x < 120 && b < depth[x]) {
                        int pixel = ((int) t_y * 32 + (int) t_x) * 3 + (sp[s].map * 32 * 32 * 3);
                        int red = sprites[pixel + 0];
                        int green = sprites[pixel + 1];
                        int blue = sprites[pixel + 2];
                        if (red != 255 && green != 0 && blue != 255) //dont draw if purple
                        {
                            GL41.glPointSize(8);
                            GL41.glColor3ub((byte) red, (byte) green, (byte) blue);
                            GL41.glBegin(GL41.GL_POINTS);
                            GL41.glVertex2i(x * 8, (int) (sy * 8 - y * 8));
                            GL41.glEnd(); //draw point
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

    float distance(float ax, float ay, float bx, float by, float ang) {
        return cos(degToRad((int) ang)) * (bx - ax) - sin(degToRad((int) ang)) * (by - ay);
    }

    private void drawRays2D() {
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

        for (r = 0; r < 120; r++) {
            int vmt = 0, hmt = 0;                                                              //vertical and horizontal map texture number 
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
                rx = (((int) px >> 6) << 6) - 0.0001f;
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
                if (mp > 0 && mp < MAP_X * MAP_Y && mapW[mp] > 0) {
                    vmt = mapW[mp] - 1;
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
            Tan = 1.0f / Tan;
            if (sin(degToRad((int) ra)) > 0.001) {
                ry = (((int) py >> 6) << 6) - 0.0001f;
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
                if (mp > 0 && mp < MAP_X * MAP_Y && mapW[mp] > 0) {
                    hmt = mapW[mp] - 1;
                    dof = 8;
                    disH = cos(degToRad((int) ra)) * (rx - px) - sin(degToRad((int) ra)) * (ry - py);
                }//hit         
                else {
                    rx += xo;
                    ry += yo;
                    dof += 1;
                }                                               //check next horizontal
            }

            float shade = 1;
            GL41.glColor3f(0, 0.8f, 0);
            if (disV < disH) {
                hmt = vmt;
                shade = 0.5f;
                rx = vx;
                ry = vy;
                disH = disV;
                GL41.glColor3f(0, 0.6f, 0);
            }//horizontal hit first

            int ca = fixAng((int) (pa - ra));
            disH = disH * cos(degToRad(ca));                            //fix fisheye 
            int lineH = (int) ((MAP_S * 640) / (disH));
            float ty_step = 32.0f / (float) lineH;
            float ty_off = 0;
            if (lineH > 640) {
                ty_off = (lineH - 640) / 2.0f;
                lineH = 640;
            }                            //line height and limit
            int lineOff = 320 - (lineH >> 1);                                               //line offset

            depth[r] = (int) disH; //save this line's depth
            //---draw walls---
            int y;
            float ty = ty_off * ty_step;//+hmt*32;
            float tx;
            if (shade == 1) {
                tx = (int) (rx / 2.0) % 32;
                if (ra > 180) {
                    tx = 31 - tx;
                }
            } else {
                tx = (int) (ry / 2.0) % 32;
                if (ra > 90 && ra < 270) {
                    tx = 31 - tx;
                }
            }
            for (y = 0; y < lineH; y++) {
                int pixel = ((int) ty * 32 + (int) tx) * 3 + (hmt * 32 * 32 * 3);
                int red = (int) (allTextures[pixel + 0] * shade);
                int green = (int) (allTextures[pixel + 1] * shade);
                int blue = (int) (allTextures[pixel + 2] * shade);
                GL41.glPointSize(8);
                GL41.glColor3ub((byte) red, (byte) green, (byte) blue);
                GL41.glBegin(GL41.GL_POINTS);
                GL41.glVertex2i(r * 8, y + lineOff);
                GL41.glEnd();
                ty += ty_step;
            }

            //---draw floors---
            for (y = lineOff + lineH; y < 640; y++) {
                float dy = y - (640 / 2.0f), deg = degToRad((int) ra), raFix = cos(degToRad(fixAng((int) (pa - ra))));
                tx = px / 2 + cos(deg) * 158 * 2 * 32 / dy / raFix;
                ty = py / 2 - sin(deg) * 158 * 2 * 32 / dy / raFix;
                mp = mapF[(int) (ty / 32.0) * MAP_X + (int) (tx / 32.0)] * 32 * 32;
                int pixel = (((int) (ty) & 31) * 32 + ((int) (tx) & 31)) * 3 + mp * 3;
                int red = (int) (allTextures[pixel + 0] * 0.7f);
                int green = (int) (allTextures[pixel + 1] * 0.7f);
                int blue = (int) (allTextures[pixel + 2] * 0.7f);
                GL41.glPointSize(8);
                GL41.glColor3ub((byte) red, (byte) green, (byte) blue);
                GL41.glBegin(GL41.GL_POINTS);
                GL41.glVertex2i(r * 8, y);
                GL41.glEnd();

                //---draw ceiling---
                mp = mapC[(int) (ty / 32.0) * MAP_X + (int) (tx / 32.0)] * 32 * 32;
                pixel = (((int) (ty) & 31) * 32 + ((int) (tx) & 31)) * 3 + mp * 3;
                red = allTextures[pixel + 0];
                green = allTextures[pixel + 1];
                blue = allTextures[pixel + 2];
                if (mp > 0) {
                    GL41.glPointSize(8);
                    GL41.glColor3ub((byte) red, (byte) green, (byte) blue);
                    GL41.glBegin(GL41.GL_POINTS);
                    GL41.glVertex2i(r * 8, 640 - y);
                    GL41.glEnd();
                }
            }

            ra = fixAng((int) (ra - 0.5f));                                                               //go to next ray, 60 total
        }
    }

    private void drawSky()     //draw sky and rotate based on player rotation
    {
        int x, y;
        for (y = 0; y < 40; y++) {
            for (x = 0; x < 120; x++) {
                int xo = (int) pa * 2 - x;
                if (xo < 0) {
                    xo += 120;
                }
                xo = xo % 120; //return 0-120 based on player angle
                int pixel = (y * 120 + xo) * 3;
                int red = sky[pixel + 0];
                int green = sky[pixel + 1];
                int blue = sky[pixel + 2];
                GL41.glPointSize(8);
                GL41.glColor3ub((byte) red, (byte) green, (byte) blue);
                GL41.glBegin(GL41.GL_POINTS);
                GL41.glVertex2i(x * 8, y * 8);
                GL41.glEnd();
            }
        }
    }

    private void screen(int v) //draw any full screen image. 120x80 pixels
    {
        int x, y;
        int[] T = {};
        if (v == 1) {
            T = title;
        }
        if (v == 2) {
            T = won;
        }
        if (v == 3) {
            T = lost;
        }
        for (y = 0; y < 80; y++) {
            for (x = 0; x < 120; x++) {
                int pixel = (y * 120 + x) * 3;
                int red = (int) (T[pixel + 0] * fade);
                int green = (int) (T[pixel + 1] * fade);
                int blue = (int) (T[pixel + 2] * fade);
                GL41.glPointSize(8);
                GL41.glColor3ub((byte) red, (byte) green, (byte) blue);
                GL41.glBegin(GL41.GL_POINTS);
                GL41.glVertex2i(x * 8, y * 8);
                GL41.glEnd();
            }
        }
        if (fade < 1) {
            fade += 0.001 * fps;
        }
        if (fade > 1) {
            fade = 1;
        }
    }
}
