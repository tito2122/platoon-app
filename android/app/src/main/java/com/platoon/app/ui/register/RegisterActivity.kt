package com.platoon.app.ui.register

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.platoon.app.MainActivity
import com.platoon.app.databinding.ActivityRegisterBinding
import com.platoon.app.repository.FirestoreRepository
import com.platoon.app.util.HashUtils
import com.platoon.app.util.SessionManager
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var session: SessionManager
    private var logoBase64: String = ""

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { loadLogo(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager.getInstance(this)

        binding.btnPickLogo.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnRegister.setOnClickListener { attemptRegister() }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadLogo(uri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            val scaled = android.graphics.Bitmap.createScaledBitmap(bitmap, 256, 256, true)
            val bos = ByteArrayOutputStream()
            scaled.compress(android.graphics.Bitmap.CompressFormat.PNG, 90, bos)
            logoBase64 = Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT)
            binding.ivLogo.setImageBitmap(scaled)
            binding.ivLogo.visibility = View.VISIBLE
        } catch (e: Exception) {
            Toast.makeText(this, "שגיאה בטעינת תמונה", Toast.LENGTH_SHORT).show()
        }
    }

    private fun attemptRegister() {
        val name = binding.etPlatoonName.text?.toString()?.trim() ?: ""
        val code = binding.etPlatoonCode.text?.toString()?.trim() ?: ""
        val pass = binding.etPassword.text?.toString()?.trim() ?: ""
        val confirmPass = binding.etConfirmPassword.text?.toString()?.trim() ?: ""

        if (name.isEmpty() || code.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "יש למלא את כל השדות החובה", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass != confirmPass) {
            Toast.makeText(this, "הסיסמאות אינן תואמות", Toast.LENGTH_SHORT).show()
            return
        }

        if (code.length < 3) {
            Toast.makeText(this, "קוד פלוגה חייב להכיל לפחות 3 תווים", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val repo = FirestoreRepository(code)
                val existing = repo.getPlatoon(code)
                if (existing != null) {
                    Toast.makeText(this@RegisterActivity, "קוד פלוגה כבר תפוס", Toast.LENGTH_SHORT).show()
                    setLoading(false)
                    return@launch
                }

                val passHash = HashUtils.sha256(pass)
                repo.createPlatoon(code, name, passHash, logoBase64)

                session.platoonId = code
                session.platoonName = name
                if (logoBase64.isNotEmpty()) session.customLogoData = logoBase64

                Toast.makeText(this@RegisterActivity, "הפלוגה נוצרה בהצלחה!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "שגיאה: ${e.message}", Toast.LENGTH_LONG).show()
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
    }
}
