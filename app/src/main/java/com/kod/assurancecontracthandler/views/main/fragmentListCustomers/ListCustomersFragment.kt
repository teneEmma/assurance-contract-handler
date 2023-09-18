package com.kod.assurancecontracthandler.views.main.fragmentListCustomers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kod.assurancecontracthandler.databinding.FragmentListCustomersBinding

class ListCustomersFragment : Fragment() {

    private lateinit var binding: FragmentListCustomersBinding

    companion object {
        fun newInstance() = ListCustomersFragment()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListCustomersBinding.inflate(inflater, container, false)
        return binding.root
    }
}