package com.example.lukas.where2go

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.provider.ContactsContract;
import android.provider.UserDictionary
import java.util.*
import android.Manifest.permission
import android.Manifest.permission.READ_CONTACTS
import android.support.v4.app.ActivityCompat
import android.content.DialogInterface
import android.os.Build
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.support.v4.content.ContextCompat



private const val PERMISSION_REQUEST_CONTACT = 3

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var geo: Geocoder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        geo = Geocoder(this@MapsActivity)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    fun ClosedRange<Int>.random() =
            Random().nextInt(endInclusive - start) +  start
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        askForContactPermission()
    }

    fun showContacts() {
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        val mProjection = arrayOf<String>(
                ContactsContract.Profile._ID,
                ContactsContract.Profile.DISPLAY_NAME_PRIMARY)

        val mCursor = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,   // The content URI of the words table
                mProjection,           // The columns to return for each row
                null,                  // Selection criteria
                null,                  // Selection criteria
                null)                  // The sort order for the returned rows

        if (mCursor != null) {
            while (mCursor.moveToNext()) {
                val address = getAddress(mCursor.getString(0))
                val name = mCursor.getString(1);

                if (address != null) {
                    mMap.addMarker(MarkerOptions()
                            .position(LatLng(address.latitude, address.longitude))
                            .title("Aantal: $name"))
                }
            }
        }
    }


    fun askForContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this@MapsActivity,
                            arrayOf(Manifest.permission.READ_CONTACTS),
                            PERMISSION_REQUEST_CONTACT)
            } else {
                showContacts()
            }
        } else {
            showContacts()
        }
    }

    fun getAddress(contactId: String): Address? {
        val geo = geo ?: return null
        val postal_uri = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI;
        val postal_cursor = getContentResolver().query(
                postal_uri,
                null,
                ContactsContract.Data.CONTACT_ID + "=" + contactId.toString(),
                null,
                null);
        var location: Address? = null
        while(postal_cursor.moveToNext())
        {
            val address = postal_cursor.getString(postal_cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
            if (!address.isNullOrEmpty()) {
                val locations = geo.getFromLocationName(address, 1)
                if (locations.size > 0) {
                    location = locations[0];
                }
            }
        }
        postal_cursor.close();
        return location;
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CONTACT -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showContacts()
                }
                return
            }
        }
    }
}

