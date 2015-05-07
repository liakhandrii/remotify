package ua.com.liakh.remotify;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
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

        connectionButton = (ImageButton)findViewById(R.id.connection_button);

        View touchpad = (View)findViewById(R.id.background);
        touchpad.setOnTouchListener(this);

        try {
            connectToServer(serverAddress);
            connectionButtonEnabled(true);
        } catch(IOException e){
            letThemKnow();
        }

        connectionButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                loadLocalServers();

                CharSequence servers[] = new CharSequence[localServers.size()+1];

                for (int i = 0; i < localServers.size(); i++){
                    servers[i] = localServers.get(i);
                }
                servers[localServers.size()] = "Custom...";

                AlertDialog.Builder builder = new AlertDialog.Builder(Touchpad.this);
                builder.setTitle("Choose a server");
                builder.setItems(servers, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int option) {
                       if (option < localServers.size()){
                           try {
                               serverAddress = localServers.get(option);
                               connectToServer(serverAddress);
                               connectionButtonEnabled(true);
                           } catch(IOException e){
                               letThemKnow();
                           }
                       }else{
                           AlertDialog.Builder alert = new AlertDialog.Builder(Touchpad.this);

                           alert.setTitle("Enter server IP");


                           final EditText input = new EditText(Touchpad.this);
                           alert.setView(input);

                           alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int whichButton) {
                                   String value = input.getText().toString();
                                   try {
                                       serverAddress = value;
                                       connectToServer(serverAddress);
                                       connectionButtonEnabled(true);
                                   } catch(IOException e){
                                       letThemKnow();
                                   }
                               }
                           });

                           alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int whichButton) {

                               }
                           });

                           alert.show();

                       }
                    }
                });
                builder.show();

            }

        });
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = Math.round(event.getX());
        int y = Math.round(event.getY());

        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                prevXPosition = -1;
                prevYPosition = -1;

                pointersCounter++;
                mouseDownTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                prevXPosition = -1;
                prevYPosition = -1;

                if (pointersCounter == 1){
                    if (!moved && (System.currentTimeMillis() - mouseDownTime) <= DELAY ){
                        sendClick();
                        playClickAnimation(x, y);
                    }
                }

                if (pointersCounter == 2){
                    if (!moved && (System.currentTimeMillis() - mouseDownTime) <= DELAY ){
                        sendRightClick();
                        playRightClickAnimation();
                    }
                }
                pointersCounter = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1 && pointersCounter == 1){
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
                }else if (event.getPointerCount() == 2 && pointersCounter == 2){
                    int centerX = Math.round((event.getX(0) + event.getX(1))/2f);
                    int centerY = Math.round((event.getY(0) + event.getY(1))/2f);

                    //double dx = prevXPosition - (event.getX(0) + event.getX(1))/2f;
                    //double dy = prevXPosition - (event.getY(0) + event.getY(1))/2f;
                    //Log.d("Scroll","dx: "+dx+" dy: "+dy);

                    if (prevXPosition == -1){
                        prevXPosition = centerX;
                        prevYPosition = centerY;
                        return true;
                    }

                    int xMove = prevXPosition - centerX;
                    int yMove = prevYPosition - centerY;

                    final float coef = 4;

                    if (xMove >= coef){
                        xMove = Math.round(xMove/coef);
                    }

                    if (yMove >= coef){
                        yMove = Math.round(yMove/coef);
                    }

                    if (xMove!=0||yMove!=0) {
                        sendScroll(xMove, yMove);
                        prevXPosition = centerX;
                        prevYPosition = centerY;
                        moved = true;
                    }else{
                        moved = false;
                    }
                }

                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                prevXPosition = -1;
                prevYPosition = -1;
                pointersCounter++;
                break;
        }
        return true;
    }

    private void sendScroll(int x, int y){
        if (Math.abs(x) > Math.abs(y))
            y = 0;
        else
            x = 0;
        if (socket != null)
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println("SCROLL");
                out.println(x);
                out.println(y);
            }catch(IOException e){
                letThemKnow();
            }
        else{
            letThemKnow();
        }
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
                letThemKnow();
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
            }catch(Exception e){
                letThemKnow();
            }
        else{
            letThemKnow();
        }
    }

    private void sendRightClick(){
        if (socket != null)
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println("RIGHT_CLICK");
            }catch(IOException e){
                letThemKnow();
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
                    localServers.clear();
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
        t.setDaemon(true);
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
                        out.println("co");
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {

                        }
                    }
                }catch (IOException e){
                    letThemKnow();
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

    private void playRightClickAnimation(){
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rightclick);
        View rightclick = (View)findViewById(R.id.rightclick);

        rightclick.startAnimation(animation);
    }

    private void letThemKnow(){
        connectionButtonEnabled(false);
    }

    private void connectionButtonEnabled(boolean b){
        if (b)
            connectionButton.setBackgroundResource(R.drawable.connected);
        else
            connectionButton.setBackgroundResource(R.drawable.not_connected);
    }

    private void connectToServer(String ip) throws IOException{
            socket = new Socket(serverAddress, PORT);
            startSayingImHere();
    }

    private int pointersCounter = 0;
    private int prevXPosition = -1;
    private int prevYPosition = -1;
    private boolean moved = false;
    private long mouseDownTime = -1;
    private final long DELAY = 200;
    private Socket socket;
    private ImageButton connectionButton;


}
