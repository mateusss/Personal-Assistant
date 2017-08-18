package br.com.bm.personalassistant.activity.main;

import android.content.Intent;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.Tone;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneCategory;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneOptions;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneScore;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import br.com.bm.personalassistant.R;

import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final int REQUEST_CODE = 1;
    private Button btAtivarReconhecimento;
    private TextView text1;
    private TextView text2;
    private TextView text3;
    private TextView text4;
    private TextView text5;
    private TextView text6;

    private Handler handler;

    private static ToneScore t1;
    private static ToneScore t2;
    private static ToneScore t3;
    private static ToneScore t4;
    private static ToneScore t5;

    private static MessageResponse msg;


    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        iniciaComponentes(view);
        return view;
    }

    private void iniciaComponentes(View view)  {
        btAtivarReconhecimento = (Button) view.findViewById(R.id.btAtivarReconhecimento);
        btAtivarReconhecimento.setOnClickListener(ativarReconhecimento);

        text1 = (TextView) view.findViewById(R.id.text1);
        text2 = (TextView) view.findViewById(R.id.text2);
        text3 = (TextView) view.findViewById(R.id.text3);
        text4 = (TextView) view.findViewById(R.id.text4);
        text5 = (TextView) view.findViewById(R.id.text5);
        text6 = (TextView) view.findViewById(R.id.text6);
        handler = new Handler();
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
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "O que deseja?");
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
                iniciaAnalise(resultados.get(0));
            }
        }
    }

    private void iniciaAnalise(final String texto) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ToneAnalyzer service = new ToneAnalyzer(ToneAnalyzer.VERSION_DATE_2016_05_19);
                service.setUsernameAndPassword("78ea83c3-ad4b-4f2d-a0ac-7918ef39d952", "1HatBDQAYkr8");

                ToneOptions options = new ToneOptions.Builder()
                        .addTone(Tone.EMOTION).build();
                ToneAnalysis tone = service.getTone(texto, options).execute();
                for (ToneCategory tom : tone.getDocumentTone().getTones()) {
                    for (ToneScore score : tom.getTones()) {
                        populateBars(score);
                    }
                }

                ConversationService conversationService = new ConversationService(ConversationService.VERSION_DATE_2016_09_20);
                conversationService.setUsernameAndPassword("c1c17428-7393-4dc2-95ad-2a1ec0b45984", "6nQeljNo8Db0");

                final Map<String, Object> context = new HashMap<String, Object>();
                MessageRequest newMessage = new MessageRequest.Builder().inputText(texto).context(context).build();
                conversationService.message("ab3c6acd-5faa-4d21-b0ce-4aa46dffaf78", newMessage).enqueue(new ServiceCallback<MessageResponse>() {
                    @Override
                    public void onResponse(MessageResponse response) {
                        System.out.println(response);
                        Log.e("##Conversation##", response.getContext().toString());
                        msg = response;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Map<String, Object> output = msg.getOutput();
                                List<String> responseText = (List<String>) output.get("text");
                                if (responseText.size() > 0) {
                                    text6.setText(responseText.get(0));
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) { }
                });
            }
        }).start();
    }

    private void populateBars(ToneScore score) {
        switch (score.getName()) {
            case "Anger": {
                t1 = score;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text1.setText("Raiva: "+t1.getScore());
                    }
                });
                break;
            }
            case "Disgust": {
                t2 = score;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text2.setText("Desgosto: "+t2.getScore());
                    }
                });
                break;
            }
            case "Fear": {
                t3 = score;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text3.setText("Medo: "+t3.getScore());
                    }
                });
                break;
            }
            case "Joy": {
                t4 = score;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text4.setText("Alegria: "+t4.getScore());
                    }
                });
                break;
            }
            case "Sadness": {
                t5 = score;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        text5.setText("Tristeza: "+t5.getScore());
                    }
                });
                break;
            }
        }
    }

}
