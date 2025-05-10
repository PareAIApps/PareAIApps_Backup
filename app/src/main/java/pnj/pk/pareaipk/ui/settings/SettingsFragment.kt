package pnj.pk.pareaipk.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.databinding.FragmentSettingsBinding
import pnj.pk.pareaipk.ui.about.AboutActivity
import pnj.pk.pareaipk.ui.account.AccountActivity
import pnj.pk.pareaipk.ui.login.LoginActivity

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class.java]
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // Setting up onClickListeners
        binding.btnLogout.setOnClickListener {
            logOut()
        }

        binding.btnAbout.setOnClickListener {
            val intent = Intent(activity, AboutActivity::class.java)
            startActivity(intent)
        }

        // Add this code to open the AccountActivity
        binding.btnAccount.setOnClickListener {
            val intent = Intent(activity, AccountActivity::class.java)
            startActivity(intent)
        }

        return binding.root
    }

    private fun logOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
