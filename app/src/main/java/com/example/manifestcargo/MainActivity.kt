package com.example.manifestcargo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.manifestcargo.data.AppDatabase
import com.example.manifestcargo.data.CargoItem
import com.example.manifestcargo.databinding.ActivityMainBinding
import com.example.manifestcargo.util.ExcelExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        // Event Simpan ke Database
        binding.btnSave.setOnClickListener {
            saveData()
        }

        // Event Export ke Excel
        binding.btnExport.setOnClickListener {
            exportData()
        }

        // Event Hapus Semua
        binding.btnClearAll.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                database.cargoDao().deleteAll()
            }
        }

        // Observe Perubahan Data Real-time
        lifecycleScope.launch {
            database.cargoDao().getAllItems().collect { itemList ->
                updateUI(itemList)
            }
        }
    }

    private fun saveData() {
        val awb = binding.etAwbNo.text.toString()
        val flight = binding.etFlightNo.text.toString()
        val pti = binding.etPti.text.toString()
        val pcs = binding.etPcsQty.text.toString().toIntOrNull() ?: 0
        val pcsWt = binding.etPcsQtyWt.text.toString().toDoubleOrNull() ?: 0.0
        val subTotal = binding.etSubTotal.text.toString().toDoubleOrNull() ?: 0.0
        val desc = binding.etDescription.text.toString()
        val cust = binding.etCustomer.text.toString()
        val pag = binding.etNoPag.text.toString()

        if (pti.isEmpty() || awb.isEmpty()) {
            Toast.makeText(this, "AWB dan PTI tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        val cargo = CargoItem(
            awbNo = awb,
            flightNo = flight,
            pti = pti,
            pcsQty = pcs,
            pcsQtyWt = pcsWt,
            subTotalKg = subTotal,
            description = desc,
            customer = cust,
            noPag = pag
        )

        lifecycleScope.launch(Dispatchers.IO) {
            database.cargoDao().insert(cargo)
            withContext(Dispatchers.Main) {
                clearInputFields()
                Toast.makeText(this@MainActivity, "Data tersimpan!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val list = database.cargoDao().getAllItemsList()
            if (list.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Tidak ada data untuk diexport!", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val file = ExcelExporter.exportWithTemplate(this@MainActivity, list)
            withContext(Dispatchers.Main) {
                if (file != null) {
                    Toast.makeText(this@MainActivity, "Excel Berhasil Dibuat:\n${file.name}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@MainActivity, "Gagal membuat Excel!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI(items: List<CargoItem>) {
        binding.tvTableTitle.text = "Tabel Data (${items.size})"
        binding.listContainer.removeAllViews()

        for (item in items) {
            val itemView = layoutInflater.inflate(R.layout.item_cargo_card, binding.listContainer, false)
            
            val tvInfo = itemView.findViewById<android.widget.TextView>(R.id.tvCardInfo)
            val btnDelete = itemView.findViewById<android.widget.Button>(R.id.btnDeleteCard)

            tvInfo.text = "PTI: ${item.pti}\nPcs: ${item.pcsQty} | SubTotal: ${item.subTotalKg} Kg\nDesc: ${item.description} | Cust: ${item.customer}"

            btnDelete.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    database.cargoDao().deleteById(item.id)
                }
            }

            binding.listContainer.addView(itemView)
        }
    }

    private fun clearInputFields() {
        binding.etPti.text?.clear()
        binding.etPcsQty.text?.clear()
        binding.etPcsQtyWt.text?.clear()
        binding.etSubTotal.text?.clear()
        binding.etDescription.text?.clear()
        binding.etCustomer.text?.clear()
        binding.etNoPag.text?.clear()
    }
}
