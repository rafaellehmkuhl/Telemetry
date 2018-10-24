package whitesun.telemetry;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;

public class GroundstationConnection extends Activity {

    private RadioButton rbHeartbeat;
    private Button btnPersConfig;
    private Button btnPularConfig;
    private TextView tvTransmissorState;

    UsbManager usbManager;
    UsbDevice device;
    static UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    public final String ACTION_USB_PERMISSION = "com.hariharan.arduinousb.USB_PERMISSION";
    String ultimoDadoRecebido = "";
    public static Protocolo protocolo = new Protocolo();
    private EventBus bus = EventBus.getDefault();
    int idApelidoSelecionado = -1;
    public static boolean receiveingHeartbeat = false;
    public static boolean platformConfigured= false;
    public static boolean platformTransmmiting = false;
    public static boolean platformRecording = false;

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            try {
                ultimoDadoRecebido = ultimoDadoRecebido + new String(arg0, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            serialPort.setBaudRate(57600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                        } else { }
                    } else { }
                } else { }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                tvTransmissorState.setText("Transmissor conectado");
                startConnection();
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                tvTransmissorState.setText("Transmissor desconectado");
                stopConnection();
            }
        }

        ;
    };

    private void findViews() {
        rbHeartbeat = (RadioButton) findViewById(R.id.rbHeartbeat);
        btnPersConfig = (Button) findViewById(R.id.btnPersConfig);
        btnPularConfig = (Button) findViewById(R.id.btnPularConfig);
        tvTransmissorState = (TextView) findViewById(R.id.tvTransmissorState);

        btnPersConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroundstationConnection.this, GroundstationConfiguration.class);
                GroundstationConnection.this.startActivity(intent);
            }
        });

        btnPularConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Send "jumping configuration" code here
                GroundstationConnection.sendData("!sc@");
                Intent intent = new Intent(GroundstationConnection.this, GroundstationTelecomandos.class);
                GroundstationConnection.this.startActivity(intent);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groundstation_connection);
        findViews();
        registerUSB();
        registerProcessingTimer();
        registerDataShowingTimer();
        registerHeartbeatTimer();
        disableButtons();
        startConnection();
    }

    public void registerProcessingTimer() {

        final Handler handler = new Handler();
        Timer timerProcessaDado = new Timer();
        TimerTask taskProcessaDado = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        if (!ultimoDadoRecebido.equals("")) {
                            ultimoDadoRecebido = protocolo.processaInput(ultimoDadoRecebido);
                        }
                    }
                });
            }
        };

        timerProcessaDado.schedule(taskProcessaDado, 0, 10);

    }

    public void registerDataShowingTimer() {

        final Handler handler2 = new Handler();
        Timer timerMostraDado = new Timer();
        TimerTask taskMostraDado = new TimerTask() {
            @Override
            public void run() {
                handler2.post(new Runnable() {
                    public void run() {

                        for (int i = 0; i < protocolo.getDados().size(); i++) {
                            boolean taFocando = false;
                            if (i == idApelidoSelecionado) {
                                taFocando = true;
                            }

                            float ultimoTempoRecebido = protocolo.getDados().get(i).getTempoRecebimento().get(protocolo.getDados().get(i).getTempoRecebimento().size() - 1);
                            EventoTrocaDados evento = new EventoTrocaDados(protocolo.getDados().get(i).apelido, protocolo.getDados().get(i).getValores().get(protocolo.getDados().get(i).getValores().size() - 1), ultimoTempoRecebido, taFocando);
                            bus.post(evento);
                        }
                    }
                });
            }
        };

        timerMostraDado.schedule(taskMostraDado, 0, 10);
    }

    public void registerHeartbeatTimer() {

        final Handler handler3 = new Handler();
        Timer timerCheckHeartbeat = new Timer();
        TimerTask taskCheckHeartbeat = new TimerTask() {
            @Override
            public void run() {
                handler3.post(new Runnable() {
                    public void run() {

                        Calendar rightNow = Calendar.getInstance();

                        // offset to add since we're not UTC
                        long offset = rightNow.get(Calendar.ZONE_OFFSET) + rightNow.get(Calendar.DST_OFFSET);
                        long sinceMidnight = (rightNow.getTimeInMillis() + offset) % (24 * 60 * 60 * 1000);

                        float segundosDesdeMeianoite = (float) sinceMidnight / 1000;

                        for (int i = 0; i < protocolo.getDados().size(); i++) {
                            if (protocolo.getDados().get(i).apelido.equals("htb")) {
                                if (protocolo.getDados().get(i).getValores().get(protocolo.getDados().get(i).getValores().size() - 1) == 1) {
                                    if (segundosDesdeMeianoite - protocolo.getDados().get(i).getTempoRecebimento().get(protocolo.getDados().get(i).getTempoRecebimento().size() - 1) <= 2.0) {
                                        rbHeartbeat.setChecked(true);
                                        receiveingHeartbeat = true;
                                        btnPersConfig.setEnabled(true);
                                        btnPularConfig.setEnabled(true);
                                    } else {
                                        rbHeartbeat.setChecked(false);
                                        receiveingHeartbeat = false;
                                        platformTransmmiting = false;
                                        platformRecording = false;
                                        disableButtons();
                                    }

                                } else {
                                    rbHeartbeat.setChecked(false);
                                    receiveingHeartbeat = false;
                                    platformTransmmiting = false;
                                    platformRecording = false;
                                    disableButtons();
                                }
                            }

                            if (protocolo.getDados().get(i).apelido.equals("tmt")) {
                                if (protocolo.getDados().get(i).getValores().get(protocolo.getDados().get(i).getValores().size() - 1) == 1) {
                                    if (segundosDesdeMeianoite - protocolo.getDados().get(i).getTempoRecebimento().get(protocolo.getDados().get(i).getTempoRecebimento().size() - 1) <= 2.0) {
                                        platformTransmmiting = true;
                                    } else {
                                        platformTransmmiting = false;
                                    }
                                } else {
                                    platformTransmmiting = false;
                                }
                            }

                            if (protocolo.getDados().get(i).apelido.equals("gvd")) {
                                if (protocolo.getDados().get(i).getValores().get(protocolo.getDados().get(i).getValores().size() - 1) == 1) {
                                    if (segundosDesdeMeianoite - protocolo.getDados().get(i).getTempoRecebimento().get(protocolo.getDados().get(i).getTempoRecebimento().size() - 1) <= 2.0) {
                                        platformRecording = true;
                                    } else {
                                        platformRecording = false;
                                    }
                                } else {
                                    platformRecording = false;
                                }
                            }

                            if (protocolo.getDados().get(i).apelido.equals("cfg")) {
                                if (protocolo.getDados().get(i).getValores().get(protocolo.getDados().get(i).getValores().size() - 1) == 1) {
                                    if (segundosDesdeMeianoite - protocolo.getDados().get(i).getTempoRecebimento().get(protocolo.getDados().get(i).getTempoRecebimento().size() - 1) <= 2.0) {
                                        platformConfigured = true;
                                    } else {
                                        platformConfigured = false;
                                    }
                                } else {
                                    platformConfigured = false;
                                }
                            }

                        }
                    }
                });
            }
        };

        timerCheckHeartbeat.schedule(taskCheckHeartbeat, 0, 10);
    }

    public void registerUSB() {

        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
    }

    public void startConnection() {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 0x0403)//3DR vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                    tvTransmissorState.setText("Transmissor conectado");
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep) {
                    break;
                }
            }
        }
    }

    public void stopConnection() {

        serialPort.close();
        disableButtons();
    }

    public static void sendData(String dado) {
        dado = dado + "\n";
        serialPort.write(dado.getBytes());
    }


    public void disableButtons() {

        btnPersConfig.setEnabled(false);
        btnPularConfig.setEnabled(false);
    }

}