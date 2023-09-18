package com.kod.assurancecontracthandler.views.main.fragmentFileSelection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.kod.assurancecontracthandler.databinding.FragmentSelectFileBinding

//TODO: Add request rationale for android 10 and below
class SelectFileFragment : Fragment() {

    private var _binding: FragmentSelectFileBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectFileBinding.inflate(inflater, container, false)
        return binding.root
    }

}