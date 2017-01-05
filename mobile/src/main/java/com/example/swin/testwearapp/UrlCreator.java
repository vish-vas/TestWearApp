package com.example.swin.testwearapp;

import android.content.Context;
import android.util.Log;

import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Mac;
/**
 * Created by Vov on 15/11/16.
 */

public class UrlCreator
{
    final int DEVELOPER_ID = 1000667;
    final String SECURITY_KEY = "badecc32-c54b-11e5-a65e-029db85e733b";
    final String BASE_URL = "https://timetableapi.ptv.vic.gov.au";
    final String route_type = "3"; //route_type "3" means vline transport.
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH'%3A'mm'%3A'ss'Z'");
    private int request_type;

    /**
     * Method to demonstrate building of Timetable API URL
     *
     * @param baseURL - Timetable API base URL without slash at the end ( Example :http://timetableapi.ptv.vic.gov.au )
     * @param privateKey - Developer Key supplied by PTV (((Example :"9c132d31-6a30-4cac- 8d8b-8a1970834799")
     * @param uri - Request URI with parameters(Example :/v2/mode/0/line/8/stop/1104/directionid/0/departures/all/limit/5?for_utc=2014-08-15T06:18:08Z)
     * @param developerId- Developer ID supplied by PTV
     * @return Complete URL with signature
     * @throws Exception
     *
     */
    public String buildTTAPIURL(final String baseURL, final String privateKey, final String uri, final int developerId) throws Exception
    {
        String HMAC_SHA1_ALGORITHM = "HmacSHA1";
        StringBuffer uriWithDeveloperID = new StringBuffer().append(uri).append(uri.contains("?") ? "&" : "?").append("devid=" + developerId);
        byte[] keyBytes = privateKey.getBytes();
        byte[] uriBytes = uriWithDeveloperID.toString().getBytes();
        Key signingKey = new SecretKeySpec(keyBytes, HMAC_SHA1_ALGORITHM);
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        mac.init(signingKey);
        byte[] signatureBytes = mac.doFinal(uriBytes);
        StringBuffer signature = new StringBuffer(signatureBytes.length * 2);
        for (byte signatureByte : signatureBytes)
        {
            int intVal = signatureByte & 0xff;
            if (intVal < 0x10)
            {
                signature.append("0");
            }
            signature.append(Integer.toHexString(intVal));
        }
        StringBuffer url = new StringBuffer(baseURL).append(uri).append(uri.contains("?") ? "&" : "?").append("devid=" + developerId).append("&signature=" + signature.toString().toUpperCase());
        return url.toString();
    }

    //receives integer requestType to distinguish the type of request
    //receives either empty string array or an array containing several parameters required for api request.
    public String createUrl(int requestType, String[] params)
    {
        this.request_type = requestType;
        String requestUri = "";
        switch(requestType)
        {
            case R.string.request_departures:
                String route_id = params[0];
                String stop_id = params[1];
                String direction_id = params[2];
                String date_utc = dateFormat.format(new Date());
                requestUri = "/v3/departures/route_type/"+route_type+"/stop/"+stop_id+"/route/"+route_id+
                        "?direction_id="+direction_id+"&date_utc="+date_utc+"&max_results=3";
                break;
            case R.string.request_stops:
                String route_Id = params[0];
                requestUri =  "/v3/stops/route/"+route_Id+"/route_type/"+route_type;
                break;
            case R.string.request_routes:
                requestUri = "/v3/routes?route_types="+route_type;
                break;
            case R.string.request_direction:
                String route_ID = params[0];
                requestUri = "/v3/directions/route/"+route_ID;
                break;
            default:
                Log.i("UrlCreator - createUrl", "invalid create url request");
        }
        try
        {
            return buildTTAPIURL(BASE_URL, SECURITY_KEY, requestUri, DEVELOPER_ID);
        }
        catch(Exception e)
        {
            Log.i("UrlCreator ", "buildTTAPIURL - Creating url failed"+e.getMessage());
        }
        return null;
    }

    public int getRequest_type() {
        return request_type;
    }
}
