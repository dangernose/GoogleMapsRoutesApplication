package com.example.pettrackingsystem;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;

import java.text.DateFormat;
import java.util.Date;

public class MQTTClient {


    private static final String TAG = "MQTTClient";
    private MainActivity activity = null;
    private String mqttBroker = "tcp://test.mosquitto.org:1883";
    private String mqttTopic = "codifythings/";
    private String deviceId = "androidClient4";
    private String messageContent = "Time,Lat,Lang";

    public MQTTClient(MainActivity activity){
        this.activity = activity;
    }

    public void connectToMQTT() throws MqttException{
        mqttTopic = mqttTopic + activity.getPetID();
        Log.i(TAG,"Setting Connection Options");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);

        Log.i(TAG, "Creating New Client");
        MqttClient client = new MqttClient(mqttBroker,deviceId,new MemoryPersistence());
        client.connect(options);

        Log.i(TAG,"Subscribing To Topic");
        client.setCallback(new MqttEventCallback());
        client.subscribe(mqttTopic,0);
    }

    private class MqttEventCallback implements MqttCallback {
        @Override
        public void connectionLost(Throwable cause) {

        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.i(TAG,"New Message Arrived from Topic - " + topic);

            try{
                Log.d("DATA",String.valueOf(message.getPayload().toString()));
                String sensorMessage = new String(message.getPayload());

                activity.createNotification("Pet Location Activity",sensorMessage);
                activity.updateView(sensorMessage);
            }catch (Exception ex){
                Log.e(TAG,ex.getMessage());
            }


        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    }

    public void publishToMQTT() throws MqttException {
        // Request clean session in the connection options.
        Log.i(TAG, "Setting Connection Options");
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);



        // Attempt a connection to MQTT broker using the values
        // of connection variables.
        Log.i(TAG, "Creating New Client");
        MqttClient client = new MqttClient(mqttBroker, deviceId,
                new MemoryPersistence());
        client.connect(options);



        // Publish message to topic
        Log.i(TAG, "Publishing to Topic");
        MqttMessage mqttMessage =
                new MqttMessage(messageContent.getBytes());
        mqttMessage.setQos(2);
        client.publish(mqttTopic, mqttMessage);
        Log.i(TAG, "Publishing Complete");



        Log.i(TAG, "Disconnecting from MQTT");
        client.disconnect();
    }

}
