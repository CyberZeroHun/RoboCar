package hu.uniobuda.nik.robocar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by thecy on 2016. 04. 26..
 */
public class SplashActivity extends AppCompatActivity {

    /*
    A várakozás ideje egy konstans millisecundumokban megadva.
    Ennyi idő telik el, amíg a SplashScreen látszódik.
    Ezután váltunk át a következő activity-nkre.
    */
    private static int VARAKOZAS = 3000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Betöltjük a splash képernyő elrendezését az xml-ből.
        setContentView(R.layout.activity_splash);

        //Létrehozunk egy timer-t, ami a tick esemény után az alábbi kódot hívja meg.
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        //Intent-el meghívjuk a következő Activity-nket.
                        Intent i = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(i);
                        /*
                        Hogy a vissza gombbal se lehessen visszajönni, be is zárjuk ezt a SplashScreen-t.
                        A finish meghívja: pause, stop és destroy-t, tehát teljesen bezárja a SplashActivity-t.
                        */
                        finish();
                    }
                }, VARAKOZAS);
    }
}
