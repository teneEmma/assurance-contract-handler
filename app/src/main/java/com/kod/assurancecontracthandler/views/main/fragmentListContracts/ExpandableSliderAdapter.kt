package com.kod.assurancecontracthandler.views.main.fragmentListContracts

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.isVisible
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.utilities.DataTypesConversionAndFormattingUtils
import com.kod.assurancecontracthandler.databinding.ExpandableSliderItemBinding
import com.kod.assurancecontracthandler.databinding.StepZizePopUpContentBinding
import kotlin.math.roundToInt


class ExpandableSliderAdapter(
    private val context: Context,
    private val groupTitleList: List<String>,
    private val childrenTitleList: List<List<String>>,
    private val slidersValues: Map<String, Map<String, Pair<Float, Float>?>>,
    private val sliderListenerCallback: (Pair<Int, Int>, Pair<Float, Float>) -> Unit,
) : BaseExpandableListAdapter() {

    private val childrenSelected: MutableList<MutableList<Int>> =
        mutableListOf(mutableListOf(), mutableListOf(), mutableListOf())
    private var _stepSizeForPrices: Float = 1000F
    private var _stepSizeForPower: Float = 1F
    private var _stepSizeForTime: Float = 1F

    private fun setSliderValues(
        groupPosition: Int, childPosition: Int, expandableSBinding: ExpandableSliderItemBinding
    ) {
        val groupKey = groupTitleList[groupPosition]
        val childKey = childrenTitleList[groupPosition][childPosition]
        when (groupPosition) {
            0 -> {
                val minValue =
                    slidersValues[groupKey]?.get(childKey)?.first ?: ConstantsVariables.minPriceValue.toFloat()
                val maxValue =
                    slidersValues[groupKey]?.get(childKey)?.second ?: ConstantsVariables.maxPriceValue.toFloat()
                expandableSBinding.priceSlider.valueFrom = ConstantsVariables.minPriceValue.toFloat()
                expandableSBinding.priceSlider.valueTo = ConstantsVariables.maxPriceValue.toFloat()
                expandableSBinding.priceSlider.stepSize = _stepSizeForPrices
                expandableSBinding.priceSlider.values = mutableListOf(minValue, maxValue)
                expandableSBinding.priceSlider.setLabelFormatter { value ->
                    "$value${ConstantsVariables.priceUnit}"
                }
                setTextViewValues(expandableSBinding, minValue, maxValue, groupPosition)
            }

            1 -> {
                val minValue =
                    slidersValues[groupKey]?.get(childKey)?.first ?: ConstantsVariables.minPowerValue.toFloat()
                val maxValue =
                    slidersValues[groupKey]?.get(childKey)?.second ?: ConstantsVariables.maxPowerValue.toFloat()
                expandableSBinding.priceSlider.valueFrom = ConstantsVariables.minPowerValue.toFloat()
                expandableSBinding.priceSlider.valueTo = ConstantsVariables.maxPowerValue.toFloat()
                expandableSBinding.priceSlider.stepSize = _stepSizeForPower
                expandableSBinding.priceSlider.values = mutableListOf(minValue, maxValue)
                expandableSBinding.priceSlider.setLabelFormatter { value ->
                    "$value${ConstantsVariables.powerUnit}"
                }
                setTextViewValues(expandableSBinding, minValue, maxValue, groupPosition)
            }

            2 -> {
                val minValue =
                    slidersValues[groupKey]?.get(childKey)?.first ?: ConstantsVariables.minTimeValue.toFloat()
                val maxValue =
                    slidersValues[groupKey]?.get(childKey)?.second ?: ConstantsVariables.maxTimeValue.toFloat()
                expandableSBinding.priceSlider.valueFrom = ConstantsVariables.minTimeValue.toFloat()
                expandableSBinding.priceSlider.valueTo = ConstantsVariables.maxTimeValue.toFloat()
                expandableSBinding.priceSlider.stepSize = _stepSizeForTime
                expandableSBinding.priceSlider.values = mutableListOf(minValue, maxValue)
                expandableSBinding.priceSlider.setLabelFormatter { value ->
                    "${value.toInt()}${ConstantsVariables.timeUnit}"
                }
                setTextViewValues(expandableSBinding, minValue, maxValue, groupPosition)
            }
        }

    }

    override fun getChildView(
        groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?
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

        expandableSBinding.llEtPriceRange.visibility = if (!childrenSelected[groupPosition].contains(childPosition)) {
            View.GONE
        } else {
            View.VISIBLE
        }
        myConvertView.setOnClickListener {

            expandableSBinding.llEtPriceRange.visibility = if (expandableSBinding.llEtPriceRange.isVisible) {
                View.GONE
            } else {
                View.VISIBLE
            }

            expandableSBinding.priceSlider.apply {
                setTextViewValues(
                    binding = expandableSBinding,
                    groupPosition = groupPosition,
                    minValue = values[0],
                    maxValue = values[1]
                )
                addOnChangeListener { slider, _, _ ->
                    stepSize = when (groupPosition) {
                        0 -> _stepSizeForPrices
                        1 -> _stepSizeForPower
                        else -> _stepSizeForTime
                    }
                    val minValue = getRoundedValueCorrespondingToStepSize(slider.values[0], stepSize)
                    val maxValue = getRoundedValueCorrespondingToStepSize(slider.values[1], stepSize)
                    setTextViewValues(
                        binding = expandableSBinding,
                        groupPosition = groupPosition,
                        minValue = minValue,
                        maxValue = maxValue
                    )
                    sliderListenerCallback(
                        Pair(groupPosition, childPosition), Pair(minValue, maxValue)
                    )
                }
            }

            childrenSelected[groupPosition].add(childPosition)
        }


        expandableSBinding.etTitlePriceSlider.text = expandedListText

        return myConvertView
    }

    /**
     * Sets the text views for actual minimum and maximum text fields and adds the unit to them.
     * @param binding The view of the filter dialog
     * @param minValue The minimum value by of slider.
     * @param maxValue The maximum value of the slider
     * @param groupPosition The group position of the slider. In order to know the unit to add
     */
    private fun setTextViewValues(
        binding: ExpandableSliderItemBinding, minValue: Float, maxValue: Float, groupPosition: Int
    ) {
        binding.priceSlider.values = mutableListOf(minValue, maxValue)
        val minValueString = DataTypesConversionAndFormattingUtils.addPowerOrPriceUnitToAttribute(
            minValue.toInt(), groupPosition
        )
        val maxValueString = DataTypesConversionAndFormattingUtils.addPowerOrPriceUnitToAttribute(
            maxValue.toInt(), groupPosition
        )
        binding.etPriceRangeMin.text = minValueString
        binding.etPriceRangeMax.text = maxValueString
    }

    private fun getRoundedValueCorrespondingToStepSize(value: Float?, stepSize: Float): Float {
        if (value == null) {
            return 0F
        }
        val roundedValue: Int = (value / stepSize).roundToInt() * stepSize.toInt()
        return roundedValue.toFloat()
    }

    override fun getGroupCount(): Int = groupTitleList.size

    override fun getChildrenCount(groupPosition: Int): Int = childrenTitleList[groupPosition].size

    override fun getGroup(groupPosition: Int): Any = groupTitleList[groupPosition]

    override fun getChild(groupPosition: Int, childPosition: Int): Any = childrenTitleList[groupPosition][childPosition]

    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()

    override fun hasStableIds(): Boolean = false

    override fun getGroupView(
        groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?
    ): View {
        var myConvertView = convertView
        if (myConvertView == null) {
            val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            myConvertView = layoutInflater.inflate(R.layout.main_group_sliders, parent, false)
        }

        val groupListTextView = myConvertView?.findViewById<TextView>(R.id.textView3)
        myConvertView?.findViewById<ImageView>(R.id.iv_btn_add)?.setOnClickListener { v ->
            val stepSize = onStepSizeIncreased(groupPosition)
            displayPopupWindow(v, stepSize)
        }

        myConvertView?.findViewById<ImageView>(R.id.iv_btn_reduce)?.setOnClickListener { v ->
            val stepSize = onStepSizeDecreased(groupPosition)
            displayPopupWindow(v, stepSize)
        }
        groupListTextView?.text = getGroup(groupPosition) as String
        return myConvertView!!
    }

    private fun displayPopupWindow(anchorView: View, stepSize: String, ) {
        val popup = PopupWindow(context)
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popUpBinding = StepZizePopUpContentBinding.inflate(layoutInflater)

        popUpBinding.tvCaption.text = stepSize
        popup.contentView = popUpBinding.root
        popup.height = WindowManager.LayoutParams.WRAP_CONTENT
        popup.width = WindowManager.LayoutParams.WRAP_CONTENT
        popup.isOutsideTouchable = true
        popup.isFocusable = false
        popup.setBackgroundDrawable(BitmapDrawable())
        popup.showAsDropDown(anchorView)

        Handler(Looper.getMainLooper()).postDelayed({
            popup.dismiss()
        }, 800L)
    }

    private fun onStepSizeIncreased(groupPosition: Int): String {
        return when (groupPosition) {
            0 -> {
                val value: Float = getNextStepSizeForDesiredNumber(
                    actualStepSize = _stepSizeForPrices.toInt(),
                    desiredNumber = ConstantsVariables.maxPriceValue,
                    multiple = 1000
                )
                if (value <= ConstantsVariables.maxPriceValue) {
                    _stepSizeForPrices = value
                }
                _stepSizeForPrices.toInt().toString()
            }

            1 -> {
                val value: Float = getNextStepSizeForDesiredNumber(
                    actualStepSize = _stepSizeForPower.toInt(),
                    desiredNumber = ConstantsVariables.maxPowerValue,
                    multiple = 5
                )
                if (value <= ConstantsVariables.maxPowerValue) {
                    _stepSizeForPower = value
                }
                _stepSizeForPower.toInt().toString()
            }

            else -> {
                val value: Float = getNextStepSizeForDesiredNumber(
                    actualStepSize = _stepSizeForTime.toInt(),
                    desiredNumber = ConstantsVariables.maxTimeValue,
                    multiple = 5
                )
                if (value <= ConstantsVariables.maxTimeValue) {
                    _stepSizeForTime = value
                }
                _stepSizeForTime.toInt().toString()
            }
        }
    }

    private fun onStepSizeDecreased(groupPosition: Int): String {
        return when (groupPosition) {
            0 -> {
                val value: Float = getPrecedentStepSizeForDesiredNumber(
                    actualStepSize = _stepSizeForPrices.toInt(),
                    desiredNumber = ConstantsVariables.maxPriceValue,
                    multiple = 5000
                )
                if (value <= ConstantsVariables.maxPriceValue) {
                    _stepSizeForPrices = value
                }
                _stepSizeForPrices.toInt().toString()
            }

            1 -> {
                val value: Float = getPrecedentStepSizeForDesiredNumber(
                    actualStepSize = _stepSizeForPower.toInt(),
                    desiredNumber = ConstantsVariables.maxPowerValue,
                    multiple = 5
                )
                if (value <= ConstantsVariables.maxPowerValue) {
                    // Removing unwanted non multiples of 5
                    _stepSizeForPower = value
                }
                _stepSizeForPower.toInt().toString()
            }

            else -> {
                val value: Float = getPrecedentStepSizeForDesiredNumber(
                    actualStepSize = _stepSizeForTime.toInt(),
                    desiredNumber = ConstantsVariables.maxTimeValue,
                    multiple = 5
                )
                if (value <= ConstantsVariables.maxTimeValue) {
                    // Removing unwanted non multiples of 5
                    _stepSizeForTime = value
                }
                _stepSizeForTime.toInt().toString()
            }
        }
    }

    private fun getNextStepSizeForDesiredNumber(actualStepSize: Int, desiredNumber: Int, multiple: Int): Float {
        // Removing unwanted non multiples of 5
        val stepSize = (actualStepSize - actualStepSize % multiple) + multiple
        if (stepSize >= desiredNumber) {
            return stepSize.toFloat()
        }
        return if (desiredNumber % stepSize != 0) {
            getNextStepSizeForDesiredNumber(stepSize, desiredNumber, multiple)
        } else {
            stepSize.toFloat()
        }
    }

    private fun getPrecedentStepSizeForDesiredNumber(actualStepSize: Int, desiredNumber: Int, multiple: Int): Float {
        // Removing unwanted non multiples of 5
        val stepSize = (actualStepSize - actualStepSize % multiple) - multiple
        if (stepSize <= 1) {
            return 1F
        }
        return if (desiredNumber % stepSize != 0) {
            getPrecedentStepSizeForDesiredNumber(stepSize, desiredNumber, multiple)
        } else {
            stepSize.toFloat()
        }
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
}