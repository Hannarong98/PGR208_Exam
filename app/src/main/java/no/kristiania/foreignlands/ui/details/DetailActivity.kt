package no.kristiania.foreignlands.ui.details

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_detail.*
import no.kristiania.foreignlands.R
import no.kristiania.foreignlands.data.api.NoForeignLandsApiService
import no.kristiania.foreignlands.data.repository.DetailsRepository
import no.kristiania.foreignlands.data.utils.NetworkConnectionInterceptor
import no.kristiania.foreignlands.ui.map.MapsActivity

class DetailActivity : AppCompatActivity() {

    private var placeID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        placeID = intent.getStringExtra("placeID")
        val networkConnectionInterceptor = NetworkConnectionInterceptor(this)
        val api = NoForeignLandsApiService(networkConnectionInterceptor)
        val repository = DetailsRepository(api)

        val viewModel by viewModels<DetailViewModel> {
            DetailViewModelFactory(
                repository,
                placeID!!
            )
        }
        viewModel.detail.observe(this, Observer { place ->

            var placeComment = place.comments.replace("(<[^>]*>)|(&[a-z]+;)".toRegex(), "")

            if (placeComment.isEmpty()) {
                placeComment = getString(R.string.place_no_description)
                detail_description.text = placeComment
            } else {
                detail_description.text =
                    HtmlCompat.fromHtml(place.comments, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }

            detail_name.text = place.name

            detail_pin_button.setOnClickListener {
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("lat", place.lat)
                intent.putExtra("long", place.lon)
                intent.putExtra("placeName", place.name)

                if (hasPermission()) {
                    startActivity(intent)
                } else {
                    requestForPermission()
                    if (hasPermission())
                        startActivity(intent)
                }

            }

            Glide.with(this)
                .load(place.banner)
                .placeholder(R.drawable.img_placeholder)
                .into(detail_image)
        })

    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestForPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                100
            )
        } else {
            Toast.makeText(this, "Permission has already been granted", Toast.LENGTH_LONG).show()
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            100 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }


}
