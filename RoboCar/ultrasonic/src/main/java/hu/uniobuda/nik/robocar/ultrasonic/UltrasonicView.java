package hu.uniobuda.nik.robocar.ultrasonic;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

//A globális konstansok importálásához
import static hu.uniobuda.nik.robocar.ultrasonic.GlobalisKonstansok.*;

/**
 * Created by thecy on 2016. 04. 05..
 */
public class UltrasonicView extends View {

    Paint mireFest, mireFest2, mireFest3;
    ArrayList<Float> adatok;
    ArrayList<Point> pontok;
    int aktSzog,elsoSzog, hatsoSzog;
    Path utvonal, haromszogUtvonal;

    int tavolsag, tavolsag_osztas, szog, szog_osztas;
    int vonalszin, szovegszin, mertvonalszin, korvonalszin, mutatoszin, mutatoharomszogszin;

    private void beallitas_kozosresz(){
        //a jelölő háromszöget kezdetben egyenesnek vesszük és a jelölőre állítjuk: első szög, hátsó szög
        aktSzog=(180-szog)/2;
        elsoSzog=aktSzog;
        hatsoSzog=aktSzog;
        //amire festünk
        mireFest=new Paint();
        mireFest2=new Paint();
        mireFest3=new Paint();
        //beállítások, melyek nem változnak
        mireFest.setAntiAlias(true);
        mireFest2.setAntiAlias(true);
        mireFest2.setStrokeJoin(Paint.Join.ROUND);
        mireFest3.setAntiAlias(true);
        //kezdeti feltöltés 0-ákkal
        //a szögek 0-tól vannak a beállított legutolsó szögig, pl most 150
        adatok=new ArrayList<Float>(szog);
        for (int i=0;i<szog;i++){
            adatok.add(0f);
        }
        pontok=new ArrayList<Point>(szog);
        for (int i=0;i<szog;i++){
            pontok.add(new Point(0, 0));
        }
        //az útvonalakhoz
        utvonal=new Path();
        haromszogUtvonal=new Path();
    }

    private void beallitas(Context context){
        /*
        Amikot nincsenek attribútumok, akkor előre definiált
        konstansokat használunk.
        */
        tavolsag = MAX_TAV;
        tavolsag_osztas = ALAP_TAV_OSZTAS;
        szog = MAX_SZOG;
        szog_osztas = ALAP_SZOG_OSZTAS;
        vonalszin = Color.BLACK;
        szovegszin = Color.BLACK;
        mertvonalszin = Color.BLACK;
        korvonalszin = Color.BLACK;
        mutatoszin = Color.BLACK;
        mutatoharomszogszin = Color.argb(125,0,0,0);
        //utána meghívjuk, ami mindkét konstruktorban közös
        beallitas_kozosresz();
    }

    private void beallitas(Context context, AttributeSet attrs){
        /*
        Amikor vannak attribútumok, akkor megpróbljuk belőlük kinyerni
        A szükséges étrékeket
        */
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.UltrasonicView,
                0, 0);
        try {
            tavolsag = a.getInteger(R.styleable.UltrasonicView_tavolsag, TAV);
            tavolsag_osztas = a.getInteger(R.styleable.UltrasonicView_tavolsag_osztok, ALAP_TAV_OSZTAS);
            szog = a.getInteger(R.styleable.UltrasonicView_szog, ALAP_SZOG);
            szog_osztas = a.getInteger(R.styleable.UltrasonicView_szog_osztok, ALAP_SZOG_OSZTAS);
            vonalszin = a.getColor(R.styleable.UltrasonicView_vonal_szin, Color.BLACK);
            szovegszin = a.getColor(R.styleable.UltrasonicView_szoveg_szin, Color.BLACK);
            mertvonalszin = a.getColor(R.styleable.UltrasonicView_mert_vonal_szin, Color.BLACK);
            korvonalszin = a.getColor(R.styleable.UltrasonicView_korvonal_szin, Color.BLACK);
            mutatoszin = a.getColor(R.styleable.UltrasonicView_mutato_szin, Color.BLACK);
            mutatoharomszogszin = a.getColor(R.styleable.UltrasonicView_mutato_haromszog_szin, Color.argb(125, 0, 0, 0));
        } catch (Exception e){
            beallitas(context);
            /*
            Hiba esetén a sima beállítást hívjuk meg, hogy valami azért legyen beállítva.
            De ilyenkor a közös rész kétszer futna le, így egy return-al visszatérünk,
            hogy ne fusson le még egyszer feleslegesen;
            */
            return;
        } finally {
            a.recycle();
        }

        //a mindenkori maximumokat nem haladhatja meg a felhasználó által megadott beállítás
        tavolsag = (tavolsag>MAX_TAV)?MAX_TAV:tavolsag;
        szog = (szog>MAX_SZOG)?MAX_SZOG:szog;


        //utána meghívjuk, ami mindkét konstruktorban közös
        beallitas_kozosresz();
    }

    public UltrasonicView(Context context) {
        super(context);
        beallitas(context);
    }

    public UltrasonicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        beallitas(context,attrs);
    }

    public UltrasonicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        beallitas(context,attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public UltrasonicView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        beallitas(context,attrs);
    }

    public void UjErtek(int szg, float tav, int ir){
        //mielőtt bármit csinálnánk levágjuk a távnak azon részét, ami már
        //nem jeleníthető meg a skálánkon
        if(tav>tavolsag) tav=tavolsag;
        //felvesszük az adatot (de eltolva)
        int valami=((180-szog)/2); //30
        adatok.set(szg-valami, tav);

        //oda állítja a jelölőt, ahol az új beérkező adat van
        aktSzog=szg;

        //a haladási iránytól függően beállítjuk az új értékeket (első szög, hátsó szög)
        // a jelölőhöz és a jelölő kis háromszöghöz
        int jeloloFele=JELOLO_HAROMSZOG_MERETE / 2;
        //TODO: EZT A RÉSZÉT KELL MAJD MEGCSINÁLNI, EMIATT CSÚSZIK A HATÁROKON
        /*
        if(ir==1) { //balról megyünk jobbra
            if (szg <= jeloloFele) { //a jelölő bal oldali felét még nem tudjuk teljesen kirajzolni
                hatsoSzog = 0;
                elsoSzog = aktSzog + jeloloFele;
            } else if (szg >= szog - jeloloFele){ //a jelölő jobb oldali felét már nem tudjuk teljesen kirajzolni
                elsoSzog = szog;
                //hatsoSzog = aktSzog - jeloloFele;
                hatsoSzog = aktSzog - (szog - aktSzog);
            } else { //mindkét felét ki tudjuk simán rajzolni
                elsoSzog = aktSzog + jeloloFele;
                hatsoSzog = aktSzog - jeloloFele;
            }
        } else { //ugyanaz csak jobbról megyünk balra
            if (szg >= szog - jeloloFele) {
                hatsoSzog = szog;
                elsoSzog = aktSzog - jeloloFele;
            } else  if (szg <= jeloloFele) {
                elsoSzog = 0;
                //hatsoSzog = aktSzog + jeloloFele
                hatsoSzog = aktSzog +(0 + aktSzog);
            } else {
                elsoSzog = aktSzog - jeloloFele;
                hatsoSzog = aktSzog + jeloloFele;
            }
        }
        */
        //
        /*
        balról jobbra, amikor minden kirajzolható: 3
        jobbról balra, amikor minden kirajzolható: 6
        1->3->2 , majd visszafelé 4->6->5
        */
        //szg: az aktuálisan mért szög, most 30..150
        //jeloloFele: most a jelölö 10, így a fele 5
        //szog: a teljes szögtartomány: most 150-30=120

        //a valamivel itt is korrigálnunk kell (a kezdő szöggel)
        if(ir==1) { //balról megyünk jobbra
            //amikor balról jobbra megy, akkor
            //az első szög a jobb oldali, a hátsó szög pedig a bal oldali
            if ((szg-valami) <= jeloloFele) { //a jelölő bal oldali felét még nem tudjuk teljesen kirajzolni
                hatsoSzog = 0+valami;
                elsoSzog = aktSzog + jeloloFele;
                Log.d("Teszt","1) bal(hatsoszog): "+hatsoSzog+" jobb(elsoszog): "+elsoSzog);
            } else if (szg >= szog + valami - jeloloFele){ //a jelölő jobb oldali felét már nem tudjuk teljesen kirajzolni
                elsoSzog = szog+valami;
                //hatsoSzog = aktSzog - jeloloFele;
                hatsoSzog = aktSzog - (szog - aktSzog)-valami;
                Log.d("Teszt","2) bal(hatsoszog): "+hatsoSzog+" jobb(elsoszog): "+elsoSzog);
            } else { //mindkét felét ki tudjuk simán rajzolni
                elsoSzog = aktSzog + jeloloFele;
                hatsoSzog = aktSzog - jeloloFele;
                Log.d("Teszt","3) bal(hatsoszog): "+hatsoSzog+" jobb(elsoszog): "+elsoSzog);
            }
        } else { //ugyanaz csak jobbról megyünk balra
            //amikor jobbról balra megy, akkor
            //az első szög a bal oldali, a hátsó szög pedig a jobb oldali
            if (szg >= szog + valami - jeloloFele) { // a jobb oldali felét még nem
                hatsoSzog = szog+valami;
                elsoSzog = aktSzog - jeloloFele;
                Log.d("Teszt","4) bal(elsoszog): "+elsoSzog+" jobb(hatsoszog): "+hatsoSzog);
            } else  if (szg-valami <= jeloloFele) { // a bal oldali felét már nem
                elsoSzog = 0+valami;
                //hatsoSzog = aktSzog + jeloloFele
                hatsoSzog = aktSzog +(0 + aktSzog)-valami;
                Log.d("Teszt","5) bal(elsoszog): "+elsoSzog+" jobb(hatsoszog): "+hatsoSzog);
            } else { //mindkét felét ki tudja
                elsoSzog = aktSzog - jeloloFele;
                hatsoSzog = aktSzog + jeloloFele;
                Log.d("Teszt","6) bal(elsoszog): "+elsoSzog+" jobb(hatsoszog): "+hatsoSzog);
            }
        }
        //jelezzük, hogy új értékkor újra kell majd rajzoltatni
        //invalidate();
        postInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //olyanokat mérünk csak itt, ami folyton változik
        int szel=getWidth()- VIZSZINTES_MARGO;
        /*
        A magasságnál figyelembe kell vegyük, hogy álló nézetben nagyon megnyúlna a rajzunk.
        Ezért korrigálnunk kell. Számoljunk 16:9 méretaránnyal.
        int mag=getHeight()- FUGGOLEGES_MARGO;
        */
        int mag=getHeight()- FUGGOLEGES_MARGO;
        if(mag>szel) mag=szel/16*9;
        //viszont szélességnél is lehet, hogy elcsúsznak az arányok, ha nincs elég helye
        //így fordítva is visszaszorozzuk
        if(szel>mag) szel=mag/9*16;
        /*
        TODO: De ekkor vízszintesen nem középen lesz a kis ábránk, így el kell toljuk.
        int vizszintes_eltolas_kozepre=getWidth() - VIZSZINTES_MARGO-szel/2;
        */

        //most 120°-os a teljes tartományunk
        // a kezdőszög (180-120)/2)=30°
        // a zárószög pedig 30+120=150°
        // de el kell tolnunk 180°-al, hogy a helyére kerüljön
        //a kezdőt eltoljuk, de mivel a végsőt is ez alapján számítjuk, így az is eltolódik
        //tehát kezdő 210, végső 330°
        int kezdSzog=180+((180-szog)/2); //30
        int vegSzog=kezdSzog+szog; //150

        int scaledSize = getResources().getDimensionPixelSize(R.dimen.ultrasonic_font_size);
        mireFest.setTextSize(scaledSize);
        mireFest.setTextAlign(Paint.Align.CENTER);
        mireFest.setStyle(Paint.Style.STROKE);

        //körívek
        int tav_leptek=tavolsag/tavolsag_osztas;
        for (int tv = 0; tv< TAV; tv+=tav_leptek){ //500cm : 0..500cm
            //mireFest.setColor(Color.GREEN);
            mireFest.setStrokeWidth(7f);
            //mireFest.setColor(ContextCompat.getColor(getContext(),R.color.dark_green));
            mireFest.setColor(vonalszin);
            int param1 = (szel/2)*tv/ TAV;
            int param2 = 2*(mag/2)*tv/ TAV; //ezt kétszer kell venni a dupla magasság miatt
            RectF r = new RectF(param1+ VIZSZINTES_MARGO /2, param2+ FUGGOLEGES_MARGO, szel-param1+ VIZSZINTES_MARGO /2, 2*mag-param2+ FUGGOLEGES_MARGO); //2mag, hogy kitöltse a képet
            canvas.drawArc(r, kezdSzog, szog, false, mireFest);
            //méter szöveg
            param1 = (szel/2)*(tv+tav_leptek)/ TAV; //azért kell +100 mert a következő léptékhez tartozik
            param2 = 2*(mag/2)*(tv+tav_leptek)/ TAV;
            mireFest.setStrokeWidth(2f);
            mireFest.setColor(szovegszin);
            double parameter = kezdSzog * Math.PI / 180;
            float stopX = (float) (szel/2 + param1 * Math.cos(parameter));
            float stopY = (float) (mag + param2 * Math.sin(parameter));
            canvas.drawText(((tv*1f+tav_leptek)/100) + "m", stopX + VIZSZINTES_MARGO / 2- SZOVEG_VIZSZINTES_OFFSET, stopY + FUGGOLEGES_MARGO + SZOVEG_FUGGOLEGES_OFFSET /2, mireFest);
            parameter = vegSzog * Math.PI / 180;
            stopX = (float) (szel/2 + param1 * Math.cos(parameter));
            stopY = (float) (mag + param2 * Math.sin(parameter));
            canvas.drawText(((tv*1f+tav_leptek)/100)+"m",stopX+ VIZSZINTES_MARGO /2+ SZOVEG_VIZSZINTES_OFFSET,stopY+ FUGGOLEGES_MARGO + SZOVEG_FUGGOLEGES_OFFSET /2,mireFest);
        }
        //körcikk vonalak
        int szog_leptek=szog/szog_osztas;
        for (int szg=kezdSzog;szg<=vegSzog;szg+=szog_leptek){ //120° : 29..151°
            //vonal végpont az ellipszis egyenlete
            mireFest.setStrokeWidth(7f);
            mireFest.setColor(vonalszin);
            double parameter = szg * Math.PI / 180;
            float stopX = (float) (szel/2 + szel/2 * Math.cos(parameter));
            float stopY = (float) (mag + mag * Math.sin(parameter)); //(mag/2)*2, azaz mag kell, mert fent az eltolás miatt 2mag van
            canvas.drawLine(szel/2  + VIZSZINTES_MARGO /2, mag+ FUGGOLEGES_MARGO, stopX + VIZSZINTES_MARGO /2, stopY+ FUGGOLEGES_MARGO, mireFest);
            //fok szöveg
            mireFest.setStrokeWidth(2f);
            mireFest.setColor(szovegszin);
            canvas.drawText(szg-180+"°",stopX+ VIZSZINTES_MARGO /2,stopY+ FUGGOLEGES_MARGO - SZOVEG_FUGGOLEGES_OFFSET,mireFest);
        }

        //mert ertekek kirajzolása
        for(int i=0; i<szog; i++){
            mireFest2.setStrokeWidth(7f);
            mireFest2.setColor(mertvonalszin);
            //double parameter = (180+i+30) * Math.PI / 180;
            double parameter = (i+kezdSzog) * Math.PI / 180;
            float stopX = (float) (szel/2 + (adatok.get(i)/ TAV)*szel/2 * Math.cos(parameter));
            float stopY = (float) (mag + (adatok.get(i)/ TAV)*mag * Math.sin(parameter)); //(mag/2)*2, azaz mag kell, mert fent az eltolás miatt 2mag van
            pontok.set(i, new Point((int) stopX, (int) stopY));
            canvas.drawLine(szel / 2 + VIZSZINTES_MARGO / 2, mag + FUGGOLEGES_MARGO, stopX + VIZSZINTES_MARGO / 2, stopY+ FUGGOLEGES_MARGO, mireFest2);
        }

        //mert ertekek egybefüggő
        double parameter = kezdSzog * Math.PI / 180;
        float stopX2 = (float) (szel/2 + szel/2 * Math.cos(parameter));
        float stopY2 = (float) (mag + mag * Math.sin(parameter)); //(mag/2)*2, azaz mag kell, mert fent az eltolás miatt 2mag van
        mireFest2.setColor(korvonalszin);
        mireFest2.setStyle(Paint.Style.STROKE);
        utvonal.reset();
        utvonal.moveTo(stopX2 + VIZSZINTES_MARGO / 2, stopY2 + FUGGOLEGES_MARGO);
        //utvonal.lineTo(stopX2 + VIZSZINTES_MARGO / 2, stopY2 + FUGGOLEGES_MARGO);
        for (int i=0; i<szog; i++){
            utvonal.lineTo((pontok.get(i)).x+ VIZSZINTES_MARGO /2, (pontok.get(i)).y+ FUGGOLEGES_MARGO);
        }
        parameter = vegSzog * Math.PI / 180;
        stopX2 = (float) (szel / 2 + szel / 2 * Math.cos(parameter));
        stopY2 = (float) (mag + mag * Math.sin(parameter)); //(mag/2)*2, azaz mag kell, mert fent az eltolás miatt 2mag van
        utvonal.lineTo(stopX2 + VIZSZINTES_MARGO / 2, stopY2 + FUGGOLEGES_MARGO);
        //utvonal.moveTo(stopX2 + VIZSZINTES_MARGO / 2, stopY2 + FUGGOLEGES_MARGO);
        canvas.drawPath(utvonal, mireFest2);

        //hol tartunk átmenetes radar 3szög
        mireFest3.setStrokeWidth(7f);
        //mireFest3.setColor(0xff40f078);
        //mireFest3.setARGB(125,80,240,120);
        mireFest3.setColor(mutatoharomszogszin);
        mireFest3.setStyle(Paint.Style.FILL_AND_STROKE);
        //mireFest3.setShader(new LinearGradient(0,0,szel,mag,0x00000000,0xff40f078, Shader.TileMode.CLAMP));
        int startX = szel/2;
        int startY = mag;
        haromszogUtvonal.reset();
        haromszogUtvonal.moveTo(startX + VIZSZINTES_MARGO / 2, startY + FUGGOLEGES_MARGO);
        //a jelölő háromszög eleje az alap  skálán van megadva, hogy hol tart, jelen esetben a 0..150-es skálán elsoszognyire
        //korábban már megbeszéltük, hogy 180°-al el kell tolni a szöget, hogy jó legyen
        //a * Math.PI / 180 csak a számításhoz alakít át rad-iánba
        //de akkor mi az a +30???
        //double parameter = (180+elsoSzog+30) * Math.PI / 180;
         parameter = (elsoSzog+180) * Math.PI / 180;
         stopX2 = (float) (szel/2 + szel/2 * Math.cos(parameter));
         stopY2 = (float) (mag + mag * Math.sin(parameter)); //(mag/2)*2, azaz mag kell, mert fent az eltolás miatt 2mag van
        haromszogUtvonal.lineTo(stopX2 + VIZSZINTES_MARGO / 2, stopY2 + FUGGOLEGES_MARGO);
        //a jelölő háromszög eleje
        //parameter = (180+hatsoSzog+30) * Math.PI / 180;
        parameter = (hatsoSzog+180) * Math.PI / 180;
        stopX2 = (float) (szel/2 + szel/2 * Math.cos(parameter));
        stopY2 = (float) (mag + mag * Math.sin(parameter)); //(mag/2)*2, azaz mag kell, mert fent az eltolás miatt 2mag van
        haromszogUtvonal.lineTo(stopX2 + VIZSZINTES_MARGO / 2, stopY2 + FUGGOLEGES_MARGO);
       //kirajzolja
        canvas.drawPath(haromszogUtvonal, mireFest3);

        //hol tartunk - radar vonal
        //ez pedig a jelölő háromszögbe rajzolja a radar vonalát
        mireFest3.setStrokeWidth(7f);
        mireFest3.setColor(mutatoszin);
        //parameter = (180+aktSzog+30) * Math.PI / 180;
        parameter = (aktSzog+180) * Math.PI / 180;
        stopX2 = (float) (szel/2 + szel/2 * Math.cos(parameter));
        stopY2 = (float) (mag + mag * Math.sin(parameter)); //(mag/2)*2, azaz mag kell, mert fent az eltolás miatt 2mag van
        canvas.drawLine(szel / 2 + VIZSZINTES_MARGO / 2, mag + FUGGOLEGES_MARGO, stopX2 + VIZSZINTES_MARGO / 2, stopY2 + FUGGOLEGES_MARGO, mireFest3);
    }
}
