package com.labactivity.crammode

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

// Model class for Summary
data class Summary(
    val topic: String = "",
    val keyTerms: String = "",
    val importantDates: String = "",
    val summaryText: String = ""
)

class SummaryAdapter(options: FirestoreRecyclerOptions<Summary>) :
    FirestoreRecyclerAdapter<Summary, SummaryAdapter.SummaryViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_summary, parent, false)  // item_summary.xml is the item layout
        return SummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int, model: Summary) {
        // Bind the data to the views
        holder.bind(model)
    }

    class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val topicText: TextView = itemView.findViewById(R.id.topicTextView)  // Updated ID to match item_summary.xml
        private val keyTermsText: TextView = itemView.findViewById(R.id.keyTermsTextView)  // Updated ID to match item_summary.xml
        private val importantDatesText: TextView = itemView.findViewById(R.id.importantDatesTextView)  // Updated ID to match item_summary.xml
        private val summaryText: TextView = itemView.findViewById(R.id.summaryTextView)  // Updated ID to match item_summary.xml

        fun bind(summary: Summary) {
            topicText.text = summary.topic
            keyTermsText.text = summary.keyTerms
            importantDatesText.text = summary.importantDates
            summaryText.text = summary.summaryText
        }
    }
}
class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val topicText: TextView = itemView.findViewById(R.id.topicTextView)
    private val keyTermsText: TextView = itemView.findViewById(R.id.keyTermsTextView)
    private val importantDatesText: TextView = itemView.findViewById(R.id.importantDatesTextView)
    private val summaryText: TextView = itemView.findViewById(R.id.summaryTextView)

    private val editButton: Button = itemView.findViewById(R.id.editButton)
    private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

    fun bind(summary: Summary) {
        topicText.text = summary.topic
        keyTermsText.text = summary.keyTerms
        importantDatesText.text = summary.importantDates
        summaryText.text = summary.summaryText

        editButton.setOnClickListener {
            // Handle edit
        }

        deleteButton.setOnClickListener {
            // Handle delete
        }
    }
}
class SummaryActivityy : AppCompatActivity() {

   override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_summary)

    }
}
