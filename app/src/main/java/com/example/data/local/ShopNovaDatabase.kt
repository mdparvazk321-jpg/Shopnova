package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AddressEntity
import com.example.data.model.BannerEntity
import com.example.data.model.CartItemEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.CouponEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItemEntity
import com.example.data.model.ProductEntity
import com.example.data.model.ReturnRequestEntity
import com.example.data.model.ReviewEntity
import com.example.data.model.SellerEntity
import com.example.data.model.UserEntity
import com.example.data.model.WishlistEntity

@Database(
    entities = [
        UserEntity::class,
        SellerEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        ReviewEntity::class,
        CartItemEntity::class,
        WishlistEntity::class,
        AddressEntity::class,
        CouponEntity::class,
        BannerEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        ReturnRequestEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ShopNovaDatabase : RoomDatabase() {
    abstract fun shopNovaDao(): ShopNovaDao

    companion object {
        @Volatile
        private var INSTANCE: ShopNovaDatabase? = null

        fun getInstance(context: Context): ShopNovaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShopNovaDatabase::class.java,
                    "shopnova_bd.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
