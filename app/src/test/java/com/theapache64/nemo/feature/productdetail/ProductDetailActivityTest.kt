package com.theapache64.nemo.feature.productdetail

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.nhaarman.mockitokotlin2.whenever
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions.assertNotDisplayed
import com.theapache64.nemo.R
import com.theapache64.nemo.data.local.table.cart.CartEntity
import com.theapache64.nemo.data.remote.Config
import com.theapache64.nemo.data.remote.NemoApi
import com.theapache64.nemo.data.repository.CartRepo
import com.theapache64.nemo.data.repository.ConfigRepo
import com.theapache64.nemo.di.module.ApiModule
import com.theapache64.nemo.productSuccessFlow
import com.theapache64.nemo.utils.test.IdlingRule
import com.theapache64.nemo.utils.test.MainCoroutineRule
import com.theapache64.nemo.utils.test.monitorActivity
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito

/**
 * Created by theapache64 : Dec 08 Tue,2020 @ 08:09
 */
@UninstallModules(ApiModule::class)
@HiltAndroidTest
@LargeTest
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class ProductDetailActivityTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    val fakeNemoApi: NemoApi = Mockito.mock(NemoApi::class.java)

    @BindValue
    @JvmField
    val configRepo: ConfigRepo = Mockito.mock(ConfigRepo::class.java)

    @BindValue
    @JvmField
    val cartRepo: CartRepo = Mockito.mock(CartRepo::class.java)


    @get:Rule
    val idlingRule = IdlingRule()

    @get:Rule
    val coroutineRule = MainCoroutineRule()


    @Before
    fun init() {

        whenever(configRepo.getLocalConfig()).thenReturn(
            Config(
                totalProducts = 1000,
                productsPerPage = 10,
                currency = "$",
                deliveryCharge = 10,
                totalPages = 10
            )
        )
    }

    // Good product show image, title, rating, price, details, buy now button
    @Test
    fun givenProduct_whenGoodProduct_thenImageTitleRatingPriceAndBuyNowShown() {
        val productId = 1

        whenever(fakeNemoApi.getProduct(productId))
            .thenReturn(productSuccessFlow)

        whenever(cartRepo.getCartProductsFlow()).thenReturn(flowOf(listOf()))

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch<ProductDetailActivity>(
            ProductDetailActivity.getStartIntent(
                context,
                productId
            )
        ).run {
            idlingRule.dataBindingIdlingResource.monitorActivity(this)
            assertDisplayed(R.id.iv_product_image)
            assertDisplayed(R.id.tv_product_title)
            assertDisplayed(R.id.mrb_product_rating)
            assertDisplayed(R.id.tv_price)
            assertDisplayed(R.id.b_buy_now)
        }
    }

    // If product already exist in cart, GOTO cart should show
    @Test
    fun givenProduct_whenExistInCart_thenGoToCartDisplayed() {
        val productId = 1

        whenever(fakeNemoApi.getProduct(productId))
            .thenReturn(productSuccessFlow)

        whenever(cartRepo.getCartProductsFlow()).thenReturn(
            flowOf(listOf(CartEntity(productId, 10)))
        )

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch<ProductDetailActivity>(
            ProductDetailActivity.getStartIntent(
                context,
                productId
            )
        ).run {
            idlingRule.dataBindingIdlingResource.monitorActivity(this)
            assertDisplayed(R.id.b_go_to_cart)
            assertNotDisplayed(R.id.b_add_to_cart)
        }

    }

    // If product doesn't exist in cart, 'Add to cart' should show
    @Test
    fun givenProduct_whenNotInCart_thenAddToCartDisplayed() {
        val productId = 1

        whenever(fakeNemoApi.getProduct(productId))
            .thenReturn(productSuccessFlow)

        // empty products in cart
        whenever(cartRepo.getCartProductsFlow()).thenReturn(flowOf(listOf()))

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch<ProductDetailActivity>(
            ProductDetailActivity.getStartIntent(
                context,
                productId
            )
        ).run {
            idlingRule.dataBindingIdlingResource.monitorActivity(this)
            assertDisplayed(R.id.b_add_to_cart)
            assertNotDisplayed(R.id.b_go_to_cart)
        }

    }

}