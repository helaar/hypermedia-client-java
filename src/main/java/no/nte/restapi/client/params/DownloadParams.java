package no.nte.restapi.client.params;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.util.LinkedList;
import java.util.List;


public class DownloadParams {



    private List<String> meterkeys = new LinkedList<String>();
    private String fromDate;
    private String toDate;
    private String preferredMediaType;
    private String seriesType;
    private String intervalType;
    private ClientParams clientParams = new ClientParams();

    public void setHostname(String hostname) {
        clientParams.setHostname(hostname);
    }

    public String getHostname() {
        return clientParams.getHostname();
    }

    public void setPort(int port) {
        clientParams.setPort(port);
    }

    public int getPort() {
        return clientParams.getPort();
    }

    public void setUsername(String username) {
        clientParams.setUsername(username);
    }

    public String getUsername() {
        return clientParams.getUsername();
    }

    public void setPassword(String password) {
        clientParams.setPassword(password);
    }

    public String getPassword() {
        return clientParams.getPassword();
    }

    public void setProtocol(String protocol) {
        clientParams.setProtocol(protocol);
    }

    public String getProtocol() {
        return clientParams.getProtocol();
    }

    public void addMeterKey(String meterkey) {
        meterkeys.add(meterkey);
    }

    public List<String> getMeterKeys(){
        return meterkeys;
    }

    public void setFromDate(String fromDate) {
        checkFormat(fromDate);
        this.fromDate = fromDate;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setToDate(String toDate) {
        checkFormat(toDate);
        this.toDate = toDate;
    }

    public String getToDate() {
        return toDate;
    }

    public DateTimeFormatter getDateFormatter() {
        DateTimeFormatterBuilder dateTimeFormatterBuilder = new DateTimeFormatterBuilder();
        dateTimeFormatterBuilder
                .appendYear(4, 4)
                .appendLiteral('-')
                .appendMonthOfYear(2)
                .appendLiteral('-')
                .appendDayOfMonth(2);
        return dateTimeFormatterBuilder.toFormatter();
    }

    private void checkFormat(String fromDate) {
        getDateFormatter().parseDateTime(fromDate);
    }

    public void setPreferredMediaType(String preferredMediaType) {
        this.preferredMediaType = preferredMediaType;
    }

    public String getPreferredMediaType() {
        return preferredMediaType;
    }

    public void setSeriesType(String seriesType) {
        this.seriesType = seriesType;
    }

    public String getSeriesType() {
        return seriesType;
    }

    public String getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(String intervalType) {
        this.intervalType = intervalType;
    }

    public ClientParams getClientParams() {
        return clientParams;
    }
}
