package ngvl.android.blogger.ui;

import ngvl.android.blogger.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class AboutDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_about, null);
		
		String version = "BETA";
		try {
			version = getActivity().getPackageManager().getPackageInfo(
					getActivity().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		TextView txtVersion = (TextView)v.findViewById(R.id.txtVersion);
		txtVersion.setText(getString(R.string.version, version));
		
		AlertDialog dialog = new AlertDialog.Builder(getActivity())
			.setView(v)
			.setPositiveButton(android.R.string.ok, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dismiss();
				}
			}).create();
		
		return dialog;
	}
}
