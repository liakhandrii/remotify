package ua.com.liakh.remotify;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;


public class Touchpad extends ActionBarActivity implements View.OnTouchListener{

    private static String serverAddress = "";
    private static final int PORT = 10480;
    private ArrayList<String> localServers = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        loadLocalServers();

        if (localServers.size() == 1)
            serverAddress = localServers.get(0);

        setContentView(R.layout.activity_touchpad);
        View touchpad = (View)findViewById(R.id.background);
        touchpad.setOnTouchListener(this);

        try {
            socket = new Socket(serverAddress, PORT);
            startSayingImHere();
        }catch(Exception e) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null)
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println("DISCONNECT");
                socket.close();
            }catch(IOException e){

            }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_touchpad, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {

        int x = Math.round(event.getX());
        int y = Math.round(event.getY());

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                prevXPosition = -1;
                prevYPosition = -1;

                mouseDownTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                prevXPosition = -1;
                prevYPosition = -1;

                if (!moved && (System.currentTimeMillis() - mouseDownTime) <= DELAY ){
                    sendClick();
                    playClickAnimation(x, y);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getActionIndex() == 1){
                    if (prevXPosition == -1){
                        prevXPosition = x;
                        prevYPosition = y;
                        return true;
                    }
                    int xMove = x - prevXPosition;
                    int yMove = y - prevYPosition ;
                    if (xMove!=0||yMove!=0) {
                        sendMouseMove(xMove, yMove);
                        prevXPosition = x;
                        prevYPosition = y;
                        moved = true;
                    }else{
                        moved = false;
                    }
                }else if (event.getActionIndex() == 2){
                    Log.d("Multitouch", "moved");
                }

                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d("Multitouch", event.getActionIndex()+"");
                break;
        }
        return true;
    }*/

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = Math.round(event.getX());
        int y = Math.round(event.getY());

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                prevXPosition = -1;
                prevYPosition = -1;

                mouseDownTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                prevXPosition = -1;
                prevYPosition = -1;

                if (!moved && (System.currentTimeMillis() - mouseDownTime) <= DELAY ){
                    sendClick();
                    playClickAnimation(x, y);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getActionIndex() == 1){
                    if (prevXPosition == -1){
                        prevXPosition = x;
                        prevYPosition = y;
                        return true;
                    }
                    int xMove = x - prevXPosition;
                    int yMove = y - prevYPosition ;
                    if (xMove!=0||yMove!=0) {
                        sendMouseMove(xMove, yMove);
                        prevXPosition = x;
                        prevYPosition = y;
                        moved = true;
                    }else{
                        moved = false;
                    }
                }else if (event.getActionIndex() == 2){
                    Log.d("Multitouch", "moved");
                }

                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d("Multitouch", event.getActionIndex()+"");
                break;
        }
        return true;
    }

    private void sendMouseMove(int x, int y){
        int absX = Math.abs(x);
        int absY = Math.abs(y);

        if (absX >= 10 && absX < 20)
            x *= 1.3;
        if (absX >= 20)
            x *= 2;

        if (absY >= 10 && absY < 20)
            y *= 1.3;
        if (absY >= 20)
            y *= 2;
        if (socket != null)
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println("MOVE");
                out.println(x);
                out.println(y);
            }catch(IOException e){
                e.printStackTrace();
            }
        else{
            letThemKnow();
        }
    }

    private void sendClick(){
        if (socket != null)
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println("CLICK");
            }catch(IOException e){

            }
        else{
            letThemKnow();
        }
    }

    private void loadLocalServers(){
        Thread t = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Socket mainServer = new Socket("remotify.cloudapp.net", 10481);
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mainServer.getOutputStream())), true);
                    out.println("client_getList");
                    String ip = "";
                    do{
                        BufferedReader in = new BufferedReader(new InputStreamReader(mainServer.getInputStream()));
                        ip = in.readLine();
                        if (!ip.equals("end")) {
                            localServers.add(ip);
                        }
                    }while(!ip.equals("end"));
                    mainServer.close();
                }catch (IOException e){

                }
            }
        };
        t.start();
        try {
            t.join();
        }catch(InterruptedException e){

        }
    }

    private void startSayingImHere(){
        Thread t = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    while (true) {
                        out.println("c");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {

                        }
                    }
                }catch (IOException e){

                }
            }
        };
        t.setDaemon(true);
        t.start();
    }

    private  void playClickAnimation(int x, int y){
        View touchpad = (View)findViewById(R.id.background);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.clickcircle);
        View clickcircle = (View)findViewById(R.id.clickcircle);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin=x;
        params.topMargin=y;
        params.bottomMargin = touchpad.getHeight() - y - clickcircle.getHeight();
        params.rightMargin = touchpad.getWidth() - x - clickcircle.getWidth();

        clickcircle.setLayoutParams(params);
        clickcircle.startAnimation(animation);
    }

    private void letThemKnow(){
        //TODO Indicate connection lost
    }

    private int pointerID;
    private int prevXPosition = -1;
    private int prevYPosition = -1;
    private boolean moved = false;
    private long mouseDownTime = -1;
    private final long DELAY = 200;
    private Socket socket;

}
