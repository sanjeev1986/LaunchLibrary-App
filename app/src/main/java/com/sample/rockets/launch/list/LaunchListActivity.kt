package com.sample.rockets.launch.list

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.calculateDiff
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sample.rockets.LaunchApp
import com.sample.rockets.R
import com.sample.rockets.api.Launch
import com.sample.rockets.common.BaseActivity
import com.sample.rockets.common.ViewModelResult
import com.sample.rockets.launch.details.LaunchDetailsActivity
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_launch_list.*
import java.util.*

/**
 * Displays top twn upcoming launches
 */
class LaunchListActivity : BaseActivity(), SearchView.OnQueryTextListener {

    companion object {
        const val REQUEST_LAUNCH_STATUS = 1001
    }

    override fun onQueryTextSubmit(query: String?): Boolean = true


    override fun onQueryTextChange(newText: String?): Boolean {
        newText?.trim()
            ?.let {
                if (it.isEmpty()) {
                    discoverParentLayout.isEnabled = true
                    viewmodel.fetchLaunchList()
                } else {
                    discoverParentLayout.isEnabled = false//disable refresh if search is active
                    viewmodel.search(it)
                }
            }
        return true
    }

    private val viewmodel by lazy {
        ViewModelProviders.of(
            this
            , LaunchApp.getInstance(this).appViewModerFactory.newLaunchListViewModelFactory()
        ).get(LaunchListViewModel::class.java)
    }

    fun onClick(launch: Launch) {
        val intent = Intent(this, LaunchDetailsActivity::class.java)
        intent.putExtra(LaunchDetailsActivity.EXTRA_LAUNCH_NAME, launch.missions.firstOrNull()?.name ?: launch.name)
        intent.putExtra(
            LaunchDetailsActivity.EXTRA_LAUNCH_AGENCY,
            launch.missions.firstOrNull()?.agencies?.firstOrNull()?.name ?: getString(R.string.unknown)
        )
        intent.putExtra(
            LaunchDetailsActivity.EXTRA_LAUNCH_DESC,
            launch.missions.firstOrNull()?.description ?: launch.name
        )
        val cal = Calendar.getInstance().apply { timeInMillis = launch.wsstamp * 1000L }
        intent.putExtra(LaunchDetailsActivity.EXTRA_LAUNCH_TIME, cal.timeInMillis)
        intent.putExtra(LaunchDetailsActivity.EXTRA_LAUNCH_DATE, launch.windowstart)
        launch.location?.pads?.takeIf { it.isNotEmpty() }?.first()?.apply {
            intent.putExtra(LaunchDetailsActivity.EXTRA_LAUNCH_LAT_LNG, LatLng(latitude, longitude))
        }
        startActivityForResult(intent, REQUEST_LAUNCH_STATUS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch_list)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.launch_list_title)
        discoverParentLayout.isRefreshing = true

        val movieAdapter = LaunchListAdapter()
        launchListView.apply {
            adapter = movieAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
        val dividerItemDecoration = DividerItemDecoration(
            this, RecyclerView.VERTICAL
        )
        launchListView.addItemDecoration(dividerItemDecoration)
        discoverParentLayout.apply {
            setProgressBackgroundColorSchemeColor(resources.getColor(R.color.white))
            setColorSchemeColors(resources.getColor(R.color.colorAccent))
            setOnRefreshListener {
                viewmodel.fetchLaunchList(refresh = true)
            }
        }

        viewmodel.uiLiveData.observe(this, Observer {

            when (it) {
                is ViewModelResult.Progress -> {
                    discoverParentLayout.isRefreshing = true
                }
                is ViewModelResult.Success -> {
                    movieAdapter.submit(it.result)
                    discoverParentLayout.isRefreshing = false
                }
                is ViewModelResult.Failure -> {
                    discoverParentLayout.isRefreshing = false
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        getString(R.string.no_network),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

        })
        viewmodel.fetchLaunchList()
    }

    inner class LaunchListAdapter :
        RecyclerView.Adapter<LaunchViewHolder>() {

        private val launchItems = mutableListOf<Launch>()

        fun submit(items: List<Launch>) {
            val diffResult = calculateDiff(object : DiffUtil.Callback() {
                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return launchItems[oldItemPosition] == items[newItemPosition]
                }

                override fun getOldListSize(): Int = launchItems.size

                override fun getNewListSize(): Int = items.size

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return launchItems[oldItemPosition].name == items[newItemPosition].name
                            && launchItems[oldItemPosition].id == items[newItemPosition].id
                }
            })

            diffResult.dispatchUpdatesTo(this)
            launchItems.clear()
            launchItems.addAll(items)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaunchViewHolder =
            LaunchViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_launch, parent, false)
            )

        override fun onBindViewHolder(holder: LaunchViewHolder, position: Int) {
            holder.bind(launchItems[position])
        }

        override fun getItemCount(): Int = launchItems.size

    }

    inner class LaunchViewHolder(private var view: View) :
        RecyclerView.ViewHolder(view) {
        private val launchTitle = view.findViewById<TextView>(R.id.launchTitle)
        private val launchDescription = view.findViewById<TextView>(R.id.launchDescription)
        fun bind(launch: Launch) {
            launchTitle.text = launch.rocket?.name
            launchDescription.text = launch.missions.firstOrNull()?.name
            view.setOnClickListener {
                this@LaunchListActivity.onClick(launch)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.browser_menu, menu)
        (menu.findItem(R.id.action_search).actionView as SearchView).setOnQueryTextListener(this)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LAUNCH_STATUS && resultCode == Activity.RESULT_OK) {
            viewmodel.fetchLaunchList(true)
        }
    }

}