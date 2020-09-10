package com.theapache64.nemo.feature.home

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.theapache64.nemo.R
import com.theapache64.nemo.databinding.ActivityHomeBinding
import com.theapache64.nemo.feature.base.BaseActivity
import com.theapache64.nemo.feature.home.banner.BannerAdapter
import com.theapache64.nemo.feature.home.category.CategoriesAdapter
import com.theapache64.nemo.feature.products.ProductsActivity
import com.theapache64.nemo.utils.calladapter.flow.Resource
import com.theapache64.nemo.utils.extensions.gone
import com.theapache64.nemo.utils.extensions.invisible
import com.theapache64.nemo.utils.extensions.visible
import com.zhpan.bannerview.constants.PageStyle
import com.zhpan.indicator.enums.IndicatorSlideMode
import dagger.hilt.android.AndroidEntryPoint
import io.github.inflationx.viewpump.ViewPumpContextWrapper

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>(R.layout.activity_home) {

    companion object {
        fun getStartIntent(context: Context): Intent {
            return Intent(context, HomeActivity::class.java).apply {
                // data goes here
            }
        }

    }

    override val viewModel: HomeViewModel by viewModels()

    override fun onCreate() {
        binding.viewModel = viewModel

        binding.csrlHome.setOnRefreshListener {
            viewModel.refresh()
        }

        binding.bvpHome
            .setAdapter(BannerAdapter())
            .setLifecycleRegistry(lifecycle)
            .setIndicatorSlideMode(IndicatorSlideMode.WORM)
            .setPageMargin(resources.getDimensionPixelOffset(R.dimen.home_banner_item_margin))
            .setRevealWidth(resources.getDimensionPixelOffset(R.dimen.home_banner_reveal_width))
            .setIndicatorStyle(IndicatorSlideMode.SCALE)
            .setPageStyle(PageStyle.MULTI_PAGE_SCALE)
            .create()

        // Banners
        viewModel.banners.observe(this, Observer {
            when (it) {
                is Resource.Loading -> {
                    binding.lvHome.showLoading(R.string.home_loading_banners)
                    binding.bvpHome.invisible()
                }
                is Resource.Success -> {

                    binding.lvHome.hideLoading()

                    if (it.data.isEmpty()) {
                        // hide
                        binding.bvpHome.gone()
                    } else {
                        // render
                        binding.bvpHome.visible()
                        binding.bvpHome.refreshData(it.data)
                    }
                }
                is Resource.Error -> {
                    binding.lvHome.showError(it.errorData)
                }
            }
        })

        // Categories
        viewModel.categories.observe(this, Observer {
            when (it) {
                is Resource.Loading -> {
                    binding.rvCategories.gone()
                    binding.tvLabelCategories.gone()
                }

                is Resource.Success -> {
                    val adapter = CategoriesAdapter(this, it.data) { position ->
                        val category = it.data[position]
                        startActivity(ProductsActivity.getStartIntent(this, category))
                    }
                    binding.rvCategories.adapter = adapter
                    binding.rvCategories.visible()
                    binding.tvLabelCategories.visible()
                }

                is Resource.Error -> {

                }
            }
        })
    }
}