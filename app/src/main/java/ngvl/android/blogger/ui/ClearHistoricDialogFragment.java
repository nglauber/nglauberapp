package ngvl.android.blogger.ui;

import ngvl.android.blogger.storage.BuscaSuggestionProvider;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.DialogFragment;

public class ClearHistoricDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		 builder.setMessage("Esta operação irá apagar todo o histórico de buscas.");
		 builder.setPositiveButton(android.R.string.yes, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					clearHistoricSuggestions();
				}
			});
		 
		 builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int arg1) {
	                dialog.cancel();
	            }
	        });

		 AlertDialog dialog = builder.create();
		 dialog.setTitle("Atenção");
		
		return dialog;
	}
	
	private void clearHistoricSuggestions(){
    	SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getActivity(),
    	        BuscaSuggestionProvider.AUTHORITY, BuscaSuggestionProvider.MODE);
    	suggestions.clearHistory();
 }

		
	
}
