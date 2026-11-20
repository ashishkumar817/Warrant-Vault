package com.warrantvault;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.warrantvault.dao.UserDAO;
import com.warrantvault.models.Product;
import com.warrantvault.models.Warranty;
import com.warrantvault.utils.BillOcrParser;
import com.warrantvault.utils.DateUtils;
import com.warrantvault.utils.WarrantyUtils;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

public class WarrantyLogicTest {

    @Test
    public void testDateUtilsEndDateCalculation() {
        // Test user's exact specification:
        // Purchase Date: 12 Aug 2026, Warranty: 12 Months -> 11 Aug 2027
        String startDate = "12 Aug 2026";
        String endDate = DateUtils.calculateEndDate(startDate, 12, "Months");
        assertEquals("11 Aug 2027", endDate);

        // 2 Years test
        String twoYears = DateUtils.calculateEndDate(startDate, 2, "Years");
        assertEquals("11 Aug 2028", twoYears);
    }

    @Test
    public void testWarrantyActiveComputation() {
        Calendar cal = Calendar.getInstance();

        // Start date 1 month ago
        cal.add(Calendar.MONTH, -1);
        String start = DateUtils.formatDate(cal.getTime());

        // End date 10 months in the future
        cal.add(Calendar.MONTH, 11);
        String end = DateUtils.formatDate(cal.getTime());

        Product product = new Product();
        product.setName("Test Product");
        Warranty warranty = new Warranty(1, start, end, "12 Months", "Apple", "");
        product.setWarranty(warranty);

        WarrantyUtils.computeWarrantyMetrics(product);

        assertEquals(WarrantyUtils.STATUS_ACTIVE, product.getStatus());
        assertTrue(product.getDaysRemaining() > 30);
    }

    @Test
    public void testWarrantyExpiringSoonComputation() {
        Calendar cal = Calendar.getInstance();

        // End date 12 days in future
        cal.add(Calendar.DAY_OF_YEAR, 12);
        String end = DateUtils.formatDate(cal.getTime());

        // Start date 1 year ago
        cal.add(Calendar.YEAR, -1);
        String start = DateUtils.formatDate(cal.getTime());

        Product product = new Product();
        product.setName("Dell Monitor");
        Warranty warranty = new Warranty(1, start, end, "1 Year", "Dell", "");
        product.setWarranty(warranty);

        WarrantyUtils.computeWarrantyMetrics(product);

        assertEquals(WarrantyUtils.STATUS_EXPIRING, product.getStatus());
        assertTrue(product.getDaysRemaining() <= 30 && product.getDaysRemaining() > 0);
    }

    @Test
    public void testWarrantyExpiredComputation() {
        Calendar cal = Calendar.getInstance();

        // Expired 15 days ago
        cal.add(Calendar.DAY_OF_YEAR, -15);
        String end = DateUtils.formatDate(cal.getTime());

        cal.add(Calendar.YEAR, -1);
        String start = DateUtils.formatDate(cal.getTime());

        Product product = new Product();
        product.setName("Old Watch");
        Warranty warranty = new Warranty(1, start, end, "1 Year", "Apple", "");
        product.setWarranty(warranty);

        WarrantyUtils.computeWarrantyMetrics(product);

        assertEquals(WarrantyUtils.STATUS_EXPIRED, product.getStatus());
        assertEquals(0, product.getDaysRemaining());
        assertEquals(100, product.getElapsedPercentage());
    }

    @Test
    public void testPasswordHashing() {
        String hash1 = UserDAO.hashPassword("secret123");
        String hash2 = UserDAO.hashPassword("secret123");
        String hashDiff = UserDAO.hashPassword("otherPassword");

        assertNotNull(hash1);
        assertEquals(64, hash1.length()); // SHA-256 is 64 hex characters
        assertEquals(hash1, hash2);
        assertFalse(hash1.equals(hashDiff));
    }

    @Test
    public void testBillOcrParser() {
        String sampleReceipt = "Samsung Experience Store\n" +
                "MG Road, Bengaluru\n" +
                "Invoice No: INV-98273\n" +
                "Date: 12 Aug 2026\n" +
                "Product: Samsung Galaxy S25\n" +
                "Model: SM-S931B\n" +
                "Serial No: SM-98273\n" +
                "Warranty: 12 Months\n" +
                "Price: ₹79,999\n" +
                "Total: ₹79,999.00\n" +
                "Thank you for shopping!";

        BillOcrParser.ExtractedBillData data = BillOcrParser.parse(sampleReceipt);

        assertEquals("Samsung", data.brand);
        assertEquals("Samsung Experience Store", data.seller);
        assertEquals("INV-98273", data.invoiceNumber);
        assertEquals("12 Aug 2026", data.purchaseDate);
        assertEquals("Samsung Galaxy S25", data.productName);
        assertEquals("Mobiles", data.category);
        assertEquals("79999", data.purchasePrice);
        assertEquals("12 Months", data.warrantyPeriod);
    }
}
