package pnj.pk.pareaipk.ui.scan

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import pnj.pk.pareaipk.R
import pnj.pk.pareaipk.databinding.FragmentScanBinding
import pnj.pk.pareaipk.utils.getImageUri
import pnj.pk.pareaipk.utils.uriToFile

class ScanFragment : Fragment() {

    private lateinit var binding: FragmentScanBinding
    private val scanViewModel: ScanViewModel by viewModels()
    private var currentImageUri: Uri? = null

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Toast.makeText(requireContext(), getString(R.string.img_cant_found), Toast.LENGTH_SHORT)
                .show()
            showDefaultImage()
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess && currentImageUri != null) {
            showImage()
        } else {
            currentImageUri = null
            showDefaultImage()
            Toast.makeText(requireContext(), getString(R.string.img_cant_found), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(
                requireContext(),
                getString(R.string.camera_permission),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanBinding.inflate(inflater, container, false)

        (requireActivity() as AppCompatActivity).supportActionBar?.hide()


        currentImageUri = savedInstanceState?.getParcelable(STATE_IMAGE_URI)

        if (currentImageUri != null) {
            showImage()
        } else {
            showDefaultImage()
        }

        checkAndRequestPermission()

        binding.btnGaleri.setOnClickListener { startGallery() }
        binding.btnKamera.setOnClickListener { startCamera() }
        binding.btnCekApel.setOnClickListener { analyzeImage() }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_title_instruction))
            .setMessage(getString(R.string.dialog_message_instruction))
            .setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()

        return binding.root
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun startCamera() {
        currentImageUri = getImageUri(requireContext())
        if (currentImageUri == null) {
            Toast.makeText(requireContext(), getString(R.string.uri_photo_fail), Toast.LENGTH_SHORT)
                .show()
            return
        }
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.placeholderImage.setImageURI(it)
        } ?: showDefaultImage()
    }

    private fun showDefaultImage() {
        binding.placeholderImage.setImageResource(R.mipmap.gallery)
    }

    private fun analyzeImage() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, requireContext())
            if (imageFile.exists()) {
                moveToResult(uri)
            } else {
                showDefaultImage()
                currentImageUri = null
                Toast.makeText(requireContext(), getString(R.string.img_empty), Toast.LENGTH_SHORT)
                    .show()
            }
        } ?: run {
            showDefaultImage()
            Toast.makeText(requireContext(), getString(R.string.img_empty), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun moveToResult(uri: Uri) {
        val intent = Intent(requireContext(), HasilScanActivity::class.java).apply {
            putExtra(HasilScanActivity.KEY_IMG_URI, uri.toString())
        }
        startActivity(intent)
    }

    private fun checkAndRequestPermission() {
        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }
    }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            requireContext(),
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(STATE_IMAGE_URI, currentImageUri)
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
        private const val STATE_IMAGE_URI = "state_image_uri"
    }
}
