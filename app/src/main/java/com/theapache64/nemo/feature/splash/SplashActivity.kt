package com.theapache64.nemo.feature.splash

import android.content.DialogInterface
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Observer
import com.theapache64.nemo.R
import com.theapache64.nemo.databinding.ActivitySplashBinding
import com.theapache64.nemo.feature.base.BaseActivity
import com.theapache64.nemo.feature.products.ProductsActivity
import com.theapache64.nemo.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity :
    BaseActivity<ActivitySplashBinding, SplashViewModel>(R.layout.activity_splash) {

    override fun onCreate() {
        binding.executePendingBindings()

        // Go to products
        viewModel.shouldGoToProducts.observe(this, Observer { shouldGoToProducts ->
            if (shouldGoToProducts) {
                val intent = ProductsActivity.getStartIntent(this)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    binding.tvAppName,
                    getString(R.string.transition_app_logo_to_products_title)
                )
                startActivity(intent, options.toBundle())
                finish()
            }
        })

        viewModel.shouldShowConfigSyncError.observe(this, Observer { shouldShow ->
            if (shouldShow) {
                showConfigSyncError()
            }
        })

        viewModel.shouldShowProgress.observe(this, Observer { shouldShow ->
            if (shouldShow) {
                binding.pbConfigSync.visible()
            } else {
                binding.pbConfigSync.visibility = View.INVISIBLE
            }
        })
    }

    private fun showConfigSyncError() {
        AlertDialog.Builder(this)
            .setTitle(R.string.splash_sync_error_title)
            .setMessage(R.string.splash_sync_error_message)
            .setPositiveButton(R.string.action_retry) { _: DialogInterface, _: Int ->
                viewModel.onRetryClicked()
            }
            .create()
            .show()
    }

    override val viewModel: SplashViewModel by viewModels()
}