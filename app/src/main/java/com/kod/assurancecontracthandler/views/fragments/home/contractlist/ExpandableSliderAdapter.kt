package com.kod.assurancecontracthandler.views.fragments.home.contractlist

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.slider.RangeSlider
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.utilities.DataTypesConversionAndFormattingUtils
import com.kod.assurancecontracthandler.databinding.ExpandableSliderItemBinding

class ExpandableSliderAdapter(
    private val context: Context,
    private val groupTitleList: List<String>,
    private val childrenTitleList: List<List<String>>,
    private val slidersValues: Map<String, Map<String, Pair<Float, Float>?>>,
    private val sliderListenerCallback: (Pair<Int, Int>, Pair<Float, Float>) -> Unit
) : BaseExpandableListAdapter() {

    private val childrenSelected: MutableList<MutableList<Int>> = mutableListOf(mutableListOf(), mutableListOf())

    private fun setSliderValues(
        groupPosition: Int,
        childPosition: Int,
        expandableSBinding: ExpandableSliderItemBinding
    ) {
        val groupKey = groupTitleList[groupPosition]
        val childKey = childrenTitleList[groupPosition][childPosition]
        if (groupPosition == 0) {
            val minValue = slidersValues[groupKey]?.get(childKey)?.first ?: 1000.0F
            val maxValue = slidersValues[groupKey]?.get(childKey)?.second ?: 500000.0F
            expandableSBinding.priceSlider.valueFrom = 1000.0F
            expandableSBinding.priceSlider.valueTo = 500000.0F
            expandableSBinding.priceSlider.stepSize = 1000.0F
            expandableSBinding.priceSlider.values = mutableListOf(minValue, maxValue)
            expandableSBinding.priceSlider.setLabelFormatter { value ->
                "$value${ConstantsVariables.priceUnit}"
            }
            expandableSBinding.etPriceRangeMax.text = ConstantsVariables.maxPriceText
            expandableSBinding.etPriceRangeMin.text = ConstantsVariables.minPriceText
        } else {
            val minValue = slidersValues[groupKey]?.get(childKey)?.first ?: 1.0F
            val maxValue = slidersValues[groupKey]?.get(childKey)?.second ?: 30.0F
            expandableSBinding.priceSlider.valueFrom = 1.0F
            expandableSBinding.priceSlider.valueTo = 30.0F
            expandableSBinding.priceSlider.stepSize = 1.0F
            expandableSBinding.priceSlider.values = mutableListOf(minValue, maxValue)
            expandableSBinding.priceSlider.setLabelFormatter { value ->
                "$value${ConstantsVariables.powerUnit}"
            }
            expandableSBinding.etPriceRangeMax.text = ConstantsVariables.maxPowerText
            expandableSBinding.etPriceRangeMin.text = ConstantsVariables.minPowerText
        }

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

        if (myConvertView == null) {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            myConvertView = layoutInflater.inflate(R.layout.expandable_slider_item, parent, false)
        }
        val expandableSBinding = ExpandableSliderItemBinding.bind(myConvertView!!)

        try {
            setSliderValues(groupPosition, childPosition, expandableSBinding)
        } catch (e: Exception) {
            Log.e("CRASH_SLIDERS", e.stackTraceToString())
        }

        expandableSBinding.llEtPriceRange.visibility =
            if (!childrenSelected[groupPosition].contains(childPosition)) {
                View.GONE
            } else {
                View.VISIBLE
            }
        myConvertView.setOnClickListener {

            expandableSBinding.llEtPriceRange.visibility =
                if (expandableSBinding.llEtPriceRange.isVisible) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

            expandableSBinding.priceSlider.apply {

                addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
                    override fun onStartTrackingTouch(slider: RangeSlider) {

                    }

                    override fun onStopTrackingTouch(slider: RangeSlider) {
                        values = mutableListOf(slider.values[0], slider.values[1])
                        val minValueString = DataTypesConversionAndFormattingUtils.addPowerOrPriceUnitToAttribute(
                            slider.values[0].toString(),
                            groupPosition
                        )
                        val maxValueString = DataTypesConversionAndFormattingUtils.addPowerOrPriceUnitToAttribute(
                            slider.values[1].toString(),
                            groupPosition
                        )
                        expandableSBinding.etPriceRangeMin.text = minValueString
                        expandableSBinding.etPriceRangeMax.text = maxValueString
                        sliderListenerCallback(
                            Pair(groupPosition, childPosition),
                            Pair(slider.values[0], slider.values[1])
                        )
                    }
                })
            }

            childrenSelected[groupPosition].add(childPosition)
        }


        expandableSBinding.etTitlePriceSlider.text = expandedListText

        return myConvertView
    }

    override fun getGroupCount(): Int =
        groupTitleList.size

    override fun getChildrenCount(groupPosition: Int): Int =
        childrenTitleList[groupPosition].size

    override fun getGroup(groupPosition: Int): Any =
        groupTitleList[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any =
        childrenTitleList[groupPosition][childPosition]

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
        if (myConvertView == null) {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            myConvertView = layoutInflater.inflate(R.layout.main_group_sliders, parent, false)
        }

        val groupListTextView = myConvertView?.findViewById<TextView>(R.id.textView3)
        groupListTextView?.text = getGroup(groupPosition) as String
        return myConvertView!!
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
}