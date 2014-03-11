package no.nte.restapi.client;
import no.nte.restapi.client.params.DownloadParams;
import org.jdom.JDOMException;

import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Example of object that uses the general {@link HypermediaClient} for a specific purpose.
 */
public class DownloadMeterReadingClient {
    private final int NR_OF_TRIES = 4;


    /**
     * Uses {@link HypermediaClient} to navigate the API like a browser by submitting forms and
     * clicking links
     * @param downloadParams
     * @return list of downloadlocations for later retrieval
     * @throws java.io.IOException
     */
    public List<URL> getMeterReadings(DownloadParams downloadParams) throws IOException {
        List<URL> locations = new LinkedList<URL>();
        HypermediaClient client = new HypermediaClient(downloadParams.getClientParams());
        try {
            boolean initialize = client.initialize();
            if (initialize) {
                for(String meterKey: downloadParams.getMeterKeys()){
                    //submit "meterQuery"-form
                    HashMap<String, String> meterQueryFormParams = new HashMap<String, String>();
                    meterQueryFormParams.put("meterKey", meterKey);
                    client.submitForm("meterQuery", meterQueryFormParams);

                    //client has moved to new state/new page. Same as browser does when submitting form
                    //submit "meterReadingOrder"-form
                    HashMap<String, String> meterReadingOrderFormParams = new HashMap<String, String>();
                    meterReadingOrderFormParams.put("seriesType", downloadParams.getSeriesType());
                    meterReadingOrderFormParams.put("dateFrom", downloadParams.getFromDate());
                    meterReadingOrderFormParams.put("dateTo", downloadParams.getToDate());
                    meterReadingOrderFormParams.put("intervalType", downloadParams.getIntervalType());
                    client.submitForm("meterReadingOrder", meterReadingOrderFormParams);

                    //again, client has moved to new state/new page
                    //get location of ordered meterreadings
                    locations.add(new URL(client.getResponse().getFirstHeader("Location").getValue()));

                    //navigate to up to meterpage
                    //could have done this also: client.followLink("up"); since it is only "up" link on page
                    client.select("https://api.demosteinkjer.no/docs#meterdataorder").followLink("up");

                    //navigate up to start-page, and we are ready to order more data
                    client.select("https://api.demosteinkjer.no/docs#meter").followLink("up");
                }
            } else{
                throw new IOException(client.getResponse().toString());
            }
        } catch (JDOMException e) {
            throw new IOException(e.getMessage());
        }
        return locations;
    }

    /**
     * Requests the actual data with the given mediatype
     * @param location resource to fetch
     * @param userName username
     * @param password password
     * @param preferredMediatype
     * @return the actual data with the given mediatype (if supported)
     * @throws IOException
     * @throws InterruptedException
     */
    public String getData(URL location, String userName, String password, String preferredMediatype) throws IOException, InterruptedException {
        for(int i = 0; i<NR_OF_TRIES; i++){
            URLConnection connection = getConnection(
                    userName,
                    password,
                    preferredMediatype,
                    location);
            connection.connect();
            if (connection instanceof HttpURLConnection) {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                //if request went well, and resource is ready
                if (httpConnection.getResponseCode() == 200) {
                    return getContent(httpConnection);
                }
                else if(httpConnection.getResponseCode() / 400 == 1){
                    throw new IOException(String.format("Client error: %d", httpConnection.getResponseCode()));
                }
                else if(httpConnection.getResponseCode() / 500 == 1){
                    throw new IOException(String.format("Server error: %d", httpConnection.getResponseCode()));
                }
                else{
                    httpConnection.disconnect();
                    Thread.sleep(2 * 1000);
                }
            }
        }
        throw new IOException("Could not connect to location");
    }

    /**
     * Method that gives examples of how to navigate API .
     * It just does some navigation, and prints out the current URL as it traverses
     * @param downloadParams
     * @throws IOException
     * @throws JDOMException
     */
    public void demoNavigation(DownloadParams downloadParams) throws IOException, JDOMException {
        HypermediaClient client = new HypermediaClient(downloadParams.getClientParams());
        boolean initialize = client.initialize();
        if(initialize){
            System.out.println(client.select("http://api.demosteinkjer.no/docs#downloads").followLink("self").getContext());
            client.select("http://api.demosteinkjer.no/docs#download");
            if(client.getNodeCount() > 0){
                System.out.println(client.index(0).followLink("self").getContext());
                System.out.println(client.followLink("up").getContext());
            }
            System.out.println(client.followLink("up").getContext());
            HashMap<String, String> meterQueryFormParams = new HashMap<String, String>();
            meterQueryFormParams.put("meterKey", downloadParams.getMeterKeys().get(0));
            System.out.println(client.submitForm("meterQuery", meterQueryFormParams).getContext());

            HashMap<String, String> meterReadingLatestFormParams = new HashMap<String, String>();
            meterReadingLatestFormParams.put("seriesType", downloadParams.getSeriesType());
            System.out.println(client.submitForm("meterReadingLatest", meterReadingLatestFormParams).getContext());
            System.out.println(client.followLink("up").getContext());
            System.out.println(client.followLink("up").getContext());
        }
    }


    private URLConnection getConnection(String username, String password, String preferredMediaType, URL location) throws IOException {
        URLConnection connection = location.openConnection();
        String usernameAndPassword = username + ":" + password;
        String encoded = DatatypeConverter.printBase64Binary(usernameAndPassword.getBytes());
        connection.setRequestProperty("Authorization", "Basic " + encoded);
        connection.setRequestProperty("Accept", preferredMediaType);
        return connection;
    }

    private String getContent(HttpURLConnection httpConnection) throws IOException {
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(httpConnection.getInputStream()));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            buffer.append(line + " ");
        }
        return buffer.toString();
    }
}

