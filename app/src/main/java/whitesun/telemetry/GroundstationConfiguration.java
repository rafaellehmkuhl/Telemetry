package whitesun.telemetry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;

public class GroundstationConfiguration extends Activity {

    private EditText editNomeAeronave;
    private EditText editLocalVoo;
    private EditText editTemperatura;
    private EditText editAnguloAsa;
    private EditText editAnguloProfundor;
    private EditText editInfoExtra;
    private Button btnFinalizarConfig;

    private void findViews() {

        editNomeAeronave = (EditText) findViewById(R.id.editNomeAeronave);
        editLocalVoo = (EditText) findViewById(R.id.editLocalVoo);
        editTemperatura = (EditText) findViewById(R.id.editTemperatura);
        editAnguloAsa = (EditText) findViewById(R.id.editAnguloAsa);
        editAnguloProfundor = (EditText) findViewById(R.id.editAnguloProfundor);
        editInfoExtra = (EditText) findViewById(R.id.editInfoExtra);
        btnFinalizarConfig = (Button) findViewById(R.id.btnFinalizarConfig);


        btnFinalizarConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Insert code for sending the configuration here
                GroundstationConnection.sendData("!config:NOME_DA_AERONAVE=" + editNomeAeronave.getText().toString() + "@");
                GroundstationConnection.sendData("!config:LOCAL_DE_VOO=" + editLocalVoo.getText().toString() + "@");
                GroundstationConnection.sendData("!config:TEMPERATURA=" + editTemperatura.getText().toString() + "@");
                GroundstationConnection.sendData("!config:ANGULO_INCIDENCIA_ASA=" + editAnguloAsa.getText().toString() + "@");
                GroundstationConnection.sendData("!config:ANGULO_INCIDENCIA_PROFUNDOR=" + editAnguloProfundor.getText().toString() + "@");
                GroundstationConnection.sendData("!config:INFOS_EXTRAS=" + editInfoExtra.getText().toString() + "@");
                GroundstationConnection.sendData("!sc@");
                Intent intent = new Intent(GroundstationConfiguration.this, GroundstationTelecomandos.class);
                GroundstationConfiguration.this.startActivity(intent);
            }
        });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groundstation_configuration);
        findViews();
        registerHeartbeatTimer();
    }

    public void registerHeartbeatTimer() {

        final Handler handler = new Handler();
        Timer timerCheckHeartbeat = new Timer();
        TimerTask taskCheckHeartbeat= new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (GroundstationConnection.receiveingHeartbeat) {
                            enableButtons();
                        } else {
                            disableButtons();
                        }
                    }
                });
            }
        };

        timerCheckHeartbeat.schedule(taskCheckHeartbeat, 0, 100);

    }

    private void enableButtons() {
        btnFinalizarConfig.setEnabled(true);
    }

    private void disableButtons() {
        btnFinalizarConfig.setEnabled(false);
    }
}
