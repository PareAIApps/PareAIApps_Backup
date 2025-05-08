package pnj.pk.pareaipk.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.databinding.FragmentSettingsBinding
import pnj.pk.pareaipk.ui.login.LoginActivity

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val logoutButton: LinearLayout = binding.root.findViewById(R.id.btn_logout)
        logoutButton.setOnClickListener {
            logOut()
        }

        val aboutButton: LinearLayout = binding.root.findViewById(R.id.btn_about)
        aboutButton.setOnClickListener {
            // Navigasi ke AboutActivity
            val intent = Intent(activity, pnj.pk.pareaipk.ui.about.AboutActivity::class.java)
            startActivity(intent)
        }

        return root
    }


    private fun logOut() {
        // Log out the user (clear session, Firebase auth sign out, etc)
        // For example, using FirebaseAuth signOut method:
        FirebaseAuth.getInstance().signOut()

        // Navigate back to the LoginActivity
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish() // Finish the current activity
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
