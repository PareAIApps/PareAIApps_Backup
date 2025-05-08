package pnj.pk.pareaipk.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val imageUri: String,
    val result: String,
    val confidenceScore: Int,
    val scanDate: String,
    val explanation: String,
    val description: String,
    val suggestion: String,
    val tools_receipt: String,
    val tutorial: String,
    val medicine: String
)