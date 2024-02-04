package com.example.gameproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    //Code from this program has been used from Beginning Android Games
    //Review SurfaceView, Canvas, continue

    static boolean faster;
    GameSurface gameSurface;
    Button shootButton;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        //gameSurface.setBackground(getResources().getDrawable(R.drawable.secondroad));
        setContentView(gameSurface);

        gameSurface.setId(View.generateViewId());
        gameSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!faster)
                    faster = true;
                else
                    faster = false;
            }
        });










    }

    @Override
    protected void onPause(){
        super.onPause();
        gameSurface.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        gameSurface.resume();
    }






    //----------------------------GameSurface Below This Line--------------------------
    public class GameSurface extends SurfaceView implements Runnable,SensorEventListener{


        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap mainCar;
        Bitmap enemyCar;
        int mainCarX =0;
        int x=200;
        String time ="1:00";
        Paint paintProperty;
        Paint paintProperty2;
        double speed = 0;
        int seconds = 60;
        boolean end  = false;
        int screenWidth;
        int screenHeight;
        int score = 0;
        double enemySpeed=1;
        double enemyCarX = Math.random()*500;
        int enemyCarY =-200;
        int mainCarXPos =0;
        int mainCarYPos =0;
        float enemyXStart = (float) Math.random()*500;
        MediaPlayer player;
        MediaPlayer player2;
        boolean changedImage = false;
        int tempSeconds = 0;
        boolean changeImageBack = false;
        double missleX = 8000;
        double missleY;
        boolean missleShot = false;
        Bitmap missle;
        boolean missleHit = false;
        int missleHitSeconds;
        boolean missleHitSecondsCooldownBool = false;

        public GameSurface(Context context) {
            super(context);
            holder=getHolder();

            player = MediaPlayer.create(MainActivity.this,R.raw.gooff);
            player2 = MediaPlayer.create(MainActivity.this,R.raw.hitsound);
            player.start();

            mainCar= BitmapFactory.decodeResource(getResources(),R.drawable.blackcar);
            mainCar = Bitmap.createScaledBitmap(mainCar,300,300,false);

            missle= BitmapFactory.decodeResource(getResources(),R.drawable.missle);
            missle = Bitmap.createScaledBitmap(missle,300,300,false);

            enemyCar= BitmapFactory.decodeResource(getResources(),R.drawable.redcar);
            enemyCar = Bitmap.createScaledBitmap(enemyCar,300,-300,false);



            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth=sizeOfScreen.x;
            screenHeight=sizeOfScreen.y;

            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this,accelerometerSensor,sensorManager.SENSOR_DELAY_GAME);


            paintProperty= new Paint();
            paintProperty.setTextSize(100);
            paintProperty2= new Paint();
            paintProperty2.setTextSize(100);
            paintProperty2.setColor(Color.WHITE);

            AsyncThread asyncThread = new AsyncThread();
            asyncThread.execute();

            missleY = (screenHeight / 2) - mainCar.getHeight()+500;

        }

        @Override
        public void run() {



                while (running == true) {
                    if(seconds == 0)
                        running=false;
                    if (holder.getSurface().isValid() == false)
                        continue;
                    Canvas canvas = holder.lockCanvas();

                    canvas.drawRGB(0, 100, 100);



                    canvas.drawText(time, (screenWidth / 2) - 90, 150, paintProperty);
                    canvas.drawText("Score: "+ score, (screenWidth / 2) - 180, 260, paintProperty);

                    if (enemyCarX >= screenWidth-200)
                        enemySpeed = -1;
                    else if(enemyCarX <= -80)
                        enemySpeed = 1;



                    if(faster) {
                        enemyCarY += 4;
                        enemyCarX+=(enemySpeed*4);
                    }
                    else {
                        enemyCarY+=2;
                        enemyCarX+=(enemySpeed*2);
                    }



                    canvas.drawBitmap(enemyCar, (float)enemyCarX, enemyCarY, null);
                    //canvas.drawBitmap(enemyCar, (screenWidth / 2) - mainCar.getWidth() / 2 + mainCarX+100, (screenHeight / 2) - mainCar.getHeight()+800, null);


                    canvas.drawBitmap(mainCar, (screenWidth / 2) - mainCar.getWidth() / 2 + mainCarX, (screenHeight / 2) - mainCar.getHeight()+500, null);

                    //enemyCarY = (screenHeight / 2) - mainCar.getHeight()+800;
                    //enemyCarX = (screenWidth / 2) - mainCar.getWidth() / 2 + mainCarX+100;
                    mainCarYPos = (screenHeight / 2) - mainCar.getHeight()+500;
                    mainCarXPos = (screenWidth / 2) - mainCar.getWidth() / 2 + mainCarX;

                    if(missleShot){
                        if(missleX==8000)
                            missleX = mainCarXPos;
                        if(Math.abs(missleX-enemyCarX)<=120 && Math.abs(missleY-enemyCarY)<=300&&!missleHit) {
                            enemyCarY=-200;
                            enemyCarX = Math.random()*500;
                            enemySpeed*=-1;
                            missleHit = true;
                        }
                        else {
                            if(!missleHit)
                                canvas.drawBitmap(missle, (float) missleX, (float) missleY, null);
                        }
                        Log.d("TAG", "MissleX:" + missleX);
                        Log.d("TAG", "MissleY:" + missleY);
                        if(missleY<=-500) {
                            missleX = 8000;
                            missleY = (screenHeight / 2) - mainCar.getHeight() + 500;
                            missleHit = false;
                            missleHitSecondsCooldownBool=false;
                        }
                        else
                            missleY-=10;
                    }



                    //if(enemyCarX-mainCarX<=120 && enemyCarY-mainCarY<=300)

                    if(Math.abs(enemyCarX-mainCarXPos)<=120 && Math.abs(enemyCarY-mainCarYPos)<=300) {
                        mainCar = BitmapFactory.decodeResource(getResources(), R.drawable.brokencar);
                        mainCar = Bitmap.createScaledBitmap(mainCar,400,300,false);
                        enemyCarY=-200;
                        enemyCarX = Math.random()*500;
                        if(score!=0)
                            score--;
                        player.pause();
                        enemySpeed*=-1;
                        changedImage = true;
                        /*try {
                            player.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                         */
                        player2.start();
                    }

                    else if(enemyCarY>1900){
                        score++;
                        enemyCarY=-200;
                    }

                    if(!player2.isPlaying())
                        player.start();
                    /*Log.d("TAG", "mainCarX: " + mainCarX);
                    Log.d("TAG", "enemyCarX: " + enemyCarX);
                    Log.d("TAG", "mainCarY: " + mainCarY);
                    Log.d("TAG", "enemyCarY: " + enemyCarY);

                     */

                    if(changeImageBack){
                        mainCar = BitmapFactory.decodeResource(getResources(), R.drawable.blackcar);
                        mainCar = Bitmap.createScaledBitmap(mainCar,300,300,false);
                        changeImageBack = false;
                    }



                    if (mainCarX <= 500 && mainCarX >= -500)
                        mainCarX += speed;
                    else if (mainCarX >= 500 && speed < 0)
                        mainCarX += speed;
                    else if (mainCarX <= -500 && speed > 0)
                        mainCarX += speed;





                    holder.unlockCanvasAndPost(canvas);
                }


                    if (holder.getSurface().isValid()){
                        Canvas canvas = holder.lockCanvas();

                        canvas.drawRGB(0, 0, 0);

                        canvas.drawText("GAME OVER", (screenWidth / 2) - 300, (screenHeight / 2) - 130, paintProperty2);
                        canvas.drawText("You're score was: " + score, (screenWidth / 2) - 450, (screenHeight / 2)+30, paintProperty2);

                        if(!running)
                            player.stop();

                        holder.unlockCanvasAndPost(canvas);
                    }

        }

        public void resume(){
            running=true;
            gameThread=new Thread(this);
            gameThread.start();
        }

        public void pause() {
            running = false;
            while (true) {
                try {
                    gameThread.join();
                } catch (InterruptedException e) {
                }
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            //tilt phone and change position

            double z = event.values[2];
            Log.d("TAG",""+z);

            if(z>=9.5)
                speed = 0;
            else {
                if ((double) event.values[0] > 0) {
                    if (z < 9.5)
                        speed = -2;
                    if (z <= 8)
                        speed = -4;
                }
                else {
                    if (z < 9.5)
                        speed = 2;
                    if (z <= 8)
                        speed = 4;
                }
            }

             /*
            else {
                if((double)event.values[0] > 0){
                        speed = (z-9.5);
                }
                else{
                    speed = (9.5-z);
                }
            }

              */


            //Log.d("TAG", "Speed"+speed);

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public class AsyncThread extends AsyncTask<Void,Void,Void>{

            @Override
            protected Void doInBackground(Void... voids) {
                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        seconds--;
                        time = seconds/60+":"+seconds%60;
                        if(seconds<10)
                            time = seconds/60+":0"+seconds%60;
                        if(tempSeconds-seconds==3) {
                            changeImageBack = true;
                            tempSeconds= 0;
                        }

                        if(changedImage){
                            tempSeconds = seconds;
                            changedImage = false;
                        }

                        if(60-seconds==5)
                            missleShot=true;

                        if(missleHit&& !missleHitSecondsCooldownBool) {
                            missleHitSeconds = seconds;
                            missleHitSecondsCooldownBool = true;
                        }
                        if(missleHitSeconds-seconds==20){
                            missleHit=false;
                            missleHitSecondsCooldownBool=false;
                        }



                    }
                },0,1000);
                return null;
            }
        }

    }//GameSurface
}//Activity