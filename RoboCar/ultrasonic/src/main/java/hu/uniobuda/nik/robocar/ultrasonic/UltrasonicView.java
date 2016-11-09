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
        aktSzog=szg;
        adatok.set(szg, tav);

        int kulonbseg=0;

        //a haladási iránytól függően beállítjuk az új értékeket a jelölőhöz és a jelölő kis háromszöghöz
        if(ir==1) {
            if (szg <= JELOLO_HAROMSZOG_MERETE / 2) {
                hatsoSzog=0;
                elsoSzog=aktSzog+ JELOLO_HAROMSZOG_MERETE /2;
            } else if (szg>= szog- JELOLO_HAROMSZOG_MERETE /2){
                elsoSzog=szog;
                //hatsoSzog=aktSzog-JELOLO_HAROMSZOG_MERETE/2;
                hatsoSzog=aktSzog-(szog-aktSzog);
            } else {
                elsoSzog=aktSzog+ JELOLO_HAROMSZOG_MERETE /2;
                hatsoSzog=aktSzog- JELOLO_HAROMSZOG_MERETE /2;
            }
        } else {
            if (szg >= szog - JELOLO_HAROMSZOG_MERETE / 2) {
                hatsoSzog=szog;
                elsoSzog=aktSzog- JELOLO_HAROMSZOG_MERETE /2;
            } else  if (szg <= JELOLO_HAROMSZOG_MERETE / 2) {
                elsoSzog=0;
                //hatsoSzog=aktSzog+JELOLO_HAROMSZOG_MERETE/2;
                hatsoSzog=aktSzog+(0+aktSzog);
            } else {
                elsoSzog=aktSzog- JELOLO_HAROMSZOG_MERETE /2;
                hatsoSzog=aktSzog+ JELOLO_HAROMSZOG_MERETE /2;
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

        int kezdSzog=180+((180-szog)/2);
        int vegSzog=kezdSzog+szog;

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
            double parameter = (180+i+30) * Math.PI / 180;
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
        int startY =mag;
        haromszogUtvonal.reset();
        haromszogUtvonal.moveTo(startX + VIZSZINTES_MARGO / 2, startY + FUGGOLEGES_MARGO);
        parameter = (180+elsoSzog+30) * Math.PI / 180;
        stopX2 = (float) (szel/2 + szel/2 * Math.cos(parameter));
        stopY2 = (float) (mag + mag * Math.sin(parameter)); //(mag/2)*2, azaz mag kell, mert fent az eltolás miatt 2mag van
        haromszogUtvonal.lineTo(stopX2 + VIZSZINTES_MARGO / 2, stopY2 + FUGGOLEGES_MARGO);
        parameter = (180+hatsoSzog+30) * Math.PI / 180;
        stopX2 = (float) (szel/2 + szel/2 * Math.cos(parameter));
        stopY2 = (float) (mag + mag * Math.sin(parameter)); //(mag/2)*2, azaz mag kell, mert fent az eltolás miatt 2mag van
        haromszogUtvonal.lineTo(stopX2 + VIZSZINTES_MARGO / 2, stopY2 + FUGGOLEGES_MARGO);
        canvas.drawPath(haromszogUtvonal, mireFest3);

        //hol tartunk - radar vonal
        mireFest3.setStrokeWidth(7f);
        mireFest3.setColor(mutatoszin);
        parameter = (180+aktSzog+30) * Math.PI / 180;
        stopX2 = (float) (szel/2 + szel/2 * Math.cos(parameter));
        stopY2 = (float) (mag + mag * Math.sin(parameter)); //(mag/2)*2, azaz mag kell, mert fent az eltolás miatt 2mag van
        canvas.drawLine(szel / 2 + VIZSZINTES_MARGO / 2, mag + FUGGOLEGES_MARGO, stopX2 + VIZSZINTES_MARGO / 2, stopY2 + FUGGOLEGES_MARGO, mireFest3);
    }
}
