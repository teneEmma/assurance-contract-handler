package com.kod.assurancecontracthandler.views.fragments.home.contractlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.kod.assurancecontracthandler.R

class GridViewItemAdapter(val context: Context, private val titles: List<String>, private val values: List<String?>) : BaseAdapter() {
    private var inflater: LayoutInflater? = null
    override fun getCount(): Int = values.size

    override fun getItem(p0: Int): Any? = null

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View? {
        var itemView: View? = p1
        if (inflater == null)
            inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if(p1 == null)
            itemView = inflater?.inflate(R.layout.grid_view_item, null)

        itemView?.let {view-> bindViews(view, p0) }
        return itemView
    }

    private fun bindViews(itemView: View, position: Int){
        val title: TextView = itemView.findViewById(R.id.tv_title)
        val value: TextView = itemView.findViewById(R.id.tv_value)

        title.text = titles[position]
        value.text = values[position]
    }
    override fun getItemId(p0: Int): Long = 0
}