package com.kod.assurancecontracthandler.views.fragments.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kod.assurancecontracthandler.views.fragments.home.contractlist.ContractListFragment
import com.kod.assurancecontracthandler.views.fragments.home.listcustomers.ListCustomersFragment

class TabAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment =
        when(position){
        1-> ListCustomersFragment.newInstance()
        else -> ContractListFragment.newInstance()
    }
}