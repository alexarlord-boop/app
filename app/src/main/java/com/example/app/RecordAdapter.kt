package com.example.app

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class RecordAdapter(
    val records: MutableList<RecordDto>,
    val recyclerViewInterface: RecyclerViewInterface
) :
    RecyclerView.Adapter<RecordAdapter.MyViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class MyViewHolder(view: View, recyclerViewInterface: RecyclerViewInterface) :
        RecyclerView.ViewHolder(view) {
        val street: TextView
        val name: TextView
        val house: TextView
        val flat: TextView

        init {
            // Define click listener for the ViewHolder's View
            street = view.findViewById(R.id.renter_street)
            name = view.findViewById(R.id.renter_name)
            house = view.findViewById(R.id.renter_house)
            flat = view.findViewById(R.id.renter_flat)
            view.setOnClickListener(View.OnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    recyclerViewInterface.onItemCLick(pos)
                }
            })
        }

    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_record, viewGroup, false)

        return MyViewHolder(view, recyclerViewInterface)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.street.text = records[position].street
        viewHolder.name.text = records[position].name
        viewHolder.house.text = records[position].houseNumber
        viewHolder.flat.text = records[position].flatNumber.toString().split(".")[0]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = records.size


}