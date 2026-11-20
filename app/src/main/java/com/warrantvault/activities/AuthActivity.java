package com.warrantvault.activities;

import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.warrantvault.R;
import com.warrantvault.dao.UserDAO;
import com.warrantvault.databinding.ActivityAuthBinding;
import com.warrantvault.databinding.DialogResetPasswordBinding;
import com.warrantvault.models.User;
import com.warrantvault.utils.DateUtils;
import com.warrantvault.utils.SessionManager;

public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private UserDAO userDAO;
    private SessionManager sessionManager;
    private boolean isLoginSelected = true;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleGoogleSignInResult(task);
            });

    private final ActivityResultLauncher<Intent> accountPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String email = result.getData().getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (email != null && !email.trim().isEmpty()) {
                        String name = email.split("@")[0];
                        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                        processGoogleUser(email, name);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userDAO = new UserDAO(this);
        sessionManager = new SessionManager(this);

        setupTabs();
        setupInputWatchers();
        setupKeyboardActions();
        setupActions();
    }

    private void setupTabs() {
        binding.tabLogin.setOnClickListener(v -> switchToLogin());
        binding.tabSignUp.setOnClickListener(v -> switchToSignUp());
        binding.tvSwitchToSignUp.setOnClickListener(v -> switchToSignUp());
        binding.tvSwitchToLogin.setOnClickListener(v -> switchToLogin());
    }

    private void switchToLogin() {
        if (isLoginSelected) return;
        isLoginSelected = true;

        clearAllErrors();

        binding.tabLogin.setBackgroundResource(R.drawable.bg_search_bar);
        binding.tabLogin.setTextColor(ContextCompat.getColor(this, R.color.brand_blue));
        binding.tabLogin.setTypeface(null, Typeface.BOLD);

        binding.tabSignUp.setBackground(null);
        binding.tabSignUp.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        binding.tabSignUp.setTypeface(null, Typeface.NORMAL);

        binding.llLoginForm.setVisibility(View.VISIBLE);
        binding.llSignUpForm.setVisibility(View.GONE);
    }

    private void switchToSignUp() {
        if (!isLoginSelected) return;
        isLoginSelected = false;

        clearAllErrors();

        binding.tabSignUp.setBackgroundResource(R.drawable.bg_search_bar);
        binding.tabSignUp.setTextColor(ContextCompat.getColor(this, R.color.brand_blue));
        binding.tabSignUp.setTypeface(null, Typeface.BOLD);

        binding.tabLogin.setBackground(null);
        binding.tabLogin.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        binding.tabLogin.setTypeface(null, Typeface.NORMAL);

        binding.llSignUpForm.setVisibility(View.VISIBLE);
        binding.llLoginForm.setVisibility(View.GONE);
    }

    private void setupInputWatchers() {
        addClearErrorWatcher(binding.etLoginEmail, binding.tilLoginEmail);
        addClearErrorWatcher(binding.etLoginPassword, binding.tilLoginPassword);
        addClearErrorWatcher(binding.etSignUpName, binding.tilSignUpName);
        addClearErrorWatcher(binding.etSignUpEmail, binding.tilSignUpEmail);
        addClearErrorWatcher(binding.etSignUpPassword, binding.tilSignUpPassword);
        addClearErrorWatcher(binding.etSignUpConfirmPassword, binding.tilSignUpConfirmPassword);
    }

    private void addClearErrorWatcher(TextInputEditText editText, TextInputLayout layout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout.getError() != null) {
                    layout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupKeyboardActions() {
        binding.etLoginPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleLogin();
                return true;
            }
            return false;
        });

        binding.etSignUpConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleSignUp();
                return true;
            }
            return false;
        });
    }

    private void setupActions() {
        binding.btnLogin.setOnClickListener(v -> handleLogin());
        binding.btnSignUp.setOnClickListener(v -> handleSignUp());
        binding.tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        binding.btnGoogleSignIn.setOnClickListener(v -> launchGoogleSignIn());
        binding.btnGoogleSignUp.setOnClickListener(v -> launchGoogleSignIn());
    }

    private void launchGoogleSignIn() {
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .requestProfile()
                    .build();
            GoogleSignInClient client = GoogleSignIn.getClient(this, gso);
            // Sign out to ensure the account chooser is always displayed
            client.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = client.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        } catch (Exception e) {
            fallbackAccountPicker();
        }
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String email = account.getEmail();
                String displayName = account.getDisplayName();
                if (displayName == null || displayName.trim().isEmpty()) {
                    displayName = (account.getGivenName() != null) ? account.getGivenName() : "Google User";
                }
                processGoogleUser(email, displayName);
            }
        } catch (ApiException e) {
            // If user did not cancel and play services failed, try account picker fallback
            if (e.getStatusCode() != 12501 && e.getStatusCode() != 12502) {
                fallbackAccountPicker();
            }
        }
    }

    private void fallbackAccountPicker() {
        try {
            Intent intent = AccountManager.newChooseAccountIntent(
                    null,
                    null,
                    new String[]{"com.google"},
                    false,
                    null,
                    null,
                    null,
                    null
            );
            accountPickerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Could not launch Google Sign In", Toast.LENGTH_SHORT).show();
        }
    }

    private void processGoogleUser(String email, String displayName) {
        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(this, "Could not retrieve Google account email", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = userDAO.getUserByEmail(email);
        long userId;
        if (user != null) {
            userId = user.getId();
            displayName = user.getFullName();
        } else {
            User newUser = new User(displayName, email, "", DateUtils.getTodayDb());
            userId = userDAO.registerUser(newUser, "GOOGLE_AUTH_" + System.currentTimeMillis());
        }

        if (userId != -1) {
            sessionManager.createLoginSession(userId, displayName, email);
            Toast.makeText(this, "Welcome, " + displayName + "!", Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            Toast.makeText(this, "Failed to sign in with Google", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLogin() {
        String email = binding.etLoginEmail.getText() != null ? binding.etLoginEmail.getText().toString().trim() : "";
        String password = binding.etLoginPassword.getText() != null ? binding.etLoginPassword.getText().toString() : "";

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilLoginEmail.setError("Please enter a valid email address");
            binding.etLoginEmail.requestFocus();
            return;
        } else {
            binding.tilLoginEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilLoginPassword.setError("Password cannot be empty");
            binding.etLoginPassword.requestFocus();
            return;
        } else {
            binding.tilLoginPassword.setError(null);
        }

        // Verify user against database
        User user = userDAO.loginUser(email, password);
        if (user != null) {
            sessionManager.createLoginSession(user.getId(), user.getFullName(), user.getEmail());
            Toast.makeText(this, "Welcome back, " + user.getFullName() + "!", Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            // Check if email exists to give specific, user-friendly feedback
            if (!userDAO.isEmailRegistered(email)) {
                binding.tilLoginEmail.setError("No account found with this email. Switch to Sign Up.");
                binding.etLoginEmail.requestFocus();
            } else {
                binding.tilLoginPassword.setError("Incorrect password. Tap Forgot Password to reset.");
                binding.etLoginPassword.requestFocus();
            }
        }
    }

    private void handleSignUp() {
        String name = binding.etSignUpName.getText() != null ? binding.etSignUpName.getText().toString().trim() : "";
        String email = binding.etSignUpEmail.getText() != null ? binding.etSignUpEmail.getText().toString().trim() : "";
        String password = binding.etSignUpPassword.getText() != null ? binding.etSignUpPassword.getText().toString() : "";
        String confirmPassword = binding.etSignUpConfirmPassword.getText() != null ? binding.etSignUpConfirmPassword.getText().toString() : "";

        if (TextUtils.isEmpty(name) || name.length() < 2) {
            binding.tilSignUpName.setError("Full name must be at least 2 characters");
            binding.etSignUpName.requestFocus();
            return;
        } else {
            binding.tilSignUpName.setError(null);
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilSignUpEmail.setError("Please enter a valid email address");
            binding.etSignUpEmail.requestFocus();
            return;
        } else {
            binding.tilSignUpEmail.setError(null);
        }

        if (userDAO.isEmailRegistered(email)) {
            binding.tilSignUpEmail.setError("Email already registered. Please log in.");
            binding.etSignUpEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            binding.tilSignUpPassword.setError("Password must be at least 6 characters");
            binding.etSignUpPassword.requestFocus();
            return;
        } else {
            binding.tilSignUpPassword.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            binding.tilSignUpConfirmPassword.setError("Passwords do not match");
            binding.etSignUpConfirmPassword.requestFocus();
            return;
        } else {
            binding.tilSignUpConfirmPassword.setError(null);
        }

        if (!binding.cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the Terms & Privacy Policy to continue", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User(name, email, "", DateUtils.getTodayDb());
        long newUserId = userDAO.registerUser(newUser, password);

        if (newUserId != -1) {
            sessionManager.createLoginSession(newUserId, name, email);
            Toast.makeText(this, "Account created successfully! Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            Toast.makeText(this, "Registration failed. Please check inputs and try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showForgotPasswordDialog() {
        DialogResetPasswordBinding dialogBinding = DialogResetPasswordBinding.inflate(LayoutInflater.from(this));

        // Pre-fill email if user already entered something in login
        String currentEmail = binding.etLoginEmail.getText() != null ? binding.etLoginEmail.getText().toString().trim() : "";
        if (!currentEmail.isEmpty()) {
            dialogBinding.etResetEmail.setText(currentEmail);
        }

        addClearErrorWatcher(dialogBinding.etResetEmail, dialogBinding.tilResetEmail);
        addClearErrorWatcher(dialogBinding.etResetNewPassword, dialogBinding.tilResetNewPassword);

        new MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Update Password", (dialog, which) -> {
                    // Overridden below to prevent auto-dismiss on error
                })
                .setNegativeButton("Cancel", null)
                .create();

        androidx.appcompat.app.AlertDialog alertDialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Update Password", null)
                .setNegativeButton("Cancel", null)
                .show();

        alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String email = dialogBinding.etResetEmail.getText() != null ? dialogBinding.etResetEmail.getText().toString().trim() : "";
            String newPassword = dialogBinding.etResetNewPassword.getText() != null ? dialogBinding.etResetNewPassword.getText().toString() : "";

            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                dialogBinding.tilResetEmail.setError("Enter a valid email address");
                return;
            }

            if (!userDAO.isEmailRegistered(email)) {
                dialogBinding.tilResetEmail.setError("No account found with this email");
                return;
            }

            if (TextUtils.isEmpty(newPassword) || newPassword.length() < 6) {
                dialogBinding.tilResetNewPassword.setError("Password must be at least 6 characters");
                return;
            }

            boolean success = userDAO.updatePassword(email, newPassword);
            if (success) {
                Toast.makeText(this, "Password updated successfully! Please log in.", Toast.LENGTH_SHORT).show();
                binding.etLoginEmail.setText(email);
                binding.etLoginPassword.setText(newPassword);
                switchToLogin();
                alertDialog.dismiss();
            } else {
                Toast.makeText(this, "Failed to update password. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearAllErrors() {
        binding.tilLoginEmail.setError(null);
        binding.tilLoginPassword.setError(null);
        binding.tilSignUpName.setError(null);
        binding.tilSignUpEmail.setError(null);
        binding.tilSignUpPassword.setError(null);
        binding.tilSignUpConfirmPassword.setError(null);
    }

    private void navigateToMain() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
