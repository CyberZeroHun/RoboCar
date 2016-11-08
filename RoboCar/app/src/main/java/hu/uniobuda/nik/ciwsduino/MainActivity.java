package hu.uniobuda.nik.ciwsduino;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import static hu.uniobuda.nik.ciwsduino.GlobalisKonstansok.*;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    /* TODO:
    Átkerültünk ide a SplashActivity-ről.
    Ha app-ot váltunk, majd visszajövünk ide, akkor nincs gond, ez az Activity jön be ismét.
    De ha itt nyomunk egy visszát, akkor lekicsinyíti az appot, majd ha
    így jövünk vissza bele, akkor újból a SplashScreen töltődik-be.
    Ezt javítani kell még.
    Jelenleg úgy oldom meg, hogy ezen az Activity-n felülírtam a Back működését.
    */

    //Ebben tároljuk majd a fülekhez szükséges objektumokat.
    private ArrayList<FulFragmenObject> ffo;

    //SectionPager a fülekhez és ViewPager a nézetek swipeolásához
    private FulPagerAdapter fpa;
    private ViewPager vp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Betöltjük a main képernyő elrendezését az xml-ből.
        setContentView(R.layout.activity_main);

        //Példányosítjuk a listát és felvesszük bele a füleket.
        ffo = new ArrayList<FulFragmenObject>();
        ffo.add(
                new FulFragmenObject(
                        new MainFragment(),
                        this.getResources().getString(R.string.maintab),
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_ultrahang_feher)

                )
        );
        ffo.add(
                new FulFragmenObject(
                        new NevjegyFragment(),
                        this.getResources().getString(R.string.nevjegy),
                        ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_nevjegy_feher)

                )
        );

        //Beazonosítjuk amit kell majd használnunk a layout-ban.
        Toolbar tb = (Toolbar) findViewById(R.id.eszkozsor);
        vp = (ViewPager) findViewById(R.id.lapozo);
        TabLayout tl = (TabLayout) findViewById(R.id.fulek);

        //A toolbar beállítása.
        setSupportActionBar(tb);

        /*
        A fragmenteket kezelő menedzseren keresztül
        érjük el a fülek fragmentjeit a későbbiekben.
        */
        fpa = new FulPagerAdapter(getSupportFragmentManager(), ffo);


        //A viewpager beállítása.
        vp.setAdapter(fpa);

        //A TabLayout, amely a füleket fogja tartalmazni.
        tl.setupWithViewPager(vp);

    }

    /*
    Jelenleg ezzel védem ki, hogy ne lehessen vissza gombra kimenni a semmibe
    erről az Activity-ről. Mivel felülírom a megnyomásnak a viselkedését úgy,
    hogy ne történjen semmi, ezért a fenti TO-DO megoldódott.
    */
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        return;
    }
}
