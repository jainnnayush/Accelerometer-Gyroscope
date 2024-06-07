package com.example.myfirstapplication

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.myfirstapplication.R

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private lateinit var accelerometerTextView: TextView
    private lateinit var gyroscopeTextView: TextView
    private lateinit var orientationTextView: TextView
    private lateinit var positionTextView:TextView

    private val acceleration = floatArrayOf(0f, 0f, 0f)

    private var velocity = FloatArray(3) { 0f }
    private var position = FloatArray(3) { 0f }
    private var lastUpdateTime: Long = 0

    private var yawAngle: Float = 0.0f
    private var lastTimestamp: Long = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        accelerometerTextView = findViewById(R.id.accelerometerTextView)
        gyroscopeTextView = findViewById(R.id.gyroscopeTextView)
        orientationTextView = findViewById(R.id.orientationTextView)
        positionTextView=findViewById(R.id.positionTextView)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { acc ->
            sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.also { gyr ->
            sensorManager.registerListener(this, gyr, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0] * 100
                val y = event.values[1] * 100
                val z = event.values[2] * 100
                acceleration[0]=x
                acceleration[1]=y
                acceleration[2]=z
                val positions=updatePosition(acceleration,event.timestamp)

                accelerometerTextView.text = "Accelerometer\nx: $x\ny: $y\nz: $z"
                positionTextView.text = "Position\nx: ${positions[0]}\ny: ${positions[1]}\nz: ${positions[2]}"
            }
            Sensor.TYPE_GYROSCOPE -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                gyroscopeTextView.text = "Gyroscope\nx: $x\ny: $y\nz: $z"
                calculateYaw(event.timestamp, z)
            }
        }
    }

    private fun calculateYaw(timestamp: Long, yawRate: Float) {
        if (lastTimestamp != 0L) {
            val dt = (timestamp - lastTimestamp) * 1.0e-9 // Convert nanoseconds to seconds
            yawAngle = yawAngle + (yawRate * dt * 180/Math.PI).toFloat()
        }
        lastTimestamp = timestamp

        orientationTextView.text = "Yaw Angle: $yawAngle"
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Handle sensor accuracy changes if needed
    }
    private fun updatePosition(linearAcceleration: FloatArray, currentTime: Long): FloatArray {
        linearAcceleration[2]=0f
        if (lastUpdateTime == 0L) {
            lastUpdateTime = currentTime
            return position
        }
        val dt = (currentTime - lastUpdateTime) * 1.0f / 1_000_000_000.0f
        lastUpdateTime = currentTime
        for (i in 0 until 3) {
            velocity[i] += linearAcceleration[i] * dt
            position[i] += velocity[i] * dt
        }
        return position
    }
}