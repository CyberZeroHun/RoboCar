package hu.uniobuda.nik.ciwsduino;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thecy on 2016. 04. 26..
 */
public class FulPagerAdapter extends FragmentPagerAdapter {

    //Át kell vennünk a füleket, amiket ebben fogunk tárolni.
    ArrayList<FulFragmenObject> ffo;

    /*
    Kötelező megvalósítani a konstruktorát.
    Most mi ebben adjuk át neki a füleket.
     */
    public FulPagerAdapter(FragmentManager fm, ArrayList<FulFragmenObject> ffo) {
        super(fm);
        this.ffo = ffo;
    }

    //Kötelező felül írnunk ezt a metódust, amely visszaad egy konkrét fragmentet.
    @Override
    public Fragment getItem(int hanyadik) {
        return ffo.get(hanyadik).getF();
    }

    //Kötelező felül írnunk ezt a metódust, ami megadja a fülek számát.
    @Override
    public int getCount() {
        return ffo.size();
    }

    //Ezt pedig azért írjuk felül, hogy visszaadjuk az oldalak címeit.
    @Override
    public CharSequence getPageTitle(int hanyadik) {
        /* TODO:
        Próbáltam átadni neki képet is többféleképpen is, de nem akarta megjeleníteni.
        A későbbiekben erre még vissza kell térni.
        Az alábbiakban itt a próbált 2 módszer.

        pannableStringBuilder sb = new SpannableStringBuilder(" " + fulNevek.get(position));
        Drawable main_create = ikonok.get(position);
        main_create.setBounds(0, 0, main_create.getIntrinsicWidth(), main_create.getIntrinsicHeight());
        ImageSpan span = new ImageSpan(main_create, ImageSpan.ALIGN_BASELINE);
        sb.setSpan(span, 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;

        SpannableString ss = new SpannableString(" "+fulNevek.get(position));
        ss.setSpan(new ImageSpan(ikonok.get(position), DynamicDrawableSpan.ALIGN_BASELINE),0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
        */
        return ffo.get(hanyadik).getNev();
    }


}
