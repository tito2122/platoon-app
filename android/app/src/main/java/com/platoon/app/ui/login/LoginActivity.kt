package com.platoon.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.platoon.app.MainActivity
import com.platoon.app.databinding.ActivityLoginBinding
import com.platoon.app.repository.FirestoreRepository
import com.platoon.app.ui.register.RegisterActivity
import com.platoon.app.util.HashUtils
import com.platoon.app.util.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager

    // Hardcoded bypass code
    private val BYPASS_CODE = "3194"
    private val BYPASS_PASS = "3194"
    private val BYPASS_NAME = "פלוגה ראשית"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager.getInstance(this)

        if (session.isLoggedIn()) {
            goToMain()
            return
        }

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val code = binding.etPlatoonCode.text?.toString()?.trim() ?: ""
        val pass = binding.etPassword.text?.toString()?.trim() ?: ""

        if (code.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "יש למלא קוד פלוגה וסיסמה", Toast.LENGTH_SHORT).show()
            return
        }

        // Bypass check
        if (code == BYPASS_CODE && pass == BYPASS_PASS) {
            session.platoonId = code
            session.platoonName = BYPASS_NAME
            goToMain()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val repo = FirestoreRepository(code)
                val platoon = repo.getPlatoon(code)
                if (platoon == null) {
                    Toast.makeText(this@LoginActivity, "פלוגה לא נמצאה", Toast.LENGTH_SHORT).show()
                } else {
                    val storedHash = platoon["password"] as? String ?: ""
                    val inputHash = HashUtils.sha256(pass)
                    if (storedHash == inputHash || storedHash == pass) {
                        session.platoonId = code
                        session.platoonName = platoon["name"] as? String ?: code
                        val logo = platoon["logo"] as? String ?: ""
                        if (logo.isNotEmpty()) session.customLogoData = logo
                        goToMain()
                    } else {
                        Toast.makeText(this@LoginActivity, "סיסמה שגויה", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "שגיאה: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
