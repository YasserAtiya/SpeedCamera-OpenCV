/*
*   Speed Demon by Javanaut
*   Dan Diamond, Trung Nguyen, Yasser Atiya
*   Summer 2016
*   Application uses OpenCV to calculate the distance between the camera and two points.
*   The app has a built in timer that the user then starts upon the measured object
*   reaching the first point and subsequently stops at the second point.
*   The app then calculates dx/dt for the velocity of the object.
*
*   Skeleton OpenCV function set up by Graham Coulby
*/

package com.speeddemon;

import android.app.Dialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import static org.opencv.imgproc.Imgproc.Canny;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2
{
    private JavaCameraView mOpenCvCameraView;
    private Mat edge;
    Sensor accelerometer;
    SensorManager sm;
    TextView distanceOutput, distanceAngleOutput, initialTimeOutput;
    double phoneHeight = 1.22;
    double x, y, z, distance, distanceAngle;
    //SeekBar thresholdBar;
    //int threshold;
    boolean checkDistance = true;
    long refreshRate, previousTime;


    // Time objects
    Button time1Button;
    Button time2Button;
    Button resetTimeBtn;
    TextView dtView;
    int timeInit = 0;
    int timeFinal = 0;
    double dt = 0.0;

    // Distance objects
    Button dist1Button;
    Button dist2Button;
    Button resetDistBtn;
    double distInit = 0.0;
    double distFinal = 0.0;
    TextView dist1View;
    TextView dist2View;
    double distTraversed = 0.0;
    TextView dxView;

    // Speed objects
    double speed = 0.0;
    TextView speedView;
    Button speedButton;

    Button instructionsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getWindow().setBackgroundDrawable();
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.MainActivityCameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        distanceOutput = (TextView) findViewById(R.id.distanceOutput);
        //thresholdBar = (SeekBar) findViewById(R.id.thresholdBar);
        distanceAngleOutput = (TextView) findViewById(R.id.distanceAngle);

        // Time Objects
        time1Button = (Button) findViewById(R.id.btnTime1);
        time2Button = (Button) findViewById(R.id.btnTime2);
        resetTimeBtn = (Button) findViewById(R.id.btnResetTime);
        initialTimeOutput = (TextView) findViewById(R.id.initialTime);
        dtView = (TextView) findViewById(R.id.dtView);

        // Dist Objects
        dist1View = (TextView) findViewById(R.id.dist1View);
        dist2View = (TextView) findViewById(R.id.dist2View);
        dist1Button = (Button) findViewById(R.id.btnDist1);
        dist2Button = (Button) findViewById(R.id.btnDist2);
        resetDistBtn = (Button) findViewById(R.id.btnResetDist);
        dxView = (TextView) findViewById(R.id.dxView);

        // Speed Objects
        speedView = (TextView) findViewById(R.id.spdTxtView);
        speedButton = (Button) findViewById(R.id.btnGetSpeed);

        instructionsButton = (Button) findViewById(R.id.instructionsBtn);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(new SensorEventListener()
        {
            @Override
            public void onSensorChanged(SensorEvent event)
            {
                setXYZ(event.values[0], event.values[1], event.values[2]);

                int rotation = (int) Math.round(Math.toDegrees(Math.atan2(x, y)));

                refreshRate = System.currentTimeMillis() - previousTime;

                previousTime = System.currentTimeMillis();


                if(checkDistance)
                {
                    distance = getDistance();
                    distanceOutput.setText(distance + "m");
                    distanceAngleOutput.setText("Angle: " + distanceAngle);
                }

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy)
            {

            }
        }, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);



        // Distance button events
        dist1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                distInit = getDistance();
                String dist = Double.toString(distInit);
                dist1View.setText(dist);
            }
        });

        dist2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                distFinal = getDistance();
                String dist = Double.toString(distFinal);
                dist2View.setText(dist);

                distTraversed = Math.abs(distFinal - distInit);
                //String distTrav = Double.toString(distTraversed);
                String distTrav = String.format("%.3f", distTraversed);
                dxView.setText(distTrav);
            }
        });

        resetDistBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                distInit = 0.0;
                distFinal = 0.0;
                distTraversed = 0.0;

                String distInitial = Double.toString(distInit);
                dist1View.setText(distInitial);

                String distFin = Double.toString(distFinal);
                dist2View.setText(distFin);

                String distTrav = Double.toString(distTraversed);
                dxView.setText(distTrav);
            }
        });

        resetTimeBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                timeInit = 0;
                timeFinal = 0;
                dt =  0;
                String time = Double.toString(dt);
                dtView.setText(time);
            }
        });

        // Time button events
        time1Button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                timeInit = (int) System.currentTimeMillis();
                String time = Integer.toString(timeInit);
                initialTimeOutput.setText(time);
            }
        });

        time2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeFinal = (int) System.currentTimeMillis();
                dt =  ( (double) timeFinal - timeInit ) / 1000;
                String time = Double.toString(dt);
                dtView.setText(time);
            }
        });

        // Speed button events
        speedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (dt > 0.001 && distTraversed > 0.01)
                {
                    speed = ( distTraversed / dt );
                }

                // String speedString = Double.toString(speed);
                String speedString = String.format("%.3f", speed);
                speedView.setText(speedString);
            }
        });

        instructionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showInstructions(v);
                showChangeLangDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCameraViewStarted(int width, int height)
    {
        edge = new Mat();
    }

    @Override
    public void onCameraViewStopped()
    {
        edge.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        //threshold = thresholdBar.getProgress();
        edge = inputFrame.rgba();
//        if(((Switch) findViewById(R.id.edgeSwitch)).isChecked())
//            Canny(edge, edge,threshold , threshold*3);
        return edge;
    }

    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
    }

    public void onDestroy()
    {
        super.onDestroy();
        if (mOpenCvCameraView != null)
        {
            mOpenCvCameraView.disableView();
        }
    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)
    {
        public static final String TAG = "";

        public void onManagerConnected(int status)
        {
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "Speed Demon loaded successfully");
                    mOpenCvCameraView.enableView();
                    break;
                }
                default:
                {
                    super.onManagerConnected(status);
                }
            }
        }
    };

    private void setXYZ(double x, double y, double z)
    {
        double norm = Math.sqrt(x * x + y * y + z * z);

        this.x = x / norm;
        this.y = y / norm;
        this.z = z / norm;
    }

    private double getDistance()
    {
        double inclination = Math.toDegrees(Math.acos(z));
        double angle;

        if(inclination > 90)
            angle = 90;
        else if(inclination < 0 || y < 0)
            angle = 0;
        else
            angle = Math.round((inclination) * 10.0) / 10.0;

        distanceAngle = angle;
        double tanAngle = Math.tan(Math.toRadians(angle));

        return Math.round(((phoneHeight * tanAngle) - 0.1) * 100.0) / 100.0;
    }


    private double round(double d, int decimals)
    {
        double power = Math.pow(10, decimals);
        if(decimals == 0)
        {
            return Math.round(d);
        }
        else
        {
            return Math.round(d * power) / power;
        }
    }

    public void showChangeLangDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.custom_dialog, null);
        dialogBuilder.setView(dialogView);

        final TextView customDialogTextView = (TextView) dialogView.findViewById(R.id.instructions_textView);

        dialogBuilder.setTitle(R.string.instructions_title);
        dialogBuilder.setPositiveButton(R.string.done_button_text, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Nothing to do here for now.
                //Can consume/process customDialogTextView here if needed.
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }
}

