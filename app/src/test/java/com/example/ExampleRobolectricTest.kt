package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DatabaseSeeder
import com.example.data.local.SessionManager
import com.example.data.local.ShopNovaDatabase
import com.example.data.model.OrderStatuses
import com.example.data.model.PaymentMethods
import com.example.data.model.PaymentStatuses
import com.example.data.repository.ShopNovaRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: ShopNovaDatabase
    private lateinit var repository: ShopNovaRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, ShopNovaDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ShopNovaRepository(db.shopNovaDao(), SessionManager(context))
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `app name matches ShopNova BD`() {
        assertEquals("ShopNova BD", context.getString(R.string.app_name))
    }

    @Test
    fun `database seeds 12 categories and demo products`() = runTest {
        val dao = db.shopNovaDao()
        DatabaseSeeder.seedIfNeeded(dao)

        val categories = dao.observeActiveCategories().first()
        val products = dao.observeApprovedProducts().first()

        assertEquals(12, categories.size)
        assertTrue(products.size >= 16)
        assertTrue(products.any { it.isFlashSale })
    }

    @Test
    fun `checkout with COD and coupon calculates total and places order`() = runTest {
        val dao = db.shopNovaDao()
        DatabaseSeeder.seedIfNeeded(dao)

        val userId = "user_buyer_demo"
        val product = dao.observeApprovedProducts().first().first()
        val address = dao.observeAddresses(userId).first().first()
        val coupon = repository.validateCoupon("NOVA15", product.discountedPrice).getOrThrow()

        repository.addToCart(userId, product, product.colors.first(), product.sizes.first(), 1)
        val cartItems = dao.getCartItems(userId).map { it to product }

        val orderResult = repository.placeOrder(
            userId = userId,
            address = address,
            cartWithProducts = cartItems,
            paymentMethod = PaymentMethods.COD,
            walletNumber = "",
            trxId = "",
            coupon = coupon
        )

        assertTrue(orderResult.isSuccess)
        val order = orderResult.getOrThrow()
        assertEquals(OrderStatuses.PLACED, order.orderStatus)
        assertEquals(PaymentStatuses.COD_DUE, order.paymentStatus)
        assertTrue(order.couponDiscount > 0.0)
        assertTrue(dao.getCartItems(userId).isEmpty())
    }
}
