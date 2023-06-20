package com.example.travelday_2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup



class CommunityParentFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_community_parent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLayout()
    }

    private fun initLayout() {
        val communityMainFragment = CommunityMainFragment()
        childFragmentManager.beginTransaction().apply {
            add(R.id.frag_container_community, communityMainFragment)
            commit()
        }
    }


}