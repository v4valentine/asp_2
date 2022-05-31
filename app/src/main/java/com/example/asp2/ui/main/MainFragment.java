package com.example.asp2.ui.main;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.asp2.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;

public class MainFragment extends Fragment {

    ////////////////////////////////
    //BLUETOOTH VARIABLES

    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;
    // Identificador unico de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    private static String address = null;
    private static BluetoothAdapter bluetoothAdapter;
    private static Button act_bluetooth;
    private static String nombre_asp = "ASP";
    ////////////////////////////////

    private MainViewModel mViewModel;
    private static RadioButton ruta1;
    private static RadioButton ruta2;
    private static RadioButton ruta3;
    private static RadioGroup grupo;
    private static TextView estado;
    private static EditText num_ciclos;
    private static Button btn;
    private static Switch estado_switch;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        // TODO: Use the ViewModel

        ruta1 = (RadioButton) getActivity().findViewById(R.id.ruta1);
        ruta2 = (RadioButton) getActivity().findViewById(R.id.ruta2);
        ruta3 = (RadioButton) getActivity().findViewById(R.id.ruta3);
        grupo = (RadioGroup) getActivity().findViewById(R.id.ruta_elegida);
        estado = (TextView) getActivity().findViewById(R.id.estado);
        num_ciclos = (EditText) getActivity().findViewById(R.id.numero_ciclos);
        btn = (Button) getActivity().findViewById(R.id.boton_activar);
        estado_switch = (Switch) getActivity().findViewById(R.id.estado_switch);
        act_bluetooth = (Button) getActivity().findViewById(R.id.act_bluetooth);
        btn.setEnabled(false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cambiar_estado();
                String i = "a";

                if(grupo.getCheckedRadioButtonId() == ruta1.getId()){
                    i = "a";
                }else if(grupo.getCheckedRadioButtonId() == ruta2.getId()){
                    i = "b";
                }else if(grupo.getCheckedRadioButtonId() == ruta3.getId()){
                    i = "c";
                }

                MyConexionBT.write(""+i);
            }
        });

        act_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conectar_asp();

                /*final Runnable r = new Runnable() {
                    public void run() {
                        conectar_asp();
                    }
                };*/

            }
        });
    }

    public void Cambiar_estado() {

        if (estado_switch.isChecked()) {
            estado.setText(R.string.descansar);
            estado.setBackgroundResource(R.color.desocupado);
            btn.setText(R.string.trabajar);
            Toast.makeText(getContext(), "Limpieza terminada!", Toast.LENGTH_SHORT).show();
            estado_switch.setChecked(false);
            num_ciclos.setEnabled(true);
            ruta1.setEnabled(true);
            ruta2.setEnabled(true);
            ruta3.setEnabled(true);

            MyConexionBT.write("x");

        } else {
            estado.setText(R.string.trabajando);
            estado.setBackgroundResource(R.color.ocupado);
            btn.setText(R.string.descansar);
            Toast.makeText(getContext(), "Comenzando la limpieza!", Toast.LENGTH_SHORT).show();
            estado_switch.setChecked(true);
            num_ciclos.setEnabled(false);
            ruta1.setEnabled(false);
            ruta2.setEnabled(false);
            ruta3.setEnabled(false);

        }

    }

    public void buscar_asp() {
        if (bluetoothAdapter == null) {

        }else if (!bluetoothAdapter.isEnabled()) {

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        } else {

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {

                Boolean x = true;

                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals(nombre_asp)) {

                        address = device.getAddress();

                        x = false;
                    }
                }

                if (x) {
                    Toast.makeText(getContext(), "Dispositivo no conectado", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getContext(), "Dispositivo no conectado", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        VerificarEstadoBT();

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

        }
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    public void conectar_asp() {

        //Setea la direccion MAC

        buscar_asp();
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establece la conexión con el socket Bluetooth.
        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                btSocket.connect();
                btn.setEnabled(true);
                Toast.makeText(getContext(), "Se conecto a ASP", Toast.LENGTH_SHORT).show();

            }
        } catch (IOException e) {
            Toast.makeText(getContext(), "No se encontro la ASP", Toast.LENGTH_SHORT).show();

            try {
                btSocket.close();
            } catch (IOException e2) {}
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
    }

    private void VerificarEstadoBT() {

        if(btAdapter==null) {
            Toast.makeText(getContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //Crea la clase que permite crear el evento de conexión
    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] byte_in = new byte[1];
            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    mmInStream.read(byte_in);
                    char ch = (char) byte_in[0];
                    bluetoothIn.obtainMessage(handlerState, ch).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String input)
        {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                Toast.makeText(getContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        }
    }

    @Override
    public void onDestroy() {

        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                ;
            }
        }

        super.onDestroy();
    }

    /*
    public void Guardar_estado(){
        try {

            FileOutputStream stream = new FileOutputStream(File.createTempFile("estado", null, getContext().getCacheDir()));

            try {

                if(estado_switch.isSelected()){

                    stream.write("0".getBytes(StandardCharsets.UTF_8));
                }else {

                    stream.write("1".getBytes(StandardCharsets.UTF_8));
                }

            } finally {
                stream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Boolean leer_estado(){

        Integer contents = 0;

        File estado = new File(getContext().getCacheDir(), "estado");

        Integer length = (int) estado.length();

        byte[] bytes = new byte[length];

        try {

            FileInputStream in = new FileInputStream(estado);
            try {
                in.read(bytes);
            } finally {
                in.close();
            }

            contents = Integer.parseInt(new String(bytes));

            Toast.makeText(getContext(), ""+contents, Toast.LENGTH_SHORT).show();

        }catch (Exception e){
            System.out.print(e);
        };

        if(contents == 1){

            return true;

        }else {

            return false;
        }
    }
*/

}
