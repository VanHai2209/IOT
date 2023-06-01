package com.example.th1;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Notification;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private TextView txtGas, txtFire, txtStatus, txtEnviroment;
    private Button btn;
    private ImageView imageView;
    private Switch window;
    private int Gas, Fire;
    private static final int REQUEST_CALL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtStatus = findViewById(R.id.textView4);
        imageView = findViewById(R.id.imageView3);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makePhoneCall();
            }
        });
        txtGas = findViewById(R.id.textView);
        txtFire = findViewById(R.id.textView2);
        txtEnviroment = findViewById(R.id.textView3);
        window = findViewById(R.id.switch1);
        window.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (window.isChecked()) {
                    Toast.makeText(MainActivity.this, "Cửa mở", Toast.LENGTH_SHORT).show();
                    SendData(true, "window");
                } else {
                    SendData(false, "window");
                    Toast.makeText(MainActivity.this, "Cửa đóng", Toast.LENGTH_SHORT).show();
                }
            }
        });
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRefRead = database.getReference("window");
        myRefRead.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Boolean value = dataSnapshot.getValue(Boolean.class);
                if (value) {
                    window.setChecked(true);
                } else {
                    window.setChecked(false);
                }
                Log.d(TAG, "Value is: " + value);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Ngắt cảnh báo", Toast.LENGTH_SHORT).show();
                SendData(true, "interrupt");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                SendData(false, "interrupt");
            }
        });
        DatabaseReference myRefGas = database.getReference("gas");
        DatabaseReference myRefFire = database.getReference("fire");
        myRefGas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Integer value = dataSnapshot.getValue(Integer.class);
                Gas = value;
                txtGas.setText("Gas: " + value);
                checkValue();
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        myRefFire.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Integer value = dataSnapshot.getValue(Integer.class);
                Fire = value;
                checkValue();
                if (Fire == 1) {
                    txtFire.setText("Fire: No Fire");
                } else txtFire.setText("Fire: Fire");

                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


    }

    private void pushNotify(String StringAlert) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Notification notification = new NotificationCompat.Builder(this, MyNotification.CHANNEL_ID)
                .setContentTitle("Alert From App Warning")
                .setContentText(StringAlert)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(bitmap)
                .setColor(Color.RED)
                .build();
        NotificationManagerCompat.from(this).notify(getIdNotify(), notification);
    }
    private int getIdNotify(){
        return (int) new Date().getTime();
    }
    public void checkValue() {

        switch (Fire) {
            case 0:
                if (Gas < 600) {
                    String stringAlert = "<---Fire Dectected--->";
                    txtEnviroment.setTextColor(Color.YELLOW);
                    txtStatus.setTextColor(Color.YELLOW);
                    txtStatus.setText(stringAlert);
                    pushNotify(stringAlert);
                    sendEmail(stringAlert);
                } else {
                    String stringAlert = "<---WARNING--->";
                    txtStatus.setTextColor(Color.RED);
                    txtEnviroment.setTextColor(Color.RED);
                    txtStatus.setText(stringAlert);
                    pushNotify(stringAlert);
                    sendEmail(stringAlert);
                }
                // code block
                break;
            case 1:
                if (Gas < 600) {
                    txtEnviroment.setTextColor(Color.GREEN);
                    txtStatus.setTextColor(Color.GREEN);
                    txtStatus.setText("<----Normal---->");
                } else {
                    String stringAlert = "<---Gas Dectected--->";
                    txtEnviroment.setTextColor(Color.YELLOW);
                    txtStatus.setTextColor(Color.YELLOW);
                    txtStatus.setText(stringAlert);
                    pushNotify(stringAlert);
                    sendEmail(stringAlert);
                }
                // code block
                break;
        }
    }

    public void sendEmail(String message) {
        String mEmail = "nguyenvanhai22092001@gmail.com";
        String mSubject = "Alert";
        String mMessage = message;
        JavaMailAPI javaMailAPI = new JavaMailAPI(this, mEmail, mSubject, mMessage);
        javaMailAPI.execute();
    }

    private void makePhoneCall() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
        } else {
            String dial = "tel:114";
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void SendData(boolean value, String path) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRefSend = database.getReference(path);
        myRefSend.setValue(value);
    }
}