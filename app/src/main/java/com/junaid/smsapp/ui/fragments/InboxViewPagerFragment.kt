package com.junaid.smsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.junaid.smsapp.R
import kotlinx.android.synthetic.main.fragment_inbox_view_pager.view.*

class InboxViewPagerFragment : Fragment() {


    lateinit var mView: View


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_inbox_view_pager, container, false)

        setUi()
        return mView
    }

    private fun setUi() {
        mView.tabs.setupWithViewPager(mView.viewpager)
        setUpViewPagerAdapter()
    }

    private fun setUpViewPagerAdapter() {
        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment( InboxFragment(), "SMS")
        adapter.addFragment(SmartInboxFragment(),"Smart Sms")

        mView.viewpager.adapter = adapter
    }

    class ViewPagerAdapter(manager : FragmentManager) : FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val  mFragmentList =   ArrayList<Fragment>()
        private val  mFragmentTitleList =  ArrayList<String>()



        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }


        override fun getCount(): Int {
            return mFragmentList.size
        }

         fun addFragment( fragment : Fragment,  title : String)
        {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }


        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }
    }

}
