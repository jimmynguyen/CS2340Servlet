package model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

import database.DataManager;
import database.Itinerary;
import database.Preference;
import database.SQLPreferenceQuery;

import java.sql.SQLException;

/**
 * Code sample for accessing the Yelp API V2.
 * 
 * This program demonstrates the capability of the Yelp API version 2.0 by using the Search API to
 * query for businesses by a search term and location, and the Business API to query additional
 * information about the top result from the search query.
 * 
 * //<p>
 * //See <a href="http://www.yelp.com/developers/documentation">Yelp Documentation</a> for more info.
 * 
 */
public class YelpAPI {

    private HttpServletRequest servletRequest;

    private static final String API_HOST = "api.yelp.com";
    private String term;
    private String location; // pull from itinerary
    private int radius_filter; // in meters 40000 max ~ 25 miles
    private int offset = 0; // offset number of businesses returned by
    private int limit = 10; // number of businesses returned
    private static final String SEARCH_PATH = "/v2/search";
    private static final String BUSINESS_PATH = "/v2/business";

    /*
     * Update OAuth credentials below from the Yelp Developers API site:
     * http://www.yelp.com/developers/getting_started/api_access
     */
    private static final String CONSUMER_KEY = "L3VcR6niZzxcCGpztkiXJw";
    private static final String CONSUMER_SECRET = "54yul9UQqeYzW-YIR8L0OsuWBw4";
    private static final String TOKEN = "mbCQyUh4whUZwQ1Hz5pZxV9RuNBhZzEV";
    private static final String TOKEN_SECRET = "DP4fTB8orO0XLpaoaVY_la__v0g";

    OAuthService service;
    Token accessToken;

    /**
     * Setup the Yelp API OAuth credentials.
     * 
     * @param consumerKey Consumer key
     * @param consumerSecret Consumer secret
     * @param token Token
     * @param tokenSecret Token secret
     */
    public YelpAPI(HttpServletRequest servletRequest) {
        this.service =
        new ServiceBuilder().provider(TwoStepOAuth.class).apiKey(CONSUMER_KEY)
        .apiSecret(CONSUMER_SECRET).build();
        this.accessToken = new Token(TOKEN, TOKEN_SECRET);
        this.servletRequest = servletRequest;
    }

    /**
     * Queries the Search API based on the command line arguments and takes the first result to query
     * the Business API.
     * 
     * //@param yelpApi <tt>YelpAPI</tt> service instance
     * //@param yelpApiCli <tt>YelpAPICLI</tt> command line arguments
     */
    public void queryAPI() throws SQLException {
        this.term = getTerm();
        this.location = getLocation();
        this.radius_filter = convertMilesToMeters(getRadiusFilter());
        String searchResponseJSON = searchForBusinessesByLocation();

        JSONParser parser = new JSONParser();
        JSONObject response = null;
        try {
            response = (JSONObject) parser.parse(searchResponseJSON);
        } catch (ParseException pe) {
            System.out.println("Error: could not parse JSON response:");
            System.out.println(searchResponseJSON);
            System.exit(1);
        }

        JSONArray businesses = (JSONArray) response.get("businesses");
        // for (int i = 0; i < businesses.size(); i++) {
        //     JSONObject firstBusiness = (JSONObject) businesses.get(i);
        //     String firstBusinessID = firstBusiness.get("id").toString();
        //     System.out.println(String.format(
        //             "%s businesses found, querying business info for the top result \"%s\" ...",
        //             businesses.size(), firstBusinessID));

        //         // Select the first business and display business details
        //     String businessResponseJSON = searchByBusinessId(firstBusinessID.toString());
        //     System.out.println(String.format("Result for business \"%s\" found:", firstBusinessID));
        //     System.out.println(businessResponseJSON);
        // }
        final HttpSession session = servletRequest.getSession();
        session.setAttribute("businesses", businesses);
    }

    private String getTerm() {
        final String queryString = servletRequest.getQueryString();
        final int startIndex = queryString.indexOf("=") + 1;
        String eventID = queryString.substring(startIndex);
        return (String) servletRequest.getParameter("eventType" + eventID); 
    }

    private String getLocation() {
        HttpSession session = servletRequest.getSession();
        Itinerary activeItinerary = (Itinerary) session.getAttribute("activeItinerary");
        return activeItinerary.getAddress();
    }

    private int getRadiusFilter() throws SQLException{
        HttpSession session = servletRequest.getSession();
        Itinerary activeItinerary = (Itinerary) session.getAttribute("activeItinerary");
        final int preferenceID = activeItinerary.getPreferenceID();
        SQLPreferenceQuery query = new SQLPreferenceQuery();
        Preference activePreferences = query.getPreferencesByID(preferenceID);
        return activePreferences.getMaxDistance();
    }

    private int convertMilesToMeters(int miles) {
        int meters = miles*40000/25;
        return meters;
    }

    /**
     * Creates and sends a request to the Search API by term and location.
     * //<p>
     * //See <a href="http://www.yelp.com/developers/documentation/v2/search_api">Yelp Search API V2</a>
     * for more info.
     * 
     * @param term <tt>String</tt> of the search term to be queried
     * @param location <tt>String</tt> of the location
     * @return <tt>String</tt> JSON Response
     */
    public String searchForBusinessesByLocation() {
        OAuthRequest request = createOAuthRequest(SEARCH_PATH);
        request.addQuerystringParameter("term", term);
        request.addQuerystringParameter("location", location);
        request.addQuerystringParameter("limit", String.valueOf(limit));
        request.addQuerystringParameter("offset", String.valueOf(offset));
        request.addQuerystringParameter("radius_filter", String.valueOf(radius_filter));
        return sendRequestAndGetResponse(request);
    }

    /**
     * Creates and sends a request to the Business API by business ID.
     * //<p>
     * //See <a href="http://www.yelp.com/developers/documentation/v2/business">Yelp Business API V2</a>
     * for more info.
     * 
     * @param businessID <tt>String</tt> business ID of the requested business
     * @return <tt>String</tt> JSON Response
     */
    public String searchByBusinessId(String businessID) {
        OAuthRequest request = createOAuthRequest(BUSINESS_PATH + "/" + businessID);
        return sendRequestAndGetResponse(request);
    }

    /**
     * Creates and returns an {@link OAuthRequest} based on the API endpoint specified.
     * 
     * @param path API endpoint to be queried
     * //@return <tt>OAuthRequest</tt>
     */
    private OAuthRequest createOAuthRequest(String path) {
        OAuthRequest request = new OAuthRequest(Verb.GET, "http://" + API_HOST + path);
        return request;
    }

    /**
     * Sends an {@link OAuthRequest} and returns the {@link Response} body.
     * 
     * @param request {@link OAuthRequest} corresponding to the API request
     * //@return <tt>String</tt> body of API response
     */
    private String sendRequestAndGetResponse(OAuthRequest request) {
        // System.out.println("Querying " + request.getCompleteUrl() + " ...");
        this.service.signRequest(this.accessToken, request);
        Response response = request.send();
        return response.getBody();
    }

    // /**
    //  * Command-line interface for the sample Yelp API runner.
    //  */
    // private static class YelpAPICLI {
    //     @Parameter(names = {"-q", "--term"}, description = "Search Query Term")
    //     public String term = DEFAULT_TERM;

    //     @Parameter(names = {"-l", "--location"}, description = "Location to be Queried")
    //     public String location = DEFAULT_LOCATION;
    // }

    // /**
    //  * Main entry for sample Yelp API requests.
    //  * <p>
    //  * After entering your OAuth credentials, execute <tt><b>run.sh</b></tt> to run this example.
    //  */
    // public static void main(String[] args) {
    //     YelpAPICLI yelpApiCli = new YelpAPICLI();
    //     new JCommander(yelpApiCli, args);

    //     YelpAPI yelpApi = new YelpAPI(CONSUMER_KEY, CONSUMER_SECRET, TOKEN, TOKEN_SECRET);
    //     queryAPI(yelpApi, yelpApiCli);
    // }
}