package com.example.data

object ProductCatalog {
    val products: List<Product> = listOf(
        Product(
            id = "prod_mithila_saree",
            name = "Bhagalpuri Tussar Silk Saree",
            hiName = "भागलपुरी टसर सिल्क साड़ी",
            category = ProductCategory.MITHILA,
            price = 2499.0,
            mrp = 3699.0,
            rating = 4.8,
            badge = "GI Tagged",
            description = "Handwoven authentic Bhagalpuri tussar silk with Mithila border motif and matching blouse piece."
        ),
        Product(
            id = "prod_makhana",
            name = "Mithila GI Phool Makhana (500g)",
            hiName = "मिथिला फूल मखाना (500 ग्राम)",
            category = ProductCategory.GROCERY,
            price = 549.0,
            mrp = 699.0,
            rating = 4.9,
            badge = "Farm Fresh",
            description = "Jumbo 6-Suta roasted-grade lotus seeds sourced directly from Darbhanga & Madhubani ponds."
        ),
        Product(
            id = "prod_biometric_scanner",
            name = "USB Type-C Fingerprint RD Scanner",
            hiName = "बायोमेट्रिक फिंगरप्रिंट स्कैनर",
            category = ProductCategory.ELECTRONICS,
            price = 2299.0,
            mrp = 3100.0,
            rating = 4.6,
            badge = "CSP Essential",
            description = "L1-certified optical fingerprint reader compatible with Android OTG for banking & KYC desks."
        ),
        Product(
            id = "prod_sattu_combo",
            name = "Roasted Chana Sattu & Mustard Oil Pack",
            hiName = "शुद्ध चना सत्तू व कच्ची घानी तेल",
            category = ProductCategory.GROCERY,
            price = 420.0,
            mrp = 520.0,
            rating = 4.7,
            badge = "Bestseller",
            description = "1 kg stone-ground gram sattu paired with 1 L cold-pressed kachi ghani mustard oil."
        ),
        Product(
            id = "prod_madhubani_frame",
            name = "Handmade Madhubani Wall Art Frame",
            hiName = "हस्तनिर्मित मधुबनी पेंटिंग फ्रेम",
            category = ProductCategory.MITHILA,
            price = 1199.0,
            mrp = 1799.0,
            rating = 4.9,
            badge = "Artisan Made",
            description = "Natural mineral-pigment Kohbar painting on handmade paper framed in seasoned teak wood."
        ),
        Product(
            id = "prod_thermal_printer",
            name = "58mm Bluetooth Thermal Receipt Printer",
            hiName = "ब्लूटूथ थर्मल बिल प्रिंटर (58mm)",
            category = ProductCategory.ELECTRONICS,
            price = 1849.0,
            mrp = 2699.0,
            rating = 4.5,
            badge = "Shop Utility",
            description = "Portable rechargeable receipt printer for Kirana bills, AEPS receipts, and Hisab Kitab slips."
        ),
        Product(
            id = "prod_brass_kadhai",
            name = "Heavy-Gauge Brass Kadhai (2.5L)",
            hiName = "शुद्ध पीतल कड़ाही (2.5 लीटर)",
            category = ProductCategory.HOME,
            price = 1599.0,
            mrp = 2299.0,
            rating = 4.7,
            badge = "Kitchen Classic",
            description = "Traditional kalai-lined solid brass cookware with riveted cool-touch handles."
        ),
        Product(
            id = "prod_khadi_kurta",
            name = "Patna Handspun Khadi Cotton Kurta",
            hiName = "पटना हैंडस्पन खादी कॉटन कुर्ता",
            category = ProductCategory.FASHION,
            price = 899.0,
            mrp = 1399.0,
            rating = 4.6,
            badge = "Comfort Fit",
            description = "Breathable pure khadi cotton kurta with mandarin collar, ideal for all-season daily wear."
        ),
        Product(
            id = "prod_powerbank",
            name = "20000mAh 22.5W Fast Power Bank",
            hiName = "20000mAh फास्ट चार्जिंग पावर बैंक",
            category = ProductCategory.ELECTRONICS,
            price = 1299.0,
            mrp = 1999.0,
            rating = 4.5,
            badge = "35% Off",
            description = "Dual USB + Type-C PD power bank with digital LED battery percentage indicator."
        ),
        Product(
            id = "prod_steel_tiffin",
            name = "Insulated 4-Tier Stainless Lunch Box",
            hiName = "इंसुलेटेड 4-टियर स्टील टिफिन",
            category = ProductCategory.HOME,
            price = 749.0,
            mrp = 1099.0,
            rating = 4.4,
            badge = "Value Pick",
            description = "Leak-proof food-grade 304 steel containers with thermal outer jacket keeping meals warm 6 hours."
        )
    )
}
