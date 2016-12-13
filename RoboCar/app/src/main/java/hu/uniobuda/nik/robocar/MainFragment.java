package hu.uniobuda.nik.robocar;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Timestamp;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import hu.uniobuda.nik.joystick.ArrowKeys;
import hu.uniobuda.nik.joystick.OnArrowKeyClickListener;
import hu.uniobuda.nik.robocar.ultrasonic.UltrasonicView;
import hu.uniobuda.nik.joystick.Joystick;
import hu.uniobuda.nik.joystick.JoystickEventListener;

/**
 * Created by thecy on 2016. 04. 25..
 */
public class MainFragment extends Fragment {

    //a bluetooth példány
    BluetoothSPP bt = null;

    public void blueToothCsatlakozas(Context c){
        if(!bt.isBluetoothAvailable()) {
            //ha nincs bt a telefonban hiba
            Toast.makeText(c, getResources().getString(R.string.hiba1), Toast.LENGTH_SHORT).show();
            //finish();
            return;
        } else if(!bt.isBluetoothEnabled()){
            //ha nincs még bekapcsolva
            bt.enable();
        } else {
            if(!bt.isServiceAvailable()) {
                bt.setupService();
            }
            bt.startService(BluetoothState.DEVICE_OTHER);
            bt.autoConnect("RoboCarBT");
        }
    }

    private Joystick joystick1;
    private Joystick joystick2;
    private TextView angleTextView;
    private TextView speedTextView;
    private ArrowKeys arrowControl;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //beinflate-elem a fragment-be az xml tartalmát
        final View view = inflater.inflate(R.layout.fragment_main, container, false);
        //final Context ctx = view.getContext();
        final Context ctx = getActivity().getApplicationContext();

        //a bluetoothoz
        bt = new BluetoothSPP(ctx);
        bt.setAutoConnectionListener(new BluetoothSPP.AutoConnectionListener() {
            @Override
            public void onAutoConnectionStarted() {

            }

            @Override
            public void onNewConnection(String name, String address) {

            }
        });

        bt.setBluetoothStateListener(new BluetoothSPP.BluetoothStateListener() {
            @Override
            public void onServiceStateChanged(int state) {
                if(state == BluetoothState.STATE_CONNECTED) {
                    Toast.makeText(ctx, getResources().getString(R.string.uzenet1), Toast.LENGTH_SHORT).show();
                    //ezzel próbálunk gyorsítani, hogy ezt kikapcsoljuk
                    //bt.stopAutoConnect();
                    //bt.cancelDiscovery();
                }
            }
        });

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                //itt kezelem le a kapott üzenetet
                //amit itt megkapunk az tuti egy üzenet, szóval nem teljesen úgy kell lekezelni, mint Arduino-n

                /*
                csomag kezdés jel >
                csomag lezáró jel |
                adat szeparátor jel #

                minden üzenetnek van csomagtípusa!!!
                csomagtípusok:
                kimenők:
                0: tesztüzenet: üzenet
                1: motorvezértlő: szög
                2: motorvezérlő: sebesség
                4: menny előre ~ 42cm-t: nincs paraméter
                5: menny hátra ~ 42cm-t: nincs paraméter
                6: fordulj balra ~ 90°-ot: nincs paraméter
                7: fordulj jobbra ~ 90°-ot: nincs paraméter
                bejovok:
                3: ultrahang: szög(fok), távolság(cm), irány (merre tart a radar)
                    irányból 1 az alap, -1 ha visszafordul

                tehát a csomag maximum 4 részből áll
                típusa, plusz 3 adat

                csomag szerkezete:
                >csomagtípus#adat1#adat2#adatn|
               */

                String bejovoAdat = message;
                //Log.d("Teszt",message);
                int vanKezdo = bejovoAdat.indexOf('>');
                int vanLezaro = bejovoAdat.indexOf('|');
                while(vanLezaro > -1){
                    if(vanKezdo > -1){
                        //tömb létrehozása a csomagnak
                        //annyi a hossza ahány a csomag maximálix elemszáma
                        String[] csomag = new String[4];
                        //van lezáró és kezdő jelünk is, daraboljuk fel az üzenetünket
                        String feldolgozando = bejovoAdat.substring(vanKezdo+1, vanLezaro);
                        //Log.d("Teszt",feldolgozando);
                        int sorszam=0;
                        int honnan=0;
                        int elvalaszto= feldolgozando.indexOf('#',honnan);
                        while(elvalaszto > -1){
                            csomag[sorszam]=feldolgozando.substring(honnan,elvalaszto);
                            //Log.d("Teszt",csomag[sorszam]);
                            //a következő csomgarészhez
                            sorszam++;
                            honnan=elvalaszto+1;
                            elvalaszto= feldolgozando.indexOf('#',honnan);
                        }
                        //ha nincs már elválasztó, akkor ez az utolsó csomagdarabka
                        csomag[sorszam]=feldolgozando.substring(honnan, feldolgozando.length());
                        //Log.d("Teszt",csomag[sorszam]);
                        //levesszük a most feldolgozott bejovő adatot az elejéről
                        bejovoAdat = bejovoAdat.substring(vanLezaro+1,bejovoAdat.length());
                        //kivesszük a parancsot és annak megfelelően cselekszünk
                        String mit;
                        int ellenorizendo=-1; //mínusz értékeket biztosan nem fogok használni
                        try {
                            ellenorizendo = Integer.parseInt(csomag[0]);
                        } catch (Exception e){}
                        if(ellenorizendo>=0) {
                            switch (ellenorizendo) {
                                case 0:
                                    //Ez csak teszt
                                    break;
                                case 3:
                                    try {
                                        //szög, távolság, és hogy éppen melyik irányba tart a radar
                                        //Log.d("Teszt","Szög: "+Integer.parseInt(csomag[1])+" Táv: "+Integer.parseInt(csomag[2])+" Irány:"+Integer.parseInt(csomag[3]));
                                        uv.UjErtek(Integer.parseInt(csomag[1]),Integer.parseInt(csomag[2]), Integer.parseInt(csomag[3]));
                                    } catch (Exception e){}
                                    break;
                                default:
                                    //Ekkor hiba van
                                    break;
                            }
                        }
                    } else {
                        //ha van lezáró jel, de kezdő nincs, akkor a csomag eleje elveszett
                        //töröljük ki az első lezárójelig a bejövő adatot
                        bejovoAdat=bejovoAdat.substring(vanLezaro+1,bejovoAdat.length());
                    }
                    //az új csomaghoz
                    vanKezdo = bejovoAdat.indexOf('>');
                    vanLezaro = bejovoAdat.indexOf('|');
                }
            }
        });

        blueToothCsatlakozas(ctx);

        angleTextView = (TextView)view.findViewById(R.id.angleTextView);
        speedTextView = (TextView)view.findViewById(R.id.speedTextView);

        joystick1 = (Joystick)view.findViewById(R.id.joystick_1);
        joystick2 = (Joystick)view.findViewById(R.id.joystick_2);

        joystick1.setJoystickEventListener(new JoystickEventListener() {
            @Override
            public void onPositionChange(float x, float y, float deg) {
                angleTextView.setText(Float.toString(Math.round(deg)) + "°");

                //csatlakozzon, ha még nincs (ez most mindenféleképpen csatlakozik újra, majd javítjuk)
                blueToothCsatlakozas(ctx);
                //ezzel próbálunk gyorsítani, hogy ezt kikapcsoljuk
                //bt.stopAutoConnect();
                //bt.cancelDiscovery();
                //a szög elküldése bt-on
                String s = ">1#"+Float.toString(Math.round(deg))+"|";
                bt.send(s, false);
                //Log.d("KULD", Calendar.getInstance().getTime().toString());
            }

            @Override
            public void onJoystickReleased() {

            }

            @Override
            public void onJoystickTouched() {

            }
        });

        joystick2.setJoystickEventListener(new JoystickEventListener() {
            @Override
            public void onPositionChange(float x, float y, float deg) {
                speedTextView.setText(Float.toString(Math.round(y * 100)));

                //csatlakozzon, ha még nincs (ez most mindenféleképpen csatlakozik újra, majd javítjuk)
                blueToothCsatlakozas(ctx);
                //ezzel próbálunk gyorsítani, hogy ezt kikapcsoljuk
                //bt.stopAutoConnect();
                //bt.cancelDiscovery();
                //a sebesség elküldése bt-on
                String s = ">2#"+Float.toString(Math.round(y * 100))+"|";
                bt.send(s, false);
                //Log.d("KULD", Calendar.getInstance().getTime().toString());
            }

            @Override
            public void onJoystickReleased() {

            }

            @Override
            public void onJoystickTouched() {

            }
        });

        arrowControl = (ArrowKeys) view.findViewById(R.id.vezerlo);

        arrowControl.setOnArrowButtonClickListener(new OnArrowKeyClickListener() {
            @Override
            public void onClick(byte direction) {
                if (direction == ArrowKeys.DIRECTION_UP) {
                    //csatlakozzon, ha még nincs (ez most mindenféleképpen csatlakozik újra, majd javítjuk)
                    blueToothCsatlakozas(ctx);
                    //ezzel próbálunk gyorsítani, hogy ezt kikapcsoljuk
                    //bt.stopAutoConnect();
                    //bt.cancelDiscovery();
                    //a előre léptetés elküldése bt-on
                    String s = ">4|";
                    bt.send(s, false);
                } else if (direction == ArrowKeys.DIRECTION_DOWN) {
                    //csatlakozzon, ha még nincs (ez most mindenféleképpen csatlakozik újra, majd javítjuk)
                    blueToothCsatlakozas(ctx);
                    //ezzel próbálunk gyorsítani, hogy ezt kikapcsoljuk
                    //bt.stopAutoConnect();
                    //bt.cancelDiscovery();
                    //a hátra léptetés elküldése bt-on
                    String s = ">5|";
                    bt.send(s, false);
                } else if (direction == ArrowKeys.DIRECTION_LEFT) {
                    //csatlakozzon, ha még nincs (ez most mindenféleképpen csatlakozik újra, majd javítjuk)
                    blueToothCsatlakozas(ctx);
                    //ezzel próbálunk gyorsítani, hogy ezt kikapcsoljuk
                    //bt.stopAutoConnect();
                    //bt.cancelDiscovery();
                    //a balra forgatás elküldése bt-on
                    String s = ">6|";
                    bt.send(s, false);
                } else if (direction == ArrowKeys.DIRECTION_RIGHT) {
                    //csatlakozzon, ha még nincs (ez most mindenféleképpen csatlakozik újra, majd javítjuk)
                    blueToothCsatlakozas(ctx);
                    //ezzel próbálunk gyorsítani, hogy ezt kikapcsoljuk
                    //bt.stopAutoConnect();
                    //bt.cancelDiscovery();
                    //a jobbra forgatás elküldése bt-on
                    String s = ">7|";
                    bt.send(s, false);
                }
            }
        });

        // To dismiss the dialog
        return view;
    }

    /*
    Teszt adatok, amelyeket folyósón mértünk
    medián szűrő alkalmazásával és visszhangot csökkentő hengerrel.
    A Bluetooth kapcsolatot helyettesítjük a példában a teszt adatokkal.

    private int testSzog, testTartomany, testSzogKezdet, testSzogVeg;
    private int irany;
    private Timer idozito;
    private TimerTask feladat;
    private int elteltIdo;
    private float testTav;

    //180 fokos teszttömb: 2*180 elem (0..179 --> 180 szög)
    private int tavolsagok[] = {
            1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,
            21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,
            41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,
            61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,
            81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,
            101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,
            121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,
            141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,
            161,162,163,164,165,166,167,168,169,170,171,172,173,174,175,176,177,178,179,
            1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,
            21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,
            41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,
            61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,
            81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,
            101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,
            121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,
            141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,
            161,162,163,164,165,166,167,168,169,170,171,172,173,174,175,176,177,178,179
    };
    */

    private UltrasonicView uv;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //beazonosítjuk az ultrahang nézetet
        uv=(UltrasonicView) view.findViewById(R.id.ultrahang);
        /*
        //Ezek csak teszt adatok
        //megadjuk a teszteléshez a kezdeti értékeket
        testSzogKezdet=30;
        testSzog=30;
        testTartomany=120;
        testSzogVeg=testSzogKezdet+testTartomany-1;
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
                if(testSzog==testSzogVeg || testSzog==testSzogKezdet){
                    irany=-irany;
                }
                //ha azt akarjuk, ha megálljon, ha egyszer eljutott az egyik szélétől a másikig
                //if(testSzog==180) feladat.cancel();
                elteltIdo++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // az időzítő elindítása
        idozito.schedule(feladat,0,50);
        */
    }
}
