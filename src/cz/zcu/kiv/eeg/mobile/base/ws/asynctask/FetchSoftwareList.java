package cz.zcu.kiv.eeg.mobile.base.ws.asynctask;

import android.content.SharedPreferences;
import android.util.Log;
import cz.zcu.kiv.eeg.mobile.base.R;
import cz.zcu.kiv.eeg.mobile.base.archetypes.CommonActivity;
import cz.zcu.kiv.eeg.mobile.base.archetypes.CommonService;
import cz.zcu.kiv.eeg.mobile.base.data.Values;
import cz.zcu.kiv.eeg.mobile.base.data.adapter.SoftwareAdapter;
import cz.zcu.kiv.eeg.mobile.base.data.container.xml.Software;
import cz.zcu.kiv.eeg.mobile.base.data.container.xml.SoftwareList;
import cz.zcu.kiv.eeg.mobile.base.ws.ssl.HttpsClient;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.xml.SimpleXmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static cz.zcu.kiv.eeg.mobile.base.data.ServiceState.*;

/**
 * Common service (Asynctask) for fetching softwareList from eeg base.
 *
 * @author Petr Miko
 */
public class FetchSoftwareList extends CommonService<Void, Void, List<Software>> {

    private static final String TAG = FetchSoftwareList.class.getSimpleName();
    private final SoftwareAdapter softwareAdapter;

    /**
     * Constructor.
     *
     * @param activity        parent activity
     * @param softwareAdapter adapter for holding collection of software
     */
    public FetchSoftwareList(CommonActivity activity, SoftwareAdapter softwareAdapter) {
        super(activity);
        this.softwareAdapter = softwareAdapter;
    }

    /**
     * Method, where all softwareList are read from server.
     * All heavy lifting is made here.
     *
     * @param params omitted here
     * @return list of fetched software
     */
    @Override
    protected List<Software> doInBackground(Void... params) {
        SharedPreferences credentials = getCredentials();
        String username = credentials.getString("username", null);
        String password = credentials.getString("password", null);
        String url = credentials.getString("url", null) + Values.SERVICE_SOFTWARE;

        setState(RUNNING, R.string.working_ws_software);
        HttpAuthentication authHeader = new HttpBasicAuthentication(username, password);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAuthorization(authHeader);
        requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        HttpEntity<Object> entity = new HttpEntity<Object>(requestHeaders);

        // Create a new RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(HttpsClient.getClient()));
        restTemplate.getMessageConverters().add(new SimpleXmlHttpMessageConverter());

        try {
            // Make the network request
            Log.d(TAG, url);
            ResponseEntity<SoftwareList> response = restTemplate.exchange(url, HttpMethod.GET, entity,
                    SoftwareList.class);
            SoftwareList body = response.getBody();

            if (body != null) {
                return body.getSoftwareList();
            }

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            setState(ERROR, e);
        } finally {
            setState(DONE);
        }
        return Collections.emptyList();
    }

    /**
     * Fetched records are put into Software adapter and sorted.
     *
     * @param resultList fetched records
     */
    @Override
    protected void onPostExecute(List<Software> resultList) {
        softwareAdapter.clear();
        if (resultList != null && !resultList.isEmpty()) {
            Collections.sort(resultList, new Comparator<Software>() {
                @Override
                public int compare(Software lhs, Software rhs) {
                    int sub = lhs.getTitle().toLowerCase().compareTo(rhs.getTitle().toLowerCase());

                    if (sub > 0) return 1;
                    else if (sub < 0) return -1;
                    else return lhs.getId() - rhs.getId();
                }
            });

            for (Software artifact : resultList) {
                softwareAdapter.add(artifact);
            }
        }
    }

}
