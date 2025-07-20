package com.example.scienceyouthhub;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<UserModel> userList = new ArrayList<>();
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        recyclerView = view.findViewById(R.id.usersRecyclerView);
        FloatingActionButton addFab = view.findViewById(R.id.addUserFab);

        db = FirebaseFirestore.getInstance();
        adapter = new UserAdapter(userList, new UserAdapter.OnUserActionListener() {
            @Override
            public void onEdit(UserModel user) { showUserDialog(user, true); }
            @Override
            public void onDelete(UserModel user) { deleteUser(user); }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        addFab.setOnClickListener(v -> showUserDialog(null, false));

        loadUsers();
        return view;
    }

    private void loadUsers() {
        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        UserModel user = doc.toObject(UserModel.class);
                        userList.add(user);
                    }
                    adapter.setUsers(userList);
                });
    }

    private void showUserDialog(@Nullable UserModel user, boolean isEdit) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_user, null);
        EditText emailInput = dialogView.findViewById(R.id.dialogUserEmail);
        EditText passwordInput = dialogView.findViewById(R.id.dialogUserPassword);
        EditText nameInput = dialogView.findViewById(R.id.dialogUserName);
        EditText ageInput = dialogView.findViewById(R.id.dialogUserAge);
        Spinner roleSpinner = dialogView.findViewById(R.id.dialogUserRole);

        ArrayAdapter<CharSequence> rolesAdapter = ArrayAdapter.createFromResource(
                getContext(), R.array.user_roles, android.R.layout.simple_spinner_item);
        rolesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(rolesAdapter);

        if (user != null) {
            nameInput.setText(user.getName());
            ageInput.setText(String.valueOf(user.getAge()));
            // Роль в спиннере
            for (int i = 0; i < rolesAdapter.getCount(); i++) {
                if (rolesAdapter.getItem(i).toString().equals(user.getType())) {
                    roleSpinner.setSelection(i);
                    break;
                }
            }
            // Email нельзя редактировать
            emailInput.setText(user.getId()); // если id == email (или подставь поле email)
            emailInput.setEnabled(false);
            passwordInput.setVisibility(View.GONE); // не меняем пароль при редактировании
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(isEdit ? "Изменить пользователя" : "Добавить пользователя")
                .setView(dialogView)
                .setPositiveButton(isEdit ? "Сохранить" : "Добавить", (d, which) -> {
                    String email = emailInput.getText().toString().trim();
                    String password = passwordInput.getText().toString().trim();
                    String name = nameInput.getText().toString().trim();
                    String ageStr = ageInput.getText().toString().trim();
                    String role = roleSpinner.getSelectedItem().toString();

                    if (name.isEmpty() || ageStr.isEmpty() || role.isEmpty() || (!isEdit && (email.isEmpty() || password.isEmpty()))) {
                        Toast.makeText(getContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int age = Integer.parseInt(ageStr);

                    if (!isEdit) {
                        // Новый пользователь — регистрируем через FirebaseAuth
                        FirebaseAuth.getInstance()
                                .createUserWithEmailAndPassword(email, password)
                                .addOnSuccessListener(authResult -> {
                                    String userId = authResult.getUser().getUid();
                                    UserModel newUser = new UserModel(userId, name, age, role);
                                    db.collection("users").document(userId)
                                            .set(newUser)
                                            .addOnSuccessListener(aVoid -> {
                                                loadUsers();
                                                Toast.makeText(getContext(), "Пользователь добавлен", Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(getContext(), "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    } else {
                        // Редактируем только Firestore-данные
                        String userId = user.getId();
                        UserModel updatedUser = new UserModel(userId, name, age, role);
                        db.collection("users").document(userId)
                                .set(updatedUser)
                                .addOnSuccessListener(aVoid -> {
                                    loadUsers();
                                    Toast.makeText(getContext(), "Изменения сохранены", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("Отмена", null)
                .create();
        dialog.show();
    }

    private void deleteUser(UserModel user) {
        db.collection("users").document(user.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    loadUsers();
                    Toast.makeText(getContext(), "Пользователь удалён", Toast.LENGTH_SHORT).show();
                });
    }
}
