package br.com.bm.personalassistant.activity.main;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

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
                tvResultado.setText(resultados.get(0));
            }
        }
    }

}
