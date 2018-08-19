package whitesun.telemetry;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.ToggleButton;

import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

public class GroundstationTelecomandos extends Activity {

    public static RadioButton rbHeartbeatTelecom;
    public static RadioButton rbReceivedConfigs;
    public static RadioButton rbTransmitingTelecom;
    public static RadioButton rbRecordingTelecom;
    public static ToggleButton tbTransmissao;
    public static Button btnGraficos;
    public static Button btnZerarSensores;
    public static ToggleButton tbGravacao;
    public static Button btnFinalizarTeste;
    public static Button btnPendrive;
    public static Button btnBluetooth;
    public static Button btnReiniciar;
    public static Button btnDesligar;
    public static Button btnTerminal;

    private EventBus bus = EventBus.getDefault();


    private void findViews() {
        rbHeartbeatTelecom = (RadioButton) findViewById(R.id.rbHeartbeatTelecom);
        rbReceivedConfigs = (RadioButton) findViewById(R.id.rbReceivedConfigs);
        rbTransmitingTelecom = (RadioButton) findViewById(R.id.rbTransmitingTelecom);
        rbRecordingTelecom = (RadioButton) findViewById(R.id.rbRecordingTelecom);
        tbTransmissao = (ToggleButton) findViewById(R.id.tbTransmissao);
        btnGraficos = (Button) findViewById(R.id.btnGraficos);
        btnZerarSensores = (Button) findViewById(R.id.btnZerarSensores);
        tbGravacao = (ToggleButton) findViewById(R.id.tbGravacao);
        btnFinalizarTeste = (Button) findViewById(R.id.btnFinalizarTeste);
        btnPendrive = (Button) findViewById(R.id.btnPendrive);
        btnBluetooth = (Button) findViewById(R.id.btnBluetooth);
        btnReiniciar = (Button) findViewById(R.id.btnReiniciar);
        btnDesligar = (Button) findViewById(R.id.btnDesligar);
        btnTerminal = (Button) findViewById(R.id.btnTerminal);

        tbTransmissao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tbTransmissao.isChecked()) {
                    GroundstationConnection.sendData("!lt@");
                } else {
                    GroundstationConnection.sendData("!dt@");
                }
            }
        });

        tbGravacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tbGravacao.isChecked()) {
                    GroundstationConnection.sendData("!lg@");
                } else {
                    GroundstationConnection.sendData("!dg@");
                }
            }
        });

        btnFinalizarTeste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroundstationConnection.sendData("!ft@");
                Intent intent = new Intent(GroundstationTelecomandos.this, GroundstationConnection.class);
                GroundstationTelecomandos.this.startActivity(intent);
            }
        });

        btnGraficos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: insert plotting command
                Intent intent = new Intent(GroundstationTelecomandos.this, Painel_Graficos_geral.class);
                GroundstationTelecomandos.this.startActivity(intent);
            }
        });

        btnZerarSensores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callSensorZeroingDialog();
            }
        });

        btnPendrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroundstationConnection.sendData("!spd@");
            }
        });

        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroundstationConnection.sendData("!sbt@");
            }
        });

        btnReiniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroundstationConnection.sendData("!rp@");
            }
        });

        btnDesligar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroundstationConnection.sendData("!sp@");
            }
        });

        btnTerminal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Call terminal activity
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groundstation_telecomandos);
        findViews();
        registerHeartbeatTimer();
        bus.register(this);
    }

    public void registerHeartbeatTimer() {

        final Handler handler = new Handler();
        Timer timerCheckHeartbeat = new Timer();
        TimerTask taskCheckHeartbeat= new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {

                        if (GroundstationConnection.platformTransmmiting) {
                            rbTransmitingTelecom.setChecked(true);
                            tbTransmissao.setChecked(true);
                            btnGraficos.setEnabled(true);
                            btnZerarSensores.setEnabled(true);
                        } else {
                            rbTransmitingTelecom.setChecked(false);
                            tbTransmissao.setChecked(false);
                            btnGraficos.setEnabled(false);
                            btnZerarSensores.setEnabled(false);
                        }

                        if (GroundstationConnection.platformRecording) {
                            rbRecordingTelecom.setChecked(true);
                            tbGravacao.setChecked(true);
                        } else {
                            rbRecordingTelecom.setChecked(false);
                            tbGravacao.setChecked(false);
                        }

                        if (GroundstationConnection.receiveingHeartbeat) {
                            rbHeartbeatTelecom.setChecked(true);
                        } else {
                            rbHeartbeatTelecom.setChecked(false);
                        }

                        if (GroundstationConnection.platformConfigured) {
                            rbReceivedConfigs.setChecked(true);
                        } else {
                            rbReceivedConfigs.setChecked(false);
                        }

                        if (GroundstationConnection.platformConfigured && GroundstationConnection.receiveingHeartbeat) {
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

    @Override
    protected void onDestroy() {
        // Unregister
        bus.unregister(this);
        super.onDestroy();
    }

    public void onEvent(EventoTrocaDados evento) {

        if (evento.getApelido().equals("sin")) { // Modo de operação
            if (evento.getValor() == 0){
                aguardando();
            } else if (evento.getValor() == 1) {
                aguardando();
            } else if (evento.getValor() == 2) {
                transmBasica();
            } else if (evento.getValor() == 3) {
                transmApenas();
            } else if (evento.getValor() == 4) {
                gravando();
            } else {
                // Do nothing
            }
        }
    }

    private void aguardando() {
        // TODO: Enable/Disable buttons depending on current state
    }

    private void gravando() {
        // TODO: Enable/Disable buttons depending on current state
    }

    private void transmBasica() {
        // TODO: Enable/Disable buttons depending on current state
    }

    private void transmApenas() {
        // TODO: Enable/Disable buttons depending on current state
    }

    private void callSensorZeroingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione o sensor a ser zerado:");
        builder.setItems(new CharSequence[]{"Pitot", "GPS", "Células"},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                GroundstationConnection.sendData("!zp@");
                                break;
                            case 1:
                                GroundstationConnection.sendData("!zpgs@");
                                break;
                            case 2:
                                GroundstationConnection.sendData("!tc@");
                                break;
                            default: break;
                        }
                    }
                });
        builder.show();
    }

    private void enableButtons() {
        tbTransmissao.setEnabled(true);
        tbGravacao.setEnabled(true);
        btnFinalizarTeste.setEnabled(true);
        btnPendrive.setEnabled(true);
        btnBluetooth.setEnabled(true);
        btnReiniciar.setEnabled(true);
        btnDesligar.setEnabled(true);
        btnTerminal.setEnabled(true);
    }

    private void disableButtons() {
        tbTransmissao.setEnabled(false);
        btnGraficos.setEnabled(false);
        btnZerarSensores.setEnabled(false);
        tbGravacao.setEnabled(false);
        btnFinalizarTeste.setEnabled(false);
        btnPendrive.setEnabled(false);
        btnBluetooth.setEnabled(false);
        btnReiniciar.setEnabled(false);
        btnDesligar.setEnabled(false);
        btnTerminal.setEnabled(false);
    }

}
