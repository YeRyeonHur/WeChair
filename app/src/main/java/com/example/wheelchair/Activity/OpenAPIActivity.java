package com.example.wheelchair.Activity;

import android.os.AsyncTask;
import android.util.Log;

import com.example.wheelchair.DTO.MapPointDTO;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class OpenAPIActivity extends AsyncTask<Void, Void, String> {
    private String url;
    public OpenAPIActivity(String url){
        this.url = url;
    }


    @Override
    protected String doInBackground(Void... voids) {
        //String url = "http://apis.data.go.kr/B552584/UlfptcaAlarmInqireSvc/getUlfptcaAlarmInfo?year=2020&pageNo=1&numOfRows=100&returnType=xml&serviceKey=L2VM7f1PPrN4%2FiRYxA9H%2F47FcZ6L8Mp72fB67Gqj0YjzlKQ%2FgmqtTURCNbQf7e2jIaMkdordccx0dbQx3UmPeg%3D%3D"

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document doc = null;
        try {
            doc = dBuilder.parse(url);
        } catch (IOException | SAXException e) {
            e.printStackTrace();
        }

        // root tag
        doc.getDocumentElement().normalize();
        System.out.println("Root element: " + doc.getDocumentElement().getNodeName()); // Root element: result

        NodeList nList = doc.getElementsByTagName("item");

        for(int temp = 0; temp < nList.getLength(); temp++){
            Node nNode = nList.item(temp);
            if(nNode.getNodeType() == Node.ELEMENT_NODE){

                Element eElement = (Element) nNode;
                Log.d("OPEN_API","data Time  : " + getTagValue("dataTime", eElement));
                Log.d("OPEN_API","미세먼지  : " + getTagValue("pm10Value", eElement));
                Log.d("OPEN_API","초미세먼지 : " + getTagValue("pm25Value", eElement));
            }	// for end
        }	// if end


        return null;

    }
    @Override
    protected void onPostExecute(String str) {
        super.onPostExecute(str);
    }

    private String getTagValue(String tag,Element eElement){
        NodeList nlList = eElement.getElementsByTagName(tag).item(0).getChildNodes();
        Node nValue = (Node)nlList.item(0);
        if(nValue==null){
            return null;
        }
        return nValue.getNodeValue();
    }

}