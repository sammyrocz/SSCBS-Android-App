package in.ac.du.sscbs.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemClickListener {

    /*News Activity*/

    ArrayList<String> Links;
    Context c = this;
    ArrayAdapter<String> adapter;
    ListView list;
    RequestQueue queue;
    LinkedHashMap<String, String> data;
    Stack<LinkedHashMap<String, String>> hashdata;
    final String URL = "http://www.sscbs.du.ac.in";

    Progress progress;
    ErrorDialogMessage errorDialogMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle("News");
        setSupportActionBar(toolbar);

        errorDialogMessage = new ErrorDialogMessage(this);
        progress = new Progress(this);
        progress.show();


        hashdata = new Stack<LinkedHashMap<String, String>>();
        queue = VolleySingleton.getInstance().getRequestQueue();
        list = (ListView) findViewById(R.id.lv_news);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        final StringRequest firstReq = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Document d = Jsoup.parse(response);
                Elements mainPageLinks = d.select("div.gn_browser div.gn_news");

                Links = new ArrayList<String>();

                Elements redirectingLinks = mainPageLinks.select("a[href]");

                data = new LinkedHashMap<String, String>();

                for (Element temp : redirectingLinks) {


                    String tempText = temp.text();
                    if (tempText.length() > 0) {
                        Links.add(temp.text());
                        data.put(temp.text(), temp.attr("abs:href"));
                    }
                }


                hashdata.push(data);

                adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_list_item_1, Links);
                list.setAdapter(adapter);
                progress.stop();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {

                    progress.stop();
                    errorDialogMessage.show();


               }
        });

        firstReq.setTag("News");

        queue.add(firstReq);
        list.setOnItemClickListener(this);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (hashdata.size() == 2) {

            LinkedHashMap<String, String> popped = hashdata.pop();
            Links.clear();


            Set set = hashdata.peek().entrySet();
            Iterator i = set.iterator();
            while (i.hasNext()) {
                Map.Entry me = (Map.Entry) i.next();
                Links.add(me.getKey().toString());
            }

            adapter.notifyDataSetChanged();


        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Intent intent = null;
        if (id == R.id.nav_notices) {

            intent = new Intent(MainActivity.this, Notices.class);
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {


        } else if (id == R.id.nav_aboutus) {

            intent = new Intent(MainActivity.this, AboutUs.class);
        }
        startActivity(intent);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        TextView tv = (TextView) view;

        String s = tv.getText().toString();
        if (!hashdata.empty() && hashdata.size() == 1) {

            progress.show();

            LinkedHashMap<String, String> temp = hashdata.peek();

            String link = temp.get(s);

            final StringRequest linksRequest = new StringRequest(Request.Method.GET, link, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {


                    data = new LinkedHashMap<String, String>();
                    Links.clear();
                    adapter.clear();

                    LinkedHashMap<String, String> temphash = new LinkedHashMap<String, String>();
                    Document d = Jsoup.parse(response);
                    Elements InsideLinks = d.select("div.item-page a[href]");

                    for (Element temp : InsideLinks) {

                        String t = temp.text();
                        if (t.length() > 0) {

                            Links.add(t);
                            temphash.put(t, temp.attr("abs:href"));
                        }
                    }


                    hashdata.push(temphash);
                    adapter.notifyDataSetChanged();
                    progress.stop();
                }


            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError e) {
                    progress.stop();
                    errorDialogMessage.show();
                }
            });


            linksRequest.setTag("News");
            queue.add(linksRequest);
        } else {


            if (!hashdata.empty()) {


                LinkedHashMap<String, String> temp = hashdata.peek();

                String link = temp.get(s);

                Intent openBrowser = new Intent(Intent.ACTION_VIEW);
                openBrowser.setData(Uri.parse(link));
                startActivity(openBrowser);


            }

        }


    }
}
