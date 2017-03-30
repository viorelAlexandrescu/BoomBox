package n1njagangsta.boombox;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import n1njagangsta.boombox.Activities.MainActivity;
import n1njagangsta.boombox.Exceptions.NoSongsException;

/**
 * Created by viorel on 06.03.2017.
 */

public class PrepareMusicRetrieverTask extends AsyncTask<Void, Void, Void> {
    MusicRetriever mRetriever;
    MusicRetrieverPreparedListener mListener;
    Context context;
    public PrepareMusicRetrieverTask(MusicRetriever retriever,
                                     MusicRetrieverPreparedListener listener,
                                     Context context) {
        this.mRetriever = retriever;
        this.mListener = listener;
        this.context = context;
    }
    @Override
    protected Void doInBackground(Void... arg0){
        try {
            mRetriever.prepare();
        } catch (NoSongsException nse){
            //todo here check in shared prefs if the app is at first time use
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.Initial_App_No_Songs_Dialog_Title)
                    .setMessage(R.string.Initial_App_No_Songs_Dialog_Message)
                    .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //todo here prepare the UI only to show songs on the streaming server after connecting
                        }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        mListener.onMusicRetrieverPrepared();
    }

    public interface MusicRetrieverPreparedListener {
        void onMusicRetrieverPrepared();
    }
}
