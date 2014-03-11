package no.nte.restapi.client;

import com.comcast.cim.rest.client.xhtml.*;
import no.nte.restapi.client.params.ClientParams;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Convenience-wrapper around comcast-client.
 * Navigates by clicking links and submitting forms
 */
public class HypermediaClient {

    private static final String DEFAULT_HOST = "api.demosteinkjer.no";
    private static final int DEFAULT_PORT = 443;
    private XhtmlNavigator navigator;
    private XhtmlApplicationState currentState;
    private String hostname = DEFAULT_HOST;
    private int port = DEFAULT_PORT;
    private String username;
    private String password;
    private String resource = "";
    private String protocol = "https";
    private boolean initialized = false;
    private List<Element> selectedNodes;
    private Element currentNode;

    public HypermediaClient(ClientParams clientParams) {
        hostname = clientParams.getHostname() == null ? DEFAULT_HOST : clientParams.getHostname();
        port = clientParams.getPort() == -1 ? DEFAULT_PORT : clientParams.getPort();
        username = clientParams.getUsername();
        password = clientParams.getPassword();
        protocol = clientParams.getProtocol() == null ? protocol: clientParams.getProtocol() ;
    }



    /**
     * Initialize state, go to start page.
     * @return true if successful initialization
     * @throws java.io.IOException
     */
    public boolean initialize() throws IOException {
        if (!initialized) {
            HttpClientBuilder builder = HttpClients.custom();

            if (username != null && password != null) {
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(
                        new AuthScope(hostname, port),
                        new UsernamePasswordCredentials(username, password));

                builder.setDefaultCredentialsProvider(credsProvider);

            }
            HttpClient client = builder.build();
            XhtmlHttpClient httpClient = new XhtmlHttpClient(client, new XhtmlResponseHandlerFactory());

            navigator = new XhtmlNavigator(new XhtmlParser(), new RequestBuilder(), httpClient);
            currentState = httpClient.execute(new HttpGet(getBaseUrl()));
            initialized = true;
        }
        return currentState != null && currentState.succeeded();
    }

    /**
     * Follows a link
     * @param rel of link
     * @return this
     * @throws org.jdom.JDOMException
     * @throws java.io.IOException
     */
    public HypermediaClient followLink(String rel) throws JDOMException, IOException {
        if(currentNode != null){
            currentState = navigator.followLink(currentState, currentNode, rel);
        }else{
            currentState =  navigator.followLink(currentState,rel);
        }
        selectedNodes = null;
        currentNode = null;
        return this;
    }

    /**
     * Submits a form
     * @param formName name of form
     * @param args form arguments
     * @return this
     * @throws org.jdom.JDOMException
     * @throws java.io.IOException
     */
    public HypermediaClient submitForm(String formName, Map<String, String> args) throws JDOMException, IOException {
        currentState = navigator.submitForm(currentState, formName, args);
        selectedNodes = null;
        currentNode = null;
        return this;
    }

    /**
     * if the navigation succeeded
     * @return true if link/formsubmission succeeded
     */
    public boolean succeeded(){
        return currentState.succeeded();
    }


    /**
     * Select links in list for navigation. (element must be present on current site)
     * Examples
     * select("https://api.demosteinkjer.no/docs#meter").followLink("up");
     * select("https://api.demosteinkjer.no/docs#downloads").index(2).followLink("self");
     * @param docClass class of links
     * @return client
     * @throws org.jdom.JDOMException
     */
    public HypermediaClient select(String docClass) throws JDOMException {
        return select(docClass, getDocument().getRootElement());
    }

    /**
     * number of nodes i current selection
     * Example
     * select("https://api.demosteinkjer.no/docs#downloads")
     * getNodeCount() gives the number of
     *
     * @return -1 if no nodes is selected, else
     */
    public int getNodeCount(){
        if(selectedNodes == null){
            return -1;
        }
        return selectedNodes.size();
    }

    /**
     * Sets currentNode for navigation
     * @param i, index of node to select
     * @return
     */
    public HypermediaClient index(int i) {
        currentNode = selectedNodes.get(i);
        return this;
    }

    /**
     * Get the
     * @return the HttpResponse
     */
    public HttpResponse getResponse() {
        return currentState.getHttpResponse();
    }

    /**
     * Get det parsed Document the client is currently on
     * @return the document
     */
    public Document getDocument() {
        return currentState.getDocument();
    }

    /**
     * Print ut actual content of page for debugging-purposes
     * @return actual content of page the client is currently on
     */
    public String getContentForDebug(){
        if(getDocument() != null){
            return new XMLOutputter().outputString(getDocument());
        }
        return "Document is null";
    }

    /**
     * Gets the current URL
     * @return current URL
     */
    public URL getContext() {
        return currentState.getContext();
    }

    private String getBaseUrl() {
        if(port == 80 || port == 443){
            return String.format("%s://%s%s", protocol, hostname,resource);
        }
        return String.format("%s://%s:%d%s", protocol, hostname, port, resource);
    }

    private HypermediaClient select(String docClass, Element rootElement) throws JDOMException {
        XPath xPath = XPath.newInstance(String.format("//*[@class='%s']", docClass));
        xPath.addNamespace("h", "http://www.w3.org/1999/xhtml");
        selectedNodes = xPath.selectNodes(rootElement);
        if(selectedNodes.size() > 0){
            currentNode = selectedNodes.get(0);
        } else{
            currentNode = null;
        }
        return this;
    }
}
