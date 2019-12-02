package com.junaid.smsapp.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import com.junaid.smsapp.R
import com.junaid.smsapp.model.ContactAddress


class AutoCompleteAdapter(var mContext: Context, var recipientsFull: ArrayList<ContactAddress>) :
    ArrayAdapter<ContactAddress>(mContext, R.layout.auto_fill_item, recipientsFull) {

    var tempArr = ArrayList<ContactAddress>()
    var suggestions = ArrayList<ContactAddress>()

    init {
        tempArr = ArrayList(recipientsFull)
    }

    override fun getFilter(): Filter {
        return countryFilter
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        Log.d("TAG", "getView: ")
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.auto_fill_item, parent, false)
        }
        val name: TextView = view!!.findViewById(R.id.title)
        val address: TextView = view.findViewById(R.id.subTittle)

        val countryItem = tempArr[position]

        name.text = countryItem.name ?: countryItem.address
        address.text = countryItem.address

        return view

    }

    override fun getCount(): Int {
        return tempArr.size
    }

    private val countryFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults {
            val results = FilterResults()

            if (constraint.isEmpty()) {
                suggestions.clear()
                suggestions.addAll(recipientsFull)
                tempArr.addAll(recipientsFull)
            } else {
                suggestions.clear()
                val filterPattern =
                    constraint.toString().trim().toLowerCase()
                for (item in recipientsFull) {
//                    item.name?.toLowerCase()?.contains(filterPattern) != false ||
                    if (item.name?.toLowerCase()!!.contains(filterPattern)) {
                        suggestions.add(item)
                    }
                }
            }
            results.values = suggestions
            results.count = suggestions.size
            return results
        }

        override fun publishResults(
            constraint: CharSequence?,
            results: FilterResults
        ) {



            if (results.count > 0) {
                clear()
                tempArr.clear()
                tempArr.addAll(ArrayList(results.values as ArrayList<ContactAddress>))
                addAll(tempArr)
                notifyDataSetChanged()
            } else {
                tempArr.addAll(ArrayList(recipientsFull))
                notifyDataSetInvalidated()
            }


        }

        override fun convertResultToString(resultValue: Any): CharSequence {
            return (resultValue as ContactAddress).name!!
        }
    }


}