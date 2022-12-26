package com.kod.assurancecontracthandler.views.fragments.home.contractlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.slider.RangeSlider
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import java.text.NumberFormat
import java.util.*
import kotlin.collections.HashMap

class ExpandableSliderAdapter(
    private val context: Context,
    private val groupTitleList: List<String>,
    private val childDataList: HashMap<String, List<String>> ): BaseExpandableListAdapter() {

    override fun getGroupCount(): Int =
        groupTitleList.size

    override fun getChildrenCount(groupPosition: Int): Int =
        childDataList[groupTitleList[groupPosition]]!!.size

    override fun getGroup(groupPosition: Int): Any =
        groupTitleList[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any =
        childDataList[groupTitleList[groupPosition]]!![childPosition]

    override fun getGroupId(groupPosition: Int): Long =
        groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long =
        childPosition.toLong()

    override fun hasStableIds(): Boolean = false

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var myConvertView = convertView
        if (myConvertView == null){
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            myConvertView = layoutInflater.inflate(R.layout.main_group_sliders, parent, false)
        }

        val groupListTextView  = myConvertView?.findViewById<TextView>(R.id.textView3)
        groupListTextView?.text = getGroup(groupPosition) as String
        return myConvertView!!
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var myConvertView = convertView
        val expandedListText = getChild(groupPosition, childPosition) as String
        if (myConvertView == null){
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            myConvertView = layoutInflater.inflate(R.layout.expandable_slider_item, parent, false)
        }

        val expandedListTextView = myConvertView?.findViewById<TextView>(R.id.et_title_prix_slider)
        val expandedListRangeSlider = myConvertView?.findViewById<RangeSlider>(R.id.prix_slider)
        if (groupPosition == 0) {
            expandedListRangeSlider?.valueFrom = 1000.0F
            expandedListRangeSlider?.valueTo = 500000.0F
            expandedListRangeSlider?.stepSize = 1000.0F
            expandedListRangeSlider?.values = mutableListOf(1000.0F, 500000.0F)
            expandedListRangeSlider!!.setLabelFormatter {value->
                "$value${ConstantsVariables.priceUnit}"
            }
        }else{
            expandedListRangeSlider?.valueFrom = 1.0F
            expandedListRangeSlider?.valueTo = 30.0F
            expandedListRangeSlider?.stepSize = 1.0F
            expandedListRangeSlider?.values = mutableListOf(1.0F, 30.0F)
            expandedListRangeSlider!!.setLabelFormatter {value->
                "$value${ConstantsVariables.powerUnit}"
            }
        }
        expandedListTextView?.text = expandedListText
        return myConvertView!!
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
}