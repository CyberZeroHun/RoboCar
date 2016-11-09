package hu.uniobuda.nik.robocar;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.regex.Pattern;

import static hu.uniobuda.nik.robocar.GlobalisKonstansok.*;

/**
 * Created by thecy on 2016. 04. 25..
 */
public class NevjegyFragment extends Fragment {

    AppCompatTextView tv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //beinflate-elem a fragment-be az xml tartalmát
        return inflater.inflate(R.layout.fragment_nevjegy, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //A TextView, amelynek a szövegével műveleteket fogunk végezni
        tv = (AppCompatTextView) getView().findViewById(R.id.nevjegysz);

        //Különböző szövegek egybeépítése erőforrásokból
        Resources res = getResources();
        String szoveg = String.format(
                    res.getString(R.string.nevjegy_szoveg),
                    res.getString(R.string.app_name),
                    GITHUB_LINK,
                    res.getString(R.string.dev),
                    res.getString(R.string.nev),
                    res.getString(R.string.email),
                    res.getString(R.string.aa),
                    AA_EMAIL,
                    res.getString(R.string.zc),
                    ZC_EMAIL,
                    res.getString(R.string.projekt)
        );

        /*
        Az alábbi rész segítségével formázzuk a TextView kívánt részeit
        */
        int c = ContextCompat.getColor(getContext(), R.color.colorAlap);
        Spannable sp = new SpannableString(szoveg);

        //amiket ki szeretnénk színezni
        String[] s = new String[]{
                res.getString(R.string.app_name)+" "+res.getString(R.string.projekt),
                res.getString(R.string.dev),
                "GitHub Repository:",
                res.getString(R.string.nev),
                res.getString(R.string.email)
        };
        int honnan;
        for (String mit:s) {
            //az aktuális szó előfordulásait bejárjuk a szövegben
            honnan = -1;
            int hossz = mit.length();
            do {
                honnan = szoveg.indexOf(mit, honnan + 1);
                if (honnan >= 0) {
                    //amelyeket csak alá szeretnénk húzni
                    if( (mit.equals(res.getString(R.string.app_name)+" "+res.getString(R.string.projekt))) || (mit.equals(res.getString(R.string.dev)))){
                        sp.setSpan(new UnderlineSpan(), honnan, honnan + hossz, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        //a többit mind színezni szeretnénk
                        sp.setSpan(new ForegroundColorSpan(c), honnan, honnan + hossz, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            } while (honnan >= 0);
        }

        tv.setText(sp);

        //Hivatkozások linkekké való átalakítása
        Linkify.addLinks(tv, Linkify.WEB_URLS);
        Linkify.addLinks(tv,Pattern.compile(AA_EMAIL),"mailto:");
        Linkify.addLinks(tv,Pattern.compile(ZC_EMAIL),"mailto:");
    }
}
