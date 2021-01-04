package dz.project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import dz.project.localData.LocalDataManager;
import dz.project.localData.querys.User;
import dz.project.remoteData.RemoteDataManager;

public class MainActivity extends AppCompatActivity {

    public static LocalDataManager dbManager;
    public static User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set starting layout
        showStartingLayout();

        // get local data
        dbManager = new LocalDataManager(this);
        user = User.getUser();
        if (user == null){
            showMainConnectLayout();
        } else {
            // connect
            showConnectionLayout();
        }
    }

    private void showStartingLayout() {
        setContentView(R.layout.activity_starting);
    }

    public void showMainLayout() {
        setContentView(R.layout.activity_main);
    }

    //----------------------------------------------------------------------------------------------
    private void showMainConnectLayout() {
        setContentView(R.layout.activity_main_connect);

        final EditText ip_EditText = findViewById(R.id.serverIp);
        ip_EditText.setText(RemoteDataManager.host);

        // handle buttons click listeners
        Button seVerifier_btn = findViewById(R.id.seVerifier_btn);
        seVerifier_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                RemoteDataManager.host = ip_EditText.getText().toString();
                // start user Data activity
                Intent userDataActivity = new Intent(MainActivity.this, UserDataActivity.class);
                startActivityForResult(userDataActivity, 1);
            }
        });
    }

    private void disconnectUser() {
        // set starting layout
        showMainConnectLayout();

        // delete user
        user = null;

        // delete stored user
        User.deleteUser();
    }

    //----------------------------------------------------------------------------------------------
    private void showConnectionLayout() {
        if (user.getStatus().equals("client")) {
            // start user Data activity
            Intent clientActivity = new Intent(MainActivity.this, ClientActivity.class);
            startActivityForResult(clientActivity, 2);

        } else if (user.getStatus().equals("deliverer")) {

        } else {
            // this should never happen
        }
    }

    // Call Back method  to get the Message form other Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode != Activity.RESULT_CANCELED) {

            user.setUid(data.getStringExtra("user_uid"));
            user.setPassword(data.getStringExtra("user_password"));
            user.setStatus(data.getStringExtra("user_status"));

            // connect
            showConnectionLayout();
        }

        if(requestCode == 2) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // close app
                finish();
            } else {
                String action = data.getStringExtra("action");
                if (action.equals("DISCONNECT_USER")) {
                    disconnectUser();
                } else {
                    // close app
                    finish();
                }
            }
        }
    }

}
