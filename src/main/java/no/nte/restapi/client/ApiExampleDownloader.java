package no.nte.restapi.client;

import no.nte.restapi.client.params.DownloadParams;

import java.net.URL;
import java.util.List;

/**
 *
 */
public class ApiExampleDownloader {

    public static void main(String[] args) {
        try {
            DownloadParams downloadParams = parseArgs(args);
            DownloadMeterReadingClient readingClient = new DownloadMeterReadingClient();

            List<URL> locations = readingClient.getMeterReadings(downloadParams);
            for(URL dataLocation: locations){
                String data = readingClient.getData(dataLocation, downloadParams.getUsername(), downloadParams.getPassword(), downloadParams.getPreferredMediaType());
                /*up to you: do something with data*/
                System.out.println(data);
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static DownloadParams parseArgs(String[] args){
        DownloadParams downloadParams = new DownloadParams();

        for (int i = 0; i < args.length; i++) {
            if ("-H".equals(args[i])) {
                downloadParams.setHostname(args[++i]);
                continue;
            }
            if ("-P".equals(args[i])) {
                downloadParams.setPort(Integer.parseInt(args[++i]));
                continue;
            }

            if ("-u".equals(args[i])) {
                downloadParams.setUsername(args[++i]);
                continue;
            }
            if ("-p".equals(args[i])) {
                downloadParams.setPassword(args[++i]);
                continue;
            }
            if ("-x".equals(args[i])) {
                downloadParams.setProtocol(args[++i]);
                continue;
            }
            if("-K".equals(args[i])){
                String[] meterKeys = args[++i].split(",");
                for(String meterKey: meterKeys ){
                    downloadParams.addMeterKey(meterKey);
                }
                continue;
            }
            if("-f".equals(args[i])){
                downloadParams.setFromDate(args[++i]);
                continue;
            }
            if("-t".equals(args[i])){
                downloadParams.setToDate(args[++i]);
                continue;
            }
            if("-F".equals(args[i])){
                downloadParams.setPreferredMediaType(args[++i]);
                continue;
            }
            if("-T".equals(args[i])){
                downloadParams.setSeriesType(args[++i]);
                continue;
            }
            if("-i".equals(args[i])){
                downloadParams.setIntervalType(args[++i]);
                continue;
            }
        }

        //some defaults
        if(downloadParams.getFromDate() == null){
            downloadParams.setFromDate("2014-03-02");
        }
        if(downloadParams.getToDate() == null){
            downloadParams.setToDate("2014-03-02");
        }
        if(downloadParams.getMeterKeys().size() == 0){
            downloadParams.addMeterKey("d6d01d193a0a4b07ac835e9c67f16b8e");
        }
        if(downloadParams.getPreferredMediaType() == null){
            downloadParams.setPreferredMediaType("application/json");
        }
        if(downloadParams.getSeriesType() == null){
            downloadParams.setSeriesType("ActivePlus");
        }
        if(downloadParams.getIntervalType() == null){
            downloadParams.setIntervalType("Hour");
        }
        return downloadParams;
    }
}
