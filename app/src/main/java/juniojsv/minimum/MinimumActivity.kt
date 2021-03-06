package juniojsv.minimum

import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import juniojsv.minimum.PreferencesEventHandler.Companion.ACTION_FORCE_RECREATE
import kotlinx.android.synthetic.main.minimum_activity.*

class MinimumActivity : AppCompatActivity(), PreferencesEventHandler.Listener {
    private lateinit var preferences: SharedPreferences
    private val preferencesEventHandler = PreferencesEventHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        appearanceHandler(preferences)
        setContentView(R.layout.minimum_activity)

        mPages.apply {
            adapter = MinimumPages(supportFragmentManager)
            setPageTransformer(true, MinimumPages.DEFAULT_PAGE_TRANSFORMER)
        }

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(preferencesEventHandler, IntentFilter(ACTION_FORCE_RECREATE))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.minimum_shortcuts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mDial ->
                startActivity(Intent(Intent.ACTION_DIAL))
            R.id.mCamera ->
                startActivity(
                        Intent.createChooser(
                                Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),
                                getString(R.string.take_pictures_with)
                        )
                )
            R.id.mPreferences ->
                startActivity(Intent(this, PreferencesActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(preferencesEventHandler)
    }

    override fun onBackPressed() {
        // Nothings
    }

    override fun onPreferenceEvent(intent: Intent) {
        when (intent.action) {
            ACTION_FORCE_RECREATE -> {
                val value = intent.getStringExtra("activity")
                if (value == "all" || value == "minimum")
                    recreate()
            }
        }
    }

    private class MinimumPages(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        val fragments: Array<Fragment> = arrayOf(ApplicationsFragment(), WidgetsFragment())

        override fun getCount(): Int = fragments.size

        override fun getItem(position: Int): Fragment = fragments[position]

        companion object {
            val DEFAULT_PAGE_TRANSFORMER = ViewPager.PageTransformer { page, position ->
                page.apply {
                    val pageWidth = width
                    when {
                        position < -1 -> {
                            alpha = 0f
                        }
                        position <= 0 -> {
                            alpha = 1f
                            translationX = 0f
                        }
                        position <= 1 -> {
                            alpha = 1 - position
                            translationX = pageWidth * -position
                        }
                        else -> {
                            alpha = 0f
                        }
                    }
                }
            }
        }
    }

}
