package whitesun.telemetry;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import de.greenrobot.event.EventBus;


public class Painel_Graficos_geral extends Activity{
    private TextView tvGraf1;
    private LinearLayout chartGraf1;
    private ImageButton btnSelectGraf1;
    private TextView tvGraf2;
    private LinearLayout chartGraf2;
    private ImageButton btnSelectGraf2;
    private TextView tvGraf3;
    private LinearLayout chartGraf3;
    private ImageButton btnSelectGraf3;

    private EventBus bus = EventBus.getDefault();

    private Graf graf1= new Graf();
    private Graf graf2 = new Graf();
    private Graf graf3 = new Graf();

    private boolean temX = false;
    private boolean temY = false;
    float ultimoX = 0;
    float ultimoY = 0;
    int idDadoGraf1 = -1;
    int idDadoGraf2 = -1;
    int idDadoGraf3 = -1;
    private boolean graf1Selected = false;
    private boolean graf2Selected = false;
    private boolean graf3Selected = false;

    String apelidoDados[] = new String[GroundstationConnection.protocolo.getDados().size()];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grafico_geral);
        findViews();

        graf1.setupChart("Grafico 1", chartGraf1, tvGraf1);
        graf2.setupChart("Grafico 2", chartGraf2, tvGraf2);
        graf3.setupChart("Grafico 3", chartGraf3, tvGraf3);

        bus.register(this);

        graf1.startMyTask();
        graf2.startMyTask();
        graf3.startMyTask();

//        SharedPreferences user = getApplicationContext().getSharedPreferences("test", MODE_PRIVATE);
//        idDadoGraf1 = user.getInt("idDadoGraf1", -1);
//        idDadoGraf2 = user.getInt("idDadoGraf2", -1);
//        idDadoGraf3 = user.getInt("idDadoGraf3", -1);
//        graf1Selected = user.getBoolean("graf1Selected", false);
//        graf2Selected = user.getBoolean("graf2Selected", false);
//        graf3Selected = user.getBoolean("graf3Selected", false);
//
        for (int i = 0; i < GroundstationConnection.protocolo.getDados().size(); i++) {
            apelidoDados[i] = GroundstationConnection.protocolo.getDados().get(i).getApelido();
        }

        GroundstationConnection.sendData("!dtg@");
//
//        graf1.mudarNome(apelidoDados[idDadoGraf1]);
//        graf2.mudarNome(apelidoDados[idDadoGraf2]);
//        graf3.mudarNome(apelidoDados[idDadoGraf3]);
    }

    @Override
    protected void onDestroy() {
        // Unregister
        bus.unregister(this);

        graf1.getChart().cancel(true);
        graf2.getChart().cancel(true);
        graf3.getChart().cancel(true);

        super.onDestroy();

        if (idDadoGraf1 != -1) {
            GroundstationConnection.sendData("!sts:" + apelidoDados[idDadoGraf1] + "=False@");
        }
        if (idDadoGraf2 != -1) {
            GroundstationConnection.sendData("!sts:" + apelidoDados[idDadoGraf2] + "=False@");
        }
        if (idDadoGraf3 != -1) {
            GroundstationConnection.sendData("!sts:" + apelidoDados[idDadoGraf3] + "=False@");
        }

//        SharedPreferences user = getApplicationContext().getSharedPreferences("test", MODE_PRIVATE);
//        SharedPreferences.Editor ed = user.edit();
//        ed.putInt("idDadoGraf1", idDadoGraf1);
//        ed.putInt("idDadoGraf2", idDadoGraf2);
//        ed.putInt("idDadoGraf3", idDadoGraf3);
//        ed.putBoolean("graf1Selected", graf1Selected);
//        ed.putBoolean("graf2Selected", graf2Selected);
//        ed.putBoolean("graf3Selected", graf3Selected);
//        ed.apply();
    }


    public void onEvent(EventoTrocaDados evento) {

        if (graf1Selected) {
            if (evento.getApelido().equals(apelidoDados[idDadoGraf1])) {
                graf1.atualiza(evento.getTempoRecebimento(), evento.getValor());
            }
        }
        if (graf2Selected) {
            if (evento.getApelido().equals(apelidoDados[idDadoGraf2])) {
                graf2.atualiza(evento.getTempoRecebimento(), evento.getValor());
            }
        }
        if (graf3Selected) {
            if (evento.getApelido().equals(apelidoDados[idDadoGraf3])) {
                graf3.atualiza(evento.getTempoRecebimento(), evento.getValor());
            }
        }
    }

    private void findViews() {
        tvGraf1 = (TextView)findViewById( R.id.tvGraf1);
        chartGraf1 = (LinearLayout)findViewById(R.id.chartGraf1);
        btnSelectGraf1 = (ImageButton) findViewById(R.id.btnSelectGraf1);
        tvGraf2 = (TextView)findViewById( R.id.tvGraf2);
        chartGraf2 = (LinearLayout)findViewById(R.id.chartGraf2);
        btnSelectGraf2 = (ImageButton) findViewById(R.id.btnSelectGraf2);
        tvGraf3 = (TextView)findViewById( R.id.tvGraf3);
        chartGraf3 = (LinearLayout)findViewById(R.id.chartGraf3);
        btnSelectGraf3 = (ImageButton) findViewById(R.id.btnSelectGraf3);

        btnSelectGraf1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (idDadoGraf1 != -1) {
                    GroundstationConnection.sendData("!sts:" + apelidoDados[idDadoGraf1] + "=False@");
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(Painel_Graficos_geral.this);
                builder.setTitle("Selecione o identificador");
                builder.setItems(apelidoDados, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        idDadoGraf1 = which;
                        graf1Selected = true;
                        graf1.mudarNome(apelidoDados[idDadoGraf1]);
                        GroundstationConnection.sendData("!sts:" + apelidoDados[idDadoGraf1] + "=True@");
                    }
                });
                builder.show();
            }
        });

        btnSelectGraf2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (idDadoGraf2 != -1) {
                    GroundstationConnection.sendData("!sts:" + apelidoDados[idDadoGraf2] + "=False@");
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(Painel_Graficos_geral.this);
                builder.setTitle("Selecione o identificador");
                builder.setItems(apelidoDados, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        idDadoGraf2 = which;
                        graf2Selected = true;
                        graf2.mudarNome(apelidoDados[idDadoGraf2]);
                        GroundstationConnection.sendData("!sts:" + apelidoDados[idDadoGraf2] + "=True@");
                    }
                });
                builder.show();
            }
        });

        btnSelectGraf3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (idDadoGraf3 != -1) {
                    GroundstationConnection.sendData("!sts:" + apelidoDados[idDadoGraf3] + "=False@");
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(Painel_Graficos_geral.this);
                builder.setTitle("Selecione o identificador");
                builder.setItems(apelidoDados, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        idDadoGraf3 = which;
                        graf3Selected = true;
                        graf3.mudarNome(apelidoDados[idDadoGraf3]);
                        GroundstationConnection.sendData("!sts:" + apelidoDados[idDadoGraf3] + "=True@");
                    }
                });
                builder.show();
            }
        });
    }

    public class Graf {
        private GraphicalView mChart;
        private TextView tvNomeDado;
        private XYSeries visitsSeries;
        private XYMultipleSeriesDataset dataset;
        private ChartTask chart = new ChartTask();
        private int indexGPS = 0;
        private XYSeriesRenderer visitsRenderer;
        private XYMultipleSeriesRenderer multiRenderer;
        private boolean isGps = false;
        private boolean temDado = false;

        boolean atualizou;

        float maxX = -1, maxY = 1, minX = 0, minY = 0;
        float valorAtual, tempoAtual;
        private String nome;

        public ChartTask getChart(){
            return chart;
        }


        private void atualiza(float temp, float val){
            atualizou = true;
            tempoAtual =  temp;
            valorAtual = val;
        }

        private void mudarNome(String novoNome) {
            nome = novoNome;
        }

        private void setupChart(String var, LinearLayout chartContainer, TextView tvnomDad){
            tvNomeDado = tvnomDad;
            nome = var;

            // Creating an  XYSeries for Visits
            visitsSeries = new XYSeries("Unique Visitors");
            // Creating a dataset to hold each series
            dataset = new XYMultipleSeriesDataset();
            // Adding Visits Series to the dataset
            dataset.addSeries(visitsSeries);

            // Creating XYSeriesRenderer to customize visitsSeries
            visitsRenderer = new XYSeriesRenderer();
            //  visitsRenderer.setColor(Color.BLACK);
            // visitsRenderer.setPointStyle(PointStyle.CIRCLE);
            //        visitsRenderer.setFillPoints(true);
            visitsRenderer.setLineWidth(2);
            visitsRenderer.setDisplayChartValues(false);


            // Creating a XYMultipleSeriesRenderer to customize the whole chart
            multiRenderer = new XYMultipleSeriesRenderer();
            multiRenderer.setYLabelsAlign(Paint.Align.RIGHT);
            multiRenderer.setChartTitle("");
            multiRenderer.setXTitle("");
            multiRenderer.setYTitle("");
            multiRenderer.setZoomButtonsVisible(false);
            multiRenderer.setShowLegend(false);
            multiRenderer.setLabelsTextSize(25);
            multiRenderer.setMargins(new int[]{20, 100, 5, 20});

            multiRenderer.setApplyBackgroundColor(true);
            multiRenderer.setBackgroundColor(Color.TRANSPARENT);
            multiRenderer.setMarginsColor(Color.argb(0x00, 0x01, 0x01, 0x01));

            multiRenderer.setYLabelsColor(0, Color.BLACK);
            multiRenderer.setXLabelsColor(Color.BLACK);
            multiRenderer.setShowGrid(true);
            multiRenderer.setGridColor(Color.GRAY);

            // Adding visitsRenderer to multipleRenderer
            // Note: The order of adding dataseries to dataset and renderers to multipleRenderer
            // should be same
            multiRenderer.addSeriesRenderer(visitsRenderer);

            mChart = ChartFactory.getLineChartView(getBaseContext(), dataset, multiRenderer);

            // Adding the Line Chart to the LinearLayout
            chartContainer.addView(mChart);
        }


        @TargetApi(Build.VERSION_CODES.HONEYCOMB) // API 11
        void startMyTask() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                chart.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            else {
                chart.execute();
            }
        }


        private class ChartTask extends AsyncTask<Void, String, Void> {
            public float tempoAnterior = 0;

            // Generates dummy data in a non-ui thread
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    while (true) {
                        String ta[] = new String[2];
                        publishProgress(ta);
                        atualizou = false;
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                    // Do nothing
                }
                return null;
            }

            // Plotting generated data in the graph
            @Override
            protected void onProgressUpdate(String... values) {
                if (tempoAtual != tempoAnterior) {
                    if (minX == -1) {
                        minX = tempoAtual;
                    }
                    if (maxX == -1) {
                        maxX = tempoAtual;
                    }
                    if (tempoAtual < minX) {
                        minX = tempoAtual;
                        multiRenderer.setXAxisMin(minX);
                    }
                    if (tempoAtual > maxX) {
                        maxX = tempoAtual;
                        multiRenderer.setXAxisMax(maxX);
                    }
                    if (valorAtual > maxY && valorAtual < 1000) {
                        maxY = valorAtual * 1.20f;
                        multiRenderer.setYAxisMax(maxY);
                    }
                    if (valorAtual < minY && valorAtual > -1000) {
                        minY = valorAtual * 0.8f;
                        multiRenderer.setYAxisMin(minY);
                    }
                    if (!tvNomeDado.getText().toString().equals(nome + " = " + valorAtual)) {
                        tvNomeDado.setText(nome + " = " + valorAtual);
                        //      multiRenderer.setChartTitle(nome);
                        visitsSeries.setTitle(nome);

                    }
                    if(visitsSeries.getItemCount()>100){
                        visitsSeries.remove(0);
                    }
                    visitsSeries.add(tempoAtual, valorAtual);
                    multiRenderer.getSeriesRendererAt(0).setColor(Color.parseColor("#5159C2"));
                    mChart.repaint();
                    tempoAnterior = tempoAtual;
                }
            }
        }
    }
}