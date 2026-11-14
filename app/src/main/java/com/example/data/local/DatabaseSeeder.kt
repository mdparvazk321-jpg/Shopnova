package com.example.data.local

import com.example.data.model.AddressEntity
import com.example.data.model.ApprovalStatus
import com.example.data.model.BannerEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.CouponEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItemEntity
import com.example.data.model.OrderStatuses
import com.example.data.model.PaymentMethods
import com.example.data.model.PaymentStatuses
import com.example.data.model.ProductEntity
import com.example.data.model.ReviewEntity
import com.example.data.model.SellerEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRoles
import java.security.MessageDigest

object PasswordHasher {
    fun hash(pinOrPassword: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$salt:$pinOrPassword".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

object DatabaseSeeder {
    suspend fun seedIfNeeded(dao: ShopNovaDao) {
        if (dao.getUserCount() > 0) return

        val now = System.currentTimeMillis()
        val flashEnd = now + 8L * 3600 * 1000 + 42L * 60 * 1000

        // 1. Seed Users (Buyer, Seller, Admin - all PIN "1234")
        val users = listOf(
            UserEntity(
                id = "user_buyer_demo",
                name = "Rahim Ahmed",
                phone = "01711223344",
                email = "rahim.ahmed@example.com",
                passwordHash = PasswordHasher.hash("1234", "salt_buyer"),
                salt = "salt_buyer",
                role = UserRoles.BUYER,
                isPhoneVerified = true,
                isActive = true,
                createdAt = now - 86400000L * 15
            ),
            UserEntity(
                id = "user_seller_demo",
                name = "Nusrat Jahan",
                phone = "01819556677",
                email = "dhakatech@shopnovabd.com",
                passwordHash = PasswordHasher.hash("1234", "salt_seller"),
                salt = "salt_seller",
                role = UserRoles.SELLER,
                isPhoneVerified = true,
                isActive = true,
                createdAt = now - 86400000L * 30
            ),
            UserEntity(
                id = "user_seller_fashion",
                name = "Tanvir Hasan",
                phone = "01612889900",
                email = "deshiloom@shopnovabd.com",
                passwordHash = PasswordHasher.hash("1234", "salt_fashion"),
                salt = "salt_fashion",
                role = UserRoles.SELLER,
                isPhoneVerified = true,
                isActive = true,
                createdAt = now - 86400000L * 25
            ),
            UserEntity(
                id = "user_admin_demo",
                name = "ShopNova Admin",
                phone = "01911000000",
                email = "admin@shopnovabd.com",
                passwordHash = PasswordHasher.hash("1234", "salt_admin"),
                salt = "salt_admin",
                role = UserRoles.ADMIN,
                isPhoneVerified = true,
                isActive = true,
                createdAt = now - 86400000L * 60
            )
        )
        dao.insertUsers(users)

        // 2. Seed Sellers
        val sellers = listOf(
            SellerEntity(
                id = "seller_dhaka_tech",
                userId = "user_seller_demo",
                shopName = "Dhaka Tech & Lifestyle",
                ownerName = "Nusrat Jahan",
                phone = "01819556677",
                pickupAddress = "Level 4, Bashundhara City, Panthapath, Dhaka",
                tradeLicenseOrNid = "TL-DHK-2026-8841",
                payoutMethod = "bKash",
                payoutAccount = "01819556677",
                status = ApprovalStatus.APPROVED,
                totalEarnings = 184500.0,
                pendingBalance = 24800.0,
                rating = 4.9,
                createdAt = now - 86400000L * 30
            ),
            SellerEntity(
                id = "seller_deshi_loom",
                userId = "user_seller_fashion",
                shopName = "Deshi Loom & Boutique",
                ownerName = "Tanvir Hasan",
                phone = "01612889900",
                pickupAddress = "House 18, Road 7, Dhanmondi, Dhaka",
                tradeLicenseOrNid = "TL-DHK-2026-3920",
                payoutMethod = "Nagad",
                payoutAccount = "01612889900",
                status = ApprovalStatus.APPROVED,
                totalEarnings = 96400.0,
                pendingBalance = 12300.0,
                rating = 4.8,
                createdAt = now - 86400000L * 25
            )
        )
        dao.insertSellers(sellers)

        // 3. Seed All 12 Exact Categories
        val categories = listOf(
            CategoryEntity("cat_electronics", "Electronics", "electronics", 0xFFEC008C, true, 1),
            CategoryEntity("cat_mobiles", "Mobile Phones", "mobile", 0xFF00B4D8, true, 2),
            CategoryEntity("cat_computers", "Computers & Accessories", "computer", 0xFF0B132B, true, 3),
            CategoryEntity("cat_mens", "Men's Fashion", "mens_fashion", 0xFF2563EB, true, 4),
            CategoryEntity("cat_womens", "Women's Fashion", "womens_fashion", 0xFFEC008C, true, 5),
            CategoryEntity("cat_watches", "Watches", "watch", 0xFF7C3AED, true, 6),
            CategoryEntity("cat_beauty", "Beauty & Personal Care", "beauty", 0xFFF43F5E, true, 7),
            CategoryEntity("cat_home", "Home & Kitchen", "home_kitchen", 0xFF059669, true, 8),
            CategoryEntity("cat_lifestyle", "Lifestyle", "lifestyle", 0xFF00B4D8, true, 9),
            CategoryEntity("cat_sports", "Sports & Fitness", "sports", 0xFFF59E0B, true, 10),
            CategoryEntity("cat_essentials", "Daily Essentials", "essentials", 0xFF10B981, true, 11),
            CategoryEntity("cat_tv", "TV & Appliances", "tv_appliances", 0xFF1E293B, true, 12)
        )
        dao.insertCategories(categories)

        // 4. Seed Promotional Banners
        val banners = listOf(
            BannerEntity(
                id = "banner_1",
                title = "Mega Tech & Style Fest",
                subtitle = "Up to 55% off on flagship gadgets, audio & apparel across Bangladesh",
                badgeText = "SHOPNOVA FEST",
                imageKey = "mega_sale",
                ctaCategory = "cat_electronics",
                isActive = true,
                sortOrder = 1
            ),
            BannerEntity(
                id = "banner_2",
                title = "Lightning Flash Deals",
                subtitle = "Limited stock drops every 8 hours • Smartwatches, sneakers & gifts",
                badgeText = "FLASH SALE",
                imageKey = "flash_deals",
                ctaCategory = "cat_watches",
                isActive = true,
                sortOrder = 2
            ),
            BannerEntity(
                id = "banner_3",
                title = "Free Express Delivery",
                subtitle = "Zero shipping fee across Dhaka & nationwide on orders ৳1,500+",
                badgeText = "FREE DELIVERY",
                imageKey = "free_delivery",
                ctaCategory = "cat_essentials",
                isActive = true,
                sortOrder = 3
            )
        )
        dao.insertBanners(banners)

        // 5. Seed Active Coupons
        val coupons = listOf(
            CouponEntity("cpn_nova15", "NOVA15", "15% Off Sitewide", "PERCENT", 15.0, 1000.0, true),
            CouponEntity("cpn_eid500", "EID500", "৳500 Off Orders ৳3,000+", "FLAT", 500.0, 3000.0, true),
            CouponEntity("cpn_freeship", "FREESHIP", "Free Delivery Nationwide", "FREESHIP", 120.0, 500.0, true)
        )
        dao.insertCoupons(coupons)

        // 6. Seed 20 Realistic Products covering all 12 categories
        val products = listOf(
            ProductEntity(
                id = "prod_1",
                sellerId = "seller_dhaka_tech",
                sellerName = "Dhaka Tech & Lifestyle",
                categoryId = "cat_electronics",
                categoryName = "Electronics",
                name = "Soundcore Q30 Hybrid ANC Wireless Headphones",
                description = "Hi-Res studio wireless headphones with 40-hour battery life, multi-mode active noise cancellation, and memory foam earcups. Official 1-year Bangladesh warranty included.",
                originalPrice = 11500.0,
                discountedPrice = 7490.0,
                discountPercent = 35,
                rating = 4.9,
                reviewCount = 148,
                colorsCsv = "Midnight Black,Sakura Pink,Ocean Cyan",
                sizesCsv = "One Size",
                stock = 32,
                soldCount = 84,
                flashSaleTargetStock = 100,
                isFlashSale = true,
                flashSaleEndMillis = flashEnd,
                isTrending = true,
                isBestSeller = true,
                isRecommended = true,
                imageResKey = "headphones"
            ),
            ProductEntity(
                id = "prod_2",
                sellerId = "seller_dhaka_tech",
                sellerName = "Dhaka Tech & Lifestyle",
                categoryId = "cat_mobiles",
                categoryName = "Mobile Phones",
                name = "Samsung Galaxy A55 5G (8GB / 256GB) Official",
                description = "6.6-inch 120Hz Super AMOLED display, 50MP OIS triple camera, IP67 water resistance, and 5000mAh battery. Official BTRC-approved Bangladesh warranty.",
                originalPrice = 54999.0,
                discountedPrice = 44990.0,
                discountPercent = 18,
                rating = 4.8,
                reviewCount = 214,
                colorsCsv = "Awesome Navy,Awesome Iceblue,Awesome Lilac",
                sizesCsv = "8GB/128GB,8GB/256GB",
                stock = 18,
                soldCount = 72,
                flashSaleTargetStock = 90,
                isFlashSale = true,
                flashSaleEndMillis = flashEnd,
                isTrending = true,
                isBestSeller = true,
                isRecommended = true,
                imageResKey = "smartphone"
            ),
            ProductEntity(
                id = "prod_3",
                sellerId = "seller_dhaka_tech",
                sellerName = "Dhaka Tech & Lifestyle",
                categoryId = "cat_watches",
                categoryName = "Watches",
                name = "Amazfit GTR 4 AMOLED Smartwatch",
                description = "Dual-band GPS, Bluetooth calling, 14-day battery, 150+ sports modes, and 5 ATM water resistance with classic brushed alloy bezel.",
                originalPrice = 19500.0,
                discountedPrice = 13990.0,
                discountPercent = 28,
                rating = 4.8,
                reviewCount = 96,
                colorsCsv = "Superspeed Black,Racetrack Grey",
                sizesCsv = "46mm",
                stock = 24,
                soldCount = 67,
                flashSaleTargetStock = 80,
                isFlashSale = true,
                flashSaleEndMillis = flashEnd,
                isTrending = true,
                isNewArrival = true,
                imageResKey = "watch"
            ),
            ProductEntity(
                id = "prod_4",
                sellerId = "seller_dhaka_tech",
                sellerName = "Dhaka Tech & Lifestyle",
                categoryId = "cat_computers",
                categoryName = "Computers & Accessories",
                name = "ASUS VivoBook 15 Core i5 13th Gen Laptop",
                description = "15.6-inch FHD IPS anti-glare display, Intel Core i5-1335U, 16GB DDR4 RAM, 512GB NVMe SSD, backlit keyboard, and fingerprint sensor.",
                originalPrice = 78500.0,
                discountedPrice = 68900.0,
                discountPercent = 12,
                rating = 4.7,
                reviewCount = 63,
                colorsCsv = "Quiet Blue,Cool Silver",
                sizesCsv = "16GB / 512GB",
                stock = 11,
                soldCount = 41,
                isBestSeller = true,
                isRecommended = true,
                imageResKey = "laptop"
            ),
            ProductEntity(
                id = "prod_5",
                sellerId = "seller_deshi_loom",
                sellerName = "Deshi Loom & Boutique",
                categoryId = "cat_mens",
                categoryName = "Men's Fashion",
                name = "Premium Egyptian Cotton Embroidered Panjabi",
                description = "Breathable 100% combed Egyptian cotton Panjabi with subtle geometric collar embroidery, tailored for Dhaka's tropical climate and festive occasions.",
                originalPrice = 3200.0,
                discountedPrice = 2190.0,
                discountPercent = 32,
                rating = 4.9,
                reviewCount = 312,
                colorsCsv = "Deep Navy,Ivory Cream,Teal Cyan",
                sizesCsv = "M,L,XL,XXL",
                stock = 48,
                soldCount = 195,
                isBestSeller = true,
                isTrending = true,
                isRecommended = true,
                imageResKey = "mens_fashion"
            ),
            ProductEntity(
                id = "prod_6",
                sellerId = "seller_deshi_loom",
                sellerName = "Deshi Loom & Boutique",
                categoryId = "cat_womens",
                categoryName = "Women's Fashion",
                name = "Tangail Half-Silk Handloom Jamdani Saree",
                description = "Authentic handwoven Jamdani motif saree crafted by Tangail artisans with matching blouse piece. Lightweight, elegant drape for weddings and Eid.",
                originalPrice = 6800.0,
                discountedPrice = 4450.0,
                discountPercent = 35,
                rating = 4.9,
                reviewCount = 187,
                colorsCsv = "Magenta Pink,Turquoise Mist,Pearl Gold",
                sizesCsv = "12 Haat + Blouse",
                stock = 19,
                soldCount = 88,
                flashSaleTargetStock = 100,
                isFlashSale = true,
                flashSaleEndMillis = flashEnd,
                isBestSeller = true,
                isTrending = true,
                imageResKey = "womens_fashion"
            ),
            ProductEntity(
                id = "prod_7",
                sellerId = "seller_deshi_loom",
                sellerName = "Deshi Loom & Boutique",
                categoryId = "cat_beauty",
                categoryName = "Beauty & Personal Care",
                name = "GlowLab 10% Niacinamide + Zinc Brightening Serum",
                description = "Dermatologist-tested daily facial serum that refines pores, balances oil, and boosts skin radiance. 100% authentic imported skincare (30ml).",
                originalPrice = 1650.0,
                discountedPrice = 990.0,
                discountPercent = 40,
                rating = 4.8,
                reviewCount = 240,
                colorsCsv = "30ml Dropper",
                sizesCsv = "30ml,50ml",
                stock = 65,
                soldCount = 91,
                flashSaleTargetStock = 100,
                isFlashSale = true,
                flashSaleEndMillis = flashEnd,
                isBestSeller = true,
                isNewArrival = true,
                imageResKey = "beauty"
            ),
            ProductEntity(
                id = "prod_8",
                sellerId = "seller_dhaka_tech",
                sellerName = "Dhaka Tech & Lifestyle",
                categoryId = "cat_home",
                categoryName = "Home & Kitchen",
                name = "NovaChef 750W Heavy-Duty Mixer Grinder (3 Jars)",
                description = "Powerful copper-motor Bangladeshi spice & chutney grinder with 3 stainless steel jars, overload protection, and sleek matte body.",
                originalPrice = 5200.0,
                discountedPrice = 3790.0,
                discountPercent = 27,
                rating = 4.7,
                reviewCount = 129,
                colorsCsv = "Pearl White,Charcoal Navy",
                sizesCsv = "3-Jar Set",
                stock = 29,
                soldCount = 64,
                isRecommended = true,
                isBestSeller = true,
                imageResKey = "home_kitchen"
            ),
            ProductEntity(
                id = "prod_9",
                sellerId = "seller_deshi_loom",
                sellerName = "Deshi Loom & Boutique",
                categoryId = "cat_lifestyle",
                categoryName = "Lifestyle",
                name = "UrbanNomad Waterproof 16\" Laptop Backpack",
                description = "Anti-theft USB-charging commuter backpack with padded 16-inch laptop sleeve, luggage strap, and water-repellent ballistic nylon.",
                originalPrice = 2850.0,
                discountedPrice = 1790.0,
                discountPercent = 37,
                rating = 4.7,
                reviewCount = 152,
                colorsCsv = "Obsidian Navy,Slate Grey,Coral Pink",
                sizesCsv = "24L",
                stock = 40,
                soldCount = 77,
                isNewArrival = true,
                isRecommended = true,
                imageResKey = "lifestyle"
            ),
            ProductEntity(
                id = "prod_10",
                sellerId = "seller_dhaka_tech",
                sellerName = "Dhaka Tech & Lifestyle",
                categoryId = "cat_sports",
                categoryName = "Sports & Fitness",
                name = "AeroStride Pro Cushion Running Sneakers",
                description = "Breathable engineered mesh upper with dual-density EVA shock absorption sole, built for morning runs at Hatirjheel or gym sessions.",
                originalPrice = 4400.0,
                discountedPrice = 2690.0,
                discountPercent = 39,
                rating = 4.8,
                reviewCount = 108,
                colorsCsv = "Cyan/White,Pink/Navy,All Black",
                sizesCsv = "40,41,42,43,44",
                stock = 35,
                soldCount = 74,
                flashSaleTargetStock = 100,
                isFlashSale = true,
                flashSaleEndMillis = flashEnd,
                isTrending = true,
                isNewArrival = true,
                imageResKey = "sports"
            ),
            ProductEntity(
                id = "prod_11",
                sellerId = "seller_deshi_loom",
                sellerName = "Deshi Loom & Boutique",
                categoryId = "cat_essentials",
                categoryName = "Daily Essentials",
                name = "Sundarban Pure Raw Mustard Honey & Chia Combo",
                description = "100% pure lab-tested Sundarban natural honey (500g) paired with organic Grade-A Chia seeds (400g) in airtight glass jars.",
                originalPrice = 1350.0,
                discountedPrice = 890.0,
                discountPercent = 34,
                rating = 4.9,
                reviewCount = 276,
                colorsCsv = "Glass Jar Combo",
                sizesCsv = "900g Pack",
                stock = 82,
                soldCount = 210,
                isBestSeller = true,
                isRecommended = true,
                imageResKey = "essentials"
            ),
            ProductEntity(
                id = "prod_12",
                sellerId = "seller_dhaka_tech",
                sellerName = "Dhaka Tech & Lifestyle",
                categoryId = "cat_tv",
                categoryName = "TV & Appliances",
                name = "Sony Bravia 43\" 4K Ultra HD Google TV",
                description = "X1 4K HDR processor, Dolby Audio, built-in Chromecast, and voice remote. Free wall-mount installation within Dhaka Metro.",
                originalPrice = 59900.0,
                discountedPrice = 49900.0,
                discountPercent = 17,
                rating = 4.9,
                reviewCount = 84,
                colorsCsv = "Bezel Black",
                sizesCsv = "43 Inch,55 Inch",
                stock = 12,
                soldCount = 39,
                isTrending = true,
                isRecommended = true,
                imageResKey = "tv_appliances"
            ),
            ProductEntity(
                id = "prod_13",
                sellerId = "seller_dhaka_tech",
                sellerName = "Dhaka Tech & Lifestyle",
                categoryId = "cat_electronics",
                categoryName = "Electronics",
                name = "Baseus 65W GaN3 Pro Fast Charger + 100W Cable",
                description = "Triple-port USB-C & USB-A GaN charger capable of fast-charging MacBook, iPad, and Galaxy smartphones simultaneously.",
                originalPrice = 3400.0,
                discountedPrice = 2150.0,
                discountPercent = 37,
                rating = 4.8,
                reviewCount = 173,
                colorsCsv = "Midnight Black,Ice White",
                sizesCsv = "UK/BD 3-Pin",
                stock = 54,
                soldCount = 144,
                isNewArrival = true,
                isBestSeller = true,
                imageResKey = "showcase"
            ),
            ProductEntity(
                id = "prod_14",
                sellerId = "seller_dhaka_tech",
                sellerName = "Dhaka Tech & Lifestyle",
                categoryId = "cat_computers",
                categoryName = "Computers & Accessories",
                name = "Keychron K2 V2 Wireless Mechanical Keyboard",
                description = "Hot-swappable RGB wireless mechanical keyboard with Gateron Brown switches for Mac & Windows.",
                originalPrice = 9800.0,
                discountedPrice = 7950.0,
                discountPercent = 19,
                rating = 4.9,
                reviewCount = 91,
                colorsCsv = "White/Cyan RGB,Dark Grey",
                sizesCsv = "Gateron Brown,Gateron Red",
                stock = 17,
                soldCount = 53,
                isNewArrival = true,
                isTrending = true,
                imageResKey = "laptop"
            ),
            ProductEntity(
                id = "prod_15",
                sellerId = "seller_deshi_loom",
                sellerName = "Deshi Loom & Boutique",
                categoryId = "cat_womens",
                categoryName = "Women's Fashion",
                name = "Noor Couture Quilted Crossbody Leather Bag",
                description = "Structured vegan pebbled leather handbag with gold-tone chain strap, twin compartments, and magnetic clasp.",
                originalPrice = 3600.0,
                discountedPrice = 2390.0,
                discountPercent = 34,
                rating = 4.7,
                reviewCount = 115,
                colorsCsv = "Blush Pink,Emerald Teal,Classic Black",
                sizesCsv = "Medium",
                stock = 27,
                soldCount = 68,
                isNewArrival = true,
                isRecommended = true,
                imageResKey = "womens_fashion"
            ),
            ProductEntity(
                id = "prod_16",
                sellerId = "seller_dhaka_tech",
                sellerName = "Dhaka Tech & Lifestyle",
                categoryId = "cat_mobiles",
                categoryName = "Mobile Phones",
                name = "Redmi Note 13 Pro+ 5G (12GB / 256GB)",
                description = "200MP OIS camera, curved 1.5K AMOLED 120Hz display, and 120W HyperCharge (0-100% in 19 mins).",
                originalPrice = 44500.0,
                discountedPrice = 38490.0,
                discountPercent = 14,
                rating = 4.8,
                reviewCount = 132,
                colorsCsv = "Aurora Purple,Midnight Black,Fusion White",
                sizesCsv = "12GB/256GB",
                stock = 22,
                soldCount = 61,
                isTrending = true,
                isRecommended = true,
                imageResKey = "smartphone"
            )
        )
        dao.insertProducts(products)

        // 7. Seed Reviews
        val reviews = listOf(
            ReviewEntity("rev_1", "prod_1", "user_buyer_demo", "Rahim Ahmed", 5, "ANC works amazingly in Dhaka traffic! Battery easily lasts all week.", now - 86400000L * 4),
            ReviewEntity("rev_2", "prod_1", "user_seller_fashion", "Tanvir Hasan", 5, "Delivered in 24 hours inside Banani. Genuine Soundcore product.", now - 86400000L * 2),
            ReviewEntity("rev_3", "prod_2", "user_buyer_demo", "Rahim Ahmed", 5, "Official BTRC verified unit, sealed box and super crisp AMOLED display.", now - 86400000L * 5),
            ReviewEntity("rev_4", "prod_6", "user_seller_demo", "Nusrat Jahan", 5, "Beautiful Jamdani weave and vibrant magenta border. Loved the packaging!", now - 86400000L * 3)
        )
        dao.insertReviews(reviews)

        // 8. Seed Saved Addresses for Demo Buyer
        val addresses = listOf(
            AddressEntity(
                id = "addr_1",
                userId = "user_buyer_demo",
                label = "Home",
                recipientName = "Rahim Ahmed",
                phone = "01711223344",
                division = "Dhaka",
                district = "Dhaka",
                area = "Banani",
                streetAddress = "House 24, Road 11, Block F, Banani",
                isDhakaMetro = true,
                isDefault = true
            ),
            AddressEntity(
                id = "addr_2",
                userId = "user_buyer_demo",
                label = "Office",
                recipientName = "Rahim Ahmed",
                phone = "01711223344",
                division = "Dhaka",
                district = "Dhaka",
                area = "Gulshan-2",
                streetAddress = "Floor 6, Crystal Tower, Gulshan Avenue",
                isDhakaMetro = true,
                isDefault = false
            )
        )
        dao.insertAddresses(addresses)

        // 9. Seed Sample Orders (one Delivered for Return testing, one Out for Delivery for Tracking!)
        val order1 = OrderEntity(
            id = "ord_demo_1",
            orderCode = "SN-BD-10482",
            userId = "user_buyer_demo",
            customerName = "Rahim Ahmed",
            customerPhone = "01711223344",
            shippingAddress = "House 24, Road 11, Block F, Banani, Dhaka",
            isDhakaMetro = true,
            subtotal = 7490.0,
            deliveryFee = 0.0,
            couponDiscount = 0.0,
            couponCode = "",
            totalAmount = 7490.0,
            paymentMethod = PaymentMethods.BKASH,
            paymentStatus = PaymentStatuses.PAID,
            paymentWalletNumber = "01711223344",
            paymentTrxId = "BKS948201X",
            orderStatus = OrderStatuses.DELIVERED,
            createdAt = now - 86400000L * 4,
            updatedAt = now - 86400000L * 1
        )
        val order2 = OrderEntity(
            id = "ord_demo_2",
            orderCode = "SN-BD-10519",
            userId = "user_buyer_demo",
            customerName = "Rahim Ahmed",
            customerPhone = "01711223344",
            shippingAddress = "Floor 6, Crystal Tower, Gulshan-2, Dhaka",
            isDhakaMetro = true,
            subtotal = 2190.0,
            deliveryFee = 60.0,
            couponDiscount = 0.0,
            couponCode = "",
            totalAmount = 2250.0,
            paymentMethod = PaymentMethods.COD,
            paymentStatus = PaymentStatuses.COD_DUE,
            orderStatus = OrderStatuses.OUT_FOR_DELIVERY,
            createdAt = now - 3600000L * 18,
            updatedAt = now - 1800000L
        )
        dao.insertOrders(listOf(order1, order2))
        dao.insertOrderItems(
            listOf(
                OrderItemEntity(
                    id = "oi_1",
                    orderId = "ord_demo_1",
                    productId = "prod_1",
                    sellerId = "seller_dhaka_tech",
                    productName = "Soundcore Q30 Hybrid ANC Wireless Headphones",
                    imageResKey = "headphones",
                    selectedColor = "Midnight Black",
                    selectedSize = "One Size",
                    unitPrice = 7490.0,
                    quantity = 1
                ),
                OrderItemEntity(
                    id = "oi_2",
                    orderId = "ord_demo_2",
                    productId = "prod_5",
                    sellerId = "seller_deshi_loom",
                    productName = "Premium Egyptian Cotton Embroidered Panjabi",
                    imageResKey = "mens_fashion",
                    selectedColor = "Deep Navy",
                    selectedSize = "L",
                    unitPrice = 2190.0,
                    quantity = 1
                )
            )
        )

        // 10. Seed Notifications
        dao.insertNotifications(
            listOf(
                NotificationEntity(
                    id = "notif_1",
                    userId = "user_buyer_demo",
                    title = "Out for Delivery!",
                    message = "Your order SN-BD-10519 is with our Dhaka rider and arriving today.",
                    type = "ORDER",
                    isRead = false,
                    createdAt = now - 1800000L
                ),
                NotificationEntity(
                    id = "notif_2",
                    userId = "ALL",
                    title = "ShopNova Flash Sale Live",
                    message = "Save up to 40% on electronics, fashion & skincare. Use code NOVA15 for extra 15% off!",
                    type = "PROMO",
                    isRead = false,
                    createdAt = now - 7200000L
                )
            )
        )
    }
}
