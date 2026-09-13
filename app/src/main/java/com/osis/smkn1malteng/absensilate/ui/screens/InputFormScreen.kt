package com.osis.smkn1malteng.absensilate.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.osis.smkn1malteng.absensilate.data.model.StudentClass
import com.osis.smkn1malteng.absensilate.data.model.StudentMajor
import com.osis.smkn1malteng.absensilate.ui.screens.components.ClassDropdown
import com.osis.smkn1malteng.absensilate.ui.screens.components.MajorDropdown
import com.osis.smkn1malteng.absensilate.util.PhoneNumberUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun InputFormScreen(
    initialName: String = "",
    initialPhone: String = "",
    initialParentPhone: String = "",
    initialKelas: StudentClass = StudentClass.X,
    initialJurusan: StudentMajor = StudentMajor.TJKT_1,
    initialTimestamp: Long = System.currentTimeMillis(),
    isEditMode: Boolean = false,
    onSave: (name: String, phone: String, parentPhone: String, kelas: StudentClass, jurusan: StudentMajor, timestamp: Long) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm", Locale("id", "ID"))

    var name by remember { mutableStateOf(initialName) }
    var phone by remember { mutableStateOf(initialPhone) }
    var parentPhone by remember { mutableStateOf(initialParentPhone) }
    var kelas by remember { mutableStateOf(initialKelas) }
    var jurusan by remember { mutableStateOf(initialJurusan) }
    var timestamp by remember { mutableStateOf(initialTimestamp) }

    var phoneError by remember { mutableStateOf<String?>(null) }
    var parentPhoneError by remember { mutableStateOf<String?>(null) }

    fun onPhoneChange(value: String) {
        // Hanya angka dan + yang diizinkan saat mengetik
        if (value.isEmpty() || value.all { it.isDigit() || it == '+' }) {
            phone = value
            phoneError = PhoneNumberUtil.validatePhone(value)
        }
    }

    fun onParentPhoneChange(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() || it == '+' }) {
            parentPhone = value
            parentPhoneError = PhoneNumberUtil.validatePhone(value)
        }
    }

    val isFormValid = name.isNotBlank() &&
            (phone.isBlank() || phoneError == null) &&
            (parentPhone.isBlank() || parentPhoneError == null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Siswa" else "Tambah Siswa") },
                navigationIcon = {
                    Button(onClick = onCancel) { Text("Batal") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Nama - WAJIB
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama *") },
                modifier = Modifier.fillMaxWidth(),
                isError = name.isBlank()
            )
            if (name.isBlank()) {
                Text(
                    text = "Nama wajib diisi",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // No. HP - OPSIONAL
            OutlinedTextField(
                value = phone,
                onValueChange = { onPhoneChange(it) },
                label = { Text("No. HP (opsional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = phoneError != null,
                supportingText = {
                    if (phoneError != null) {
                        Text(
                            text = phoneError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (phone.isNotBlank()) {
                        val normalized = PhoneNumberUtil.normalizeToInternational(phone)
                        if (normalized != null) {
                            Text(
                                text = "✓ Akan dikirim ke: $normalized",
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Format tidak dikenali",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Text(
                            text = "Kosongkan jika tidak ada",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            // No. HP Orang Tua - OPSIONAL
            OutlinedTextField(
                value = parentPhone,
                onValueChange = { onParentPhoneChange(it) },
                label = { Text("No. HP Orang Tua (opsional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = parentPhoneError != null,
                supportingText = {
                    if (parentPhoneError != null) {
                        Text(
                            text = parentPhoneError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (parentPhone.isNotBlank()) {
                        val normalized = PhoneNumberUtil.normalizeToInternational(parentPhone)
                        if (normalized != null) {
                            Text(
                                text = "✓ Akan dikirim ke: $normalized",
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Format tidak dikenali",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Text(
                            text = "Kosongkan jika tidak ada",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))

            ClassDropdown(
                selected = kelas,
                onSelected = { kelas = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            MajorDropdown(
                selected = jurusan,
                onSelected = { jurusan = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // TIMESTAMP PICKER
            OutlinedTextField(
                value = dateFormat.format(Date(timestamp)),
                onValueChange = {},
                label = { Text("Waktu Kejadian (Klik untuk ubah)") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showDateTimePicker(context, timestamp) { newTimestamp ->
                            timestamp = newTimestamp
                        }
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isFormValid && name.isNotBlank()) {
                        onSave(
                            name.trim(),
                            phone.trim(),
                            parentPhone.trim(),
                            kelas,
                            jurusan,
                            timestamp
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid
            ) {
                Text(if (isEditMode) "Update" else "Simpan")
            }
        }
    }
}

private fun showDateTimePicker(
    context: Context,
    currentTimestamp: Long,
    onTimestampSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = currentTimestamp

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val newCalendar = Calendar.getInstance()
                    newCalendar.set(year, month, dayOfMonth, hourOfDay, minute, 0)
                    onTimestampSelected(newCalendar.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}
