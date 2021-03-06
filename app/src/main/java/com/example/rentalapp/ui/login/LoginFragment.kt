package com.example.rentalapp.ui.login

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.example.rentalapp.R
import com.example.rentalapp.data.Apartment
import com.example.rentalapp.data.Network
import com.example.rentalapp.data.User
import com.example.rentalapp.ui.load.LoadingDialog
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.*
import java.lang.Exception

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)
        val username: EditText = view.findViewById(R.id.usernameTextView)
        val password: EditText = view.findViewById(R.id.passwordTextView)
        val loginBtn: Button = view.findViewById(R.id.loginBtn)
        val loadingDialog: LoadingDialog = LoadingDialog(context as Activity)

        val url = "user/login"

        loginBtn.setOnClickListener {
            loadingDialog.startLoadingDialog()

            CoroutineScope(Dispatchers.IO).launch {

                try {
                    val loginJson =

                        async {
                            try {
                                Network.login(
                                    url,
                                    username.text.toString(),
                                    password.text.toString()
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }

                    withTimeout(5000L) {
                        if (loginJson.await() == null) {
                            Snackbar.make(
                                view,
                                "Fail to login, Please check your internet connection.",
                                Snackbar.LENGTH_LONG
                            ).show()
                        } else if (loginJson.await()?.size != 1) {
                            Log.d("LoginFragment json", loginJson.toString())

                            val user = Gson().fromJson<User>(loginJson.await()?.get(0), object :
                                TypeToken<User>() {}.type)

                            Log.d("LoginFragment size", loginJson.toString())

                            val cookie = loginJson.await()?.get(1)
                            if (cookie != null) {
                                Log.d("LoginFragment cookie", cookie)
                            }
                            val myRentalsJson = loginJson.await()?.get(2)

                            CoroutineScope(Dispatchers.Main).launch {
                                //                        it.findNavController().navigate(R.id.action_loginFragment_to_userFragment,
                                //                        bundleOf(Pair("username", user.username), Pair("img", user.avatar)))

                                val pref: SharedPreferences = context?.getSharedPreferences(
                                    "userInfo",
                                    Context.MODE_PRIVATE
                                )!!

                                pref.edit().apply {
                                    putString("username", user.username)
                                    putString("img", user.avatar)
                                    putString("cookie", cookie)
                                    putString("myRentalsJson", myRentalsJson)
                                }.commit()

                                it.findNavController().navigate(
                                    R.id.action_loginFragment_to_userFragment,
                                    bundleOf(
                                        Pair("username", user.username),
                                        Pair("img", user.avatar)
                                    )
                                )
                                Snackbar.make(
                                    view,
                                    "Welcome ${user.username}.",
                                    Snackbar.LENGTH_LONG
                                )
                                    .show()
                            }
                        }
                        // fail to login
                        else if (loginJson.await()?.size == 1) {
//                        Log.d("LoginFragment", loginJson.toString())
                            val responseCode = loginJson.await()?.get(0)?.toInt()

                            if (responseCode == 401)
                                Snackbar.make(
                                    view,
                                    "Fail to login, User not found or Wrong Password.",
                                    Snackbar.LENGTH_LONG
                                ).show()
                            else if (responseCode == 400)
                                Snackbar.make(
                                    view,
                                    "Fail to login, Bad Request.",
                                    Snackbar.LENGTH_LONG
                                )
                                    .show()
                            else if (responseCode == 500)
                                Snackbar.make(
                                    view,
                                    "Fail to login, Server Error.",
                                    Snackbar.LENGTH_LONG
                                ).show()
                        }
                        loadingDialog.dismissDialog()
                    }
                } catch (e: Exception) {
                    Log.d("timeout", "timeout")
                    loadingDialog.dismissDialog()
                    Snackbar.make(
                        view,
                        "Fail to login, timeout.",
                        Snackbar.LENGTH_LONG
                    ).show()
                    e.printStackTrace()
                }
            }
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}