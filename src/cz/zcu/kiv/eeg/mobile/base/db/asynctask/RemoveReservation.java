package cz.zcu.kiv.eeg.mobile.base.db.asynctask;

import android.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;
import cz.zcu.kiv.eeg.mobile.base.R;
import cz.zcu.kiv.eeg.mobile.base.archetypes.CommonActivity;
import cz.zcu.kiv.eeg.mobile.base.archetypes.CommonService;
import cz.zcu.kiv.eeg.mobile.base.data.container.xml.Reservation;
import cz.zcu.kiv.eeg.mobile.base.db.HashConstants;
import cz.zcu.kiv.eeg.mobile.base.db.WaspDbSupport;
import cz.zcu.kiv.eeg.mobile.base.ui.NavigationActivity;
import cz.zcu.kiv.eeg.mobile.base.ui.reservation.ReservationFragment;
import net.rehacktive.wasp.WaspHash;

import static cz.zcu.kiv.eeg.mobile.base.data.ServiceState.*;

/**
 * Service (AsyncTask) for removing existing reservation from eeg base.
 * After removing record it forces reservation fragment to update its content.
 *
 * @author Petr Miko
 */
public class RemoveReservation extends CommonService<Reservation, Void, Boolean> {

    private static final String TAG = RemoveReservation.class.getSimpleName();
    private int fragmentId;

    /**
     * Constructor.
     *
     * @param context    parent activity
     * @param fragmentId identifier of reservation fragment (vital for refreshing)
     */
    public RemoveReservation(CommonActivity context, int fragmentId) {
        super(context);
        this.fragmentId = fragmentId;
    }

    /**
     * Method, where reservation information is pushed to server in order to remove it.
     * All heavy lifting is made here.
     *
     * @param params only one Reservation object is accepted
     * @return true if reservation is removed
     */
    @Override
    protected Boolean doInBackground(Reservation... params) {

        Reservation data = params[0];

        //nothing to remove
        if (data == null)
            return false;

        try {

            setState(RUNNING, R.string.working_ws_remove);

            WaspDbSupport dbSupport = new WaspDbSupport();
            WaspHash hash = dbSupport.getOrCreateHash(HashConstants.RESERVATIONS.toString());
            hash.put(data, null);

            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
            setState(ERROR, e);
        } finally {
            setState(DONE);
        }
        return false;
    }

    /**
     * If reservation was removed, attempt to update reservation fragment or at least inform user, that he should do that manually.
     *
     * @param success true if reservation is removed
     */
    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            if (activity instanceof NavigationActivity) {

                FragmentManager fm = activity.getFragmentManager();

                ReservationFragment fragment = (ReservationFragment) fm.findFragmentByTag(ReservationFragment.TAG);
                if (fragment == null)
                    fragment = (ReservationFragment) fm.findFragmentById(fragmentId);
                if (fragment != null) {
                    fragment.updateData();
                    Toast.makeText(activity, activity.getString(R.string.reser_removed), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Agenda fragment not found!");
                    Toast.makeText(activity, activity.getString(R.string.reser_removed_update), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
