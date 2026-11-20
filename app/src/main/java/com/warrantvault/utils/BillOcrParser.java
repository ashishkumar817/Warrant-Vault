package com.warrantvault.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BillOcrParser {

    public static class ExtractedBillData {
        public String productName = "";
        public String brand = "";
        public String category = "Electronics";
        public String modelNumber = "";
        public String serialNumber = "";
        public String purchaseDate = "";
        public String purchasePrice = "";
        public String seller = "";
        public String invoiceNumber = "";
        public String warrantyPeriod = "12 Months";
        public String rawText = "";
    }

    private static final String[] KNOWN_BRANDS = {
            "Samsung", "Apple", "Dell", "Sony", "LG", "HP", "Lenovo", "Asus", "Acer",
            "Whirlpool", "Bosch", "OnePlus", "Xiaomi", "Google", "Boat", "JBL", "Canon",
            "Nikon", "Philips", "Dyson", "Panasonic", "Voltas", "Hitachi", "Bose", "Realme"
    };

    private static final String[] KNOWN_SELLERS = {
            "Croma", "Reliance Digital", "Amazon", "Flipkart", "Vijay Sales", "Apple Store",
            "Samsung Experience Store", "Poorvika", "Sangeetha", "Best Buy", "Tata CliQ", "Dell Exclusive Store"
    };

    public static ExtractedBillData parse(String ocrText) {
        ExtractedBillData data = new ExtractedBillData();
        if (ocrText == null || ocrText.trim().isEmpty()) {
            data.purchaseDate = DateUtils.getTodayDisplay();
            return data;
        }

        data.rawText = ocrText;
        String[] lines = ocrText.split("\n");

        // 1. Detect Brand
        for (String brand : KNOWN_BRANDS) {
            if (Pattern.compile("\\b" + Pattern.quote(brand) + "\\b", Pattern.CASE_INSENSITIVE).matcher(ocrText).find()) {
                data.brand = brand;
                break;
            }
        }

        // 2. Detect Seller
        for (String seller : KNOWN_SELLERS) {
            if (Pattern.compile("\\b" + Pattern.quote(seller) + "\\b", Pattern.CASE_INSENSITIVE).matcher(ocrText).find()) {
                data.seller = seller;
                break;
            }
        }
        if (data.seller.isEmpty() && lines.length > 0) {
            // Very common for the first line or header line to be the store name
            String firstLine = lines[0].trim();
            if (firstLine.length() > 2 && firstLine.length() < 35 && !firstLine.matches(".*\\d{4}.*")) {
                data.seller = firstLine;
            }
        }

        // 3. Detect Invoice Number (INV-..., Invoice No, Bill No, etc.)
        Pattern invPattern = Pattern.compile("(?:Invoice|INV|Bill|Receipt|Order)(?:\\s*(?:No|Num|Number|#))?[\\s#:\\-]*([A-Z0-9\\-]+)", Pattern.CASE_INSENSITIVE);
        Matcher invMatcher = invPattern.matcher(ocrText);
        if (invMatcher.find()) {
            data.invoiceNumber = invMatcher.group(1).trim();
        } else {
            // General invoice pattern e.g. INV-98273
            Pattern codePattern = Pattern.compile("\\b[A-Z]{2,4}[\\-_][0-9]{4,8}\\b");
            Matcher codeMatcher = codePattern.matcher(ocrText);
            if (codeMatcher.find()) {
                data.invoiceNumber = codeMatcher.group(0);
            }
        }

        // 4. Detect Date (e.g., 12 Aug 2026, 12/08/2026, 2026-08-12)
        Pattern datePattern = Pattern.compile("(\\b\\d{1,2}[\\s/\\-.](?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|\\d{1,2})[\\s/\\-.]\\d{2,4}\\b)", Pattern.CASE_INSENSITIVE);
        Matcher dateMatcher = datePattern.matcher(ocrText);
        if (dateMatcher.find()) {
            data.purchaseDate = DateUtils.normalizeToDisplay(dateMatcher.group(1));
        } else {
            data.purchaseDate = DateUtils.getTodayDisplay();
        }

        // 5. Detect Price (₹, Rs, INR, Total, Amount, Price)
        Pattern pricePattern = Pattern.compile("(?:₹|Rs\\.?|INR|Total|Amount|Price)[\\s:]*(?:₹|Rs\\.?)?\\s*([0-9,]+(?:\\.[0-9]{2})?)", Pattern.CASE_INSENSITIVE);
        Matcher priceMatcher = pricePattern.matcher(ocrText);
        if (priceMatcher.find()) {
            String pStr = priceMatcher.group(1).replace(",", "").trim();
            if (pStr.endsWith(".00")) {
                pStr = pStr.substring(0, pStr.length() - 3);
            }
            data.purchasePrice = pStr;
        }

        // 6. Detect Warranty Period (e.g., Warranty: 12 Months, 1 Year, 2 Years)
        Pattern warrPrefixPattern = Pattern.compile("(?:Warranty|Guarantee)(?:\\s*Period)?[\\s#:\\-]*(\\d{1,2})\\s*(Months?|Years?|M|Y|yr|mo)\\b", Pattern.CASE_INSENSITIVE);
        Matcher warrPrefixMatcher = warrPrefixPattern.matcher(ocrText);
        if (warrPrefixMatcher.find()) {
            String count = warrPrefixMatcher.group(1);
            String unit = warrPrefixMatcher.group(2).toLowerCase();
            if (unit.startsWith("y")) {
                data.warrantyPeriod = count + " Years";
            } else {
                data.warrantyPeriod = count + " Months";
            }
        } else {
            Pattern generalWarrPattern = Pattern.compile("\\b(\\d{1,2})\\s*(Months?|Years?|yr|mo)\\b", Pattern.CASE_INSENSITIVE);
            Matcher generalWarrMatcher = generalWarrPattern.matcher(ocrText);
            if (generalWarrMatcher.find()) {
                String count = generalWarrMatcher.group(1);
                String unit = generalWarrMatcher.group(2).toLowerCase();
                if (unit.startsWith("y")) {
                    data.warrantyPeriod = count + " Years";
                } else {
                    data.warrantyPeriod = count + " Months";
                }
            }
        }

        // 7. Detect Serial Number / Model Number
        Pattern snPattern = Pattern.compile("(?:S/?N|Serial)(?:\\s*(?:No|Num|Number|#))?[\\s#:\\-]*([A-Z0-9\\-]+)", Pattern.CASE_INSENSITIVE);
        Matcher snMatcher = snPattern.matcher(ocrText);
        if (snMatcher.find()) {
            data.serialNumber = snMatcher.group(1).trim();
        }

        Pattern modelPattern = Pattern.compile("(?:Model|Item)(?:\\s*(?:No|Num|Number|#))?[\\s#:\\-]*([A-Za-z0-9\\-\\s]{3,20})", Pattern.CASE_INSENSITIVE);
        Matcher modelMatcher = modelPattern.matcher(ocrText);
        if (modelMatcher.find()) {
            data.modelNumber = modelMatcher.group(1).trim();
        }

        // 8. Product Name & Category
        String lower = ocrText.toLowerCase();
        if (lower.contains("galaxy") || lower.contains("iphone") || lower.contains("phone") || lower.contains("mobile")) {
            data.category = "Mobiles";
            if (lower.contains("galaxy s25")) data.productName = "Samsung Galaxy S25";
            else if (lower.contains("iphone")) data.productName = "Apple iPhone";
            else data.productName = (data.brand.isEmpty() ? "" : data.brand + " ") + "Smartphone";
        } else if (lower.contains("monitor") || lower.contains("display")) {
            data.category = "Electronics";
            data.productName = (data.brand.isEmpty() ? "Dell" : data.brand) + " UltraSharp Monitor";
        } else if (lower.contains("laptop") || lower.contains("macbook") || lower.contains("thinkpad") || lower.contains("inspiron")) {
            data.category = "Laptops";
            data.productName = (data.brand.isEmpty() ? "" : data.brand + " ") + "Laptop";
        } else if (lower.contains("headphone") || lower.contains("earbud") || lower.contains("audio") || lower.contains("speaker")) {
            data.category = "Audio";
            data.productName = (data.brand.isEmpty() ? "Sony" : data.brand) + " Headphones";
        } else if (lower.contains("washing machine") || lower.contains("refrigerator") || lower.contains("microwave")) {
            data.category = "Appliances";
            data.productName = (data.brand.isEmpty() ? "LG" : data.brand) + " Appliance";
        } else if (lower.contains("watch")) {
            data.category = "Electronics";
            data.productName = (data.brand.isEmpty() ? "Apple" : data.brand) + " Watch";
        }

        if (data.productName.isEmpty()) {
            // Find first line that looks like an item description
            for (String line : lines) {
                String clean = line.trim();
                if (clean.length() > 4 && clean.length() < 35 && !clean.equalsIgnoreCase(data.seller) && !clean.contains("Invoice") && !clean.contains("Total")) {
                    data.productName = clean;
                    break;
                }
            }
            if (data.productName.isEmpty()) {
                data.productName = (data.brand.isEmpty() ? "New Product" : data.brand + " Product");
            }
        }

        return data;
    }
}
