package cz.zcu.kiv.eeg.mobile.base.db.asynctask;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import cz.zcu.kiv.eeg.mobile.base.R;
import cz.zcu.kiv.eeg.mobile.base.archetypes.CommonActivity;
import cz.zcu.kiv.eeg.mobile.base.archetypes.CommonService;
import cz.zcu.kiv.eeg.mobile.base.data.Values;
import cz.zcu.kiv.eeg.mobile.base.data.container.xml.ElectrodeLocation;
import cz.zcu.kiv.eeg.mobile.base.db.HashConstants;
import cz.zcu.kiv.eeg.mobile.base.db.WaspDbSupport;
import net.rehacktive.wasp.WaspHash;

import static cz.zcu.kiv.eeg.mobile.base.data.ServiceState.DONE;
import static cz.zcu.kiv.eeg.mobile.base.data.ServiceState.ERROR;

/**
 * Common service (Asynctask) for creating new Electrode Location on eeg base.
 *
 * @author Petr Miko
 */
public class CreateElectrodeLocation extends CommonService<ElectrodeLocation, Void, ElectrodeLocation> {

    private final static String TAG = CreateElectrodeLocation.class.getSimpleName();

    /**
     * Constructor, which sets reference to parent activity.
     *
     * @param context parent activity
     */
    public CreateElectrodeLocation(CommonActivity context) {
        super(context);
    }

    /**
     * Method, where electrode location information is pushed to server in order to create user.
     * All heavy lifting is made here.
     *
     * @param electrodeLocations only one ElectrodeLocation object is accepted
     * @return information about created electrode location
     */
    @Override
    protected ElectrodeLocation doInBackground(ElectrodeLocation... electrodeLocations) {

        ElectrodeLocation location = electrodeLocations[0];

        try {
            WaspDbSupport support = new WaspDbSupport();
            WaspHash hash = support.getOrCreateHash(HashConstants.ELECTRODES_LOC.toString());
            hash.put("hash" + location.hashCode(), location);

        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            setState(ERROR, e);
        } finally {
            setState(DONE);
        }
        return location;
    }

    /**
     * Informs user whether ElectrodeLocation creation was successful or not.
     *
     * @param location returned electrode location info if any
     */
    @Override
    protected void onPostExecute(ElectrodeLocation location) {
        if (location != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(Values.ADD_ELECTRODE_LOCATION_KEY, location);
            Toast.makeText(activity, R.string.creation_ok, Toast.LENGTH_SHORT).show();
            activity.setResult(Activity.RESULT_OK, resultIntent);
            activity.finish();
        } else {
            Toast.makeText(activity, R.string.creation_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
