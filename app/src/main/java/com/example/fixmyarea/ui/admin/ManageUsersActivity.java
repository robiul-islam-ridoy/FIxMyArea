package com.example.fixmyarea.ui.admin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fixmyarea.R;
import com.example.fixmyarea.adapters.UserAdapter;
import com.example.fixmyarea.firebase.FirebaseConstants;
import com.example.fixmyarea.firebase.FirebaseManager;
import com.example.fixmyarea.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manage Users Activity - Admin can view, add, edit, and delete users
 */
public class ManageUsersActivity extends AppCompatActivity implements UserAdapter.UserActionListener {

    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private ProgressBar progressBar;
    private TextView emptyState;
    private FloatingActionButton fabAddUser;

    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Users");
        }

        firebaseManager = FirebaseManager.getInstance();

        initViews();
        loadUsers();
    }

    private void initViews() {
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
        fabAddUser = findViewById(R.id.fabAddUser);

        // Setup RecyclerView
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(userAdapter);

        // FAB click listener
        fabAddUser.setOnClickListener(v -> showAddUserDialog());
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        firebaseManager.getAllUsers().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);

            if (task.isSuccessful() && task.getResult() != null) {
                userList.clear();

                for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }

                userAdapter.notifyDataSetChanged();

                if (userList.isEmpty()) {
                    emptyState.setVisibility(View.VISIBLE);
                } else {
                    emptyState.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                emptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showAddUserDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_user, null);

        EditText nameInput = dialogView.findViewById(R.id.nameInput);
        EditText emailInput = dialogView.findViewById(R.id.emailInput);
        EditText phoneInput = dialogView.findViewById(R.id.phoneInput);
        EditText nidInput = dialogView.findViewById(R.id.nidInput);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        RadioGroup roleGroup = dialogView.findViewById(R.id.roleGroup);

        new AlertDialog.Builder(this)
                .setTitle("Add New User")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String email = emailInput.getText().toString().trim();
                    String phone = phoneInput.getText().toString().trim();
                    String nid = nidInput.getText().toString().trim();
                    String password = passwordInput.getText().toString();

                    int selectedRoleId = roleGroup.getCheckedRadioButtonId();
                    RadioButton selectedRole = dialogView.findViewById(selectedRoleId);
                    String role = selectedRole.getTag().toString();

                    if (validateUserInput(name, email, phone, nid, password)) {
                        createUser(name, email, phone, nid, password, role);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createUser(String name, String email, String phone, String nid, String password, String role) {
        progressBar.setVisibility(View.VISIBLE);

        // Create Firebase Auth account first
        firebaseManager.signUpWithEmail(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String userId = task.getResult().getUser().getUid();

                // Create user profile in Firestore with specified role
                firebaseManager.createUserByAdmin(userId, name, email, phone, nid, "", role)
                        .addOnSuccessListener(aVoid -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "User created successfully", Toast.LENGTH_SHORT).show();
                            loadUsers(); // Reload list
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Failed to create profile: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        });
            } else {
                progressBar.setVisibility(View.GONE);
                String error = task.getException() != null ? task.getException().getMessage()
                        : "Failed to create account";
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateUserInput(String name, String email, String phone, String nid, String password) {
        if (TextUtils.isEmpty(name) || name.length() < 3) {
            Toast.makeText(this, "Name must be at least 3 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(phone) || phone.length() < 10) {
            Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(nid)) {
            Toast.makeText(this, "NID is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onEditUser(User user) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_user, null);

        EditText nameInput = dialogView.findViewById(R.id.nameInput);
        EditText emailInput = dialogView.findViewById(R.id.emailInput);
        EditText phoneInput = dialogView.findViewById(R.id.phoneInput);
        EditText nidInput = dialogView.findViewById(R.id.nidInput);
        EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        RadioGroup roleGroup = dialogView.findViewById(R.id.roleGroup);

        // Hide password field for editing
        passwordInput.setVisibility(View.GONE);

        // Pre-fill data
        nameInput.setText(user.getUserName());
        emailInput.setText(user.getEmail());
        emailInput.setEnabled(false); // Can't change email
        phoneInput.setText(user.getPhone());
        nidInput.setText(user.getNid());

        // Select current role
        if (FirebaseConstants.ROLE_ADMIN.equals(user.getRole())) {
            roleGroup.check(R.id.radioAdmin);
        } else {
            roleGroup.check(R.id.radioUser);
        }

        new AlertDialog.Builder(this)
                .setTitle("Edit User")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String phone = phoneInput.getText().toString().trim();
                    String nid = nidInput.getText().toString().trim();

                    int selectedRoleId = roleGroup.getCheckedRadioButtonId();
                    RadioButton selectedRole = dialogView.findViewById(selectedRoleId);
                    String role = selectedRole.getTag().toString();

                    if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(nid)) {
                        updateUser(user.getUserId(), name, phone, nid, role);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateUser(String userId, String name, String phone, String nid, String role) {
        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> updates = new HashMap<>();
        updates.put(FirebaseConstants.FIELD_USER_NAME, name);
        updates.put(FirebaseConstants.FIELD_USER_PHONE, phone);
        updates.put(FirebaseConstants.FIELD_USER_NID, nid);
        updates.put(FirebaseConstants.FIELD_USER_ROLE, role);

        firebaseManager.updateUser(userId, updates).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);

            if (task.isSuccessful()) {
                Toast.makeText(this, "User updated successfully", Toast.LENGTH_SHORT).show();
                loadUsers();
            } else {
                Toast.makeText(this, "Failed to update user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete " + user.getUserName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteUser(user.getUserId()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser(String userId) {
        progressBar.setVisibility(View.VISIBLE);

        firebaseManager.deleteUserData(userId).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);

            if (task.isSuccessful()) {
                Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show();
                loadUsers();
            } else {
                Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
