package com.cod1.notification_aplication
//vreau sa dau insert in aplicatie pentru house id si zi,si lazy column cu recomandari
import android.Manifest
import android.R.attr.fontWeight
import android.R.attr.textSize
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Message
import android.se.omapi.Channel
import android.service.autofill.OnClickAction
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.cod1.notification_aplication.ui.theme.Notification_AplicationTheme
import kotlinx.coroutines.launch
import kotlin.jvm.java
import androidx.compose.material3.TextField
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.key
import androidx.compose.ui.text.font.FontWeight

val NOTIFICATION_ID=1
val CHANNEL_ID ="0"
class MainActivity : ComponentActivity() {
    private var recommendations by mutableStateOf<List<SpecificMessage>>(emptyList())
    private var generalMessage by mutableStateOf("")

    private var selfSufficiency by mutableStateOf<Double?>(null)

    private var selfConsumption by mutableStateOf<Double?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        CreateNotificationChannel()
        setContent {
            Notification_AplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RecommendationPusherAndScreen(
                        name = "Recommendation system",
                        recommendations = recommendations,
                        generalMessage= generalMessage,
                        selfSufficiency=selfSufficiency,
                        selfConsumption=selfConsumption,
                        onSendClick = {houseId,day-> SendNotification(houseId,day)} ,
                    )

                }
            }
        }
    }

    fun CreateNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    private fun SendNotification(houseId: Int,day: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getRecommendations(houseId = houseId, day = day   )
                recommendations=response.specific_message
                generalMessage=response.general_message
                var message=response.specific_message.firstOrNull {
                    it.MessageType==2 }?:response.specific_message.firstOrNull{
                        it.MessageType==5}?:response.specific_message.firstOrNull{
                            it.MessageType==3 }?:response.specific_message.firstOrNull{
                                it.MessageType==7}
                val response2= RetrofitClient.api.getSelfSufficiency(houseId=houseId,day=day)
                selfSufficiency=response2.Self_sufficiency
                selfConsumption=response2.Self_consumption
                ShowNotification(response.general_message, message)
            } catch (e: Exception) {
                Log.e("RECOMMENDATION_API", "Request failed", e)
                recommendations=emptyList()
                generalMessage=""
                selfSufficiency=null
                selfConsumption=null
                ShowNotification(
                    titlu = "Eroare: ${e.javaClass.simpleName}",
                    mesaj = SpecificMessage(
                        Message = e.message ?: "Eroare necunoscută",
                        MessageType = -1
                    )
                )
            }
        }
    }
    private fun ShowNotification(titlu: String, mesaj: SpecificMessage?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)


        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.green_energy_icon)
            .setContentTitle("Recomandare energie")
            .setContentText("Ai primit o notificare")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(mesaj?.Message ?:titlu)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            )
                return@with
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}
// luam datele pe care el avem , dau recomandare
//productia mai mic decat consumul
//productia similara cu consumul
//lista in aplicatie cu recomandari pentru ziua aceeia

@Composable
fun EditNumberField(value:String,onValueChange:(String)->Unit,label:String,modifier:Modifier=Modifier){
    TextField(
        value=value,
        onValueChange = { newValue -> onValueChange(newValue.filter(Char::isDigit)) },
        singleLine=true,
        label={ Text(label)},
        keyboardOptions= KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier=modifier
    )
}
@Composable
fun RecommendationPusherAndScreen(name: String,
                                  recommendations:List<SpecificMessage>,
                                  generalMessage:String,
                                  onSendClick:(houseId:Int,day: Int)->Unit,
                                  selfSufficiency: Double?,
                                  selfConsumption: Double?,
                                  modifier: Modifier = Modifier)
{
    var houseIdInput by remember { mutableStateOf("") }
    var dayInput by remember{mutableStateOf("")}
    var errorMessage by remember{mutableStateOf<String?>(null)}
Column(modifier=Modifier
    .fillMaxSize()
    .padding(16.dp)
    .paint(painterResource(id=R.drawable.green_energy_picture_hd_png), contentScale = ContentScale.FillBounds)
    , verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally){
    Text(
           text=name,
           textAlign = TextAlign.Center,
           fontSize = 20.sp,
           fontWeight= FontWeight.Bold,
           modifier= Modifier
               .padding(16.dp)
       )
    Spacer(modifier=Modifier.height(16.dp))
    EditNumberField(
        value=houseIdInput,
        onValueChange = {houseIdInput = it},
        label="House Id",
        modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth())

    Spacer(modifier=Modifier.height(16.dp))
    EditNumberField(
        value=dayInput,
        onValueChange={dayInput = it},
        label="Day",
        modifier=Modifier.padding(bottom=16.dp).fillMaxWidth()
    )
    errorMessage?.let{
        Text(
            text=it,
            color=Color.Black,
            modifier=Modifier.padding(10.dp)
        )
    }


        Button(onClick ={
            val houseId=houseIdInput.toIntOrNull()
            val day=dayInput.toIntOrNull()
            if (houseId==null || day==null || day<1){
                errorMessage="Enter a valid house Id and day"
            }else {
                errorMessage=null
                onSendClick(houseId,day)
            }
        },modifier=Modifier.padding(16.dp)  , colors = ButtonDefaults.buttonColors(contentColor = Color.Green)) {Text("Refresh Recomendations")}
        if (generalMessage.isNotBlank()){
            Text(
                text=generalMessage,
                color=Color.White,
                modifier=Modifier.fillMaxWidth().padding(12.dp)
            )
        }
        Spacer(modifier=Modifier.height(4.dp))

        if(selfSufficiency !=null && selfConsumption!=null){
            Column(modifier=Modifier.fillMaxWidth().padding(8.dp)){
                Text(
                    text="Indicatorii care reprezinta performanta retelei energetice:"
                )
                Spacer(modifier=Modifier.height(4.dp))
                Text(
                    text="Self sufficiency: ${"%.1f".format(selfSufficiency*100)}%",
                    color=Color.White
                )
                Spacer(modifier=Modifier.height(4.dp))
                Text(
                    text="Self consumption: ${"%.1f".format(selfConsumption*100)}%",
                    color=Color.White
                )
            }
        }
    LazyColumn(modifier=Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.SpaceBetween)
    {items(count = recommendations.size,
        key={index  -> recommendations[index].MessageType },
        contentType = {index->"recommendation"},
        itemContent = {index->
            val recommendation=recommendations[index]
            Column(modifier=Modifier.fillMaxWidth().padding(12.dp).border(width = 1.dp, color = Color.Green, shape = RoundedCornerShape(8.dp)).padding(6.dp))
                    {Text(
                        text="Recommendation Type ${recommendation.MessageType}",
                        color=Color.White,
                        fontSize=18.sp
                        )
                    Spacer(modifier=Modifier.height(6.dp))
                    Text(
                        text=recommendation.Message,
                        color=Color.White,
                        fontSize = 16.sp,
                        fontWeight= FontWeight.Bold
                        )

                    }
                }
            )
        }

    }
}
