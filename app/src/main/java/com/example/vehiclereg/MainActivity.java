package com.example.vehiclereg;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.http.HttpHeaders;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static Object URLEncodedUtils;
    private static Object HttpStatus;

    private static Map<String, String> getData(String str) throws MalformedURLException {

        Map<String, String> resultMap = new HashMap<>();
        String str2 = "";
        Matcher matcher = Pattern.compile("(\\w{2})(\\d{2})(\\D*?)(\\d{1,4})").matcher(str);
//        if (matcher.matches()) {
//            str2 = matcher.replaceFirst("$1$2$3-$4");
//        }
        try {
            String[] split = str2.split("-");
            String str3 = split[0];
            str = split[1];
            try {
                URL url = new URL("https://parivahan.gov.in/rcdlstatus/?pur_cd=102");

                Connection.Response execute = Jsoup.connect("https://parivahan.gov.in/rcdlstatus/?pur_cd=102")
                        .validateTLSCertificates(false)   // The site is secure now and it is expected to always validate cert
                        .followRedirects(true)
                        .ignoreHttpErrors(true)
                        .method(Connection.Method.GET).execute();
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                if (execute.statusCode() <= HttpURLConnection.HTTP_BAD_GATEWAY) {
                    Map<String, String> cookies = execute.cookies();
                    Document parse = Jsoup.parse(execute.body());
                    Element first = parse.getElementsByAttributeValue("name", "javax.faces.ViewState").first();
                    if (first == null) {
                        first = parse.getElementById("j_id1:javax.faces.ViewState:0");
                    }
                    String attr = first.attr("value");
                    String trim = Jsoup.parse(execute.body())
                            .getElementsByAttributeValueStarting("id", "form_rcdl:j_idt")
                            .select("button")
                            .get(0).attr("id").trim();

                    str = Jsoup.connect("https://parivahan.gov.in/rcdlstatus/vahan/rcDlHome.xhtml")
                            //.validateTLSCertificates(false)   // The site is secure now and it is expected to always validate cert
                            .followRedirects(true)
                            .method(Connection.Method.POST)
                            .cookies(cookies)
                            .referrer("https://parivahan.gov.in/rcdlstatus/?pur_cd=102")
                            .header("Content-Type", "application/x-www-form-urlencoded")
                            .header("Host", "parivahan.gov.in")
                            .header(HttpHeaders.ACCEPT, "application/xml, text/xml, */*; q=0.01")
                            .header(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.5")
                            .header(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br")
                            .header("X-Requested-With", "XMLHttpRequest")
                            .header("Faces-Request", "partial/ajax")
                            .header("Origin", "https://parivahan.gov.in")
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36")
                            .data("javax.faces.partial.ajax", "true")
                            .data("javax.faces.source", trim)
                            .data("javax.faces.partial.execute", "@all")
                            .data("javax.faces.partial.render", "form_rcdl:pnl_show form_rcdl:pg_show form_rcdl:rcdl_pnl")
                            .data(trim, trim)
                            .data("form_rcdl", "form_rcdl")
                            .data("form_rcdl:tf_reg_no1", str3)
                            .data("form_rcdl:tf_reg_no2", str)
                            .data("javax.faces.ViewState", attr)
                            .execute()
                            .body();

                    if (str.contains("Registration No. does not exist!!!")) {
                        resultMap.put(str, "No Record(s) Found");
                    } else {
                        String htmlString = "<!DOCTYPE html><html><body>" +
                                str.substring(str.indexOf("<table"), str.lastIndexOf("</table>")) +
                                "</body></html>";
                        Document parse2 = Jsoup.parse(htmlString);

                        Element first2 = parse2.select("table").first();
                        if (first2 != null) {
                            Elements select = first2.select("tr");
                            for (Element element : select) {
                                Elements select2 = element.select("td");
                                if (select2.size() == 2) {
                                    String key = select2.get(0).text().replace(":", "");
                                    resultMap.put(key, select2.get(1).text());
                                } else if (select2.size() == 4) {
                                    String key = select2.get(0).text().replace(":", "");
                                    resultMap.put(key, select2.get(1).text());

                                    key = select2.get(2).text().replace(":", "");
                                    resultMap.put(key, select2.get(3).text());
                                }
                            }
                        } else {
                            resultMap.put(str, "No Record(s) Found");
                        }
                    }
                } else {
                    resultMap.put(str, "500 Server Error");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } catch (Exception unused) {
            resultMap.put(str, "Server Error");
        }

        return resultMap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView msg = (TextView) findViewById(R.id.js);
        String utOrState = "MP";

        //  Must be 2 letters and numbers only    (District serial number)
        String districtNumber = "04";

        //  optional and can be blank             (Optional code for additional sub-category needs)
        String additionalCode = "UE";

        //  Must be 4 letters and numbers only    (Vehicle serial number)
        String serialNumber = "2954";
        String nos = utOrState + districtNumber + additionalCode + serialNumber;
        Map<String, String> data = null;
        try {
            data = getData(nos);
            JSONObject json = new JSONObject(data);
            msg.setText(json.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        //  System.out.println(json);
    }
}
