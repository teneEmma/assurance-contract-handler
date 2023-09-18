package com.kod.assurancecontracthandler.views.main.fragmentHome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupViewPager()
    }

    private fun setupViewPager(){
        binding.viewPager2.adapter = TabAdapter(this)
        binding.viewPager2.isUserInputEnabled = false
        TabLayoutMediator(binding.tabLayout, binding.viewPager2
        ) { tab, position ->
            when(position){
                1 -> tab.text = resources.getString(R.string.list_customers_title)
                else -> tab.text = resources.getString(R.string.list_contract_title)
            }
        }.attach()
    }
}