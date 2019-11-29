package com.junaid.smsapp.ui

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.junaid.smsapp.R
import com.junaid.smsapp.ui.fragments.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.drawer_view.*


class MainActivity : AppCompatActivity()  {


  private lateinit var mDrawerToggle: ActionBarDrawerToggle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        settingToolbar()
        settingDrawerClickListeners()
        loadFragment(InboxViewPagerFragment())
    }

    private fun loadFragment(fragment : Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.flMain,fragment).commit()
    }


    private fun settingDrawerClickListeners() {
        inbox.setOnClickListener {
            loadFragment(InboxViewPagerFragment())
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        archived.setOnClickListener {
            loadFragment(SpamFragment())
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        blocking.setOnClickListener {
            loadFragment(BlockingFragment())
            drawerLayout.closeDrawer(GravityCompat.START)
        }

    }

    private fun settingToolbar() {
        setSupportActionBar(toolbar)
        mDrawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            0,
            0
        ).apply { syncState() }

    }


}
