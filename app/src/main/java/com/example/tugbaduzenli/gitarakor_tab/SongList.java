package com.example.tugbaduzenli.gitarakor_tab;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SongList extends ActionBarActivity {

    private Toolbar toolbar;
    private ViewPager viewPager;
    private DrawerLayout drawerLayout;
    String sData;
    private ListView listView;
    public String jsonResult;
    public String url = "http://192.168.1.5/select.php";

    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        viewPager = (ViewPager) findViewById(R.id.pager);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        listView = (ListView) findViewById(R.id.listView);

        sData=getIntent().getStringExtra("data");

        nameValuePairs.add(new BasicNameValuePair("name",sData));
        Toast.makeText(getApplicationContext(),sData,Toast.LENGTH_LONG).show();


        JsonReadTask task = new JsonReadTask();
        task.execute(new String[] { url });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_song_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Async Task to access the web
    public class JsonReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(params[0]);
            try {
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            try {
                HttpResponse response = httpclient.execute(httppost);
                jsonResult = inputStreamToString(
                        response.getEntity().getContent()).toString();
            }

            catch (ClientProtocolException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private StringBuilder inputStreamToString(InputStream is) {
            String rLine = "";
            StringBuilder answer = new StringBuilder();
            BufferedReader rd = null;
            try {
                rd = new BufferedReader(new InputStreamReader(is,"UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
                while ((rLine = rd.readLine()) != null) {
                    answer.append(rLine);
                }
            }


            catch (IOException e) {
                // e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        "Error..." + e.toString(), Toast.LENGTH_LONG).show();

            }
            return answer;
        }

        @Override
        protected void onPostExecute(String result) {
            ListBuild();
        }
    }// end async task

    // build hash set for list view
    public void ListBuild() {
        final List<Map<String, String>> songList = new ArrayList<>();

        try {
            JSONObject jsonResponse = new JSONObject(jsonResult);
            final JSONArray jsonMainNode = jsonResponse.optJSONArray("songs");

            for (int i = 0; i < jsonMainNode.length(); i++) {
                final JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);

                final String singername = jsonChildNode.optString("singername");
                final String name = jsonChildNode.optString("name");

                String outPut = singername +"-"+ name;
                songList.add(createEmployee("song", outPut));


                SimpleAdapter simpleAdapter = new SimpleAdapter(this, songList,
                        android.R.layout.simple_list_item_1,
                        new String[] { "song" }, new int[] { android.R.id.text1 });
                listView.setAdapter(simpleAdapter);


                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                        JSONObject jsonChildNode = null;
                        try {
                            jsonChildNode = jsonMainNode.getJSONObject(position);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String dataId=jsonChildNode.optString("id");

                        Intent intent = new Intent(SongList.this,SongContent.class);
                        intent.putExtra("songId",dataId);
                        startActivity(intent);
                    }
                });
            }

        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Error" + e.toString(),
                    Toast.LENGTH_SHORT).show();
        }


    }

    private HashMap<String, String> createEmployee(String singername, String name) {
        HashMap<String, String> songName = new HashMap<String, String >();
        songName.put(singername, name);
        return songName;
    }

}
