package com.foc.pmdm_t3_55124290y;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Botones
    Button btnGrabacion, btnReproduccion, btnRetroceder, btnAvanzar;

    //MediaRecorder
    MediaRecorder mr = null;

    //MediaPlayer
    MediaPlayer mp = null;

    //Permiso de para usar el micrófono
    boolean permisoMicrofono = false;

    //Control estado botones
    boolean estaGrabando = false;
    boolean estaReproduciendo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Se capturan los botones de la IU
        btnGrabacion = findViewById(R.id.btnGrabacion);
        btnReproduccion = findViewById(R.id.btnReproduccion);
        btnReproduccion.setEnabled(false);
        btnRetroceder = findViewById(R.id.btnRetroceder);
        btnRetroceder.setEnabled(false);
        btnAvanzar = findViewById(R.id.btnAvanzar);
        btnAvanzar.setEnabled(false);

        //MediaPlayer
        mp = new MediaPlayer();

        //Ruta del fichero que se va a reproducir
        String ruta = getFilesDir().getAbsolutePath() + File.separator + "Grabacion.3gp";
        try {
            mp.setDataSource(ruta);
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "No se encuentra el fichero 'Grabacion.3gp'",
                    Toast.LENGTH_SHORT).show();
        }

        //Accion que se realiza al finalizar la reproducción
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(getApplicationContext(), "Reproducción finalizada", Toast.LENGTH_SHORT).show();
                botonesHabilitados(true, true, false, false);
                btnReproduccion.setText("Reproducir");
                estaReproduciendo = false;
            }
        });
    }


    // CALLBACKS DEL ACTIVITY ----------------------------------------------------------------------
    public void onPause() {
        super.onPause();
        if (mp.isPlaying()) {
            mp.pause();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGrabacion:
                if (estaGrabando) {
                    this.detenerGrabacion();
                } else {
                    this.comenzarGrabacion();
                }
                break;
            case R.id.btnReproduccion:
                if (estaReproduciendo) {
                    this.pausarReproduccion();
                } else {
                    this.comenzarReproduccion();
                }
                break;
            case R.id.btnRetroceder:
                this.retroceder();
                break;
            case R.id.btnAvanzar:
                this.avanzar();
                break;
        }
    }

    // MÉTODOS ASOCIADOS A LA GRABACIÓN: -----------------------------------------------------------
    public void comenzarGrabacion() {

        //Se comprueba que se tengan los permisos necesarios
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

            // Objeto MediaRecorder
            mr = new MediaRecorder();
            //Fuente de audio (micrófono)
            mr.setAudioSource(MediaRecorder.AudioSource.MIC);
            //Formato de salida (3GP)
            mr.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            //Ruta donde se almacenará
            String ruta = getFilesDir().getAbsolutePath() + File.separator + "Grabacion.3gp";
            mr.setOutputFile(ruta);
            //Codificador
            mr.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            //Se prepara la grabación
            try {
                mr.prepare();
            } catch (IOException e) {
                //En caso de error al preparar
                mr.reset();
                mr.release();
                mr = null;
                e.printStackTrace();
            }

            Toast.makeText(this, "Grabando", Toast.LENGTH_SHORT).show();
            //Comienza la grabacion
            mr.start();

        } else {
            //En caso de no tener los permisos
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 100);
        }

        //Gestión de botones
        this.botonesHabilitados(true, false, false, false);
        btnGrabacion.setText("Parar grabación");

        //Activa animación
        Animation anim = new AlphaAnimation(0.5f, 1.0f);
        anim.setDuration(900);
        anim.setStartOffset(150);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        btnGrabacion.startAnimation(anim);

        estaGrabando = true;
    }

    public void detenerGrabacion() {
        //Se detiene la grabacion
        mr.stop();
        //Se resetea y libera el objeto
        mr.reset();
        mr.release();
        mr = null;

        Toast.makeText(this, "Fin de la grabación", Toast.LENGTH_SHORT).show();

        //Gestión de botones
        this.botonesHabilitados(true, true, false, false);
        btnGrabacion.setText("Grabar");

        //Desactiva animación
        btnGrabacion.clearAnimation();
        btnGrabacion.clearFocus();

        estaGrabando = false;
    }

    // MÉTODOS ASOCIADOS A LA REPRODUCCION: --------------------------------------------------------

    public void comenzarReproduccion() {
        //Comienza la reproducción
        mp.start();
        Toast.makeText(this, "Reproduciendo", Toast.LENGTH_SHORT).show();

        //Gestión de botones
        this.botonesHabilitados(false, true, true, true);
        btnReproduccion.setText("Pausar reproducción");

        estaReproduciendo = true;
    }

    public void pausarReproduccion() {
        //Se pausa la reproducción
        mp.pause();
        Toast.makeText(this, "Reproduccion detenida", Toast.LENGTH_SHORT).show();

        //Gestión de botones
        this.botonesHabilitados(false, true, true, true);
        btnReproduccion.setText("Reproducir");

        estaReproduciendo = false;
    }

    public void retroceder(){
        mp.seekTo(mp.getCurrentPosition()-5000);
    }

    public void avanzar(){
        mp.seekTo(mp.getCurrentPosition()+5000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 100:
                permisoMicrofono = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permisoMicrofono) {
            finish();
        } else {
            //Si se obtiene el permiso
            comenzarGrabacion();
        }
    }


    // GESTIÓN DE LOS BOTONES ----------------------------------------------------------------------
    public void botonesHabilitados(boolean btnGrabar, boolean btnReproducir, boolean btnRetroceder, boolean btnAvanzar) {
        this.btnGrabacion.setEnabled(btnGrabar);
        this.btnReproduccion.setEnabled(btnReproducir);
        this.btnRetroceder.setEnabled(btnRetroceder);
        this.btnAvanzar.setEnabled(btnAvanzar);
    }
}