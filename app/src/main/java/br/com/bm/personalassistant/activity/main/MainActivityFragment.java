package br.com.bm.personalassistant.activity.main;

import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.alchemy.v1.model.DocumentEmotion;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.alchemy.v1.model.DocumentSentiment;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EmotionOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.bm.personalassistant.R;

import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final int REQUEST_CODE = 1;
    private Button btAtivarReconhecimento;
    private TextView tvResultado;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        iniciaComponentes(view);
        return view;
    }

    private void iniciaComponentes(View view) {
        btAtivarReconhecimento = (Button) view.findViewById(R.id.btAtivarReconhecimento);
        btAtivarReconhecimento.setOnClickListener(ativarReconhecimento);
        tvResultado = (TextView) view.findViewById(R.id.tvResultado);
    }

    private View.OnClickListener ativarReconhecimento = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            reconhecerVoz();
        }
    };

    private void reconhecerVoz() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-US");
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Olá, como foi seu dia?");
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, new Long(10000));
                startActivityForResult(intent, REQUEST_CODE);
            }
        }).start();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            ArrayList<String> resultados = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            if(resultados != null && !resultados.isEmpty()) {
                //tvResultado.setText(resultados.get(0));
                AskWatsonTask task = new AskWatsonTask();
                task.execute(new String[]{});
            }
        }
    }

    public class AskWatsonTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... textsToAnalyse) {

            NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27);
            String username = "8c03827b-c536-4c85-9a0d-e3c587a80513";
            String password = "CCejDsxI3UT2";
            service.setUsernameAndPassword(username, password);

            List<String> frases = new ArrayList<>();
            frases.add(textsToAnalyse[0]);

            EmotionOptions emotions = new EmotionOptions().Builder().targets(frases).build();
            Features features = new Features.Builder().emotion(emotions).build();

            AnalyzeOptions parameters = new AnalyzeOptions.Builder().text(text).features(features).build();

            AnalysisResults response = service.analyze(parameters).execute();

            System.out.println(response);

            return response.getAnalyzedText() ;
        }

        //setting the value of UI outside of the thread
        @Override
        protected void onPostExecute(String result) {
            tvResultado.setText("The message's sentiment is: " + result);
        }
    }

}
