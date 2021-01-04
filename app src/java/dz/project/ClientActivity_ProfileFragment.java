package dz.project;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ClientActivity_ProfileFragment extends Fragment {

    public ClientActivity_ProfileFragment() {}

    TextView user_name_txt;

    TextView email_txt;
    LinearLayout email_status_layout;
    TextView email_status_txt;
    Button add_email_btn;
    Button verify_email_btn;

    TextView phone_txt;
    LinearLayout phone_status_layout;
    TextView phone_status_txt;
    Button add_phone_btn;
    Button verify_phone_btn;

    Button disconnect_btn;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // view
        View v = inflater.inflate(R.layout.activity_client_main_profile_fragment, container, false);

        // user name
        user_name_txt = v.findViewById(R.id.client_main_profile_fragment_txt);

        // disconnect button
        disconnect_btn = v.findViewById(R.id.activity_main_client_profile_fragment_disconnect_btn);
        disconnect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                listner.ClientActivity_ProfileFragment_onInputSent("DISCONNECT");
            }
        });

        // email
        email_txt = v.findViewById(R.id.client_main_user_profile_fragment_email_txt);
        email_status_txt = v.findViewById(R.id.client_main_user_profile_fragment_email_status_txt);
        email_status_layout = v.findViewById(R.id.client_main_user_profile_fragment_email_status_layout);
        add_email_btn = v.findViewById(R.id.activity_main_add_email_btn);
        add_email_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                listner.ClientActivity_ProfileFragment_onInputSent("ADD_EMAIL");
            }
        });
        verify_email_btn = v.findViewById(R.id.activity_main_client_profile_fragment_verify_email_btn);
        verify_email_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                listner.ClientActivity_ProfileFragment_onInputSent("VERIFY_EMAIL");
            }
        });

        // phone
        phone_txt = v.findViewById(R.id.client_main_user_profile_fragment_phone_txt);
        phone_status_txt = v.findViewById(R.id.client_main_user_profile_fragment_phone_status_txt);
        phone_status_layout = v.findViewById(R.id.client_main_user_profile_fragment_phone_status_layout);
        add_phone_btn = v.findViewById(R.id.activity_main_add_phone_btn);
        add_phone_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                listner.ClientActivity_ProfileFragment_onInputSent("ADD_PHONE_NUMBER");
            }
        });
        verify_phone_btn = v.findViewById(R.id.activity_main_client_profile_fragment_verify_phone_btn);
        verify_phone_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                listner.ClientActivity_ProfileFragment_onInputSent("VERIFY_PHONE_NUMBER");
            }
        });

        // update data
        listner.ClientActivity_ProfileFragment_onInputSent("SETUP");

        return v;
    }

    //----------------------------------------------------------------------------------------------
    private ClientActivity_ProfileFragment_Listner listner;
    public interface ClientActivity_ProfileFragment_Listner {
        void ClientActivity_ProfileFragment_onInputSent(String input);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof ClientActivity_ProfileFragment_Listner) {
            listner = (ClientActivity_ProfileFragment_Listner) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ClientActivity_ProfileFragment_Listner.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        listner = null;
    }

    //----------------------------------------------------------------------------------------------
    public void setUserName(String userName) {
        user_name_txt.setText(userName);
    }

    public void setEmail(String email, boolean status) {
        if (email.equals("NO_EMAIL")) {
            email_txt.setText("aucun E-mail enregistrer.");
            add_email_btn.setText("Ajouter un Email");
            email_status_layout.setVisibility(View.GONE);
            verify_email_btn.setVisibility(View.GONE);
        } else {
            email_txt.setText(email);
            add_email_btn.setText("Modifier votre E-Mail");
            email_status_layout.setVisibility(View.VISIBLE);

            if (status) {
                email_status_txt.setText("Verifié");
                email_status_txt.setTextColor(0xff00ff00);
                verify_email_btn.setVisibility(View.GONE);
            } else {
                email_status_txt.setText("non Verifié");
                email_status_txt.setTextColor(0xffff0000);
                verify_email_btn.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setPhoneNumber(String phoneNumber, boolean status) {
        if (phoneNumber.equals("NO_PHONE_NUMBER")) {
            phone_txt.setText("aucun numero enregistrer.");
            add_phone_btn.setText("Ajouter un numero de telephone");
            phone_status_layout.setVisibility(View.GONE);
            verify_phone_btn.setVisibility(View.GONE);
        } else {
            phone_txt.setText(phoneNumber);
            add_phone_btn.setText("Modifier le numero de telephone");
            phone_status_layout.setVisibility(View.VISIBLE);

            if (status) {
                phone_status_txt.setText("Verifié");
                phone_status_txt.setTextColor(0xff00ff00);
                verify_phone_btn.setVisibility(View.GONE);
            } else {
                phone_status_txt.setText("non Verifié");
                phone_status_txt.setTextColor(0xffff0000);
                verify_phone_btn.setVisibility(View.VISIBLE);
            }
        }
    }
}