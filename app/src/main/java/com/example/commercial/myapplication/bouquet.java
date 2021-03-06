package com.example.commercial.myapplication;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;


import com.example.commercial.myapplication.api.CreationAsync;
import com.example.commercial.myapplication.api.ImageDownloaderTask;
import com.example.commercial.myapplication.metier.Chaine;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Balthazar on 04/05/2016.
 */
public class bouquet extends ListActivity {
    //creatioon de la liste chaine
    private List<Chaine> chaineList = new ArrayList<Chaine>();
    //adapter pour mettre en page voir le fichier java
    private MyAdapterChaine myAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the layout from video_main.xml
        setContentView(R.layout.bouquet_main);

        chaineList = new ArrayList<Chaine>();

        CreationAsync creationAsync = new CreationAsync(this);
        creationAsync.execute("", "", "http://www.tv.kabtel.com/AjoutVideo.php?");
        int age=getIntent().getIntExtra("age",-1);
        Log.i("agerecu", String.valueOf(age));


        String resultat = null;
        try {
            resultat = creationAsync.get();
//Le JSON est un format qui permet de recuperer le contenue qu fichier php qui lui est aussi dans un format concue pour le JSON le JSON va le recuperer le decomposer corrctement afin de
            // le ranger dans notre liste
            JSONObject jsonObject = new JSONObject(resultat);
            JSONArray jsonArray = new JSONArray(jsonObject.getString("resultat"));
            for (int i = 0; i < jsonArray.length(); i++) {
                String nom = jsonArray.getJSONObject(i).getString("nom");
                String lienChaine = jsonArray.getJSONObject(i).getString("lienChaine");
                String lienLogo = jsonArray.getJSONObject(i).getString("logoChaine");
                Chaine chaine = new Chaine(nom, lienChaine);
                chaine.setLienLogo(lienLogo);
                chaine.setLogo(new ImageView(this));
                chaineList.add(chaine);
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        myAdapter = new MyAdapterChaine(this, chaineList);

        getListView().setAdapter(myAdapter);

        EditText search = (EditText) findViewById(R.id.edit_recherche);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                MyAdapterChaine mac = myAdapter.filter(editable.toString());
                getListView().setAdapter(mac);
            }
        });

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //Toast.makeText(this, this.chaineList.get(position).getNom(), Toast.LENGTH_SHORT).show();
        Chaine c = chaineList.get(position);
        Intent it = new Intent(this, VideoViewActivity.class);
        it.putExtra("lien", c.getLienStream());
        it.putExtra("nom", c.getNom());

        startActivity(it);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        for (int i = 0; i < chaineList.size(); i++) {
            ImageDownloaderTask imagedll = new ImageDownloaderTask(chaineList.get(i).getLogo());
//                imagedll.execute("http://nuclearpixel.com/content/icons/2010-02-09_stellar_icons_from_space_from_2005/earth_128.png");
            imagedll.execute(chaineList.get(i).getLienLogo());
            try {
                Bitmap b = imagedll.get();
                chaineList.get(i).setLogo(b);
                myAdapter = new MyAdapterChaine(this, chaineList);

                getListView().setAdapter(myAdapter);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

}
