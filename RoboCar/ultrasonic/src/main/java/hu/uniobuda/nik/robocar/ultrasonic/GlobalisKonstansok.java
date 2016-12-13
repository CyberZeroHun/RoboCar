package hu.uniobuda.nik.robocar.ultrasonic;

/**
 * Created by thecy on 2016. 05. 05..
 */
public interface GlobalisKonstansok {
    //Ultasonic Custom View
    //a maximumok
    public final static int MAX_TAV = 500; //500cm
    public final static int MAX_SZOG = 180; //120° MAX_ELEM

    //az alapértelmezett értékek
    public final static int TAV = 500; //500cm
    public final static int ALAP_SZOG = 120; //120° MAX_ELEM
    /*
    Kezdetben léptékkel számoltam, de mostmár inkább az osztás alapján
    számolja a léptéket.
    public final static int SZOG_LEPTEK = 30;
    public final static int TAV_LEPTEK = 50;
    */
    public final static int ALAP_TAV_OSZTAS = 10;
    public final static int ALAP_SZOG_OSZTAS = 4;

    //ha van kis radar háromszög, ami jelöli, hogy hol tartunk
    public final static int JELOLO_HAROMSZOG_MERETE = 10;

    //pozícionoálást segítő margók és eltolások
    public final static int VIZSZINTES_MARGO =170;
    public final static int FUGGOLEGES_MARGO =130;
    public final static int SZOVEG_VIZSZINTES_OFFSET =60;
    public final static int SZOVEG_FUGGOLEGES_OFFSET =70;
}
