package br.com.bm.personalassistant.activity.main;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.ToneAnalyzer;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneAnalysis;
import com.ibm.watson.developer_cloud.tone_analyzer.v3.model.ToneOptions;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

    private void iniciaComponentes(View view)  {
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
        final ConversationService conversationService = new ConversationService(ConversationService.VERSION_DATE_2016_07_11);
        conversationService.setUsernameAndPassword("56cfc2ed-865b-46cc-b31d-2cde129f33a3", "lKmFrfIb5fno");

        final ToneAnalyzer toneService = new ToneAnalyzer(ToneAnalyzer.VERSION_DATE_2016_05_19);
        toneService.setUsernameAndPassword("78ea83c3-ad4b-4f2d-a0ac-7918ef39d952", "1HatBDQAYkr8");

        final String workspaceId = "ab3c6acd-5faa-4d21-b0ce-4aa46dffaf78";

        final Map<String, Object> context = new HashMap<String, Object>();
        toneService.getTone(texto, null).enqueue(new ServiceCallback<ToneAnalysis>() {
            @Override
            public void onResponse(ToneAnalysis toneResponsePayload) {

                // update context with the tone data returned by the Tone Analyzer
                ToneDetection.updateUserTone(context, toneResponsePayload, false);
                Log.e("tone", context.toString());
                tvResultado.setText(context.toString());
                // call Conversation Service with the input and tone-aware context
                MessageRequest newMessage = new MessageRequest.Builder().inputText(texto).context(context).build();
                conversationService.message(workspaceId, newMessage).enqueue(new ServiceCallback<MessageResponse>() {
                    @Override
                    public void onResponse(MessageResponse response) {
                        System.out.println(response);
                        Log.e("conv", response.toString());
                    }

                    @Override
                    public void onFailure(Exception e) { }
                });
            }

            @Override
            public void onFailure(Exception e) { }
        });
    }

}
