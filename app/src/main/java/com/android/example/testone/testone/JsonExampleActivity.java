/*
 * Copyright 2014 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.example.testone.testone;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.GridView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * This example demonstrates how to import RealmObjects as JSON. Realm supports JSON represented
 * as Strings, JSONObject, JSONArray or InputStreams (from API 11+)
 */
public class JsonExampleActivity extends Activity {

    private GridView mGridView;
    private TownshipAdapter mAdapter;
    private Realm realm;
    private String apiurl = "http://mmrd.herokuapp.com/api/townships";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realm_example);

        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        Realm.deleteRealm(realmConfiguration);
        realm = Realm.getInstance(realmConfiguration);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Load from file "cities.json" first time
        if(mAdapter == null) {
            List<Township> townships = null;
            try {
                townships = loadTownships();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //This is the GridView adapter
            mAdapter = new TownshipAdapter(this);
            mAdapter.setData(townships);

            //This is the GridView which will display the list of cities
            mGridView = (GridView) findViewById(R.id.cities_list);
            mGridView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            mGridView.invalidate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    public List<City> loadCities() throws IOException {

        loadJsonFromStream();
        loadJsonFromJsonObject();
        loadJsonFromString();

        return realm.where(City.class).findAll();
    }

    public List<Township> loadTownships() throws IOException {

        loadJsonUrlFromStream();
        //loadJsonFromJsonObject();
        //loadJsonFromString();

        return realm.where(Township.class).findAll();
    }

    private void loadJsonFromStream() throws IOException {
        // Use streams if you are worried about the size of the JSON whether it was persisted on disk
        // or received from the network.
        InputStream stream = getAssets().open("cities.json");

        // Open a transaction to store items into the realm
        realm.beginTransaction();
        try {
            realm.createAllFromJson(City.class, stream);
            realm.commitTransaction();
        } catch (IOException e) {
            // Remember to cancel the transaction if anything goes wrong.
            realm.cancelTransaction();
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private void loadJsonUrlFromStream() throws IOException {
        // Use streams if you are worried about the size of the JSON whether it was persisted on disk
        // or received from the network.

        Ion.with(this)
                .load(apiurl)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override public void onCompleted(Exception e, final Response<String> result) {
                        try {
                            if (e != null) throw e;
                            switch (result.getHeaders().getResponseCode()) {
                                case 200:

                                    InputStream stream =
                                            new ByteArrayInputStream(result.getResult().getBytes("UTF-8"));

                                    // Open a transaction to store items into the realm
                                    realm.beginTransaction();
                                    try {
                                        realm.createAllFromJson(Township.class, stream);
                                        realm.commitTransaction();
                                    } catch (IOException e1) {
                                        // Remember to cancel the transaction if anything goes wrong.
                                        realm.cancelTransaction();
                                    } finally {
                                        if (stream != null) {
                                            stream.close();
                                        }
                                    }
                                    /*new AsyncTask<Void, String, Void>() {
                                        @Override protected Void doInBackground(Void... params) {
                                            try {



                                            } catch (IOException e1) {
                                                e1.printStackTrace();
                                            }
                                            return null;
                                        }

                                        @Override protected void onProgressUpdate(String... values) {
                                            super.onProgressUpdate(values);
                                        }

                                        @Override protected void onPostExecute(Void aVoid) {
                                            super.onPostExecute(aVoid);

                                        }
                                    }.execute();

                                    break;*/
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });
    }

    private void loadJsonFromJsonObject() {
        Map<String, String> city = new HashMap<String, String>();
        city.put("name", "København");
        city.put("votes", "9");
        final JSONObject json = new JSONObject(city);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObjectFromJson(City.class, json);
            }
        });
    }

    private void loadJsonFromString() {
        final String json = "{ name: \"Aarhus\", votes: 99 }";

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.createObjectFromJson(City.class, json);
            }
        });
    }
}
