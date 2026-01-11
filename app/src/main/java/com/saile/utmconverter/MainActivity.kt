package com.saile.utmconverter

// =========================================================
// IMPORTS
// =========================================================
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import java.util.Locale

import org.locationtech.proj4j.CRSFactory
import org.locationtech.proj4j.CoordinateReferenceSystem
import org.locationtech.proj4j.CoordinateTransformFactory
import org.locationtech.proj4j.ProjCoordinate

// =========================================================
// MAIN ACTIVITY
// =========================================================
class MainActivity : AppCompatActivity() {

    // -----------------------------------------------------
    // UI
    // -----------------------------------------------------
    private lateinit var inputLine: EditText
    private lateinit var resultLine: TextView
    private lateinit var buttonConvert: Button
    private lateinit var buttonMaps: Button
    private lateinit var buttonCopy: Button

    private lateinit var exampleSouth: TextView
    private lateinit var exampleNorth: TextView

    // -----------------------------------------------------
    // STATE
    // -----------------------------------------------------
    private var lastLat: Double? = null
    private var lastLon: Double? = null

    // -----------------------------------------------------
    // PROJ4J
    // -----------------------------------------------------
    private val crsFactory = CRSFactory()
    private val ctFactory = CoordinateTransformFactory()

    // =====================================================
    // LIFECYCLE
    // =====================================================
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupActions()
    }

    // =====================================================
    // UI SETUP
    // =====================================================
    private fun bindViews() {
        inputLine = findViewById(R.id.inputLine)
        resultLine = findViewById(R.id.resultLine)
        buttonConvert = findViewById(R.id.buttonConvert)
        buttonMaps = findViewById(R.id.buttonMaps)
        buttonCopy = findViewById(R.id.buttonCopy)

        exampleSouth = findViewById(R.id.exampleSouth)
        exampleNorth = findViewById(R.id.exampleNorth)
    }

    private fun setupActions() {

        buttonConvert.setOnClickListener {
            convert()
        }

        buttonMaps.setOnClickListener {
            openInMaps()
        }

        buttonCopy.setOnClickListener {

            val text = resultLine.text.toString()

            if (text.isBlank() || text == "—") {
                toast("Nada para copiar")
                return@setOnClickListener
            }

            val clipboard =
                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val clip = ClipData.newPlainText("Coordenada", text)
            clipboard.setPrimaryClip(clip)

            toast("Coordenada copiada 📋")
        }

        exampleSouth.setOnClickListener {
            inputLine.setText("23K 683449 7460698")
        }

        exampleNorth.setOnClickListener {
            inputLine.setText("36R 319655 3317607")
        }
    }

    // =====================================================
    // MAIN LOGIC
    // =====================================================
    private fun convert() {

        val text = inputLine.text.toString().trim()

        try {

            if (isGeographic(text)) {
                // GEO → UTM
                val (lat, lon) = parseGeo(text)
                val utm = geoToUtm(lat, lon)

                lastLat = lat
                lastLon = lon

                resultLine.text = String.format(
                    Locale.US,
                    "%d%c %.3f %.3f",
                    utm.zone,
                    utm.band,
                    utm.easting,
                    utm.northing
                )

            } else if (isUtm(text)) {
                // UTM → GEO
                val utm = parseUtm(text)
                val (lat, lon) = utmToGeo(utm)

                lastLat = lat
                lastLon = lon

                resultLine.text = String.format(
                    Locale.US,
                    "%.8f, %.8f",
                    lat,
                    lon
                )

            } else {
                toast("Formato inválido")
            }

        } catch (e: Exception) {
            toast("Erro ao converter: ${e.message}")
        }
    }

    // =====================================================
    // FORMAT DETECTION
    // =====================================================
    private fun isGeographic(text: String): Boolean {
        val regex = Regex("^\\s*-?\\d+\\.\\d+,\\s*-?\\d+\\.\\d+\\s*$")
        return regex.matches(text)
    }

    private fun isUtm(text: String): Boolean {
        val regex = Regex("^\\d{1,2}[C-X]\\s+\\d+(\\.\\d+)?\\s+\\d+(\\.\\d+)?$")
        return regex.matches(text)
    }

    // =====================================================
    // PARSERS
    // =====================================================
    private fun parseGeo(text: String): Pair<Double, Double> {
        val parts = text.split(",")

        val lat = parts[0].trim().toDouble()
        val lon = parts[1].trim().toDouble()

        if (lat !in -90.0..90.0) error("Latitude fora do intervalo")
        if (lon !in -180.0..180.0) error("Longitude fora do intervalo")

        return Pair(lat, lon)
    }

    private fun parseUtm(text: String): UtmCoordinate {
        val parts = text.split(Regex("\\s+"))

        val zoneBand = parts[0]
        val zone = zoneBand.dropLast(1).toInt()
        val band = zoneBand.last()

        val easting = parts[1].toDouble()
        val northing = parts[2].toDouble()

        return UtmCoordinate(zone, band, easting, northing)
    }

    // =====================================================
    // PROJ4J CONVERSIONS
    // =====================================================
    private fun geoToUtm(lat: Double, lon: Double): UtmCoordinate {

        val zone = Math.floor((lon + 180.0) / 6.0).toInt() + 1
        val band = latitudeBand(lat)
        val southFlag = if (lat < 0) " +south" else ""

        val wgs84: CoordinateReferenceSystem =
            crsFactory.createFromName("EPSG:4326")

        val utmCrs: CoordinateReferenceSystem =
            crsFactory.createFromParameters(
                "UTM",
                "+proj=utm +zone=$zone$southFlag +datum=WGS84 +units=m +no_defs"
            )

        val transform =
            ctFactory.createTransform(wgs84, utmCrs)

        val src = ProjCoordinate(lon, lat)
        val dst = ProjCoordinate()

        transform.transform(src, dst)

        return UtmCoordinate(zone, band, dst.x, dst.y)
    }

    private fun utmToGeo(utm: UtmCoordinate): Pair<Double, Double> {

        val southFlag = if (utm.band < 'N') " +south" else ""

        val wgs84: CoordinateReferenceSystem =
            crsFactory.createFromName("EPSG:4326")

        val utmCrs: CoordinateReferenceSystem =
            crsFactory.createFromParameters(
                "UTM",
                "+proj=utm +zone=${utm.zone}$southFlag +datum=WGS84 +units=m +no_defs"
            )

        val transform =
            ctFactory.createTransform(utmCrs, wgs84)

        val src = ProjCoordinate(utm.easting, utm.northing)
        val dst = ProjCoordinate()

        transform.transform(src, dst)

        return Pair(dst.y, dst.x)
    }

    // =====================================================
    // MAPS
    // =====================================================
    private fun openInMaps() {
        if (lastLat == null || lastLon == null) {
            toast("Nenhuma coordenada convertida")
            return
        }

        val latStr = String.format(Locale.US, "%.8f", lastLat)
        val lonStr = String.format(Locale.US, "%.8f", lastLon)

        val uri = Uri.parse("geo:$latStr,$lonStr?q=$latStr,$lonStr")
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    // =====================================================
    // HELPERS
    // =====================================================
    private fun latitudeBand(lat: Double): Char {
        val bands = "CDEFGHJKLMNPQRSTUVWX"
        val index = ((lat + 80.0) / 8.0).toInt()
        return bands[index.coerceIn(0, bands.length - 1)]
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    // =====================================================
    // DATA CLASS
    // =====================================================
    data class UtmCoordinate(
        val zone: Int,
        val band: Char,
        val easting: Double,
        val northing: Double
    )
}
