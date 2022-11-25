package edu.nitt.delta.orientation22

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.ar.sceneform.math.Vector3
import dagger.hilt.android.AndroidEntryPoint
import dev.romainguy.kotlin.math.Float3
import edu.nitt.delta.orientation22.compose.screens.ArScreen
import edu.nitt.delta.orientation22.di.viewModel.actions.ArAction
import edu.nitt.delta.orientation22.di.viewModel.actions.MapAction
import edu.nitt.delta.orientation22.di.viewModel.uiState.ArStateViewModel
import edu.nitt.delta.orientation22.di.viewModel.uiState.MapStateViewModel
import edu.nitt.delta.orientation22.models.MarkerModel
import edu.nitt.delta.orientation22.models.game.LocationRequest
import edu.nitt.delta.orientation22.ui.theme.Orientation22androidTheme
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.LightEstimationMode
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.light.intensity


@AndroidEntryPoint
class ArResolveActivity : ComponentActivity() {
    private lateinit var arSceneView: ArSceneView
    private lateinit var cloudAnchorNode: ArModelNode
    private val viewModel:ArStateViewModel by viewModels()
    private val mapStateViewModel: MapStateViewModel by viewModels()

    private val DEFAULT_LIGHT_INTENSITY=0.3f

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fusedLocationProviderClient : FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        var locationId = intent.getIntExtra("LocationId", 1)


        setContent {
            Orientation22androidTheme {
                val context = LocalContext.current
//                viewModel.doAction(ArAction.FetchLocation)
                var locations = viewModel.locationData.value

                var glbFileUrl = remember { mutableStateOf("miyawaki.glb") }

                locationId = viewModel.locationId.value

                Log.d("locations", locations.toString())

                Log.d("glb", locations.toString())

                Log.d("location-id", viewModel.locationId.toString())
                Log.d("glb-url", viewModel.glbUrl)



                mapStateViewModel.doAction(MapAction.GetRoute)
                var routeList = mapStateViewModel.routeListData.value
                mapStateViewModel.doAction(MapAction.GetCurrentLevel)
                val currentLevel = mapStateViewModel.currentState.value

                ArScreen(updateSceneView = {arSceneView ->
                    this.arSceneView = arSceneView
                    cloudAnchorNode = ArModelNode(placementMode = PlacementMode.PLANE_HORIZONTAL)
                    setUpEnvironment(glbFileUrl)
                }, onClick = {
                    viewModel.doAction(ArAction.PostAnswer)
                }, currentLevel = currentLevel, routeList = routeList)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 50.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            viewModel.doAction(ArAction.ResolveAnchor(
                                cloudAnchorNode,
                                ArMainActivity.anchorId,
                                context = context
                            ))
                        }
                    ) {
                        Text("Resolve")
                    }
                }
            }
        }
    }

    //ua-6c2cd916a19fe941905220158f137778

    //ua-d017680d317ab1343505e54f7a958b66 - CLassics

    private fun setUpEnvironment(
        glbFileUrl: MutableState<String>,
    ){
        Log.d("URL", viewModel.glbUrl)
        arSceneView.lightEstimationMode= LightEstimationMode.DISABLED
        arSceneView.mainLight?.intensity = DEFAULT_LIGHT_INTENSITY
        arSceneView.cloudAnchorEnabled=true
        cloudAnchorNode.isVisible = false
        cloudAnchorNode.scale = Float3(ArMainActivity.xScale, ArMainActivity.yScale, ArMainActivity.zScale)
        cloudAnchorNode.isRotationEditable = false
        cloudAnchorNode.isPositionEditable = false
        cloudAnchorNode.isScaleEditable = false
        viewModel.doAction(
            ArAction.LoadModel(
                this,
                lifecycle,
                { _, _ ->
                    tapModel()
                },
                arSceneView,
                cloudAnchorNode,
                ArMainActivity.glbUrl,
                false
            )
        )
    }

    private fun tapModel(){
        Toast.makeText(applicationContext,"Model clicked successfully",Toast.LENGTH_SHORT).show()
    }
}