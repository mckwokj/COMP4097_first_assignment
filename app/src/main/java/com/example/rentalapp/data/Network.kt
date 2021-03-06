package com.example.rentalapp.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.net.HttpCookie
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

class Network {
    companion object{
        val URL = "https://morning-plains-00409.herokuapp.com/"
        suspend fun getTextFromNetwork(url: String): String{
            try {
                val builder = StringBuilder()
                val connection = URL(URL + url).openConnection() as HttpURLConnection
                var data: Int = connection.inputStream.read()

                while (data != -1) {
                    builder.append(data.toChar())
                    data = connection.inputStream.read()
                }

                return builder.toString()
            } catch (e: IOException) {
                e.printStackTrace()
                return ""
            }
        }

        suspend fun login(url: String, username: String, password: String): List<String>?{
            var connection: HttpURLConnection? = null
            try {
                val builder = StringBuilder()
                connection = URL(URL+url).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                val data = "username=${username}&password=${password}"

                connection.outputStream.apply{
                    write(data.toByteArray())
                    flush()
                }
                Log.d("Network", "Finish posting: ${connection.responseCode}")

                var userData: Int = connection.inputStream.read()

                if(connection.responseCode == 200) {

                    while (userData != -1) {
                        builder.append(userData.toChar())
                            userData = connection.inputStream.read()
                    }
                    Log.d("Network", "Finish reading: ${connection.responseCode}")

                    val cookie = HttpCookie.parse(connection.getHeaderField("Set-Cookie"))
                        .get(0).toString()

                    val myRentalJson = getMyRentals(cookie)

                    return listOf(builder.toString(), cookie, myRentalJson!!)
                }
            } catch (e: UnknownHostException) {
                Log.d("You have an exception", e.toString())
                return null
            } catch (e: Exception) {
                Log.d("You have an exception", e.toString())
            }
            return listOf(connection?.responseCode.toString())
        }

        suspend fun logout(url: String): Int?{
            try {
                val connection = URL(URL + url).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"

                Log.d("Logout", connection.responseCode.toString())

                return connection.responseCode

            } catch (e: IOException) {
                Log.d("Logout err", "Connection problem")
                return null
            }
        }

        suspend fun moveIn(id: Int, cookie: String): List<String>?{
            try {
                val responseCode: Int

                    val connection =
                        URL(URL + "user/rent/$id").openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"

                    connection.setRequestProperty("Cookie", cookie)

                    Log.d("MoveIn", connection.responseCode.toString())
                    responseCode = connection.responseCode

                val myRentalsJson = getMyRentals(cookie)

                return listOf(myRentalsJson!!, connection.responseCode.toString())

            } catch (e: IOException) {
                Log.d("MoveIn err", "Connection problem")
                return null
            }
        }

        suspend fun moveOut(id: Int, cookie: String): List<String>?{
            try {
                val connection: HttpURLConnection

                    connection = URL(URL + "user/rent/$id").openConnection() as HttpURLConnection

                connection.requestMethod = "DELETE"
                connection.setRequestProperty("Cookie", cookie)

                Log.d("MoveIn", connection.responseCode.toString())

                val myRentalsJson = getMyRentals(cookie)

                return listOf(myRentalsJson!!, connection.responseCode.toString())

            } catch (e: IOException) {
                Log.d("Moveout err", e.toString())
                return null
            }

        }

        suspend fun getMyRentals(cookie: String): String?{
            try{
                Log.d("MyRentals cookie", cookie)
                val builder = StringBuilder()
                val connection = URL(URL+"user/myRentals").openConnection() as HttpURLConnection
                connection.setRequestProperty("Cookie", cookie)

                var data: Int = connection.inputStream.read()

                while (data != -1) {
                    builder.append(data.toChar())
                    data = connection.inputStream.read()
                }

                Log.d("getMyRentals", builder.toString())

                return builder.toString()

            } catch (e: IOException){
                Log.d("MyRentals err", e.toString())
                return null
            }
        }

//        suspend fun isOnline(context: Context): Boolean {
//            val connectivityManager =
//                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            if (connectivityManager != null) {
//                val capabilities =y
//                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
//                if (capabilities != null) {
//                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
//                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
//                        return true
//                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
//                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
//                        return true
//                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
//                        Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
//                        return true
//                    }
//                }
//            }
//            Log.i("Internet", "Network failure")
//            return false
//        }

    }
}