package com.example.app

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text


class RecordAdapter(
    val records: MutableList<RecordDto>, val recyclerViewInterface: RecyclerViewInterface,
    val clickListener: (RecordDto) -> Unit
) :
    RecyclerView.Adapter<RecordAdapter.MyViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class MyViewHolder(
        view: View,
        recyclerViewInterface: RecyclerViewInterface,
        clickAtPosition: (Int) -> Unit
    ) :
        RecyclerView.ViewHolder(view) {
        val street: TextView
        val name: TextView
        val house: TextView
        val flat: TextView
        val daySuccessText: TextView
        val nightSuccessText: TextView

        init {
            // Define click listener for the ViewHolder's View
            street = view.findViewById(R.id.renter_street)
            name = view.findViewById(R.id.renter_name)
            house = view.findViewById(R.id.renter_house)
            flat = view.findViewById(R.id.renter_flat)
            daySuccessText = view.findViewById(R.id.daySuccess)
            nightSuccessText = view.findViewById(R.id.nightSuccess)

            view.setOnClickListener(View.OnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    recyclerViewInterface.onItemCLick(pos)
                    clickAtPosition(pos)
                }
            })
        }

    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_record, viewGroup, false)
        val vh = MyViewHolder(view, recyclerViewInterface) {
            clickListener(records[it])
        }
        return vh
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.street.text = records[position].street
        viewHolder.name.text = records[position].name
        viewHolder.house.text = records[position].houseNumber
        viewHolder.flat.text = records[position].flatNumber.toString().split(".")[0]


        // Show successfully updated records AND redraws items,
        // which are not updated yet but are reusing viewHolders
        if (records[position].ko_D > records[position].lastKo_D) {
            viewHolder.daySuccessText.text = records[position].ko_D.toString().split(".")[0]
            showSuccesData(viewHolder.daySuccessText)

            viewHolder.nightSuccessText.text = records[position].ko_N.toString().split(".")[0]
            showSuccesData(viewHolder.nightSuccessText)
        } else {
            hideData(viewHolder.daySuccessText)
            hideData(viewHolder.nightSuccessText)
        }
    }

    fun showSuccesData(dataElement: TextView) {
        dataElement.visibility = View.VISIBLE
        dataElement.setTextAppearance(R.style.UpdatedRecord)
    }

    fun hideData(dataElement: TextView) {
        dataElement.visibility = View.GONE
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = records.size


}