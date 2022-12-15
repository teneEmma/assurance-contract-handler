package com.kod.assurancecontracthandler.views.fragments.home.contractlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.slider.RangeSlider
import com.kod.assurancecontracthandler.R

class ExpandableSliderAdapter(
    private val context: Context,
    private val groupTitleList: List<String>,
    private val childDataList: HashMap<String, List<String>>):
    BaseExpandableListAdapter() {

    private val childrenTouched = mutableListOf<Int>()
    private var group1SliderValues = hashMapOf<Int, Pair<Int, Int>>()
    private var group2SliderValues = hashMapOf<Int, Pair<Int, Int>>()
    private val priceUnit = context.resources.getString(R.string.price_tag)
    private val powerUnit = context.resources.getString(R.string.power_tag)

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
        }else{
            expandedListRangeSlider?.valueFrom = 1.0F
            expandedListRangeSlider?.valueTo = 30.0F
            expandedListRangeSlider?.stepSize = 1.0F
            expandedListRangeSlider?.values = mutableListOf(1.0F, 30.0F)
        }
        expandedListTextView?.text = expandedListText
        return myConvertView!!
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true

    fun makeOthersVisible(groupPosition: Int, childPosition: Int, convertView: View?){
        val view = getChildView(groupPosition, childPosition , false,  convertView, null)
        childrenTouched.add(childPosition)
        val slider = view.findViewById<RangeSlider>(R.id.prix_slider)
        val priceRange = view.findViewById<LinearLayout>(R.id.ll_et_price_range)

        slider.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener{
            override fun onStartTrackingTouch(slider: RangeSlider) {

            }

            override fun onStopTrackingTouch(slider: RangeSlider) {
                val minVal = slider.values[0].toInt()
                val maxVal = slider.values[1].toInt()
                val minStr: String
                val maxStr: String
                if(groupPosition == 0){
                    group1SliderValues.put(childPosition, Pair(minVal, maxVal))
                    minStr = minVal.toString() + priceUnit
                    maxStr = maxVal.toString() + priceUnit
                }else{
                    group2SliderValues.put(childPosition, Pair(minVal, maxVal))
                    minStr = minVal.toString() + powerUnit
                    maxStr = maxVal.toString() + powerUnit
                }
                view.findViewById<TextView>(R.id.et_price_range_min).text = minStr
                view.findViewById<TextView>(R.id.et_price_range_max).text = maxStr
            }
        })
        slider.visibility = View.VISIBLE
        priceRange.visibility = View.VISIBLE
        if (groupPosition == 1){
            slider.values = mutableListOf(1.0f, 30.0f)
            slider.valueFrom = 1.0f
            slider.valueTo = 30.0f
            slider.stepSize = 1.0f
            view.findViewById<TextView>(R.id.et_price_range_min).text = view.resources.getText(R.string.puissance_min)
            view.findViewById<TextView>(R.id.et_price_range_max).text = view.resources.getText(R.string.puissance_max)
        }
    }

    fun getSliderValuesGrp1(): HashMap<Int, Pair<Int, Int>>{
        return this.group1SliderValues
    }

    fun getSliderValuesGrp2(): HashMap<Int, Pair<Int, Int>>{
        return this.group2SliderValues
    }
}