package hu.uniobuda.nik.ciwsduino;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import hu.uniobuda.nik.ciwsduino.ultrasonic.UltrasonicView;
import hu.uniobuda.nik.joystick.Joystick;
import hu.uniobuda.nik.joystick.JoystickEventListener;

/**
 * Created by thecy on 2016. 04. 25..
 */
public class MainFragment extends Fragment {

    /*
    Teszt adatok, amelyeket folyósón mértünk
    medián szűrő alkalmazásával és visszhangot csökkentő hengerrel.
    A Bluetooth kapcsolatot helyettesítjük a példában a teszt adatokkal.
     */
    private int tavolsagok[] = {
            110,126,125,126,107,107,107,107,107,105,107,105,105,105,103,104,103,103,98,98,98,98,162,97,97,97,97,97,97,98,98,98,97,99,98,52,135,193,273,197,275,214,267,267,267,266,265,265,256,265,114,265,265,55,264,257,264,93,75,76,75,75,74,74,74,74,74,74,74,73,48,73,73,74,74,73,73,73,73,74,73,73,74,74,74,74,74,49,48,47,47,48,48,47,47,46,40,45,38,45,37,45,37,44,37,41,39,41,41,40,40,40,40,40,40,40,41,41,41,41,47,47,47,47,47,41,41,41,41,40,40,40,40,40,41,40,40,42,42,41,45,45,45,45,45,45,46,46,47,46,47,47,47,47,48,48,49,50,74,74,74,74,73,73,73,73,73,73,73,74,73,49,73,74,74,74,74,74,74,74,48,75,74,74,75,75,76,195,265,265,265,264,265,265,265,265,190,195,207,266,207,267,195,195,137,274,136,102,133,133,134,252,98,275,98,276,97,171,162,99,97,107,98,102,98,159,161,105,97,103,107,104,105,105,127,108,108,105,165,54,108,126
    };

    private Joystick joystick1;
    private TextView angleTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //beinflate-elem a fragment-be az xml tartalmát
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        angleTextView = (TextView)view.findViewById(R.id.angleTextView);

        joystick1 = (Joystick)view.findViewById(R.id.joystick_1);

        joystick1.setJoystickEventListener(new JoystickEventListener() {
            @Override
            public void onPositionChange(float x, float y, float deg) {
                angleTextView.setText(getString(R.string.fegyver_szog) + Float.toString(deg));
            }

            @Override
            public void onJoystickReleased() {

            }

            @Override
            public void onJoystickTouched() {

            }
        });
        // To dismiss the dialog
        return view;
    }

    private UltrasonicView uv;
    private int testSzog;
    private int irany;
    private Timer idozito;
    private TimerTask feladat;
    private int elteltIdo;
    private float testTav;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //beazonosítjuk az ultrahang nézetet
        uv=(UltrasonicView) view.findViewById(R.id.ultrahang);

        //megadjuk a teszteléshez a kezdeti értékeket
        testSzog=0;
        irany=1;
        elteltIdo=0;

        //létrehozzuk az időzítőt amely adagolja az adatokat
        idozito = new Timer();
        feladat = new TimerTask() {
            @Override
            public void run() {
                if(elteltIdo>=tavolsagok.length){
                    elteltIdo=0;
                }
                testTav=tavolsagok[elteltIdo];

                uv.UjErtek(testSzog,testTav, irany);
                testSzog+=irany;
                if(testSzog==119 || testSzog==0) irany=-irany;
                elteltIdo++;
            }
        };

        // az időzítő elindítása
        idozito.schedule(feladat,0,50);
    }
}
