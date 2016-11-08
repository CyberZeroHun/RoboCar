package hu.uniobuda.nik.ciwsduino;

import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;

/**
 * Created by thecy on 2016. 04. 26..
 */
public class FulFragmenObject {

    //Az objektum szükséges privát változói.
    private Fragment f;
    private String nev;
    private Drawable ikon;

    //Az objektum publikus tulajdonságainak getter-ei és setter-ei.
    public Fragment getF() {
        return f;
    }
    public void setF(Fragment f) {
        this.f = f;
    }
    public String getNev() {
        return nev;
    }
    public void setNev(String nev) {
        this.nev = nev;
    }
    public Drawable getIkon() {
        return ikon;
    }
    public void setIkon(Drawable ikon) {
        this.ikon = ikon;
    }

    //Az objektumot létrehozó konstruktor.
    public FulFragmenObject(Fragment f, String nev, Drawable ikon) {
        this.f = f;
        this.nev = nev;
        this.ikon = ikon;
    }
}
